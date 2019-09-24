// vars/puppet.groovy
import devops.common.utils

def code_deploy(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.token : 'The required token parameter was not set.'
  assert fileExists(config.token) : "The RBAC token ${config.token} does not exist!"

  config.bin = config.bin == null ? 'curl' : config.bin
  config.servers = config.servers == null ? ['puppet'] : config.servers
  assert (config.servers instanceof List) : 'The servers parameter must be an array of strings.'

  // init payload
  Map payload = [:]

  // check for environments
  if (config.environments == null) {
    payload['deploy-all'] = true
  }
  else {
    assert (config.environments instanceof List) : 'The environments parameter must be an array of strings.'

    // preface environments payload
    payload['environments'] = config.environments
  }
  // check for wait
  if (config.wait) {
    payload['wait'] = config.wait
  }

  // convert map to json string
  payload = new utils().map_to_json(payload)

  // iterate through servers
  boolean errored = false
  config.servers.each() { server ->
    // trigger code manager deployment
    try {
      json = sh(returnStdout: true, script: "${config.bin} -k -X POST -H 'Content-Type: application/json' -H \"X-Authentication: `cat ${config.token}`\" \"https://${server}:8170/code-manager/v1/deploys\" -d '${payload}'")
    }
    catch(Exception error) {
      print "Failure executing curl against ${server} with token at ${config.token}!"
      throw error
    }
    // parse response
    try {
      Map response = readJSON(text: json)
    }
    catch(Exception error) {
      print "Response from ${server} is not valid JSON!"
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

def task(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.token : 'The required token parameter was not set.'
  assert fileExists(config.token) : "The RBAC token ${config.token} does not exist!"
  assert config.task : 'The required task parameter was not set.'
  assert config.scope : 'The required scope parameter was not set.'

  config.bin = config.bin == null ? 'curl' : config.bin
  config.server = config.server == null ? 'puppet' : config.server

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
  if (config.params == null) {
    payload['params'] = [:]
  }
  else {
    payload['params'] = config.params
  }

  payload['task'] = config.task
  payload['scope'] = [:]

  if (config.scope instanceof List) {
    // is the last element of the array a nested array
    if (config.scope[-1] instanceof List) {
      payload['scope']['query'] = config.scope
    }
    // otherwise it is an array of strings which is then a node list
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
  payload = new utils().map_to_json(payload)

  // trigger task orchestration
  try {
    json = sh(returnStdout: true, script: "${config.bin} -k -X POST -H 'Content-Type: application/json' -H \"X-Authentication: `cat ${config.token}`\" \"https://${server}:8143/orchestrator/v1/command/task\" -d '${payload}'")
  }
  catch(Exception error) {
    print "Failure executing curl against ${server} with token at ${config.token}!"
    throw error
  }
  // receive and parse response
  try {
    Map response = readJSON(text: json)
  }
  catch(Exception error) {
    print "Response from ${server} is not valid JSON!"
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

def token (Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.username : 'The username parameter is required.'
  assert config.password : 'The password parameter is required.'

  config.bin = config.bin == null ? 'curl' : config.bin
  config.server = config.server == null ? 'puppet' : config.server
  config.path = config.path == null ? "${env.JENKINS_HOME}/.puppetlabs" : config.path

  //construct payload
  Map payload = [:]
  payload['username'] = config.username
  payload['password'] = config.password

  // convert map to json file
  payload = new utils().map_to_json(payload)

  // check for secure ssl connection option
  String secure = config.secure == false ? '-k' : ''

  // trigger token generation
  try {
    String json = sh(returnStdout: true, script: "${config.bin} ${secure} -X POST -H 'Content-Type: application/json' \"https://${server}:4433/rbac-api/v1/auth/token\" -d '${payload}'")
  }
  catch(Exception error) {
    print "Failure executing curl against ${server} with username ${config.username}."
    throw error
  }
  // receive and parse response
  try {
    Map response = readJSON(text: json)
  }
  catch(Exception error) {
    print "Response from ${server} is not valid JSON!"
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
