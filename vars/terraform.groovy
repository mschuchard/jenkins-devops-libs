// vars/terraform.groovy
import devops.common.utils

def apply(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  if (fileExists(config.config_path)) {
    // apply the config
    try {
      sh "${config.bin} apply -input=false -no-color -auto-approve ${config.config_path}"
    }
    catch(Exception error) {
      print 'Failure using terraform apply.'
      throw error
    }
    print 'Terraform apply was successful.'
  }
  else {
    throw new Exception("Terraform config/plan ${config.config_path} does not exist!")
  }
}

def destroy(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  // -force changed to -auto-approve in 0.11.4
  installed_version = sh(returnStdout: true, script: "${config.bin} version").trim()
  if (installed_version =~ /0\.1[2-9]\.[0-9]|0\.11\.[4-9]/) {
    no_input_flag = '-auto-approve'
  }
  else {
    no_input_flag = '-force'
  }
  if (fileExists(config.dir)) {
    // apply the config
    try {
      sh "${config.bin} destroy -input=false -no-color ${no_input_flag} ${config.dir}"
    }
    catch(Exception error) {
      print 'Failure using terraform destroy.'
      throw error
    }
    print 'Terraform destroy was successful.'
  }
  else {
    throw new Exception("Terraform config ${config.dir} does not exist!")
  }
}

def init(String dir, String bin = 'terraform') {
  if (fileExists(dir)) {
    // initialize the working config directory
    try {
      sh "${bin} init -input=false -no-color ${dir}"
    }
    catch(Exception error) {
      print 'Failure using terraform init.'
      throw error
    }
    print 'Terraform init was successful.'
  }
  else {
    throw new Exception("Working config directory ${dir} does not exist!")
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
    throw new Exception('A required parameter is missing from this terraform.install block. Please consult the documentation for proper usage.')
  }

  // check if current version already installed
  if (fileExists("${config.install_path}/terraform")) {
    installed_version = sh(returnStdout: true, script: "${config.install_path}/terraform version").trim()
    if (installed_version =~ config.version) {
      print "Terraform version ${config.version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().download_file("https://releases.hashicorp.com/terraform/${config.version}/terraform_${config.version}_${config.platform}.zip", 'terraform.zip')
  unzip(zipFile: 'terraform.zip', dir: config.install_path)
  sh "chmod +rx ${config.install_path}/terraform"
  new utils().remove_file('terraform.zip')
  print "Terraform successfully installed at ${config.install_path}/terraform."
}

def plan(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  if (fileExists(config.dir)) {
    // generate a plan from the config directory
    try {
      sh "${config.bin} plan -no-color -out=${config.dir}/plan.tfplan ${config.dir}"
    }
    catch(Exception error) {
      print 'Failure using terraform plan.'
      throw error
    }
    print 'Terraform plan was successful.'
  }
  else {
    throw new Exception("Config directory ${config.dir} does not exist!")
  }
}

def validate(config) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  if (fileExists(config.dir)) {
    // validates the config directory
    try {
      sh "${bin} validate -no-color ${dir}"
    }
    catch(Exception error) {
      print 'Failure using terraform validate.'
      throw error
    }
    print 'Terraform validate was successful.'
  }
  else {
    throw new Exception("Config directory ${config.dir} does not exist!")
  }
}

def workspace(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  if (config.directory == null || config.workspace == null) {
    throw new Exception('A required parameter is missing from this terraform.workspace block. Please consult the documentation for proper usage.')
  }

  if (fileExists(config.dir)) {
    dir(config.dir) {
      // select workspace in terraform config directory
      try {
        sh "${config.bin} workspace select -no-color ${config.workspace}"
      }
      catch(Exception error) {
        print 'Failure using terraform workspace select.'
        throw error
      }
      print 'Terraform workspace selected successfully.'
    }
  }
  else {
    throw new Exception("The config directory ${config.dir} does not exist!")
  }
}
