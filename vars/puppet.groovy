// vars/puppet.groovy
import devops.common.utils

def code_deploy(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.token == null) {
    throw new Exception('The required token parameter was not set.')
  }
  else if (!(fileExists(config.token))) {
    throw new Exception("The RBAC token ${config.token} does not exist!")
  }
  config.bin = config.bin == null ? 'curl' : config.bin
  config.servers = config.servers == null ? ['puppet'] : config.servers
  if (!(config.servers instanceof String[])) {
    throw new Exception('The servers parameter must be an array of strings.')
  }

  // check for environments
  if (config.environments == null) {
    payload = '{"deploy-all": true'
  }
  else {
    if (!(config.environments instanceof String[])) {
      throw new Exception('The environments parameter must be an array of strings.')
    }
    // preface environments payload
    payload = '{"environments": ['
    // iterate through and append each environment into array
    config.environments.each() {
      payload += "\"${it}\", "
    }
    // remove trailing ', ' and then end array
    payload = payload.substring(0, payload.length() - 2)
    payload += ']'
  }
  // check for wait
  if (config.wait == true) {
    payload += ', "wait": true}'
  }
  else {
    payload += '}'
  }

  // iterate through servers
  errored = false
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
      response = readJSON(text: json)
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
      }
    }
  }
  if (errored) {
    throw 'Code Manager failed with above error info.'
  }
}

def task(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.token == null) {
    throw new Exception('The required token parameter was not set.')
  }
  else if (!(fileExists(config.token))) {
    throw new Exception("The RBAC token ${config.token} does not exist!")
  }
  if (config.task == null) {
    throw new Exception('The required task parameter was not set.')
  }
  if (config.scope == null) {
    throw new Exception('The required scope parameter was not set.')
  }
  config.bin = config.bin == null ? 'curl' : config.bin
  config.server = config.server == null ? 'puppet' : config.server

  // construct payload
  payload = '{'
  if (config.environment != null) {
    payload += " \"environment\":\"${config.environment}\","
  }
  if (config.description != null) {
    payload += " \"description\":\"${config.description}\","
  }
  if (config.noop != null) {
    payload += " \"noop\":${config.noop},"
  }
  if (config.params == null) {
    payload += " \"params\": {},"
  }
  else {
    payload += " \"params\": ${config.params},"
  }
  payload += " \"task\": \"${config.task}\","
  if (config.scope instanceof String[]) {
    // node list
    payload += ' \"scope\": {"nodes": ['
    config.environments.each() { node ->
      payload += "\"${node}\", "
    }
    // remove trailing ', ' and then end array
    payload = payload.substring(0, payload.length() - 2)
    payload += ']}'
  }
  else if (config.scope instanceof String) {
    // node group
    payload += " \"scope\": {\"node_group\": \"${config.scope}\"}"
  }
  else {
    throw new Exception('The scope parameter is an invalid type!')
  }

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
    response = readJSON(text: json)
  }
  catch(Exception error) {
    print "Response from ${server} is not valid JSON!"
    throw error
  }
  // check for errors if waited
  
}
