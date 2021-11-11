// vars/packer.groovy
import devops.common.utils

void build(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
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
    sh(label: 'Packer Build', script: "${cmd} ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using packer build.'
    throw error
  }
  print 'Packer build artifact created successfully.'
}

void fmt(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  if (config.write && config.check) {
    throw new Exception("The 'write' and 'check' options for packer.fmt are mutually exclusive - only one can be enabled.")
  }
  config.bin = config.bin ?: 'packer'

  String cmd = "${config.bin} fmt"

  if (config.diff == true) {
    cmd += " -diff"
  }
  if (config.check == true) {
    cmd += " -check"
  }
  // incompatible with above
  else if (config.write == true) {
    cmd += " -write"
  }

  try {
    int fmtStatus = sh(label: 'Packer Format', returnStatus: true, script: "${cmd} ${config.template}")

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

void init(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

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
      sh(label: 'Packer Init', script: cmd)
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

void install(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform && config.version) : 'A required parameter ("platform" or "version") is missing from the packer.install method. Please consult the documentation for proper usage.'

  new utils().makeDirParents(config.installPath)

  // check if current version already installed
  if (fileExists("${config.installPath}/packer")) {
    String installedVersion = sh(label: 'Check Packer Version', returnStdout: true, script: "${config.installPath}/packer version").trim()
    if (installedVersion ==~ config.version) {
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

void pluginInstall(String url, String installLoc) {
  // determine number of elements in loc up to final slash
  String elemCount = new File(installLoc).name.lastIndexOf('/')
  // return file path up to final slash element
  String installDir = new File(installLoc).name.take(elemCount)

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

void validate(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
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
      cmd += " -var ${var}=${value}"
    }
  }
  if (config.only) {
    assert (config.only instanceof List) : 'The only parameter must be a list of strings.'

    cmd += " -only=${config.only.join(',')}"
  }

  // validate template with packer
  try {
    sh(label: 'Packer Validate', script: "${cmd} ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using packer validate.'
    throw error
  }
  print 'Packer validate executed successfully.'
}
