// vars/goss.groovy
import devops.common.utils

void install(String version, String installPath = '/usr/bin/') {
  new utils().makeDirParents(installPath)

  // check if current version already installed
  if (fileExists("${installPath}/goss")) {
    final String installedVersion = sh(label: 'Check GoSS Version', returnStdout: true, script: "${installPath}/goss --version").trim()
    if (installedVersion =~ version) {
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
    if (installedVersion =~ version) {
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

String render(Map config) {
  // input checking
  if (config?.gossfile) {
    assert readYaml(config?.gossfile) in String : "Gossfile ${config?.gossfile} does not exist or is not a valid YAML file!"
  }
  else {
    assert readYaml('goss.yaml') in String : 'Gossfile \'goss.yaml\' does not exist or is not a valid YAML file!'
  }
  config.bin = config.bin ?: 'goss'

  // render gossfile
  try {
    String cmd = config.bin

    // check for optional global inputs and establish command
    cmd += globalArgsCmd(config)
    cmd += ' render'

    // check for optional inputs
    if (config.debug == true) {
      cmd += ' --debug'
    }

    final String rendered = sh(label: "GoSS Render ${config?.gossfile}", script: cmd, returnStdout: true)

    print 'GoSSfile rendered successfully.'

    return rendered
  }
  catch (Exception error) {
    print 'Failure using goss render.'
    throw error
  }
}

void server(Map config) {
  // input checking
  if (config?.gossfile) {
    assert readYaml(config?.gossfile) in String : "Gossfile ${config?.gossfile} does not exist or is not a valid YAML file!"
  }
  else {
    assert readYaml('goss.yaml') in String : 'Gossfile \'goss.yaml\' does not exist or is not a valid YAML file!'
  }
  if (config.logLevel) {
    assert ['error', 'warn', 'info', 'debug', 'trace'].contains(logLevel) : 'The logLevel parameter must be one of error, warn, info, debug, or trace.'
  }

  config.endpoint = config.endpoint ?: '/healthz'
  config.port = config.port ?: '8080'
  config.bin = config.bin ?: 'goss'

  // create goss rest api endpoint
  try {
    String cmd = config.bin

    // check for optional global inputs and establish command
    cmd += globalArgsCmd(config)
    cmd += ' serve'

    // check for optional inputs
    if (config.maxConcur) {
      cmd += " --max-concurrent ${config.maxConcur}"
    }
    if (config.format) {
      assert (['documentation', 'json', 'json_oneline', 'junit', 'nagios', 'prometheus', 'rspecish', 'silent', 'structured', 'tap'].contains(config.format)) : 'The "format" parameter value must be a valid accepted format for GoSS'

      cmd += " -f ${config.format}"
    }
    if (config.formatOpts) {
      assert (['perfdata', 'pretty', 'verbose'].contains(config.formatOpts)) : 'The "formatOpts" parameter value must be one of: perfdata, pretty, or verbose.'

      cmd += " -o ${config.formatOpts}"
    }
    if (config.cache) {
      cmd += " -c ${config.cache}"
    }
    if (config.logLevel) {
      cmd += " -L ${config.logLevel.toUpperCase()}"
    }

    sh(label: "GoSS Server ${config?.gossfile}", script: "nohup ${cmd} -e ${config.endpoint} -l :${config.port} &")
  }
  catch (Exception error) {
    print 'Failure using goss serve.'
    throw error
  }
  print 'GoSS server endpoint created successfully.'
}

Boolean validate(Map config) {
  // input checking
  if (config?.gossfile) {
    assert readYaml(config?.gossfile) in String : "Gossfile ${config?.gossfile} does not exist or is not a valid YAML file!"
  }
  else {
    assert readYaml('goss.yaml') in String : 'Gossfile \'goss.yaml\' does not exist or is not a valid YAML file!'
  }
  if (config.logLevel) {
    assert ['error', 'warn', 'info', 'debug', 'trace'].contains(logLevel) : 'The logLevel parameter must be one of error, warn, info, debug, or trace.'
  }
  config.bin = config.bin ?: 'goss'

  // optional inputs
  String cmd = config.bin

  // check for optional global inputs and establish command
  cmd += globalArgsCmd(config)
  cmd += ' validate --no-color'

  // check for optional inputs
  if (config.maxConcur) {
    cmd += " --max-concurrent ${config.maxConcur}"
  }
  if (config.format) {
    assert (['documentation', 'json', 'junit', 'nagios', 'prometheus', 'rspecish', 'silent', 'tap'].contains(config.format)) : 'The "format" parameter value must be a valid accepted format for GoSS'

    cmd += " -f ${config.format}"
  }
  if (config.formatOpts) {
    assert (['perfdata', 'pretty', 'sort', 'verbose'].contains(config.formatOpts)) : 'The "formatOpts" parameter value must be one of: perfdata, pretty, or verbose.'

    cmd += " -o ${config.formatOpts}"
  }
  if (config.retryTimeout) {
    cmd += " -r ${config.retryTimeout}"

    if (config.sleep) {
      cmd += " -s ${config.sleep}"
    }
  }
  if (config.logLevel) {
    cmd += " -L ${config.logLevel.toUpperCase()}"
  }

  // validate with goss
  final int returnCode = sh(label: "GoSS Validate ${config?.gossfile}", script: cmd, returnStatus: true)

  // return by code
  if (returnCode == 0) {
    print 'The system successfully validated.'
    return true
  }
  else if (returnCode == 1) {
    print 'The system failed validation.'
    return false
  }

  print 'Failure using goss validate.'
  throw new Exception('GoSS validate failed unexpectedly')
}

void validateDocker(Map config) {
  // input checking
  assert config.image in String : 'The required image parameter was not set.'
  config.bin = config.bin ?: 'dgoss'

  // run with dgoss
  try {
    String cmd = "${config.bin} run"

    // check for optional inputs
    if (config.flags) {
      assert (config.flags in Map) : 'The flags parameter must be a Map.'

      config.flags.each { String flag, String value ->
        cmd += " -e ${flag}=${value}"
      }
    }

    sh(label: "DGoSS Validate Docker ${config.image}", script: "${cmd} ${config.image}")
  }
  catch (Exception error) {
    print 'Failure using dgoss run.'
    throw error
  }
  print 'DGoSS run command was successful.'
}

Boolean validateGossfile(String gossfile) {
  // ensure gossfile exists and then check yaml syntax
  assert readFile(gossfile) in String : "Gossfile ${gossfile} does not exist!"

  try {
    readYaml(file: gossfile)
  }
  catch (Exception error) {
    print 'Gossfile failed YAML validation.'
    print error.getMessage()
    return false
  }

  print "${gossfile} is valid YAML."
  return true
}

// private method for global arguments
private String globalArgsCmd(Map config) {
  // initialize subcommand from global args
  String subCmd = ''

  // check for optional global args
  if (config.varsInline) {
    assert config.varsInline in Map : 'The inline vars parameter must be a Map.'
    final String varsInlineJSON = new utils().mapToJSON(config.varsInline)

    subCmd += " --vars-inline ${varsInlineJSON}"
  }
  else if (config.vars) {
    assert readYaml(config.vars) in String : "The vars file ${config.vars} does not exist, or is not a valid YAML or JSON file!"

    subCmd += " --vars ${config.vars}"
  }
  if (config.package) {
    assert (['apk', 'dpkg', 'pacman', 'rpm'].contains(config.package)) : 'The "package" parameter must be one of: apk, dpkg, pacman, or rpm'

    subCmd += " --package ${config.package}"
  }
  if (config?.gossfile) {
    subCmd += " -g ${config?.gossfile}"
  }

  // return subcommand based from global arguments
  return subCmd
}
