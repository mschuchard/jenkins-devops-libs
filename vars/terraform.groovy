// vars/terraform.groovy
def apply(String config_path, String bin = '/usr/bin/terraform') {
  if (fileExists(config_path)) {
    // apply the config
    try {
      sh "${config.path} apply -no-color -auto-approve=true ${config.config_path}"
    }
    catch(Exception error) {
      echo 'Failure using terraform apply.'
      throw error
    }
  }
  else {
    throw new Exception("Terraform config/plan ${config_path} does not exist!")
  }
}

def init(String dir, String bin = '/usr/bin/terraform') {
  if (fileExists(dir)) {
    // initialize the working config directory
    try {
      sh "${bin} init -no-color ${dir}"
    }
    catch(Exception error) {
      echo 'Failure using terraform init.'
      throw error
    }
  }
  else {
    throw new Exception('Working config directory does not exist!')
  }
}

def install(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.install_path = config.install_path == null ? '/usr/bin' : config.install_path
  if (config.platform == null || config.version == null) {
    throw new Exception('A required parameter is missing from this terraform.install block. Please consult the documentation for proper usage.')
  }

  // check if current version already installed
  if (fileExists("${config.install_path}/terraform")) {
    installed_version = sh(returnStdout: true, script: "${config.install_path}/terraform version").trim()
    if (installed_version =~ version) {
      echo "Terraform version ${version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise download and install specified version
  sh "curl -L https://releases.hashicorp.com/terraform/${config.version}/terraform_${config.version}_${config.platform}.zip -o terraform.zip"
  sh "unzip terraform.zip -d ${config.install_path}"
  sh "chmod +rx ${config.install_path}/terraform"
  new File("${config.install_path}/terraform.zip").delete()
  echo "Terraform successfully installed at ${config.install_path}/terraform."
}

def plan(String dir, String bin = '/usr/bin/terraform') {
  if (fileExists(dir)) {
    // generate a plan from the config directory
    try {
      sh "${bin} plan -no-color -out=${dir}/plan.tfplan ${dir}"
    }
    catch(Exception error) {
      echo 'Failure using terraform plan.'
      throw error
    }
  }
  else {
    throw new Exception('Config directory does not exist!')
  }
}

def validate(String dir, String bin = '/usr/bin/terraform') {
  if (fileExists(dir)) {
    // validates the config directory
    try {
      sh "${bin} validate -no-color ${dir}"
    }
    catch(Exception error) {
      echo 'Failure using terraform validate.'
      throw error
    }
  }
  else {
    throw new Exception('Config directory does not exist!')
  }
}
