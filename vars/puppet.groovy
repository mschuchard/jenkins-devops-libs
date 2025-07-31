// vars/puppet.groovy
import devops.common.utils

void codeDeploy(Map config) {
  // input checking
  if (config.tokenFile && config.credentialsId) {
    error(message: "The 'tokenFile' and 'credentialsId' parameters for puppet.codeDeploy are mutually exclusive; only one can be specified.")
  }
  assert config.tokenFile || (config.credentialsId in String) : 'The required token or credentialsId parameter was not set.'
  if (config.tokenFile) {
    assert readFile(config.tokenFile) in String : "The RBAC token ${config.tokenFile} does not exist or is not readable!"
  }

  if (config.servers) {
    assert (config.servers in List) : 'The servers parameter must be a list of strings.'
  }
  else {
    config.servers = ['puppet']
  }

  config.port = config.port ?: 8170

  // init payload
  Map<String,String> payload = [:]

  // check for environments
  if (config.environments) {
    assert (config.environments in List) : 'The environments parameter must be a list of strings.'

    // preface environments payload
    payload['environments'] = config.environments
  }
  else {
    payload['deploy-all'] = true
  }
  // check for wait
  if (config.wait == true) {
    payload['wait'] = true
  }

  // convert map to json string
  payload = new utils().mapToJSON(payload)

  // initialize vars
  boolean errored = false
  Map jsonResponse = [:]
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
  config.servers.each { String server ->
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
        url:                    "${server}:${config.port}/code-manager/v1/deploys",
      )
    }
    catch (Exception error) {
      print "Failure executing REST API request against ${server} with token! Returned status: ${jsonResponse.status}."
      print error
      errored = true
    }
    // parse response
    try {
      response = readJSON(text: jsonResponse.content)
    }
    catch (Exception error) {
      print "Response from ${server} is not valid JSON! Response content: ${jsonResponse.content}."
      print error
      errored = true
    }
    // check for errors if waited
    if (config.wait == true) {
      response.each { Map hash ->
        if (hash.containsKey('error')) {
          print "Response from Code Manager for environment ${hash['environment']} was an error of kind ${hash['error']['kind']}."
          print hash['error']['msg']
          errored = true
        }
        else {
          print 'Successful response from Code Manager below:'
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

void task(Map config) {
  // input checking
  if (config.tokenFile && config.credentialsId) {
    error(message: "The 'tokenFile' and 'credentialsId' parameters for puppet.task are mutually exclusive; only one can be specified.")
  }
  assert config.tokenFile || (config.credentialsId in String) : 'The required token or credentialsId parameter was not set.'
  if (config.tokenFile) {
    assert readFile(config.tokenFile) in String : "The RBAC token ${config.tokenFile} does not exist or is not readable!"
  }
  assert config.task in String : 'The required task parameter was not set.'
  assert config.scope : 'The required scope parameter was not set.'

  config.server = config.server ?: 'puppet'
  config.port = config.port ?: 8143

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

  if (config.scope in List) {
    // is the last element of the list a nested list
    if (config.scope[-1] in List) {
      payload['scope']['query'] = config.scope
    }
    // otherwise it is a list of strings which is then a node list
    else {
      payload['scope']['nodes'] = config.scope
    }
  }
  else if (config.scope in String) {
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
    error(message: 'The scope parameter is an invalid type!')
  }

  // convert map to json file
  payload = new utils().mapToJSON(payload)

  // initialize vars
  Map jsonResponse = [:]
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
      url:                    "${server}:${config.port}/orchestrator/v1/command/task",
    )
  }
  catch (Exception error) {
    print "Failure executing REST API request against ${server} with token! Returned status: ${jsonResponse.status}."
    throw error
  }
  // receive and parse response
  try {
    response = readJSON(text: json.content)
  }
  catch (Exception error) {
    print "Response from ${server} is not valid JSON! Response content: ${jsonResponse.content}."
    throw error
  }
  // handle errors in response
  response.each { Map hash ->
    if (hash.containsKey('puppetlabs.orchestrator/unknown-environment')) {
      error(message: 'The environment does not exist!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/empty-target')) {
      error(message: 'The application instance specified to deploy does not exist or is empty!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/puppetdb-error')) {
      error(message: 'The orchestrator is unable to make a query to PuppetDB!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/query-error')) {
      error(message: 'The user does not have appropriate permissions to run a query, or the query is invalid!')
    }
    else if (hash.containsKey('puppetlabs.orchestrator/not-permitted')) {
      error(message: 'The user does not have permission to run the task on the requested nodes!')
    }
    else {
      print 'Successful response from Orchestrator below:'
      print hash.toMapString()
    }
  }
  print 'Puppet Orchestrator Task execution successfully requested.'
}

void token(Map config) {
  // input checking
  assert config.username in String : 'The username parameter is required.'
  assert config.password in String : 'The password parameter is required.'

  config.server = config.server ?: 'puppet'
  config.port = config.port ?: 4433
  config.path = config.path ?: "${env.JENKINS_HOME}/.puppetlabs"

  //construct payload
  Map<String,String> payload = [:]
  payload['username'] = config.username
  payload['password'] = config.password

  // convert map to json file
  payload = new utils().mapToJSON(payload)

  // initialize vars
  Map jsonResponse = [:]
  Map response = [:]
  String token = ''

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
      url:                    "${server}:${config.port}/rbac-api/v1/auth/token",
    )
  }
  catch (Exception error) {
    print "Failure executing REST API request against ${server} with username ${config.username}. Returned status: ${jsonResponse.status}."
    throw error
  }
  // receive and parse response
  try {
    response = readJSON(text: jsonResponse.content)
  }
  catch (Exception error) {
    print "Response from ${server} is not valid JSON! Response content: ${jsonResponse.content}."
    throw error
  }

  // check if desired token save path exists and create if not
  new utils().makeDirParents(config.path)

  // acess token value and save it to file
  writeFile(file: "${config.path}/token", text: response['token'])

  print "RBAC Token retrieved successfully and stored at ${config.path}/token."
}
