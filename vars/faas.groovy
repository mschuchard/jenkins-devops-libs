// vars/faas.groovy
import devops.common.utils

def build(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.template == null) {
    throw new Exception('The required template parameter was not set.')
  }
  config.bin = config.bin == null ? 'faas-cli' : config.bin

  if (fileExists(config.template)) {
    // create image with faas
    try {
      cmd = "${config.bin} build"

      // check for optional inputs
      if (config.filter != null) {
        cmd += " --filter '${config.filter}'"
      }
      if (config.no_cache == true) {
        cmd += ' --no-cache'
      }
      if (config.parallel != null) {
        cmd += " --parallel ${config.parallel}"
      }
      if (config.regex != null) {
        cmd += " --regex '${config.regex}'"
      }
      if (config.squash == true) {
        cmd += ' --squash'
      }

      sh "${cmd} -f ${config.template}"
    }
    catch(Exception error) {
      print 'Failure using faas-cli build.'
      throw error
    }
    print 'FaaS build image created successfully.'
  }
  else {
    throw new Exception("The template file ${config.template} does not exist!")
  }
}

def deploy(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.template == null) {
    throw new Exception('The required template parameter was not set.')
  }
  if (config.replace != null && config.update != null) {
    throw new Exception('The parameters "replace" and "update" are mutually exclusive!')
  }
  config.bin = config.bin == null ? 'faas-cli' : config.bin

  if (fileExists(config.template)) {
    // deploy function with faas
    try {
      cmd = "${config.bin} deploy"

      // check for optional inputs
      if (config.filter != null) {
        cmd += " --filter '${config.filter}'"
      }
      if (config.label != null) {
        cmd += " --label ${config.label}"
      }
      if (config.regex != null) {
        cmd += " --regex '${config.regex}'"
      }
      if (config.replace == false) {
        cmd += ' --replace=false'
      }
      if (config.secret != null) {
        cmd += " --secret ${config.secret}"
      }
      if (config.update == true) {
        cmd += ' --update=true'
      }

      sh "${cmd} -f ${config.template}"
    }
    catch(Exception error) {
      print 'Failure using faas-cli deploy.'
      throw error
    }
    print 'FaaS function deployed successfully.'
  }
  else {
    throw new Exception("The template file ${config.template} does not exist!")
  }
}

def install(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.install_path = config.install_path == null ? '/usr/bin' : config.install_path
  if (config.platform == null || config.version == null) {
    throw new Exception('A required parameter is missing from this faas.install block. Please consult the documentation for proper usage.')
  }

  // check if current version already installed
  if (fileExists("${config.install_path}/faas-cli")) {
    installed_version = sh(returnStdout: true, script: "${config.install_path}/faas-cli version").trim()
    if (installed_version =~ config.version) {
      print "FaaS CLI version ${config.version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise determine extension based on platform
  def extension = ''
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
  sh "chmod +rx ${config.install_path}/faas-cli"
  print "FaaS CLI successfully installed at ${config.install_path}/faas-cli."
}

def invoke(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'faas-cli' : config.bin

  // invoke faas function
  try {
    cmd = "${config.bin} invoke"

    // check for optional inputs
    if (config.async == true) {
      cmd += ' -a'
    }
    if (config.content_type != null) {
      cmd += " --content-type ${config.content_type}"
    }
    if (config.header != null) {
      cmd += " -H ${config.header}"
    }
    if (config.method != null) {
      cmd += " -m ${config.method}"
    }
    if (config.query != null) {
      if (!(config.query instanceof String[])) {
        throw new Exception('The query parameter must be an array of strings.')
      }
      config.query.each() { query ->
        cmd += " --query ${query}"
      }
    }
    if (config.stdin != null) {
      cmd += " < ${config.stdin}"
    }

    sh cmd
  }
  catch(Exception error) {
    print 'Failure using faas-cli push.'
    throw error
  }
  print 'FaaS function container image pushed successfully.'
}

def login(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.gateway == null) {
    throw new Exception('The required gateway parameter was not set.')
  }
  else if (config.password == null) {
    throw new Exception('The required password parameter was not set.')
  }
  else if (config.user == null) {
    throw new Exception('The required user parameter was not set.')
  }
  config.bin = config.bin == null ? 'faas-cli' : config.bin

  // login to faas gateway
  try {
    cmd = "${config.bin} login"

    // check for optional inputs
    if (config.tls == false) {
      cmd += ' --tls-no-verify'
    }

    sh "${cmd} -u ${config.user} -p ${config.password} -g ${config.gateway}"
  }
  catch(Exception error) {
    print 'Failure using faas-cli login.'
    throw error
  }
  print 'Successfully logged in to FaaS gateway.'
}

def push(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.template == null) {
    throw new Exception('The required template parameter was not set.')
  }
  config.bin = config.bin == null ? 'faas-cli' : config.bin

  if (fileExists(config.template)) {
    // push function with faas
    try {
      cmd = "${config.bin} push"

      // check for optional inputs
      if (config.filter != null) {
        cmd += " --filter '${config.filter}'"
      }
      if (config.regex != null) {
        cmd += " --regex '${config.regex}'"
      }
      if (config.parallel != null) {
        cmd += " --parallel ${config.parallel}"
      }
      if (config.tag != null) {
        cmd += " --tag ${config.tag}"
      }

      sh "${cmd} -f ${config.template}"
    }
    catch(Exception error) {
      print 'Failure using faas-cli push.'
      throw error
    }
    print 'FaaS function container image pushed successfully.'
  }
  else {
    throw new Exception("The template file ${config.template} does not exist!")
  }
}

def remove(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.template == null) {
    throw new Exception('The required template parameter was not set.')
  }
  config.bin = config.bin == null ? 'faas-cli' : config.bin

  if (fileExists(config.template)) {
    // remove function with faas
    try {
      cmd = "${config.bin} rm"

      // check for optional inputs
      if (config.filter != null) {
        cmd += " --filter '${config.filter}'"
      }
      if (config.regex != null) {
        cmd += " --regex '${config.regex}'"
      }

      sh "${cmd} -f ${config.template}"
    }
    catch(Exception error) {
      print 'Failure using faas-cli remove.'
      throw error
    }
    print 'FaaS function removed successfully.'
  }
  else {
    throw new Exception("The template file ${config.template} does not exist!")
  }
}

def validate_template(String template) {
  // ensure template exists and then check yaml syntax
  if (fileExists(template)) {
    try {
      readYaml(file: template)
    }
    catch(Exception error) {
      print 'Template failed YAML validation.'
      throw error
    }
    print "${template} is valid YAML."
  }
  else {
    throw new Exception("Template ${template} does not exist!")
  }
}
