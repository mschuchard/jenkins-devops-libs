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
  assert (fileExists("${installPath}/goss")) : 'dgoss is installed but goss is not. dgoss execution requires goss.'

  // otherwise download and install specified version
  new utils().downloadFile("https://raw.githubusercontent.com/aelsabbahy/goss/v${version}/extras/dgoss/dgoss", "${installPath}/dgoss")
  sh(label: 'DGoSS CLI Executable Permissions', script: "chmod ug+rx ${installPath}/dgoss")
  print "DGoSS successfully installed at ${installPath}/dgoss."
}

String render(Map config) {
  // input checking
  config.bin = config.bin ?: 'goss'

  String cmd = config.bin

  // check for optional global inputs and establish command
  cmd += globalArgsCmd(config)
  cmd.add('render')

  // check for optional inputs
  if (config.debug == true) {
    cmd.add('--debug')
  }

  // render gossfile
  try {
    final String rendered = sh(label: "GoSS Render ${config?.gossfile}", script: cmd, returnStdout: true)

    print 'GoSSfile rendered successfully.'

    return rendered
  }
  catch (hudson.AbortException error) {
    print 'Failure using goss render.'
    throw error
  }
}

void server(Map config) {
  // input checking
  if (config.logLevel) {
    assert ['error', 'warn', 'info', 'debug', 'trace'].contains(config.logLevel) : 'The logLevel parameter must be one of error, warn, info, debug, or trace.'
  }

  config.endpoint = config.endpoint ?: '/healthz'
  config.port = config.port ?: '8080'
  config.bin = config.bin ?: 'goss'

  String cmd = config.bin

  // check for optional global inputs and establish command
  cmd += globalArgsCmd(config)
  cmd.add('serve')

  // check for optional inputs
  if (config.maxConcur) {
    cmd.addAll(['--max-concurrent', config.maxConcur])
  }
  if (config.format) {
    assert (['documentation', 'json', 'junit', 'nagios', 'prometheus', 'rspecish', 'silent', 'structured', 'tap'].contains(config.format)) : 'The "format" parameter value must be a valid accepted format for GoSS'

    cmd.addAll(['-f', config.format])
  }
  if (config.formatOpts) {
    assert (['perfdata', 'pretty', 'verbose'].contains(config.formatOpts)) : 'The "formatOpts" parameter value must be one of: perfdata, pretty, or verbose.'

    cmd.addAll(['-o', config.formatOpts])
  }
  if (config.cache) {
    cmd.addAll(['-c', config.cache])
  }
  if (config.logLevel) {
    cmd.addAll(['-L', config.logLevel.toUpperCase()])
  }

  // create goss rest api endpoint
  try {
    sh(label: "GoSS Server ${config?.gossfile}", script: "nohup ${cmd} -e ${config.endpoint} -l :${config.port} &")
  }
  catch (hudson.AbortException error) {
    print 'Failure using goss serve.'
    throw error
  }
  print 'GoSS server endpoint created successfully.'
}

Boolean validate(Map config) {
  // input checking
  if (config.logLevel) {
    assert ['error', 'warn', 'info', 'debug', 'trace'].contains(config.logLevel) : 'The logLevel parameter must be one of error, warn, info, debug, or trace.'
  }
  config.bin = config.bin ?: 'goss'

  // optional inputs
  String cmd = config.bin

  // check for optional global inputs and establish command
  cmd += globalArgsCmd(config)
  cmd.add('validate --no-color')

  // check for optional inputs
  if (config.maxConcur) {
    cmd.addAll(['--max-concurrent', config.maxConcur])
  }
  if (config.format) {
    assert (['documentation', 'json', 'junit', 'nagios', 'prometheus', 'rspecish', 'silent', 'structured', 'tap'].contains(config.format)) : 'The "format" parameter value must be a valid accepted format for GoSS'

    cmd.addAll(['-f', config.format])
  }
  if (config.formatOpts) {
    assert (['perfdata', 'pretty', 'sort', 'verbose'].contains(config.formatOpts)) : 'The "formatOpts" parameter value must be one of: perfdata, pretty, or verbose.'

    cmd.addAll(['-o', config.formatOpts])
  }
  if (config.retryTimeout) {
    cmd.addAll(['-r', config.retryTimeout])

    if (config.sleep) {
      cmd.addAll(['-s', config.sleep])
    }
  }
  if (config.logLevel) {
    cmd.addAll(['-L', config.logLevel.toUpperCase()])
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
  error(message: 'GoSS validate failed unexpectedly')
}

void validateDocker(Map config) {
  // input checking
  assert config.image in String : 'The required image parameter was not set.'
  config.bin = config.bin ?: 'dgoss'

  String cmd = "${config.bin} run"

  // check for optional inputs
  if (config.flags) {
    assert (config.flags in Map) : 'The flags parameter must be a Map.'

    config.flags.each { String flag, String value ->
        cmd.addAll(['-e', "${flag}=${value}"])
    }
  }

  // run with dgoss
  try {
    sh(label: "DGoSS Validate Docker ${config.image}", script: "${cmd} ${config.image}")
  }
  catch (hudson.AbortException error) {
    print 'Failure using dgoss run.'
    throw error
  }
  print 'DGoSS run command was successful.'
}

Boolean validateGossfile(String gossfile) {
  // ensure gossfile exists and then check yaml syntax
  assert readFile(gossfile) : "GoSSfile ${gossfile} does not exist!"

  try {
    readYaml(file: gossfile)
  }
  catch (Exception error) {
    print 'GoSSfile failed YAML validation.'
    print error.getMessage()
    return false
  }

  print "${gossfile} is valid YAML."
  return true
}

// private method for global arguments
private static String globalArgsCmd(Map config) {
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
  if (config.gossfile) {
    assert validateGossfile(config.gossfile) : "GoSSfile ${config.gossfile} does not exist or is not a valid YAML file!"

    subCmd += " -g ${config.gossfile}"
  }
  else {
    assert validateGossfile('goss.yaml') : 'GoSSfile \'goss.yaml\' does not exist or is not a valid YAML file!'
  }

  // return subcommand based from global arguments
  return subCmd
}
