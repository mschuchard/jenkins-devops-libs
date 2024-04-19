// vars/packer.groovy
import devops.common.utils
import devops.common.hcl

void build(Map config) {
  // input checking
  assert config.template instanceof String : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  config.bin = config.bin ?: 'packer'

  String cmd = "${config.bin} build -color=false"

  // check for optional inputs
  if (config.varFile) {
    assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

    cmd += " -var-file=${config.varFile}"
  }
  if (config.var) {
    assert (config.var instanceof Map) : 'The var parameter must be a Map.'

    config.var.each() { var, value ->
      // convert value to json if not string type
      if (value instanceof List || value instanceof Map) {
        value = writeJSON(json: value, returnText: true)
      }

      cmd += " -var ${var}=${value}"
    }
  }
  if (config.only) {
    assert (config.only instanceof List) : 'The only parameter must be a list of strings.'

    cmd += " -only=${config.only.join(',')}"
  }
  if (config.force == true) {
    cmd += " -force"
  }
  if (config.onError) {
    assert (['default', 'abort', 'ask', 'run-cleanup-provisioner'].contains(config.onError)) : "The argument must be one of: default, abort, ask, or run-cleanup-provisioner."

    cmd += " -on-error=${config.onError}"
  }

  // create artifact with packer
  try {
    if (config.template ==~ /\.pkr\./) {
      sh(label: 'Packer Build', script: "${cmd} ${config.template}")
    }
    else {
      dir(config.template) {
        sh(label: 'Packer Build', script: "${cmd} .")
      }
    }
  }
  catch(Exception error) {
    print 'Failure using packer build.'
    throw error
  }
  print 'Packer build artifact created successfully.'
}

void fmt(Map config) {
  // input checking
  assert config.template instanceof String : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  if (config.write && config.check) {
    throw new Exception("The 'write' and 'check' options for packer.fmt are mutually exclusive - only one can be enabled.")
  }
  config.bin = config.bin ?: 'packer'

  String cmd = "${config.bin} fmt"

  // check for optional inputs
  if (config.diff == true) {
    cmd += ' -diff'
  }
  if (config.check == true) {
    cmd += ' -check'
  }
  // incompatible with above
  else if (config.write == true) {
    cmd += ' -write'
  }

  try {
    if (config.template ==~ /\.pkr\./) {
      final int fmtStatus = sh(label: 'Packer Format', returnStatus: true, script: "${cmd} ${config.template}")
    }
    else {
      dir(config.template) {
        final int fmtStatus = sh(label: 'Packer Format', returnStatus: true, script: "${cmd} .")
      }
    }

    // report if formatting check detected issues
    if ((config.check == true) && (fmtStatus != 0)) {
      print 'Packer fmt has detected formatting errors.'
    }
  }
  catch(Exception error) {
    print 'Failure using packer fmt.'
    throw error
  }
  print 'Packer fmt was successful.'
}

void init(Map config) {
  // input checking
  assert fileExists(config.dir) : "Working template directory ${config.dir} does not exist."
  config.bin = config.bin ?: 'packer'

  String cmd = "${config.bin} init"

  // check for optional inputs
  if (config.upgrade == true) {
    cmd += ' -upgrade'
  }

  // initialize the working template directory
  try {
    dir(config.dir) {
      sh(label: 'Packer Init', script: "${cmd} .")
    }
  }
  catch(Exception error) {
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
    sh(label: 'Packer Inspect', script: "${bin} inspect ${template}")
  }
  catch(Exception error) {
    print 'Failure inspecting the template.'
    throw error
  }
  print 'Packer inspect was successful'
}

void install(Map config) {
  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform instanceof String && config.version instanceof String) : 'A required parameter ("platform" or "version") is missing from the packer.install method. Please consult the documentation for proper usage.'

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
  // determine number of elements in loc up to final slash
  final String elemCount = new File(installLoc).name.lastIndexOf('/')
  // return file path up to final slash element
  final String installDir = new File(installLoc).name.take(elemCount)

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

void plugins(Map config) {
  // input checking
  assert (['installed', 'required'].contains(config.command)) : 'The command parameter must be one of "installed" or "required".'
  config.bin = config.bin ?: 'packer'

  String cmd = "${config.bin} plugins ${config.command}"

  // check for optional inputs
  // conditional based on command to double verify dir param input both exists and is valid
  // groovy 3: if (config.command === 'required') {
  if (config.command == 'required') {
    assert config.dir instanceof String : 'The required "dir" parameter was not set.'
    assert fileExists(config.dir) : "The Packer config directory ${config.dir} does not exist!"
  }

  // interact with packer plugins
  try {
    // groovy 3: if (config.command === 'required') {
    if (config.command == 'required') {
      dir(config.dir) {
        sh(label: 'Packer Plugins', script: "${cmd} .")
      }
    }
    else {
      sh(label: 'Packer Plugins', script: cmd)
    }
  }
  catch(Exception error) {
    print 'Failure using packer plugins.'
    throw error
  }
  print 'Packer plugins executed successfully.'
}

void validate(Map config) {
  // input checking
  assert config.template instanceof String : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  config.bin = config.bin ?: 'packer'

  String cmd = "${config.bin} validate"

  // check for optional inputs
  if (config.varFile) {
    assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

    cmd += " -var-file=${config.varFile}"
  }
  if (config.var) {
    assert (config.var instanceof Map) : 'The var parameter must be a Map.'

    config.var.each() { var, value ->
      // convert value to json if not string type
      if (value instanceof List || value instanceof Map) {
        value = writeJSON(json: value, returnText: true)
      }

      cmd += " -var ${var}=${value}"
    }
  }
  if (config.only) {
    assert (config.only instanceof List) : 'The only parameter must be a list of strings.'

    cmd += " -only=${config.only.join(',')}"
  }
  if (config.evalData == true) {
    cmd += ' -evaluate-datasources'
  }
  if (config.warnUndeclVar == false) {
    cmd += ' -no-warn-undeclared-var'
  }
  if (config.syntaxOnly == true) {
    cmd += ' -syntax-only'
  }

  // validate template with packer
  try {
    if (config.template ==~ /\.pkr\./) {
      sh(label: 'Packer Validate', script: "${cmd} ${config.template}")
    }
    else {
      dir(config.template) {
        sh(label: 'Packer Validate', script: "${cmd} .")
      }
    }
  }
  catch(Exception error) {
    print 'Failure using packer validate.'
    throw error
  }
  print 'Packer validate executed successfully.'
}
