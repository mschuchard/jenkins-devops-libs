// vars/puppet.groovy
import devops.common.utils

void code_deploy(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.token : 'The required token parameter was not set.'
  assert fileExists(config.token) : "The RBAC token ${config.token} does not exist!"

  config.servers = config.servers ?: ['puppet']
  assert (config.servers instanceof List) : 'The servers parameter must be a list of strings.'

  // init payload
  Map payload = [:]

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
  def json_response = [:]
  Map response = [:]
  // initialize token with readFile relative pathing requirement stupidness
  String token = readFile("../../../../../../../../../../../${config.token}")

  // iterate through servers
  config.servers.each() { server ->
    // trigger code manager deployment
    try {
      json_response = httpRequest(
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
      print "Failure executing REST API request against ${server} with token at ${config.token}! Returned status: ${json_response.status}."
      throw error
    }
    // parse response
    try {
      response = readJSON(text: json_response.content)
    }
    catch(Exception error) {
      print "Response from ${server} is not valid JSON! Response content: ${json_response.content}."
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

void task(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.token : 'The required token parameter was not set.'
  assert fileExists(config.token) : "The RBAC token ${config.token} does not exist!"
  assert config.task : 'The required task parameter was not set.'
  assert config.scope : 'The required scope parameter was not set.'

  config.server = config.server ?: 'puppet'

  // construct payload
  Map payload = [:]

  if (config.environment) {
    payload['environment'] = config.environment
  }
  if (config.description) {
    payload['description'] = config.description
  }
  if (config.noop) {
    payload['noop'] = config.noop
  }
  if (!config.params) {
    payload['params'] = [:]
  }
  else {
    payload['params'] = config.params
  }

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
  def json_response = [:]
  Map response = [:]
  // initialize token with readFile relative pathing requirement stupidness
  String token = readFile("../../../../../../../../../../../${config.token}")

  // trigger task orchestration
  try {
    // trigger code manager deployment
    try {
      json_response = httpRequest(
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
  }
  catch(Exception error) {
    print "Failure executing REST API request against ${server} with token at ${config.token}! Returned status: ${json_response.status}."
    throw error
  }
  // receive and parse response
  try {
    Map response = readJSON(text: json.content)
  }
  catch(Exception error) {
    print "Response from ${server} is not valid JSON! Response content: ${json_response.content}."
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

void token (body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.username : 'The username parameter is required.'
  assert config.password : 'The password parameter is required.'

  config.server = config.server ?: 'puppet'
  config.path = config.path ?: "${env.JENKINS_HOME}/.puppetlabs"

  //construct payload
  Map payload = [:]
  payload['username'] = config.username
  payload['password'] = config.password

  // convert map to json file
  payload = new utils().mapToJSON(payload)

  // initialize vars
  def json_response = [:]
  Map response = [:]

  // trigger token generation
  try {
    json_response = httpRequest(
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
    print "Failure executing REST API request against ${server} with username ${config.username}. Returned status: ${json_response.status}."
    throw error
  }
  // receive and parse response
  try {
    Map response = readJSON(text: json_response.content)
  }
  catch(Exception error) {
    print "Response from ${server} is not valid JSON! Response content: ${json_response.content}."
    throw error
  }

  // check if desired token save path exists and create if not
  if (!(fileExists(config.path))) {
    new File(config.path).mkdir()
  }

  // acess token value and save it to file
  writeFile(file: "${config.path}/token", text: response['token'])

  print "RBAC Token retrieved successfully and stored at ${config.path}/token."
}
