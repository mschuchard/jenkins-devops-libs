// vars/packer.groovy
import devops.common.utils
import devops.common.hcl

void build(Map config) {
  // input checking
  assert config.template in String : 'The required template parameter was not set.'
  if (config.except && config.only) {
    error(message: "The 'except' and 'only' parameters for packer.build are mutually exclusive; only one can be specified.")
  }
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  config.bin = config.bin ?: 'packer'

  List<String> cmd = [config.bin, 'build', '-color=false']

  // check for optional inputs
  cmd += varSubCmd(config)

  if (config.except) {
    assert (config.except in List) : 'The except parameter must be a list of strings.'

    cmd.add("-except=${config.except.join(',')}")
  }
  if (config.only) {
    assert (config.only in List) : 'The only parameter must be a list of strings.'

    cmd.add("-only=${config.only.join(',')}")
  }
  if (config.force == true) {
    cmd.add('-force')
  }
  if (config.onError) {
    assert (['default', 'abort', 'ask', 'run-cleanup-provisioner'].contains(config.onError)) : 'The argument must be one of: default, abort, ask, or run-cleanup-provisioner.'

    cmd.add("-on-error=${config.onError}")
  }

  // create artifact with packer
  try {
    if (config.template ==~ /\.pkr\./) {
      sh(label: "Packer Build ${config.template}", script: "${cmd} ${config.template}")
    }
    else {
      dir(config.template) {
        sh(label: "Packer Build ${config.template}", script: "${cmd} .")
      }
    }
  }
  catch (hudson.AbortException error) {
    print 'Failure using packer build.'
    throw error
  }
  print 'Packer build artifact created successfully.'
}

Boolean fmt(Map config) {
  // input checking
  assert config.template in String : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  if (config.write && config.check) {
    error(message: "The 'write' and 'check' options for packer.fmt are mutually exclusive - only one can be enabled.")
  }
  config.bin = config.bin ?: 'packer'

  List<String> cmd = [config.bin, 'fmt']

  // check for optional inputs
  if (config.diff == true) {
    cmd.add('-diff')
  }
  if (config.check == true) {
    cmd.add('-check')
  }
  // incompatible with above
  else if (config.write == true) {
    cmd.add('-write')
  }
  if (config.recursive == true) {
    cmd.add('-recursive')
  }

  // canonically format the code
  int fmtStatus
  if (config.template ==~ /\.pkr\./) {
    fmtStatus = sh(label: "Packer Format ${config.template}", returnStatus: true, script: "${cmd} ${config.template}")
  }
  else {
    dir(config.template) {
        fmtStatus = sh(label: "Packer Format ${config.template}", returnStatus: true, script: "${cmd} .")
    }
  }

  // report if formatting check detected issues
  if (fmtStatus != 0) {
    // the format check failed
    if (config.check == true) {
      print 'Packer fmt has detected formatting errors.'
      return false
    }

    // the format command failed unexpectedly
    print 'Failure using packer fmt.'
    error(message: 'packer fmt failed unexpectedly; check logs for details')
  }

  print 'Packer fmt was successful.'
  return true
}

void init(Map config) {
  // input checking
  assert fileExists(config.dir) : "Working template directory ${config.dir} does not exist."
  config.bin = config.bin ?: 'packer'

  List<String> cmd = [config.bin, 'init']

  // check for optional inputs
  if (config.upgrade == true) {
    cmd.add('-upgrade')
  }

  // initialize the working template directory
  try {
    dir(config.dir) {
      sh(label: "Packer Init ${config.dir}", script: "${cmd} .")
    }
  }
  catch (hudson.AbortException error) {
    print 'Failure using packer init.'
    throw error
  }
  print 'Packer init was successful.'
}

void inspect(String template, String bin = '/usr/bin/packer') {
  // input checking
  assert fileExists(template) : "A file does not exist at ${template}."

  // inspect the packer template
  try {
    sh(label: "Packer Inspect ${template}", script: "${bin} inspect ${template}")
  }
  catch (hudson.AbortException error) {
    print 'Failure inspecting the template.'
    throw error
  }
  print 'Packer inspect was successful'
}

void install(Map config) {
  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform in String && config.version in String) : 'A required parameter ("platform" or "version") is missing from the packer.install method. Please consult the documentation for proper usage.'

  new utils().makeDirParents(config.installPath)

  // check if current version already installed
  if (fileExists("${config.installPath}/packer")) {
    final String installedVersion = sh(label: 'Check Packer Version', returnStdout: true, script: "${config.installPath}/packer version").trim()
    if (installedVersion =~ config.version) {
      print "Packer version ${config.version} already installed at ${config.installPath}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().downloadFile("https://releases.hashicorp.com/packer/${config.version}/packer_${config.version}_${config.platform}.zip", 'packer.zip')
  unzip(zipFile: 'packer.zip', dir: config.installPath)
  new utils().removeFile('packer.zip')
  print "Packer successfully installed at ${config.installPath}/packer."
}

Map parse(String file) {
  // return map of parsed hcl
  return new hcl().hclToMap(file)
}

void pluginInstall(String url, String installLoc) {
  // return file path up to final slash element
  final String installDir = new File(installLoc).parent ?: '.'

  // check if plugin dir exists and create if not
  new utils().makeDirParents(installDir)

  // check if plugin already installed
  if (fileExists(installLoc)) {
    print "Packer plugin already installed at ${installLoc}."
    return
  }
  // otherwise download and install plugin
  if (url ==~ /\.zip$/) {
    // append zip extension to avoid filename clashes
    installLoc = "${installLoc}.zip"
  }
  new utils().downloadFile(url, installLoc)
  if (url ==~ /\.zip$/) {
    unzip(zipFile: installLoc)
    new utils().removeFile(installLoc)
  }
  else {
    sh(label: 'Packer Plugin Executable Permissions', script: "chmod ug+rx ${installLoc}")
  }
  print "Packer plugin successfully installed at ${installLoc}."
}

void pluginsInstall(Map config) {
  config.bin = config.bin ?: 'packer'
  assert config.plugin in String : 'The required "plugin" parameter was not assigned a value.'

  List<String> cmd = [config.bin, 'install']

  // optional inputs
  if (config.force == true) {
    cmd.add('-force')
  }
  // append plugin since optional version must be last argument
  cmd += " ${config.plugin}"

  if (config.version) {
    cmd += " ${config.version}"
  }

  // install plugin
  try {
    sh(label: "Packer Plugins Install ${config.plugin}", script: cmd)
  }
  catch (hudson.AbortException error) {
    print 'Failure using packer plugins install.'
    throw error
  }
  print 'Packer plugins install executed successfully.'
}

void pluginsRemove(Map config) {
  config.bin = config.bin ?: 'packer'
  assert config.plugin in String : 'The required "plugin" parameter was not assigned a value.'

  List<String> cmd = [config.bin, 'remove', config.plugin]

  // optional inputs
  if (config.version) {
    cmd += " ${config.version}"
  }

  // remove plugin
  try {
    sh(label: "Packer Plugins Remove ${config.plugin}", script: cmd)
  }
  catch (hudson.AbortException error) {
    print 'Failure using packer plugins remove.'
    throw error
  }
  print 'Packer plugins remove executed successfully.'
}

void plugins(Map config) {
  // input checking
  assert (['installed', 'required'].contains(config.command)) : 'The command parameter must be one of "installed" or "required".'
  config.bin = config.bin ?: 'packer'

  List<String> cmd = [config.bin, 'plugins', config.command]

  // check for optional inputs
  // conditional based on command to double verify dir param input both exists and is valid
  // groovy 3: if (config.command === 'required') {
  if (config.command == 'required') {
    assert config.dir in String : 'The required "dir" parameter was not set.'
    assert fileExists(config.dir) : "The Packer config directory ${config.dir} does not exist!"
  }

  // interact with packer plugins
  try {
    // groovy 3: if (config.command === 'required') {
    if (config.command == 'required') {
      dir(config.dir) {
        sh(label: "Packer Plugins ${config.command.capitalize()}", script: "${cmd} .")
      }
    }
    else {
      sh(label: "Packer Plugins ${config.command.capitalize()}", script: cmd)
    }
    }
  catch (hudson.AbortException error) {
    print 'Failure using packer plugins.'
    throw error
  }
  print 'Packer plugins executed successfully.'
  }

Boolean validate(Map config) {
  // input checking
  assert config.template in String : 'The required template parameter was not set.'
  if (config.except && config.only) {
    error(message: "The 'except' and 'only' parameters for packer.validate are mutually exclusive; only one can be specified.")
  }
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  config.bin = config.bin ?: 'packer'

  List<String> cmd = [config.bin, 'validate']

  // check for optional inputs
  cmd += varSubCmd(config)

  if (config.except) {
    assert (config.except in List) : 'The except parameter must be a list of strings.'

    cmd.add("-except=${config.except.join(',')}")
  }
  if (config.only) {
    assert (config.only in List) : 'The only parameter must be a list of strings.'

    cmd.add("-only=${config.only.join(',')}")
  }
  if (config.evalData == true) {
    cmd.add('-evaluate-datasources')
  }
  if (config.warnUndeclVar == false) {
    cmd.add('-no-warn-undeclared-var')
  }
  if (config.syntaxOnly == true) {
    cmd.add('-syntax-only')
  }

  // validate template with packer
  int returnCode
  if (config.template ==~ /\.pkr\./) {
    returnCode = sh(label: "Packer Validate ${config.template}", script: "${cmd} ${config.template}", returnStatus: true)
  }
  else {
    dir(config.template) {
      returnCode = sh(label: "Packer Validate ${config.template}", script: "${cmd} .", returnStatus: true)
    }
  }

  // return by code
  if (returnCode == 0) {
    print 'The configs and templates successfully validated.'
    return true
  }
  else if (returnCode == 1) {
    print 'The configs and templates failed validation.'
    return false
  }
  print 'Failure using packer validate.'
  error(message: 'Packer validate failed unexpectedly')
}

// private method for vars
private String varSubCmd(Map config) {
  List<String> subCmd = []

  // check for optional var inputs
  if (config.varFile) {
    assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

    subCmd.add("-var-file=${config.varFile}")
  }
  if (config.var) {
    assert (config.var in Map) : 'The var parameter must be a Map.'

    config.var.each { String var, String value ->
        // convert value to json if not string type
        if (value in List || value in Map) {
          value = writeJSON(json: value, returnText: true)
        }

        subCmd.addAll(['-var', "${var}=${value}"])
    }
  }

  return subCmd
}
