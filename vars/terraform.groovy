// vars/terraform.groovy
import devops.common.utils

def apply(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.config_path != null : '"config_path" is a required parameter for terraform.apply.'
  assert fileExists(config.config_path) : "Terraform config/plan ${config.config_path} does not exist!"
  config.bin = config.bin == null ? 'terraform' : config.bin

  // apply the config
  try {
    String cmd = "${config.bin} apply -input=false -no-color -auto-approve"

    // check if a directory was passed for the config path
    if (!(config.config_path ==~ /plan\.tfplan/)) {
      // check for optional var inputs
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
    }
    // check for optional targets input
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

def destroy(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  // -force changed to -auto-approve in 0.11.4
  String installed_version = sh(returnStdout: true, script: "${config.bin} version").trim()
  if (installed_version ==~ /0\.1[2-9]\.[0-9]|0\.11\.[4-9]/) {
    String no_input_flag = '-auto-approve'
  }
  else {
    String no_input_flag = '-force'
  }

  assert config.config_path != null : '"config_path" is a required parameter for terraform.destroy.'
  assert fileExists(config.config_path) : "Terraform config/plan ${config.config_path} does not exist!"

  // destroy the state
  try {
    String cmd = "${config.bin} destroy -input=false -no-color ${no_input_flag}"

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
    print 'Failure using terraform destroy.'
    throw error
  }
  print 'Terraform destroy was successful.'
}

def init(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Working config directory ${config.dir} does not exist!"
  config.bin = config.bin == null ? 'terraform' : config.bin

  // initialize the working config directory
  try {
    String cmd = "${config.bin} init -input=false -no-color"

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

def imports(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources != null : 'Parameter resources must be specified.'
  assert (config.resources instanceof String[]) : 'Parameter resources must be an array of strings.'
  config.bin = config.bin == null ? 'terraform' : config.bin

  // import the resources
  try {
    String cmd = "${config.bin} import -no-color -input=false"

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
    if (config.dir != null) {
      assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"

      cmd += " -config=${config.dir}"
    }
    if (config.provider != null) {
      cmd += " -provider=${config.provider}"
    }
    if (config.state != null) {
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }

    // import each resource
    config.resources.each() { resource ->
      sh "${cmd} ${resource}"
    }
  }
  catch(Exception error) {
    print 'Failure using terraform import.'
    throw error
  }
  print 'Terraform imports were successful.'
}

def install(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.install_path = config.install_path == null ? '/usr/bin' : config.install_path
  assert (config.platform != null && config.version != null) : 'A required parameter is missing from the terraform.install method. Please consult the documentation for proper usage.'
  assert fileExists(config.install_path) : "The desired installation path at ${config.install_path} does not exist."

  // check if current version already installed
  if (fileExists("${config.install_path}/terraform")) {
    String installed_version = sh(returnStdout: true, script: "${config.install_path}/terraform version").trim()
    if (installed_version ==~ config.version) {
      print "Terraform version ${config.version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().download_file("https://releases.hashicorp.com/terraform/${config.version}/terraform_${config.version}_${config.platform}.zip", 'terraform.zip')
  unzip(zipFile: 'terraform.zip', dir: config.install_path)
  sh "chmod ug+rx ${config.install_path}/terraform"
  new utils().remove_file('terraform.zip')
  print "Terraform successfully installed at ${config.install_path}/terraform."
}

def plan(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.dir != null : '"dir" is a required parameter for terraform.plan.'
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  config.bin = config.bin == null ? 'terraform' : config.bin

  // generate a plan from the config directory
  try {
    String cmd = "${config.bin} plan -no-color -input=false -out=${config.dir}/plan.tfplan"

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
    if (config.destroy == true) {
      cmd += ' -destroy'
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
  Map config = [:]
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
  String install_loc = "${config.install_path}/${config.install_name}"

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
  else if (config.url ==~ /\.zip$/) {
    // append zip extension to avoid filename clashes
    install_loc = "${install_loc}.zip"
  }
  new utils().download_file(config.url, install_loc)
  if (config.url ==~ /\.zip$/) {
    unzip(zipFile: install_loc)
    new utils().remove_file(install_loc)
  }
  else {
    sh "chmod ug+rx ${install_loc}"
  }
  print "Terraform plugin successfully installed at ${install_loc}."
}

def state(config) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin == null ? 'terraform' : config.bin
  String cmd = config.bin

  // perform state manipulation
  try {
    if (config.state != null) {
      assert config.cmd != 'push' : 'The state parameter is incompatible with state pushing.'
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }

    // perform different commands based upon type of state action
    switch (config.cmd) {
      case 'move':
        assert (config.resources[0] instanceof String[]) : 'Parameter resources must be a nested array of strings for move command.';

        config.resources.each() { resource_pair ->
          sh "${cmd} mv ${resource_pair}[0] ${resource_pair}[1]"
        };
        break;
      case 'remove':
        assert (config.resources instanceof String[]) : 'Parameter resources must be an array of strings for remove command.';

        config.resources.each() { resource ->
          sh "${cmd} rm ${resource}"
        };
        break;
      case 'push':
        assert config.resources == null : 'Resources parameter is not allowed for push command.';

        sh "${cmd} push";
        break;
      default:
        throw new Exception("Unknown Terraform state command ${config.cmd} specified.");
    }
  }
  catch(Exception error) {
    print 'Failure using terraform state manipulation.'
    throw error
  }
  print 'Terraform state manipulation was successful.'
}

def taint(config) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources != null : 'Parameter resources must be specified.'
  assert (config.resources instanceof String[]) : 'Parameter resources must be an array of strings.'
  config.bin = config.bin == null ? 'terraform' : config.bin

  // taint the resources
  try {
    String cmd = "${config.bin} taint -no-color"

    // check for optional inputs
    if (config.module != null) {
      cmd += " -module=${config.module}"
    }
    if (config.state != null) {
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }

    // taint each resource
    config.resources.each() { resource ->
      sh "${cmd} ${resource}"
    }
  }
  catch(Exception error) {
    print 'Failure using terraform taint.'
    throw error
  }
  print 'Terraform taints were successful.'
}

def validate(config) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  config.bin = config.bin == null ? 'terraform' : config.bin

  // validate the config directory
  try {
    String cmd = "${config.bin} validate -no-color"

    // check for terraform >= 0.12 (those versions have flag for json output)
    String new_validate = sh(returnStdout: true, script: "${config.bin} validate --help") ==~ /-json/

    if (!(new_validate)) {
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
      if (config.check_vars == false) {
        cmd += ' -check-variables=false'
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

def workspace(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert (config.directory != null && config.workspace != null) : 'A required parameter is missing from this terraform.workspace block. Please consult the documentation for proper usage.'
  config.bin = config.bin == null ? 'terraform' : config.bin

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
    print "Terraform workspace ${config.workspace} selected successfully."
  }
}
