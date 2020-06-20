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
  config.bin = config.bin ? config.bin : 'terraform'

  // apply the config
  try {
    String cmd = "${config.bin} apply -input=false -no-color -auto-approve"

    // check if a directory was passed for the config path
    if (!(config.config_path ==~ /plan\.tfplan/)) {
      // check for optional var inputs
      if (config.var_file) {
        assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

        cmd += " -var_file=${config.var_file}"
      }
      if (config.var) {
        assert (config.var instanceof List) : 'The var parameter must be an array of strings.'

        config.var.each() { var ->
          cmd += " -var ${var}"
        }
      }
    }
    // check for optional targets input
    if (config.target) {
      assert (config.target instanceof List) : 'The target parameter must be an array of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
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
  config.bin = config.bin ? config.bin : 'terraform'

  // -force changed to -auto-approve in 0.11.4
  String no_input_check = sh(label: 'Check Terraform Usage', returnStdout: true, script: "${config.bin} destroy --help")
  // apply correct flag based on installed version
  String no_input_flag = ''
  if (no_input_check ==~ /-auto-approve/) {
    no_input_flag = '-auto-approve'
  }
  else {
    no_input_flag = '-force'
  }

  assert config.config_path : '"config_path" is a required parameter for terraform.destroy.'
  assert fileExists(config.config_path) : "Terraform config/plan ${config.config_path} does not exist!"

  // destroy the state
  try {
    String cmd = "${config.bin} destroy -input=false -no-color ${no_input_flag}"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof List) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }
    if (config.target) {
      assert (config.target instanceof List) : 'The target parameter must be an array of strings.'

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
  config.bin = config.bin ? config.bin : 'terraform'

  try {
    String cmd = "${config.bin} fmt -no-color"

    // check for terraform >= 0.12 (those versions have flag for recursive processing)
    String new_fmt = sh(label: 'Check Terraform Usage', returnStdout: true, script: "${config.bin} fmt --help") ==~ /-recursive/

    // check for optional inputs
    if ((new_fmt) && (config.recursive == true)) {
      cmd += " -recursive"
    }
    if (config.diff == true) {
      cmd += " -diff"
    }
    if (config.check == true) {
      cmd += " -check"
    }
    if (config.write == true) {
      cmd += " -write"
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
  config.bin = config.bin ? config.bin : 'terraform'

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
  assert (config.resources instanceof List) : 'Parameter resources must be an array of strings.'
  config.bin = config.bin ? config.bin : 'terraform'

  // import the resources
  try {
    String cmd = "${config.bin} import -no-color -input=false"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof List) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
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

def plan(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.dir : '"dir" is a required parameter for terraform.plan.'
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  config.bin = config.bin ? config.bin : 'terraform'

  // generate a plan from the config directory
  try {
    String cmd = "${config.bin} plan -no-color -input=false -out=${config.dir}/plan.tfplan"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof List) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }
    if (config.target) {
      assert (config.target instanceof List) : 'The target parameter must be an array of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
      }
    }
    if (config.destroy == true) {
      cmd += ' -destroy'
    }
    if (config.target) {
      assert (config.target instanceof List) : 'The target parameter must be an array of strings.'

      config.target.each() { target ->
        cmd += " -target=${target}"
      }
    }

    plan_output = sh(label: 'Terraform Plan', script: "${cmd} ${config.dir}", returnStdout: true)

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
    sh(label: 'Terraform CLI Executable Permissions', script: "chmod ug+rx ${install_loc}")
  }
  print "Terraform plugin successfully installed at ${install_loc}."
}

void state(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin ? config.bin : 'terraform'
  String cmd = config.bin

  // perform state manipulation
  try {
    if (config.state) {
      assert config.cmd != 'push' : 'The state parameter is incompatible with state pushing.'
      assert fileExists(config.state) : "The state file at ${config.state} does not exist."

      cmd += " -state=${config.state}"
    }

    // perform different commands based upon type of state action
    switch (config.cmd) {
      case 'move':
        assert (config.resources[0] instanceof List) : 'Parameter resources must be a nested array of strings for move command.';

        config.resources.each() { resource_pair ->
          sh(label: 'Terraform State Move', script: "${cmd} mv ${resource_pair}[0] ${resource_pair}[1]")
        };
        break;
      case 'remove':
        assert (config.resources instanceof List) : 'Parameter resources must be an array of strings for remove command.';

        config.resources.each() { resource ->
          sh(label: 'Terraform State Remove', script: "${cmd} rm ${resource}")
        };
        break;
      case 'push':
        assert !config.resources : 'Resources parameter is not allowed for push command.';

        sh(label: 'Terraform State Push', script: "${cmd} push");
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

void taint(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources instanceof List) : 'Parameter resources must be an array of strings.'
  config.bin = config.bin ? config.bin : 'terraform'

  // taint the resources
  try {
    String cmd = "${config.bin} taint -no-color"

    // check for terraform >= 0.12 (taint usage changed)
    String new_taint = sh(label: 'Check Terraform Usage', returnStdout: true, script: "${config.bin} validate --help") ==~ /-json/

    // check for optional inputs
    if (config.module && !(new_taint)) {
      cmd += " -module=${config.module}"
    }
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
  config.bin = config.bin ? config.bin : 'terraform'

  // validate the config directory
  try {
    String cmd = "${config.bin} validate -no-color"

    // check for terraform >= 0.12 (those versions have flag for json output)
    String new_validate = sh(label: 'Check Terraform Usage', returnStdout: true, script: "${config.bin} validate --help") ==~ /-json/

    if (!(new_validate)) {
      // check for optional inputs
      if (config.var_file) {
        assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

        cmd += " -var_file=${config.var_file}"
      }
      if (config.var) {
        assert (config.var instanceof List) : 'The var parameter must be an array of strings.'

        config.var.each() { var ->
          cmd += " -var ${var}"
        }
      }
      if (config.check_vars == false) {
        cmd += ' -check-variables=false'
      }
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
  config.bin = config.bin ? config.bin : 'terraform'

  assert fileExists(config.dir) : "The config directory ${config.dir} does not exist!"

  dir(config.dir) {
    // select workspace in terraform config directory
    try {
      sh(label: 'Terraform Workspace', script: "${config.bin} workspace select -no-color ${config.workspace}")
    }
    catch(Exception error) {
      print 'Failure using terraform workspace select.'
      throw error
    }
    print "Terraform workspace ${config.workspace} selected successfully."
  }
}
