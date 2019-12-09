// vars/faas.groovy
import devops.common.utils

def build(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'

  config.bin = config.bin ? config.bin : 'faas-cli'

  assert fileExists(config.template) : "The template file ${config.template} does not exist!"

  // create image with faas
  try {
    String cmd = "${config.bin} build"

    // check for optional inputs
    if (config.filter) {
      cmd += " --filter '${config.filter}'"
    }
    if (config.no_cache == true) {
      cmd += ' --no-cache'
    }
    if (config.parallel) {
      cmd += " --parallel ${config.parallel}"
    }
    if (config.regex) {
      cmd += " --regex '${config.regex}'"
    }
    if (config.squash == true) {
      cmd += ' --squash'
    }

    sh(label: 'OpenFaaS Build', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli build.'
    throw error
  }
  print 'FaaS build image created successfully.'
}

def deploy(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'

  if (config.replace && config.update) {
    throw new Exception('The parameters "replace" and "update" are mutually exclusive!')
  }
  config.bin = config.bin ? config.bin : 'faas-cli'

  assert fileExists(config.template) : "The template file ${config.template} does not exist!"

  // deploy function with faas
  try {
    String cmd = "${config.bin} deploy"

    // check for optional inputs
    if (config.filter) {
      cmd += " --filter '${config.filter}'"
    }
    if (config.label) {
      cmd += " --label ${config.label}"
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

    sh(label: 'OpenFaaS Deploy', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli deploy.'
    throw error
  }
  print 'FaaS function deployed successfully.'
}

def install(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  config.install_path = config.install_path ? config.install_path : '/usr/bin'
  assert (config.platform && config.version) : 'A required parameter is missing from this faas.install block. Please consult the documentation for proper usage.'
  assert fileExists(config.install_path) : "The desired installation path at ${config.install_path} does not exist."

  // check if current version already installed
  if (fileExists("${config.install_path}/faas-cli")) {
    String installed_version = sh(label: 'Check OpenFaaS CLI Version', returnStdout: true, script: "${config.install_path}/faas-cli version").trim()
    if (installed_version ==~ config.version) {
      print "FaaS CLI version ${config.version} already installed at ${config.install_path}."
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
  new utils().download_file("https://github.com/openfaas/faas-cli/releases/download/${config.version}/faas-cli${extension}", "${config.install_path}/faas-cli")
  extension = null
  sh(label: 'OpenFaaS CLI Executable Permissions', script: "chmod ug+rx ${config.install_path}/faas-cli")
  print "FaaS CLI successfully installed at ${config.install_path}/faas-cli."
}

def invoke(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  config.bin = config.bin ? config.bin : 'faas-cli'
  assert config.function : 'The required parameter function was not set.'

  // invoke faas function
  try {
    String cmd = "${config.bin} invoke"

    // check for optional inputs
    if (config.async == true) {
      cmd += ' -a'
    }
    if (config.content_type) {
      cmd += " --content-type ${config.content_type}"
    }
    if (config.header) {
      cmd += " -H ${config.header}"
    }
    if (config.method) {
      cmd += " -m ${config.method}"
    }
    if (config.query) {
      assert (config.query instanceof List) : 'The query parameter must be an array of strings.'

      config.query.each() { query ->
        cmd += " --query ${query}"
      }
    }
    if (config.tls == false) {
      cmd += ' --tls-no-verify'
    }
    if (config.stdin) {
      cmd += " < ${config.stdin}"
    }

    sh(label: 'OpenFaaS Invoke', script: "${cmd} ${config.function}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli push.'
    throw error
  }
  print 'FaaS function container image pushed successfully.'
}

def login(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  assert config.gateway : 'The required gateway parameter was not set.'
  assert config.password : 'The required password parameter was not set.'
  assert config.user : 'The required user parameter was not set.'
  config.bin = config.bin ? config.bin : 'faas-cli'

  // login to faas gateway
  try {
    String cmd = "${config.bin} login"

    // check for optional inputs
    if (config.tls == false) {
      cmd += ' --tls-no-verify'
    }

    sh(label: 'OpenFaaS Login', script: "${cmd} -u ${config.user} -p ${config.password} -g ${config.gateway}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli login.'
    throw error
  }
  print 'Successfully logged in to FaaS gateway.'
}

def push(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file ${config.template} does not exist!"
  config.bin = config.bin ? config.bin : 'faas-cli'

  // push function with faas
  try {
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

    sh(label: 'OpenFaaS Push', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli push.'
    throw error
  }
  print 'FaaS function container image pushed successfully.'
}

def remove(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().params_converter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file ${config.template} does not exist!"
  config.bin = config.bin ? config.bin : 'faas-cli'

  // remove function with faas
  try {
    String cmd = "${config.bin} rm"

    // check for optional inputs
    if (config.filter) {
      cmd += " --filter '${config.filter}'"
    }
    if (config.regex) {
      cmd += " --regex '${config.regex}'"
    }

    sh(label: 'OpenFaaS Remove', script: "${cmd} -f ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using faas-cli remove.'
    throw error
  }
  print 'FaaS function removed successfully.'
}

def validate_template(String template) {
  // ensure template exists and then check yaml syntax
  assert fileExists(template) : "Template ${template} does not exist!"

  try {
    readYaml(file: template)
  }
  catch(Exception error) {
    print 'Template failed YAML validation.'
    throw error
  }
  print "${template} is valid YAML."
}
