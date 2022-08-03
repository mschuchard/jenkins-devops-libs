// vars/goss.groovy
import devops.common.utils

void install(String version, String installPath = '/usr/bin/') {
  new utils().makeDirParents(installPath)

  // check if current version already installed
  if (fileExists("${installPath}/goss")) {
    final String installedVersion = sh(label: 'Check GoSS Version', returnStdout: true, script: "${installPath}/goss --version").trim()
    if (installedVersion ==~ version) {
      print "GoSS version ${version} already installed at ${installPath}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().downloadFile("https://github.com/aelsabbahy/goss/releases/download/v${version}/goss-linux-amd64", "${installPath}/goss")
  sh(label: 'GoSS CLI Executable Permissions', script: "chmod ug+rx ${installPath}/goss")
  print "GoSS successfully installed at ${installPath}/goss."
}

void installDgoss(String version, String installPath = '/usr/bin/') {
  new utils().makeDirParents(installPath)

  // check if current version already installed
  if (fileExists("${installPath}/dgoss") && fileExists("${installPath}/goss")) {
    final String installedVersion = sh(label: 'Check DGoSS Version', returnStdout: true, script: "${installPath}/goss --version").trim()
    if (installedVersion ==~ version) {
      print "DGoSS version ${version} already installed at ${installPath}."
      return
    }
  }
  assert (!(fileExists("${installPath}/dgoss"))) : 'Dgoss is installed but goss is not. Dgoss execution requires goss.'

  // otherwise download and install specified version
  new utils().downloadFile("https://raw.githubusercontent.com/aelsabbahy/goss/v${version}/extras/dgoss/dgoss", "${installPath}/dgoss")
  sh(label: 'DGoSS CLI Executable Permissions', script: "chmod ug+rx ${installPath}/dgoss")
  print "DGoSS successfully installed at ${installPath}/dgoss."
}

void server(config) {
  // input checking
  if (config.gossfile) {
    assert readYaml(config.gossfile) instanceof String : "Gossfile ${config.gossfile} does not exist or is not a valid YAML file!"
  }
  else {
    assert readYaml('goss.yaml') instanceof String : 'Gossfile \'goss.yaml\' does not exist or is not a valid YAML file!'
  }
  config.endpoint = config.endpoint ?: '/healthz'
  config.format = config.format ?: 'rspecish'
  config.port = config.port ?: '8080'
  config.bin = config.bin ?: 'goss'

  // create goss rest api endpoint
  try {
    String cmd = "${config.bin} serve"

    // check for optional inputs
    if (config.vars) {
      assert readYaml(config.vars) instanceof String : "The vars file ${config.vars} does not exist or is not a valid YAML file!"

      cmd += " --vars ${config.vars}"
    }
    if (config.gossfile) {
      cmd += " -g ${config.gossfile}"
    }

    sh(label: 'GoSS Server', script: "nohup ${cmd} -f ${config.format} -e ${config.endpoint} -l :${config.port} &")
  }
  catch(Exception error) {
    print 'Failure using goss serve.'
    throw error
  }
  print 'GoSS server endpoint created successfully.'
}

void validate(config) {
  // input checking
  if (config.gossfile) {
    assert readYaml(config.gossfile) instanceof String : "Gossfile ${config.gossfile} does not exist or is not a valid YAML file!"
  }
  else {
    assert readYaml('goss.yaml') instanceof String : 'Gossfile \'goss.yaml\' does not exist or is not a valid YAML file!'
  }
  config.format = config.format ?: 'rspecish'
  config.bin = config.bin ?: 'goss'

  // validate with goss
  try {
    String cmd = "${config.bin} validate --no-color"

    // check for optional inputs
    if (config.vars) {
      assert readYaml(config.vars) instanceof String : "The vars file ${config.vars} does not exist or is not a valid YAML file!"

      cmd += " --vars ${config.vars}"
    }
    if (config.gossfile) {
      cmd += " -g ${config.gossfile}"
    }

    sh(label: 'GoSS Validate', script: "${cmd} -f ${config.format}")
  }
  catch(Exception error) {
    print 'Failure using goss validate.'
    throw error
  }
  print 'GoSS validate command was successful.'
}

void validateDocker(config) {
  // input checking
  assert config.image : 'The required image parameter was not set.'

  config.bin = config.bin ?: 'dgoss'

  // run with dgoss
  try {
    String cmd = "${config.bin} run"

    // check for optional inputs
    if (config.flags) {
      assert (config.flags instanceof Map) : 'The flags parameter must be a Map.'

      config.flags.each() { flag, value ->
        cmd += " -e ${flag}=${value}"
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

void validateGossfile(String gossfile) {
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
