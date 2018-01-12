// vars/goss.groovy
import devops.common.utils

def install(String version, String install_path = '/usr/bin/') {
  // check if current version already installed
  if (fileExists("${install_path}/goss")) {
    installed_version = sh(returnStdout: true, script: "${install_path}/goss --version").trim()
    if (installed_version =~ version) {
      echo "Goss version ${version} already installed at ${install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().download_file("https://github.com/aelsabbahy/goss/releases/download/v${version}/goss-linux-amd64", "${install_path}/goss")
  sh "chmod +rx ${install_path}/goss"
  echo "Goss successfully installed at ${install_path}/goss."
}

def server(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw new Exception("Gossfile ${config.gossfile} does not exist!")
  }
  config.endpoint = config.endpoint == null ? '/healthz' : config.endpoint
  config.format = config.format == null ? 'rspecish' : config.format
  config.port = config.port == null ? ':8080' : config.port
  config.bin = config.bin == null ? 'goss' : config.bin

  // create goss rest api endpoint
  try {
    cmd = "${config.bin}"

    // check for optional inputs
    if (config.vars != null) {
      if (fileExists(config.vars)) {
        cmd += " --vars ${config.vars}"
      }
      else {
        throw new Exception("The vars file ${config.vars} does not exist!")
      }
    }
    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "${cmd} serve -f ${config.format} -e ${config.endpoint} -l ${config.port} &"
  }
  catch(Exception error) {
    echo 'Failure using goss serve.'
    throw error
  }
  echo 'Goss endpoint created successfully.'
}

def validate(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw new Exception("Gossfile ${config.gossfile} does not exist!")
  }
  config.format = config.format == null ? 'rspecish' : config.format
  config.port = config.port == null ? ':8080' : config.port
  config.bin = config.bin == null ? 'goss' : config.bin

  // validate with goss
  try {
    cmd = "${config.bin}"

    // check for optional inputs
    if (config.vars != null) {
      if (fileExists(config.vars)) {
        cmd += " --vars ${config.vars}"
      }
      else {
        throw new Exception("The vars file ${config.vars} does not exist!")
      }
    }
    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "${cmd} validate -f ${config.format}"
  }
  catch(Exception error) {
    echo 'Failure using goss validate.'
    throw error
  }
  echo 'Goss validate command was successful.'
}

def validate_gossfile(String gossfile) {
  // ensure gossfile exists and then check yaml syntax
  if (fileExists(gossfile)) {
    try {
      readYaml(file: gossfile)
    }
    catch(Exception error) {
      echo 'Gossfile failed YAML validation.'
      throw error
    }
    echo "${gossfile} is valid YAML."
  }
  else {
    throw new Exception("Gossfile ${gossfile} does not exist!")
  }
}
