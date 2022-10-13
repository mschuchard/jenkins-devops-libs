// vars/terraform.groovy
import devops.common.utils

void apply(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.configPath : '"configPath" is a required parameter for terraform.apply.'
  assert fileExists(config.configPath) : "Terraform config/plan ${config.configPath} does not exist!"
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} apply -input=false -no-color -auto-approve"

  // check if a directory was passed for the config path
  if (!(config.configPath ==~ /\.tfplan$/)) {
    // check for optional var inputs
    if (config.varFile) {
      assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

      cmd += " -var-file=${config.varFile}"
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

  // apply the config
  try {
    if (config.configPath ==~ /\.tfplan$/) {
      sh(label: 'Terraform Apply', script: "${cmd} ${config.configPath}")
    }
    else {
      dir(config.configPath) {
        sh(label: 'Terraform Apply', script: cmd)
      }
    }
  }
  catch(Exception error) {
    print 'Failure using terraform apply.'
    throw error
  }
  print 'Terraform apply was successful.'
}

void destroy(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin ?: 'terraform'
  assert config.configPath : '"configPath" is a required parameter for terraform.destroy.'
  assert fileExists(config.configPath) : "Terraform config/plan ${config.configPath} does not exist!"

  String cmd = "${config.bin} destroy -input=false -no-color -auto-approve"

  // check if a directory was passed for the config path
  if (!(config.configPath ==~ /\.tfplan$/)) {
    // check for optional var inputs
    if (config.varFile) {
      assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

      cmd += " -var-file=${config.varFile}"
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

  // destroy the state
  try {
    if (config.configPath ==~ /\.tfplan$/) {
      sh(label: 'Terraform Destroy', script: "${cmd} ${config.configPath}")
    }
    else {
      dir(config.configPath) {
        sh(label: 'Terraform Destroy', script: cmd)
      }
    }
  }
  catch(Exception error) {
    print 'Failure using terraform destroy.'
    throw error
  }
  print 'Terraform destroy was successful.'
}

void fmt(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  if (config.write && config.check) {
    throw new Exception("The 'write' and 'check' options for terraform.fmt are mutually exclusive - only one can be enabled.")
  }
  config.bin = config.bin ?: 'terraform'

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

  try {
    dir(config.configDir) {
      final int fmtStatus = sh(label: 'Terraform Format', returnStatus: true, script: cmd)
    }

    // report if formatting check detected issues
    if ((config.check == true) && (fmtStatus != 0)) {
      print 'Terraform fmt has detected formatting errors.'
    }
  }
  catch(Exception error) {
    print 'Failure using terraform fmt.'
    throw error
  }
  print 'Terraform fmt was successful.'
}

void imports(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources instanceof List) : 'Parameter resources must be a list of strings.'
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} import -no-color -input=false"

  // check for optional inputs
  if (config.varFile) {
    assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

    cmd += " -var-file=${config.varFile}"
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

  // import the resources
  try {
    // import each resource
    config.resources.each() { name, id ->
      sh(label: "Terraform Import ${name}", script: "${cmd} ${name} ${id}")
    }
  }
  catch(Exception error) {
    print 'Failure using terraform import.'
    throw error
  }
  print 'Terraform imports were successful.'
}

void init(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Working config directory ${config.dir} does not exist!"
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} init -input=false -no-color"

  // check for optional inputs
  if (config.pluginDir) {
    new utils().makeDirParents(config.pluginDir)

    cmd += " -plugin-dir=${config.pluginDir}"
  }
  if (config.upgrade == true) {
    cmd += ' -upgrade'
  }
  if (config.backend == false) {
    cmd += ' -backend=false'
  }
  if (config.migrateState == true) {
    cmd += ' -migrate-state'
  }
  if (config.backendConfig) {
    assert (config.backendConfig instanceof List) : 'Parameter backendConfig must be a list of strings.'

    config.backendConfig.each() { backconf ->
      assert fileExists(backconf) : "Backend config file ${backconf} does not exist!"

      cmd += " -backend-config=${backconf}"
    }
  }

  // initialize the working config directory
  try {
    dir(config.configDir) {
      sh(label: 'Terraform Init', script: cmd)
    }
  }
  catch(Exception error) {
    print 'Failure using terraform init.'
    throw error
  }
  print 'Terraform init was successful.'
}

void install(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform && config.version) : 'A required parameter is missing from the terraform.install method. Please consult the documentation for proper usage.'

  new utils().makeDirParents(config.installPath)

  // check if current version already installed
  if (fileExists("${config.installPath}/terraform")) {
    final String installedVersion = sh(label: 'Check Terraform Version', returnStdout: true, script: "${config.installPath}/terraform version").trim()
    if (installedVersion ==~ config.version) {
      print "Terraform version ${config.version} already installed at ${config.installPath}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().downloadFile("https://releases.hashicorp.com/terraform/${config.version}/terraform_${config.version}_${config.platform}.zip", 'terraform.zip')
  unzip(zipFile: 'terraform.zip', dir: config.installPath)
  new utils().removeFile('terraform.zip')
  print "Terraform successfully installed at ${config.installPath}/terraform."
}

def output(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} output -no-color"

  // check for optional inputs
  if (config.state) {
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd += " -state=${config.state}"
  }
  if (config.json == true) {
    cmd += ' -json'
  }
  if (config.name) {
    cmd += " ${config.name}"
  }

  // display outputs from the state
  try {
    // capture output(s)
    final String outputs = sh(label: 'Terraform Output', script: cmd, returnStdout: true)
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

def plan(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.dir : '"dir" is a required parameter for terraform.plan.'
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} plan -no-color -input=false"

  // check for optional inputs
  if (config.varFile) {
    assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

    cmd += " -var-file=${config.varFile}"
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
  if (config.replace) {
    assert (config.replace instanceof List) : 'The replace parameter must be a list of strings.'

    config.replace.each() { resource ->
      cmd += " -replace=${resource}"
    }
  }
  if (config.destroy == true) {
    cmd += ' -destroy'
  }
  if (config.refreshOnly == true) {
    cmd += ' -refresh-only'
  }
  String out = config.out ?: "${config.dir}/plan.tfplan"

  // generate a plan from the config directory
  try {
    // execute plan
    dir(config.dir) {
      final String planOutput = sh(label: 'Terraform Plan', script: "${cmd} -out=${out}", returnStdout: config.return)
    }

    // display plan output if specified
    if (config.display == true) {
      print 'Terraform plan output is:'
      print planOutput
    }
  }
  catch(Exception error) {
    print 'Failure using terraform plan.'
    throw error
  }
  print 'Terraform plan was successful.'

  // return plan output if requested
  if (config.return == true) {
    return planOutput
  }
}

void pluginInstall(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.url : "The required parameter 'url' was not set."
  assert config.installName : "The required parameter 'installName' was not set."

  config.installPath = config.installPath ? config.installPath : '~/.terraform/plugins'

  // set and assign plugin install location
  String installLoc = "${config.installPath}/${config.installName}"

  // check if plugin dir exists and create if not
  new utils().makeDirParents(config.installPath)

  // check if plugin already installed
  if (fileExists(installLoc)) {
    print "Terraform plugin already installed at ${installLoc}."
    return
  }
  // otherwise download and install plugin
  else if (config.url ==~ /\.zip$/) {
    // append zip extension to avoid filename clashes
    installLoc = "${installLoc}.zip"
  }

  new utils().downloadFile(config.url, installLoc)

  if (config.url ==~ /\.zip$/) {
    unzip(zipFile: installLoc)
    new utils().removeFile(installLoc)
  }
  else {
    sh(label: 'Terraform Plugin Executable Permissions', script: "chmod ug+rx ${installLoc}")
  }
  print "Terraform plugin successfully installed at ${installLoc}."
}

void state(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert (['move', 'remove', 'push', 'list'].contains(config.command)) : "The command parameter must be one of: move, remove, list, or push."
  config.bin = config.bin ?: 'terraform'
  String cmd = "${config.bin} state"

  // optional inputs
  if (config.state) {
    assert config.command != 'push' : 'The state parameter is incompatible with state pushing.'
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd += " -state=${config.state}"
  }

  // perform state manipulation
  try {
    // perform different commands based upon type of state action
    switch (config.command) {
      case 'move':
        assert (config.resources instanceof Map) : 'Parameter resources must be a Map of strings for move command.';

        config.resources.each() { from, to ->
          sh(label: "Terraform State Move ${from} to ${to}", script: "${cmd} mv ${from} ${to}")
        };
        break;
      case 'remove':
        assert (config.resources instanceof List) : 'Parameter resources must be a list of strings for remove command.';

        config.resources.each() { resource ->
          sh(label: "Terraform State Remove ${resource}", script: "${cmd} rm ${resource}")
        };
        break;
      case 'push':
        assert !config.resources : 'Resources parameter is not allowed for push command.';

        sh(label: 'Terraform State Push', script: "${cmd} push");
        break;
      case 'list':
        assert !config.resources : 'Resources parameter is not allowed for push command.';

        final String stateList = sh(label: 'Terraform State List', script: "${cmd} list", returnStdout: true)
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

void taint(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources instanceof List) : 'Parameter resources must be a list of strings.'
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} taint -no-color"

  // optional inputs
  if (config.state) {
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd += " -state=${config.state}"
  }

  // taint the resources
  try {
    // taint each resource
    config.resources.each() { resource ->
      sh(label: "Terraform Taint ${resource}", script: "${cmd} ${resource}")
    }
  }
  catch(Exception error) {
    print 'Failure using terraform taint.'
    throw error
  }
  print 'Terraform taints were successful.'
}

def validate(config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} validate -no-color"

  // optional inputs
  if (config.json == true) {
    cmd += ' -json'
  }

  // validate the config directory
  try {
    dir(config.dir) {
      final String validateOutput = sh(label: 'Terraform Validate', script: cmd, returnStdout: config.return)
    }
  }
  catch(Exception error) {
    print 'Failure using terraform validate.'
    throw error
  }
  print 'Terraform validate was successful.'

  // return validate output if requested
  if (config.return == true) {
    return validateOutput
  }
}

void workspace(config) {
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

      final String workspaces = sh(label: 'Terraform Workspace List', script: "${config.bin} workspace list", returnStdout: true)
      print workspaces

      throw error
    }
    print "Terraform workspace ${config.workspace} selected successfully."
  }
}
