// vars/terraform.groovy
import devops.common.utils

def apply(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  assert fileExists(config.config_path) : "Terraform config/plan ${config.config_path} does not exist!"

  // apply the config
  try {
    cmd = "${config.bin} apply -input=false -no-color -auto-approve"

    // check for optional inputs
    if (config.var_file != null) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var != null) {
      assert (config.var instanceof String[]) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }
    if (config.target != null) {
      assert (config.target instanceof String[]) : 'The target parameter must be an array of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
      }
    }

    sh "${cmd} ${config.config_path}"
  }
  catch(Exception error) {
    print 'Failure using terraform apply.'
    throw error
  }
  print 'Terraform apply was successful.'
}

def destroy(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

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
  assert fileExists(config.dir) : "Terraform config ${config.dir} does not exist!"

  // destroy the state
  try {
    cmd = "${config.bin} destroy -input=false -no-color ${no_input_flag}"

    // check for optional inputs
    if (config.var_file != null) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var != null) {
      assert (config.var instanceof String[]) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }
    if (config.target != null) {
      assert (config.target instanceof String[]) : 'The target parameter must be an array of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
      }
    }

    sh "${cmd} ${config.dir}"
  }
  catch(Exception error) {
    print 'Failure using terraform destroy.'
    throw error
  }
  print 'Terraform destroy was successful.'
}

def init(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin

  assert fileExists(config.dir) : "Working config directory ${config.dir} does not exist!"

  // initialize the working config directory
  try {
    cmd = "${config.bin} init -input=false -no-color"

    // check for optional inputs
    if (config.plugin_dir != null) {
      if (!fileExists(config.plugin_dir)) {
        new File(config.plugin_dir).mkdir()
      }

      cmd += " -plugin-dir=${config.plugin_dir}"
    }
    if (config.upgrade == true) {
      cmd += ' -upgrade'
    }

    sh "${cmd} ${config.dir}"
  }
  catch(Exception error) {
    print 'Failure using terraform init.'
    throw error
  }
  print 'Terraform init was successful.'
}

def install(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.install_path = config.install_path == null ? '/usr/bin' : config.install_path
  assert (config.platform != null && config.version != null) : 'A required parameter is missing from the terraform.install method. Please consult the documentation for proper usage.'

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

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"

  // generate a plan from the config directory
  try {
    cmd = "${config.bin} plan -no-color -input=false -out=${config.dir}/plan.tfplan"

    // check for optional inputs
    if (config.var_file != null) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var != null) {
      assert (config.var instanceof String[]) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }
    if (config.target != null) {
      assert (config.target instanceof String[]) : 'The target parameter must be an array of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
      }
    }

    sh "${cmd} ${config.dir}"
  }
  catch(Exception error) {
    print 'Failure using terraform plan.'
    throw error
  }
  print 'Terraform plan was successful.'
}

def plugin_install(config) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.url != null : "The required parameter 'url' was not set."
  assert config.install_name != null : "The required parameter 'install_name' was not set."

  config.install_path = config.install_path == null ? '~/.terraform.d/plugins' : config.install_path

  // set and assign plugin install location
  install_loc = "${config.install_path}/${config.install_name}"

  // check if plugin dir exists and create if not
  if (!(fileExists(config.install_path))) {
    new File(config.install_path).mkdir()
  }

  // check if plugin already installed
  if (fileExists(install_loc)) {
    print "Terraform plugin already installed at ${install_loc}."
    return
  }
  // otherwise download and install plugin
  else if (config.url =~ /\.zip$/) {
    // append zip extension to avoid filename clashes
    install_loc = "${install_loc}.zip"
  }
  new utils().download_file(config.url, install_loc)
  if (config.url =~ /\.zip$/) {
    unzip(zipFile: install_loc)
    new utils().remove_file(install_loc)
  }
  else {
    sh "chmod +rx ${install_loc}"
  }
  print "Terraform plugin successfully installed at ${install_loc}."
}

def validate(config) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin

  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"

  // validate the config directory
  try {
    cmd = "${config.bin} validate -no-color"

    // check for optional inputs
    if (config.var_file != null) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var != null) {
      assert (config.var instanceof String[]) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }

    sh "${cmd} ${config.dir}"
  }
  catch(Exception error) {
    print 'Failure using terraform validate.'
    throw error
  }
  print 'Terraform validate was successful.'
}

def workspace(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  assert (config.directory != null && config.workspace != null) : 'A required parameter is missing from this terraform.workspace block. Please consult the documentation for proper usage.'

  assert fileExists(config.dir) : "The config directory ${config.dir} does not exist!"

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
