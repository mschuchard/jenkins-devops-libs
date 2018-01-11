// vars/faas.groovy
import devops.common.utils

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
      echo "FaaS CLI version ${config.version} already installed at ${config.install_path}."
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
  echo "FaaS CLI successfully installed at ${config.install_path}/faas-cli."
}

def validate_template(String template) {
  // ensure template exists and then check yaml syntax
  if (fileExists(template)) {
    try {
      readYaml(file: template)
    }
    catch(Exception error) {
      echo 'Template failed YAML validation.'
      throw error
    }
    echo "${template} is valid YAML."
  }
  else {
    throw new Exception("Template ${template} does not exist!")
  }
}
