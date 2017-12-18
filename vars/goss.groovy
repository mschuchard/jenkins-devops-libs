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

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw "Gossfile ${config.gossfile} does not exist!"
  }
  default_input(config.endpoint, '/healthz')
  default_input(config.format, 'rspecish')
  default_input(config.port, '8080')
  default_input(config.path, 'goss')

  // create goss rest api endpoint
  try {
    if (config.gossfile == null) {
      sh "${config.path} -f ${config.format} serve -e ${config.endpoint} -l ${config.port} &"
    }
    else {
      sh "${config.path} -f ${config.format} -g ${config.gossfile} serve -e ${config.endpoint} -l ${config.port} &"
    }
  }
  catch(error) {
    echo 'Failure using goss serve:'
    throw error
  }
}

def validate(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw "Gossfile ${config.gossfile} does not exist!"
  }
  default_input(config.format, 'rspecish')
  default_input(config.path, 'goss')

  // validate with goss
  try {
    if (config.gossfile == null) {
      sh "${config.path} -f ${config.format} validate"
    }
    else {
      sh "${config.path} -f ${config.format} -g ${config.gossfile} validate"
    }
  }
  catch(error) {
    echo 'Failure using goss validate:'
    throw error
  }
}

def validate_gossfile(String gossfile) {

}
