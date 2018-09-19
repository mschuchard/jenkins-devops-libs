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
  config.server = config.server == null ? 'puppet' : config.server

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

  // trigger code manager deployment
  try {
    json = sh(returnStdout: true, script: "${config.bin} -k -X POST -H 'Content-Type: application/json' -H \"X-Authentication: `cat ${config.token}`\" \"https://${config.server}:8170/code-manager/v1/deploys\" -d '${payload}'")
  }
  catch(Exception error) {
    print "Failure executing curl against ${config.server} with token at ${config.token}!"
    throw error
  }
  // parse response
  try {
    response = readJSON(text: json)
  }
  catch(Exception error) {
    print "Response from ${config.server} is not valid JSON!"
    throw error
  }
  // check for errors if waited
  if (config.wait == true) {
    response.each() {
      if (it.containsKey('error')) {
        print "Response from Code Manager was an error of kind ${it['error']['kind']}."
        throw it['error']['msg']
      }
    }
  }
}

// https://puppet.com/docs/pe/2018.1/orchestrator_api_commands_endpoint.html#reference-6045
