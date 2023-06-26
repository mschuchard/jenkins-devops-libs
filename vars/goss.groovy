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

String render(config) {
  // input checking
  if (config.gossfile) {
    assert readYaml(config.gossfile) instanceof String : "Gossfile ${config.gossfile} does not exist or is not a valid YAML file!"
  }
  else {
    assert readYaml('goss.yaml') instanceof String : 'Gossfile \'goss.yaml\' does not exist or is not a valid YAML file!'
  }
  config.bin = config.bin ?: 'goss'

  // render gossfile
  try {
    String cmd = config.bin

    // optional inputs
    cmd += globalArgsCmd(config)
    cmd += ' render'
    if (config.debug == true) {
      cmd += ' --debug'
    }

    final String rendered = sh(label: 'GoSS Render', script: cmd, returnStdout: true)
  }
  catch(Exception error) {
    print 'Failure using goss render.'
    throw error
  }
  print 'GoSSfile rendered successfully.'

  return rendered
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
  config.port = config.port ?: '8080'
  config.bin = config.bin ?: 'goss'

  // create goss rest api endpoint
  try {
    String cmd = config.bin

    // check for optional inputs
    cmd += globalArgsCmd(config)
    cmd += ' serve'
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

    sh(label: 'GoSS Server', script: "nohup ${cmd} -e ${config.endpoint} -l :${config.port} &")
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
  config.bin = config.bin ?: 'goss'

  // validate with goss
  try {
    String cmd = config.bin

    // check for optional inputs
    cmd += globalArgsCmd(config)
    cmd += ' validate --no-color'
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
    if (config.retryTimeout) {
      cmd += " -r ${config.retryTimeout}"

      if (config.sleep) {
        cmd += " -s ${config.sleep}"
      }
    }

    sh(label: 'GoSS Validate', script: cmd)
  }
  catch(Exception error) {
    print 'Failure using goss validate.'
    throw error
  }
  print 'GoSS validate command was successful.'
}

void validateDocker(config) {
  // input checking
  assert config.image instanceof String : 'The required image parameter was not set.'
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
  assert readFile(gossfile) instanceof String : "Gossfile ${gossfile} does not exist!"

  try {
    readYaml(file: gossfile)
  }
  catch(Exception error) {
    print 'Gossfile failed YAML validation.'
    throw error
  }
  print "${gossfile} is valid YAML."
}

// "private" methods
String globalArgsCmd(Map config) {
  // initialize subcommand from global args
  String subCmd = ''

  // check for optional global args
  if (config.varsInline) {
    assert config.varsInline instanceof Map : "The inline vars parameter must be a Map."
    final String varsInlineJSON = new utils().mapToJSON(config.varsInline)

    subCmd += " --vars-inline ${varsInlineJSON}"
  }
  else if (config.vars) {
    assert readYaml(config.vars) instanceof String : "The vars file ${config.vars} does not exist, or is not a valid YAML or JSON file!"

    subCmd += " --vars ${config.vars}"
  }
  if (config.package) {
    assert (['apk', 'dpkg', 'pacman', 'rpm'].contains(config.package)) : 'The "package" parameter must be one of: apk, dpkg, pacman, or rpm'

    subCmd += " --package ${config.package}"
  }
  if (config.gossfile) {
    subCmd += " -g ${config.gossfile}"
  }

  // return subcommand based from global arguments
  return subCmd
}
