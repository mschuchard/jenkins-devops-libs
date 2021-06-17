// vars/terraform.groovy
import devops.common.utils

void apply(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.config_path : '"config_path" is a required parameter for terraform.apply.'
  assert fileExists(config.config_path) : "Terraform config/plan ${config.config_path} does not exist!"
  config.bin = config.bin ?: 'terraform'

  // apply the config
  try {
    String cmd = "${config.bin} apply -input=false -no-color -auto-approve"

    // check if a directory was passed for the config path
    if (!(config.config_path ==~ /plan\.tfplan/)) {
      // check for optional var inputs
      if (config.var_file) {
        assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

        cmd += " -var-file=${config.var_file}"
      }
      if (config.var) {
        assert (config.var instanceof Map) : 'The var parameter must be a Map.'

        config.var.each() { var, value ->
          cmd += " -var ${var}=${value}"
        }
      }
      if (config.target) {
        assert (config.target instanceof List) : 'The target parameter must be a list of strings.'

        config.target.each() { target ->
          cmd += " -target=${target}"
        }
      }
    }

    sh(label: 'Terraform Apply', script: "${cmd} ${config.config_path}")
  }
  catch(Exception error) {
    print 'Failure using terraform apply.'
    throw error
  }
  print 'Terraform apply was successful.'
}

void destroy(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin ?: 'terraform'

  assert config.config_path : '"config_path" is a required parameter for terraform.destroy.'
  assert fileExists(config.config_path) : "Terraform config/plan ${config.config_path} does not exist!"

  // destroy the state
  try {
    String cmd = "${config.bin} destroy -input=false -no-color -auto-approve"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var-file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof Map) : 'The var parameter must be a Map.'

      config.var.each() { var, value ->
        cmd += " -var ${var}=${value}"
      }
    }
    if (config.target) {
      assert (config.target instanceof List) : 'The target parameter must be a list of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
      }
    }

    sh(label: 'Terraform Destroy', script: "${cmd} ${config.config_path}")
  }
  catch(Exception error) {
    print 'Failure using terraform destroy.'
    throw error
  }
  print 'Terraform destroy was successful.'
}

void fmt(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"

  if (config.write && config.check) {
    throw new Exception("The 'write' and 'check' options for terraform.fmt are mutually exclusive - only one can be enabled.")
  }
  config.bin = config.bin ?: 'terraform'

  try {
    String cmd = "${config.bin} fmt -no-color"

    // check for optional inputs
    if (config.recursive == true) {
      cmd += ' -recursive'
    }
    if (config.diff == true) {
      cmd += ' -diff'
    }
    if (config.check == true) {
      cmd += ' -check'
    }
    // incompatible with above
    else if (config.write == true) {
      cmd += ' -write'
    }

    fmt_status = sh(label: 'Terraform Format', returnStatus: true, script: "${cmd} ${config.dir}")

    // report if formatting check detected issues
    if ((config.check == true) && (fmt_status != 0)) {
      print 'Terraform fmt has detected formatting errors.'
    }
  }
  catch(Exception error) {
    print 'Failure using terraform fmt.'
    throw error
  }
  print 'Terraform fmt was successful.'
}

void init(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Working config directory ${config.dir} does not exist!"
  config.bin = config.bin ?: 'terraform'

  // initialize the working config directory
  try {
    String cmd = "${config.bin} init -input=false -no-color"

    // check for optional inputs
    if (config.plugin_dir) {
      if (!fileExists(config.plugin_dir)) {
        new File(config.plugin_dir).mkdir()
      }

      cmd += " -plugin-dir=${config.plugin_dir}"
    }
    if (config.upgrade == true) {
      cmd += ' -upgrade'
    }
    if (config.backend == false) {
      cmd += ' -backend=false'
    }
    if (config.backendConfig) {
      assert (config.backendConfig instanceof List) : 'Parameter backendConfig must be a list of strings.'

      config.backendConfig.each() { backconf ->
        assert fileExists(backconf) : "Backend config file ${backconf} does not exist!"

        cmd += " -backend-config=${backconf}"
      }
    }

    sh(label: 'Terraform Init', script: "${cmd} ${config.dir}")
  }
  catch(Exception error) {
    print 'Failure using terraform init.'
    throw error
  }
  print 'Terraform init was successful.'
}

void imports(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources instanceof List) : 'Parameter resources must be a list of strings.'
  config.bin = config.bin ?: 'terraform'

  // import the resources
  try {
    String cmd = "${config.bin} import -no-color -input=false"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var-file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof Map) : 'The var parameter must be a Map.'

      config.var.each() { var, value ->
        cmd += " -var ${var}=${value}"
      }
    }
    if (config.dir) {
      assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"

      cmd += " -config=${config.dir}"
    }
    if (config.provider) {
      cmd += " -provider=${config.provider}"
    }
    if (config.state) {
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }

    // import each resource
    config.resources.each() { resource ->
      sh(label: 'Terraform Import', script: "${cmd} ${resource}")
    }
  }
  catch(Exception error) {
    print 'Failure using terraform import.'
    throw error
  }
  print 'Terraform imports were successful.'
}

void install(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.install_path = config.install_path ? config.install_path : '/usr/bin'
  assert (config.platform && config.version) : 'A required parameter is missing from the terraform.install method. Please consult the documentation for proper usage.'
  assert fileExists(config.install_path) : "The desired installation path at ${config.install_path} does not exist."

  // check if current version already installed
  if (fileExists("${config.install_path}/terraform")) {
    String installed_version = sh(label: 'Check Terraform Version', returnStdout: true, script: "${config.install_path}/terraform version").trim()
    if (installed_version ==~ config.version) {
      print "Terraform version ${config.version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().downloadFile("https://releases.hashicorp.com/terraform/${config.version}/terraform_${config.version}_${config.platform}.zip", 'terraform.zip')
  unzip(zipFile: 'terraform.zip', dir: config.install_path)
  new utils().removeFile('terraform.zip')
  print "Terraform successfully installed at ${config.install_path}/terraform."
}

def output(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin ?: 'terraform'

  // display outputs from the state
  try {
    String cmd = "${config.bin} output -no-color"

    // check for optional inputs
    if (config.state) {
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }
    if (config.json == true) {
      cmd += " -json"
    }
    if (config.name) {
      cmd += " ${config.name}"
    }

    // capture output(s)
    outputs = sh(label: 'Terraform Output', script: cmd, returnStdout: true)
  }
  catch(Exception error) {
    print 'Failure using terraform output.'
    throw error
  }
  // display output
  if (config.display == true) {
    print 'Terraform outputs are displayed below:'
    print outputs
  }
  // return output
  else {
    print 'Terraform output was successful.'
    return outputs
  }
}

def plan(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.dir : '"dir" is a required parameter for terraform.plan.'
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  config.bin = config.bin ?: 'terraform'

  // generate a plan from the config directory
  try {
    String cmd = "${config.bin} plan -no-color -input=false -out=${config.dir}/plan.tfplan"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var-file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof Map) : 'The var parameter must be a Map.'

      config.var.each() { var, value ->
        cmd += " -var ${var}=${value}"
      }
    }
    if (config.target) {
      assert (config.target instanceof List) : 'The target parameter must be a list of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
      }
    }
    if (config.destroy == true) {
      cmd += ' -destroy'
    }
    if (config.refreshOnly == true) {
      cmd += ' -refresh-only'
    }

    // execute plan
    String plan_output = sh(label: 'Terraform Plan', script: "${cmd} ${config.dir}", returnStdout: true)

    // display plan output if specified
    if (config.display == true) {
      print 'Terraform plan output is:'
      print plan_output
    }
  }
  catch(Exception error) {
    print 'Failure using terraform plan.'
    throw error
  }
  print 'Terraform plan was successful.'

  // return plan output if requested
  if (config.return == true) {
    return plan_output
  }
}

void plugin_install(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.url : "The required parameter 'url' was not set."
  assert config.install_name : "The required parameter 'install_name' was not set."

  config.install_path = config.install_path ? config.install_path : '~/.terraform.d/plugins'

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

  new utils().downloadFile(config.url, install_loc)

  if (config.url ==~ /\.zip$/) {
    unzip(zipFile: install_loc)
    new utils().removeFile(install_loc)
  }
  else {
    sh(label: 'Terraform Plugin Executable Permissions', script: "chmod ug+rx ${install_loc}")
  }
  print "Terraform plugin successfully installed at ${install_loc}."
}

void state(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert (['move', 'remove', 'push', 'list'].contains(config.command)) : "The argument must be one of: move, remove, list, or push."
  config.bin = config.bin ?: 'terraform'
  String cmd = "${config.bin} state"

  // perform state manipulation
  try {
    if (config.state) {
      assert config.command != 'push' : 'The state parameter is incompatible with state pushing.'
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }

    // perform different commands based upon type of state action
    switch (config.command) {
      case 'move':
        assert (config.resources instanceof Map) : 'Parameter resources must be a Map of strings for move command.';

        config.resources.each() { from, to ->
          sh(label: 'Terraform State Move', script: "${cmd} mv ${from} ${to}")
        };
        break;
      case 'remove':
        assert (config.resources instanceof List) : 'Parameter resources must be a list of strings for remove command.';

        config.resources.each() { resource ->
          sh(label: 'Terraform State Remove', script: "${cmd} rm ${resource}")
        };
        break;
      case 'push':
        assert !config.resources : 'Resources parameter is not allowed for push command.';

        sh(label: 'Terraform State Push', script: "${cmd} push");
        break;
      case 'list':
        assert !config.resources : 'Resources parameter is not allowed for push command.';

        String stateList = sh(label: 'Terraform State List', script: "${cmd} list", returnStdout: true)
        print 'Terraform state output is as follows:'
        print stateList

        break;
      default:
        // should never reach this because of above assert
        throw new Exception("Unknown Terraform state command ${config.command} specified.");
    }
  }
  catch(Exception error) {
    print 'Failure using terraform state manipulation.'
    throw error
  }
  print 'Terraform state manipulation was successful.'
}

void taint(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources instanceof List) : 'Parameter resources must be a list of strings.'
  config.bin = config.bin ?: 'terraform'

  // taint the resources
  try {
    String cmd = "${config.bin} taint -no-color"

    if (config.state) {
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }

    // taint each resource
    config.resources.each() { resource ->
      sh(label: 'Terraform Taint', script: "${cmd} ${resource}")
    }
  }
  catch(Exception error) {
    print 'Failure using terraform taint.'
    throw error
  }
  print 'Terraform taints were successful.'
}

void validate(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  config.bin = config.bin ?: 'terraform'

  // validate the config directory
  try {
    String cmd = "${config.bin} validate"

    if (config.check_vars == true) {
      cmd += ' -json'
    }
    else {
      cmd += ' -no-color'
    }

    sh(label: 'Terraform Validate', script: "${cmd} ${config.dir}")
  }
  catch(Exception error) {
    print 'Failure using terraform validate.'
    throw error
  }
  print 'Terraform validate was successful.'
}

void workspace(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert (config.dir && config.workspace) : 'A required parameter is missing from this terraform.workspace block. Please consult the documentation for proper usage.'
  config.bin = config.bin ?: 'terraform'

  assert fileExists(config.dir) : "The config directory ${config.dir} does not exist!"

  dir(config.dir) {
    // select workspace in terraform config directory
    try {
      sh(label: 'Terraform Workspace Select', script: "${config.bin} workspace select ${config.workspace}")
    }
    catch(Exception error) {
      print 'Failure using terraform workspace select. The available workspaces and your current workspace are as follows:'

      String workspaces = sh(label: 'Terraform Workspace List', script: "${config.bin} workspace list", returnStdout: true)
      print workspaces

      throw error
    }
    print "Terraform workspace ${config.workspace} selected successfully."
  }
}
