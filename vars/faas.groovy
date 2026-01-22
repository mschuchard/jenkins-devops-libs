// vars/faas.groovy
import devops.common.utils
import devops.common.helpers

void build(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  config.bin = config.bin ?: 'faas-cli'
  assert validateTemplate(config.template) : "The template file ${config.template} does not exist or is not a valid YAML file!"

  List<String> cmd = [config.bin, 'build']

  // check for optional inputs
  if (config.noCache == true) {
    cmd.add('--no-cache')
  }
  if (config.parallel) {
    cmd.addAll(['--parallel', config.parallel])
  }
  if (config.pull) {
    cmd.add('--pull')
  }
  if (config.squash == true) {
    cmd.add('--squash')
  }
  if (config.tag) {
    cmd.addAll(['--tag', config.tag])
  }

  // create image with faas
  new helpers().toolExec("OpenFaaS Build ${config.template}", cmd)
}

void deploy(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  if (config.replace && config.update) {
    error(message: 'The parameters "replace" and "update" are mutually exclusive!')
  }
  assert validateTemplate(config.template) : "The template file ${config.template} does not exist or is not a valid YAML file!"

  config.bin = config.bin ?: 'faas-cli'
  List<String> cmd = [config.bin, 'deploy']

  // check for optional inputs
  if (config.label) {
    assert (config.label in Map) : 'The label parameter must be a Map.'

    config.label.each { String label, String value ->
      cmd.addAll(['--label', "${label}=${value}"])
    }
  }
  if (config.replace === false) {
    cmd.add('--replace=false')
  }
  else if (config.update == true) {
    cmd.add('--update=true')
  }
  else if (config.strategy) {
    assert ['replace', 'update'].contains(config.strategy) : 'The strategy parameter must be either "replace" or "update".'

    cmd.add(["--${config.strategy}"])
  }
  if (config.secret) {
    cmd.addAll(['--secret', config.secret])
  }
  cmd.addAll(globalArgsCmd(config))

  // deploy function with faas
  cmd.addAll(['-f', config.template])
  new helpers().toolExec("OpenFaaS Deploy ${config.template}", cmd)
}

void install(Map config) {
  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform in String && config.version in String) : 'A required parameter is missing from this faas.install block. Please consult the documentation for proper usage.'
  new utils().makeDirParents(config.installPath)

  // check if current version already installed
  if (fileExists("${config.installPath}/faas-cli")) {
    final String installedVersion = sh(label: 'Check OpenFaaS CLI Version', returnStdout: true, script: "${config.installPath}/faas-cli version").trim()
    if (installedVersion =~ config.version) {
      print "FaaS CLI version ${config.version} already installed at ${config.installPath}."
      return
    }
  }
  // otherwise determine extension based on platform
  String extension = ''
  switch (config.platform) {
    case 'linux': extension = ''; break
    case 'windows': extension = '.exe'; break
    case 'darwin': extension = '-darwin'; break
    case 'linux-arm64': extension = '-arm64'; break
    case 'linux-armhf': extension = '-armhf'; break
    default: error(message: "Unsupported platform ${config.platform} specified!")
  }
  // download and install specified version
  new utils().downloadFile("https://github.com/openfaas/faas-cli/releases/download/${config.version}/faas-cli${extension}", "${config.installPath}/faas-cli")
  extension = null
  sh(label: 'OpenFaaS CLI Executable Permissions', script: "chmod ug+rx ${config.installPath}/faas-cli")
  print "FaaS CLI successfully installed at ${config.installPath}/faas-cli."
}

void invoke(Map config) {
  // input checking
  config.bin = config.bin ?: 'faas-cli'
  assert config.function in String : 'The required parameter function was not set.'

  List<String> cmd = [config.bin, 'invoke']

  // check for optional inputs
  if (config.async == true) {
    cmd.add('-a')
  }
  if (config.contentType) {
    cmd.addAll(['--content-type', config.contentType])
  }
  if (config.header) {
    assert (config.header in Map) : 'The header parameter must be a Map.'

    config.header.each { String header, String value ->
      cmd.addAll(['-H', "${header}=${value}"])
    }
  }
  if (config.method) {
    cmd.addAll(['-m', config.method])
  }
  if (config.query) {
    assert (config.query in Map) : 'The query parameter must be a Map.'

    config.query.each { String query, String value ->
      cmd.addAll(['--query', "${query}=${value}"])
    }
  }
  if (config.stdin) {
    cmd.addAll(['<', config.stdin])
  }
  cmd.addAll(globalArgsCmd(config))

  // invoke faas function
  cmd.add(config.function)
  new helpers().toolExec("OpenFaaS Invoke ${config.function}", cmd)
}

String list(Map config) {
  // input checking
  if (config.quiet && config.verbose) {
    error(message: 'The "quiet" and "verbose" parameters for faas.list are mutually exclusive; only one can be specified.')
  }
  config.bin = config.bin ?: 'faas-cli'

  List<String> cmd = [config.bin, 'list']

  // optional inputs
  if (config.quiet) {
    cmd.add('-q')
  }
  else if (config.verbose) {
    cmd.add('-v')
  }
  if (config.sort) {
    assert ['name', 'invocations'].contains(config.sort) : 'The "sort" parameter value must be either "name" or "invocations".'

    cmd.addAll(['--sort', config.sort])
  }
  cmd.addAll(globalArgsCmd(config))

  // list faas functions
  String functions
  try {
    functions = sh(label: 'OpenFaaS List', script: cmd.join(' '), returnStdout: true)
  }
  catch (hudson.AbortException error) {
    print 'Failure using faas-cli list.'
    throw error
  }

  print 'FaaS function list executed successfully.'
  return functions
}

void login(Map config) {
  // input checking
  assert config.password in String : 'The required password parameter was not set.'
  config.bin = config.bin ?: 'faas-cli'

  List<String> cmd = [config.bin, 'login']

  // check for optional inputs
  if (config.user) {
    cmd.addAll(['-u', config.user])
  }
  cmd.addAll(globalArgsCmd(config))

  // login to faas gateway
  cmd.addAll(['-p', config.password])
  new helpers().toolExec('OpenFaaS Login', cmd)
}

String logs(Map config) {
  // input checking
  assert config.name in String : 'The required "name" parameter was not set.'
  config.bin = config.bin ?: 'faas-cli'

  List<String> cmd = [config.bin, 'logs']

  // optional inputs
  if (config.instance) {
    cmd.add('--instance')
  }
  if (config.format) {
    assert ['plain', 'keyvalue', 'json'].contains(config.format)

    cmd.addAll(['-o', config.format])
  }
  if (config.since) {
    cmd.addAll(['--since', config.since])
  }
  cmd.addAll(globalArgsCmd(config))

  // retrieve function logs
  String logs
  try {
    cmd.add(config.name)
    logs = sh(label: "OpenFaaS Logs ${config.name}", script: cmd.join(' '), returnStdout: true)
  }
  catch (hudson.AbortException error) {
    print 'Failure using faas-cli logs.'
    throw error
  }

  print 'FaaS function log retrieval executed successfully.'
  return logs
}

void push(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert validateTemplate(config.template) : "The template file ${config.template} does not exist or is not a valid YAML file!"
  config.bin = config.bin ?: 'faas-cli'

  List<String> cmd = [config.bin, 'push']

  // check for optional inputs
  if (config.parallel) {
    cmd.addAll(['--parallel', config.parallel])
  }
  if (config.tag) {
    cmd.addAll(['--tag', config.tag])
  }

  // push function with faas
  cmd.addAll(['-f', config.template])
  new helpers().toolExec("OpenFaaS Push ${config.template}", cmd)
}

void remove(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert validateTemplate(config.template) : "The template file ${config.template} does not exist or is not a valid YAML file!"
  config.bin = config.bin ?: 'faas-cli'

  List<String> cmd = [config.bin, 'rm']

  // check for optional inputs
  cmd.addAll(globalArgsCmd(config))

  // remove function with faas
  cmd.addAll(['-f', config.template])
  new helpers().toolExec("OpenFaaS Remove ${config.template}", cmd)
}

Boolean validateTemplate(String template) {
  return new helpers().validateYamlFile(template, 'template')
}

// private method for global arguments pertaining to all methods
private static List<String> globalArgsCmd(Map config) {
  // initialize subcommand from global args
  List<String> subCmd = []

  // check for optional inputs
  if (config.filter) {
    subCmd.addAll(['--filter', "'${config.filter}'"])
  }
  if (config.gateway) {
    subCmd.addAll(['-g', config.gateway])
  }
  if (config.namespace) {
    subCmd.addAll(['-n', config.namespace])
  }
  if (config.regex) {
    subCmd.addAll(['--regex', "'${config.regex}'"])
  }
  if (config.tls === false) {
    subCmd.add(' --tls-no-verify')
  }

  // return subcommand based from global arguments
  return subCmd
}
