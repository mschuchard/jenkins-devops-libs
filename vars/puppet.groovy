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

  // trigger code manager deployment
  // check for environments
  if (config.environments == null) {
    payload = '{"deploy-all": true'
  }
  else {
    //TODO: json needs "" and these strings could be ''; also check that environments is an array of strings
    payload = "{\"environments\": ${config.environments}"
  }
  // check for wait
  if (config.wait == true) {
    payload += ', "wait": true}'
  }
  else {
    payload += '}'
  }

  try {
    json = sh(returnStdout: true, script: "${config.bin} -k -X POST -H 'Content-Type: application/json' -H \"X-Authentication: `cat ${config.token}`\" \"https://${config.server}:8170/code-manager/v1/deploys\" -d '${payload}'")
  }
  catch(Exception error) {
    print "Failure executing curl against ${config.server} with token at ${config.token}!"
    throw error
  }
  try {
    response = readJSON(text: json)
  }
  catch(Exception error) {
    print "Response from ${config.server} is not valid JSON!"
    throw error
  }
  // TODO: continue with parsing response, especially for possible errors
}

// https://puppet.com/docs/pe/2018.1/orchestrator_api_commands_endpoint.html#reference-6045
