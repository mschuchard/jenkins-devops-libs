// vars/faas.groovy
import devops.common.utils

void build(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  config.bin = config.bin ?: 'faas-cli'
  assert readYaml(config.template) instanceof String : "The template file ${config.template} does not exist or is not a valid YAML file!"

  String cmd = "${config.bin} build"

  // check for optional inputs
  if (config.filter) {
    cmd += " --filter '${config.filter}'"
  }
  if (config.noCache == true) {
    cmd += ' --no-cache'
  }
  if (config.parallel) {
    cmd += " --parallel ${config.parallel}"
  }
  if (config.pull) {
    cmd += ' --pull'
  }
  if (config.regex) {
    cmd += " --regex '${config.regex}'"
  }
  if (config.squash == true) {
    cmd += ' --squash'
  }
  if (config.tag) {
    cmd += " --tag ${config.tag}"
  }

  // create image with faas
  try {
    sh(label: 'OpenFaaS Build', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli build.'
    throw error
  }
  print 'FaaS build image created successfully.'
}

void deploy(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  if (config.replace && config.update) {
    throw new Exception('The parameters "replace" and "update" are mutually exclusive!')
  }
  assert readYaml(config.template) instanceof String : "The template file ${config.template} does not exist or is not a valid YAML file!"

  config.bin = config.bin ?: 'faas-cli'
  String cmd = "${config.bin} deploy"

  // check for optional inputs
  if (config.filter) {
    cmd += " --filter '${config.filter}'"
  }
  if (config.gateway) {
    cmd += " -g ${config.gateway}"
  }
  if (config.label) {
    assert (config.label instanceof Map) : 'The label parameter must be a Map.'

    config.label.each() { label, value ->
      cmd += " --label ${label}=${value}"
    }
  }
  if (config.namespace) {
    cmd += " -n ${config.namespace}"
  }
  if (config.regex) {
    cmd += " --regex '${config.regex}'"
  }
  if (config.replace == false) {
    cmd += ' --replace=false'
  }
  if (config.secret) {
    cmd += " --secret ${config.secret}"
  }
  if (config.update == true) {
    cmd += ' --update=true'
  }

  // deploy function with faas
  try {
    sh(label: 'OpenFaaS Deploy', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli deploy.'
    throw error
  }
  print 'FaaS function deployed successfully.'
}

void install(Map config) {
  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform instanceof String && config.version instanceof String) : 'A required parameter is missing from this faas.install block. Please consult the documentation for proper usage.'
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
    case 'linux': extension = ''; break;
    case 'windows': extension = '.exe'; break;
    case 'darwin': extension = '-darwin'; break;
    case 'linux-arm64': extension = '-arm64'; break;
    case 'linux-armhf': extension = '-armhf'; break;
    default: throw new Exception("Unsupported platform ${config.platform} specified!");
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
  assert config.function instanceof String : 'The required parameter function was not set.'

  String cmd = "${config.bin} invoke"

  // check for optional inputs
  if (config.async == true) {
    cmd += ' -a'
  }
  if (config.contentType) {
    cmd += " --content-type ${config.contentType}"
  }
  if (config.gateway) {
    cmd += " -g ${config.gateway}"
  }
  if (config.header) {
    assert (config.header instanceof Map) : 'The header parameter must be a Map.'

    config.header.each() { header, value ->
      cmd += " -H ${header}=${value}"
    }
  }
  if (config.method) {
    cmd += " -m ${config.method}"
  }
  if (config.namespace) {
    cmd += " -n ${config.namespace}"
  }
  if (config.query) {
    assert (config.query instanceof Map) : 'The query parameter must be a Map.'

    config.query.each() { query, value ->
      cmd += " --query ${query}=${value}"
    }
  }
  if (config.tls == false) {
    cmd += ' --tls-no-verify'
  }
  if (config.stdin) {
    cmd += " < ${config.stdin}"
  }

  // invoke faas function
  try {
    sh(label: 'OpenFaaS Invoke', script: "${cmd} ${config.function}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli push.'
    throw error
  }
  print 'FaaS function container image pushed successfully.'
}

void login(Map config) {
  // input checking
  assert config.password instanceof String : 'The required password parameter was not set.'
  config.bin = config.bin ?: 'faas-cli'

  String cmd = "${config.bin} login"

  // check for optional inputs
  if (config.gateway) {
    cmd += " -g ${config.gateway}"
  }
  if (config.tls == false) {
    cmd += ' --tls-no-verify'
  }
  if (config.user) {
    cmd += " -u ${config.user}"
  }

  // login to faas gateway
  try {
    sh(label: 'OpenFaaS Login', script: "${cmd} -p ${config.password}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli login.'
    throw error
  }
  print 'Successfully logged in to FaaS gateway.'
}

void push(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert readYaml(config.template) instanceof String : "The template file ${config.template} does not exist or is not a valid YAML file!"
  config.bin = config.bin ?: 'faas-cli'

  String cmd = "${config.bin} push"

  // check for optional inputs
  if (config.filter) {
    cmd += " --filter '${config.filter}'"
  }
  if (config.regex) {
    cmd += " --regex '${config.regex}'"
  }
  if (config.parallel) {
    cmd += " --parallel ${config.parallel}"
  }
  if (config.tag) {
    cmd += " --tag ${config.tag}"
  }

  // push function with faas
  try {
    sh(label: 'OpenFaaS Push', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli push.'
    throw error
  }
  print 'FaaS function container image pushed successfully.'
}

void remove(Map config) {
  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert readYaml(config.template) instanceof String : "The template file ${config.template} does not exist or is not a valid YAML file!"
  config.bin = config.bin ?: 'faas-cli'

  String cmd = "${config.bin} rm"

  // check for optional inputs
  if (config.filter) {
    cmd += " --filter '${config.filter}'"
  }
  if (config.gateway) {
    cmd += " -g ${config.gateway}"
  }
  if (config.namespace) {
    cmd += " -n ${config.namespace}"
  }
  if (config.regex) {
    cmd += " --regex '${config.regex}'"
  }

  // remove function with faas
  try {
    sh(label: 'OpenFaaS Remove', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli remove.'
    throw error
  }
  print 'FaaS function removed successfully.'
}

void validateTemplate(String template) {
  // ensure template exists and then check yaml syntax
  assert readFile(template) instanceof String : "Template ${template} does not exist!"

  try {
    readYaml(file: template)
  }
  catch(Exception error) {
    print 'Template failed YAML validation.'
    throw error
  }
  print "${template} is valid YAML."
}
