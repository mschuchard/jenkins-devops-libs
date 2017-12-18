#!/usr/bin/groovy
import devops.common.utils

// vars/goss.groovy
def install(String version, String install_path = '/usr/bin') {
  sh "curl -L https://github.com/aelsabbahy/goss/releases/download/${version}/goss-linux-amd64 -o ${install_path}/goss"
  sh "chmod +rx ${install_path}/goss"
}

def server(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def utils = new utils()

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw "Gossfile ${config.gossfile} does not exist!"
  }
  utils.default_input(config.endpoint, '/healthz')
  utils.default_input(config.format, 'rspecish')
  utils.default_input(config.port, '8080')
  utils.default_input(config.path, 'goss')

  // create goss rest api endpoint
  try {
    cmd = "${config.path} -f ${config.format}"

    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "${cmd} serve -e ${config.endpoint} -l ${config.port} &"
  }
  catch(Exception error) {
    echo 'Failure using goss serve.'
    throw(error.toString())
  }
}

def validate(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  def utils = new utils()

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw "Gossfile ${config.gossfile} does not exist!"
  }
  utils.default_input(config.format, 'rspecish')
  utils.default_input(config.path, 'goss')

  // validate with goss
  try {
    cmd = "${config.path} -f ${config.format}"

    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "${cmd} validate"
  }
  catch(Exception error) {
    echo 'Failure using goss validate.'
    throw(error.toString())
  }
}

def validate_gossfile(String gossfile) {
  if (fileExists(gossfile)) {
    try {
      readYaml(file: gossfile)
    }
    catch(Exception error) {
      echo 'Gossfile failed YAML validation.'
      throw(error.toString())
    }
    echo "${gossfile} is valid YAML."
  }
  else {
    throw "Gossfile ${gossfile} does not exist!"
  }
}
