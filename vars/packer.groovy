// vars/packer.groovy
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
  config.bin = config.bin == null ? 'packer' : config.bin

  if (fileExists(config.template)) {
    // create artifact with packer
    try {
      cmd = "${config.bin} build -color=false"

      // check for optional inputs
      if (config.var_file != null) {
        if (fileExists(config.var_file)) {
          cmd += " -var_file=${config.var_file}"
        }
        else {
          throw new Exception("The var file ${config.var_file} does not exist!")
        }
      }
      if (config.var != null) {
        if (!(config.var instanceof String[])) {
          throw new Exception('The var parameter must be an array of strings.')
        }
        config.var.each() {
          cmd += " -var ${it}"
        }
      }
      if (config.only != null) {
        cmd += " -only=${config.only}"
      }

      sh "${cmd} ${config.template}"
    }
    catch(Exception error) {
      print 'Failure using packer build.'
      throw error
    }
    print 'Packer build artifact created successfully.'
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
    throw new Exception('A required parameter is missing from this packer.install block. Please consult the documentation for proper usage.')
  }

  // check if current version already installed
  if (fileExists("${config.install_path}/packer")) {
    installed_version = sh(returnStdout: true, script: "${config.install_path}/packer version").trim()
    if (installed_version =~ config.version) {
      print "Packer version ${config.version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().download_file("https://releases.hashicorp.com/packer/${config.version}/packer_${config.version}_${config.platform}.zip", 'packer.zip')
  unzip(zipFile: 'packer.zip', dir: config.install_path)
  sh "chmod +rx ${config.install_path}/packer"
  new utils().remove_file('packer.zip')
  print "Packer successfully installed at ${config.install_path}/packer."
}

def plugin_install(String url, String install_loc) {
  install_dir = new File(install_loc).name.take(File(install_loc).name.lastIndexOf('/'))

  // check if plugin dir exists and create if not
  if (!(fileExists(install_dir))) {
    new File(install_dir).mkdir()
  }

  // check if plugin already installed
  if (fileExists(install_loc)) {
    print "Packer plugin already installed at ${install_loc}."
    return
  }
  // otherwise download and install plugin
  if (url =~ /\.zip$/) {
    // append zip extension to avoid filename clashes
    install_loc = "${install_loc}.zip"
  }
  new utils().download_file(url, install_loc)
  if (url =~ /\.zip$/) {
    unzip(zipFile: install_loc)
    new utils().remove_file(install_loc)
  }
  else {
    sh "chmod +rx ${install_loc}"
  }
  print "Packer plugin successfully installed at ${install_loc}."
}

def validate(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.template == null) {
    throw new Exception('The required template parameter was not set.')
  }
  config.bin = config.bin == null ? 'packer' : config.bin

  if (fileExists(config.template)) {
    // validate template with packer
    try {
      cmd = "${config.bin} validate"

      // check for optional inputs
      if (config.var_file != null) {
        if (fileExists(config.var_file)) {
          cmd += " -var_file=${config.var_file}"
        }
        else {
          throw new Exception("The var file ${config.var_file} does not exist!")
        }
      }
      if (config.var != null) {
        if (!(config.var instanceof String[])) {
          throw new Exception('The var parameter must be an array of strings.')
        }
        config.var.each() {
          cmd += " -var ${it}"
        }
      }
      if (config.only != null) {
        cmd += " -only=${config.only}"
      }

      sh "${cmd} ${config.template}"
    }
    catch(Exception error) {
      print 'Failure using packer validate.'
      throw error
    }
    print 'Packer validate executed successfully.'
  }
  else {
    throw new Exception("The template file ${config.template} does not exist!")
  }
}
