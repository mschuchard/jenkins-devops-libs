// vars/goss.groovy
import devops.common.utils

void install(String version, String install_path = '/usr/bin/') {
  assert fileExists(install_path) : "The desired installation path at ${install_path} does not exist."

  // check if current version already installed
  if (fileExists("${install_path}/goss")) {
    String installed_version = sh(label: 'Check GoSS Version', returnStdout: true, script: "${install_path}/goss --version").trim()
    if (installed_version ==~ version) {
      print "GoSS version ${version} already installed at ${install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().download_file("https://github.com/aelsabbahy/goss/releases/download/v${version}/goss-linux-amd64", "${install_path}/goss")
  sh(label: 'GoSS CLI Executable Permissions', script: "chmod ug+rx ${install_path}/goss")
  print "GoSS successfully installed at ${install_path}/goss."
}

void install_dgoss(String version, String install_path = '/usr/bin/') {
  assert fileExists(install_path) : "The desired installation path at ${install_path} does not exist."

  // check if current version already installed
  if (fileExists("${install_path}/dgoss") && fileExists("${install_path}/goss")) {
    String installed_version = sh(label: 'Check DGoSS Version', returnStdout: true, script: "${install_path}/goss --version").trim()
    if (installed_version ==~ version) {
      print "DGoSS version ${version} already installed at ${install_path}."
      return
    }
  }
  assert (!(fileExists("${install_path}/dgoss"))) : 'Dgoss is installed but goss is not. Dgoss execution requires goss.'

  // otherwise download and install specified version
  new utils().download_file("https://raw.githubusercontent.com/aelsabbahy/goss/v${version}/extras/dgoss/dgoss", "${install_path}/dgoss")
  sh(label: 'DGoSS CLI Executable Permissions', script: "chmod ug+rx ${install_path}/dgoss")
  print "DGoSS successfully installed at ${install_path}/dgoss."
}

void server(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  if ((config.gossfile) && (!fileExists(config.gossfile))) {
    throw new Exception("Gossfile ${config.gossfile} does not exist!")
  }
  config.endpoint = config.endpoint ? config.endpoint : '/healthz'
  config.format = config.format ? config.format : 'rspecish'
  config.port = config.port ? config.port : '8080'
  config.bin = config.bin ? config.bin : 'goss'

  // create goss rest api endpoint
  try {
    String cmd = "${config.bin}"

    // check for optional inputs
    if (config.vars) {
      assert fileExists(config.vars) : "The vars file ${config.vars} does not exist!"

      cmd += " --vars ${config.vars}"
    }
    if (config.gossfile) {
      cmd += " -g ${config.gossfile}"
    }

    sh(label: 'GoSS Server', script: "nohup ${cmd} serve -f ${config.format} -e ${config.endpoint} -l :${config.port} &")
  }
  catch(Exception error) {
    print 'Failure using goss serve.'
    throw error
  }
  print 'GoSS server endpoint created successfully.'
}

void validate(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().params_converter(body)

  // input checking
  if ((config.gossfile) && (!fileExists(config.gossfile))) {
    throw new Exception("Gossfile ${config.gossfile} does not exist!")
  }
  config.format = config.format ? config.format : 'rspecish'
  config.bin = config.bin ? config.bin : 'goss'

  // validate with goss
  try {
    String cmd = "${config.bin}"

    // check for optional inputs
    if (config.vars) {
      assert fileExists(config.vars) : "The vars file ${config.vars} does not exist!"

      cmd += " --vars ${config.vars}"
    }
    if (config.gossfile) {
      cmd += " -g ${config.gossfile}"
    }

    sh(label: 'GoSS Validate', script: "${cmd} validate -f ${config.format}")
  }
  catch(Exception error) {
    print 'Failure using goss validate.'
    throw error
  }
  print 'GoSS validate command was successful.'
}

void validate_docker(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().params_converter(body)

  // input checking
  assert config.image : 'The required image parameter was not set.'

  config.bin = config.bin ? config.bin : 'dgoss'

  // run with dgoss
  try {
    String cmd = "${config.bin} run"

    // check for optional inputs
    if (config.flags) {
      assert (config.flags instanceof List) : 'The flags parameter must be an array of strings.'

      config.flags.each() { flag ->
        cmd += " -e ${flag}"
      }
    }

    sh(label: 'DGoSS Validate Docker', script: "${cmd} ${config.image}")
  }
  catch(Exception error) {
    print 'Failure using dgoss run.'
    throw error
  }
  print 'DGoSS run command was successful.'
}

void validate_gossfile(String gossfile) {
  // ensure gossfile exists and then check yaml syntax
  assert fileExists(gossfile) : "Gossfile ${gossfile} does not exist!"

  try {
    readYaml(file: gossfile)
  }
  catch(Exception error) {
    print 'Gossfile failed YAML validation.'
    throw error
  }
  print "${gossfile} is valid YAML."
}
