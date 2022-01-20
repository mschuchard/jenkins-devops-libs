// vars/puppet.groovy
import devops.common.utils

void codeDeploy(config) {
  // input checking
  assert config.tokenFile || config.credentialsId : 'The required token or credentialsId parameter was not set.'
  if (config.tokenFile) {
    assert fileExists(config.tokenFile) : "The RBAC token ${config.tokenFile} does not exist!"
  }

  config.servers = config.servers ?: ['puppet']
  assert (config.servers instanceof List) : 'The servers parameter must be a list of strings.'

  // init payload
  Map<String,String> payload = [:]

  // check for environments
  if (!config.environments) {
    payload['deploy-all'] = true
  }
  else {
    assert (config.environments instanceof List) : 'The environments parameter must be a list of strings.'

    // preface environments payload
    payload['environments'] = config.environments
  }
  // check for wait
  if (config.wait) {
    payload['wait'] = config.wait
  }

  // convert map to json string
  payload = new utils().mapToJSON(payload)

  // initialize vars
  boolean errored = false
  def jsonResponse = [:]
  Map response = [:]
  String token = ''
  // set token with logic from appropriate parameter
  if (config.credentialsId) {
    withCredentials([token(credentialsId: config.credentialsId, variable: 'theToken')]) {
      token = theToken
    }
  }
  else if (config.tokenFile) {
    // initialize token with readFile relative pathing requirement stupidness
    token = readFile("../../../../../../../../../../../${config.tokenFile}")
  }

  // iterate through servers
  config.servers.each() { server ->
    // trigger code manager deployment
    try {
      jsonResponse = httpRequest(
        acceptType:             'APPLICATION_JSON',
        consoleLogResponseBody: true,
        contentType:            'APPLICATION_JSON',
        customHeaders:          [[name: 'X-Authentication', value: token]],
        httpMode:               'POST',
        ignoreSslErrors:        true,
        quiet:                  true,
        requestBody:            payload,
        url:                    "https://${server}:8170/code-manager/v1/deploys",
      )
    }
    catch(Exception error) {
      print "Failure executing REST API request against ${server} with token! Returned status: ${jsonResponse.status}."
      throw error
    }
    // parse response
    try {
      response = readJSON(text: jsonResponse.content)
    }
    catch(Exception error) {
      print "Response from ${server} is not valid JSON! Response content: ${jsonResponse.content}."
      throw error
    }
    // check for errors if waited
    if (config.wait == true) {
      response.each() { hash ->
        if (hash.containsKey('error')) {
          print "Response from Code Manager for environment ${hash['environment']} was an error of kind ${hash['error']['kind']}."
          print hash['error']['msg']
          errored = true
        }
        else {
          print "Successful response from Code Manager below:"
          print hash.toMapString()
        }
      }
    }
  }
  if (errored) {
    throw 'One or more Code Manager deployment(s) failed with above error info.'
  }
  print 'Code manager deployment(s) was successful.'
}

void task(config) {
  // input checking
  assert config.tokenFile || config.credentialsId : 'The required token or credentialsId parameter was not set.'
  if (config.tokenFile) {
    assert fileExists(config.tokenFile) : "The RBAC token ${config.tokenFile} does not exist!"
  }
  assert config.task : 'The required task parameter was not set.'
  assert config.scope : 'The required scope parameter was not set.'

  config.server = config.server ?: 'puppet'

  // initialize payload
  Map<String,String> payload = [:]

  if (config.environment) {
    payload['environment'] = config.environment
  }
  if (config.description) {
    payload['description'] = config.description
  }
  if (config.noop) {
    payload['noop'] = config.noop
  }
  payload['params'] = config.params ?: [:]

  payload['task'] = config.task
  payload['scope'] = [:]

  if (config.scope instanceof List) {
    // is the last element of the list a nested list
    if (config.scope[-1] instanceof List) {
      payload['scope']['query'] = config.scope
    }
    // otherwise it is a list of strings which is then a node list
    else {
      payload['scope']['nodes'] = config.scope
    }
  }
  else if (config.scope instanceof String) {
    // does the string look like an app orchestrator string
    if (config.scope ==~ /\[.*\]$/) {
      payload['scope']['application'] = config.scope
    }
    // otherwise it is a node group string
    else {
      payload['scope']['node_group'] = config.scope
    }
  }
  else {
    throw new Exception('The scope parameter is an invalid type!')
  }

  // convert map to json file
  payload = new utils().mapToJSON(payload)

  // initialize vars
  def jsonResponse = [:]
  Map response = [:]
  String token = ''
  // set token with logic from appropriate parameter
  if (config.credentialsId) {
    withCredentials([token(credentialsId: config.credentialsId, variable: 'theToken')]) {
      token = theToken
    }
  }
  else if (config.tokenFile) {
    // initialize token with readFile relative pathing requirement stupidness
    token = readFile("../../../../../../../../../../../${config.tokenFile}")
  }

  // trigger task orchestration
  try {
    jsonResponse = httpRequest(
      acceptType:             'APPLICATION_JSON',
      consoleLogResponseBody: true,
      contentType:            'APPLICATION_JSON',
      customHeaders:          [[name: 'X-Authentication', value: token]],
      httpMode:               'POST',
      ignoreSslErrors:        true,
      quiet:                  true,
      requestBody:            payload,
      url:                    "https://${server}:8143/orchestrator/v1/command/task",
    )
  }
  catch(Exception error) {
    print "Failure executing REST API request against ${server} with token! Returned status: ${jsonResponse.status}."
    throw error
  }
  // receive and parse response
  try {
    response = readJSON(text: json.content)
  }
  catch(Exception error) {
    print "Response from ${server} is not valid JSON! Response content: ${jsonResponse.content}."
    throw error
  }
  // handle errors in response
  response.each() { hash ->
    if (hash.containsKey('puppetlabs.orchestrator/unknown-environment')) {
      throw new Exception('The environment does not exist!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/empty-target')) {
      throw new Exception('The application instance specified to deploy does not exist or is empty!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/puppetdb-error')) {
      throw new Exception('The orchestrator is unable to make a query to PuppetDB!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/query-error')) {
      throw new Exception('The user does not have appropriate permissions to run a query, or the query is invalid!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/not-permitted')) {
      throw new Exception('The user does not have permission to run the task on the requested nodes!')
    }
    else {
      print "Successful response from Orchestrator below:"
      print hash.toMapString()
    }
  }
  print 'Puppet Orchestrator Task execution successfully requested.'
}

void token (config) {
  // input checking
  assert config.username : 'The username parameter is required.'
  assert config.password : 'The password parameter is required.'

  config.server = config.server ?: 'puppet'
  config.path = config.path ?: "${env.JENKINS_HOME}/.puppetlabs"

  //construct payload
  Map<String,String> payload = [:]
  payload['username'] = config.username
  payload['password'] = config.password

  // convert map to json file
  payload = new utils().mapToJSON(payload)

  // initialize vars
  def jsonResponse = [:]
  Map response = [:]

  // trigger token generation
  try {
    jsonResponse = httpRequest(
      acceptType:             'APPLICATION_JSON',
      consoleLogResponseBody: true,
      contentType:            'APPLICATION_JSON',
      customHeaders:          [[name: 'X-Authentication', value: token]],
      httpMode:               'POST',
      ignoreSslErrors:        !config.secure,
      quiet:                  true,
      requestBody:            payload,
      url:                    "https://${server}:4433/rbac-api/v1/auth/token",
    )
  }
  catch(Exception error) {
    print "Failure executing REST API request against ${server} with username ${config.username}. Returned status: ${jsonResponse.status}."
    throw error
  }
  // receive and parse response
  try {
    response = readJSON(text: jsonResponse.content)
  }
  catch(Exception error) {
    print "Response from ${server} is not valid JSON! Response content: ${jsonResponse.content}."
    throw error
  }

  // check if desired token save path exists and create if not
  new utils().makeDirParents(config.path)

  // acess token value and save it to file
  writeFile(file: "${config.path}/token", text: response['token'])

  print "RBAC Token retrieved successfully and stored at ${config.path}/token."
}
