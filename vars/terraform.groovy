// vars/terraform.groovy
import devops.common.utils
import devops.common.hcl

void apply(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.configPath instanceof String : '"configPath" is a required parameter for terraform.apply.'
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
        // convert value to json if not string type
        if (value instanceof List || value instanceof Map) {
          value = writeJSON(json: value, returnText: true)
        }

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

void destroy(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.bin = config.bin ?: 'terraform'
  assert config.configPath instanceof String : '"configPath" is a required parameter for terraform.destroy.'
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
        // convert value to json if not string type
        if (value instanceof List || value instanceof Map) {
          value = writeJSON(json: value, returnText: true)
        }

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

void fmt(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  if (config.write && config.check) {
    throw new Exception("The 'write' and 'check' options for terraform.fmt are mutually exclusive; only one can be specified.")
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

void graph(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.plan && config.dir) {
    throw new Exception("The 'plan' and 'dir' parameters for terraform.graph are mutually exclusive; only one can be specified.")
  }
  else if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  String cmd = config.bin ?: 'terraform'

  // check for plan versus dir target
  if (config.plan) {
    cmd += " graph -plan=${config.plan}"
  }
  else {
    // cannot cleanly use dir step for this, and also because of later graph file write
    cmd += " -chdir=${config.dir} graph"
  }

  // check for optional inputs
  if (config.type) {
    assert (['plan', 'plan-refresh-only', 'plan-destroy', 'apply'].contains(config.type)) : 'The type parameter must be one of: plan, plan-refresh-only, plan-destroy, or apply.'

    cmd += " -type=${config.type}"
  }
  if (config.drawCycles == true) {
    cmd += ' -draw-cycles'
  }

  try {
    final String dotGraph = sh(label: 'Terraform Graph', script: cmd)
  }
  catch(Exception error) {
    print 'Failure using terraform graph.'
    throw error
  }
  print 'Terraform graph was successful. Writing graph output to "graph.gv" in current working directory.'

  writeFile(file: 'graph.gv', text: dotGraph)
}

void imports(Map config) {
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
      // convert value to json if not string type
      if (value instanceof List || value instanceof Map) {
        value = writeJSON(json: value, returnText: true)
      }

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
      sh(label: "Terraform Import ${name}", script: "${cmd} '${name}' ${id}")
    }
  }
  catch(Exception error) {
    print 'Failure using terraform import.'
    throw error
  }
  print 'Terraform imports were successful.'
}

void init(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
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
  if (config.forceCopy == true) {
    cmd += ' -force-copy'
  }
  if (config.backendConfig) {
    assert (config.backendConfig instanceof List) : 'Parameter backendConfig must be a list of strings.'

    config.backendConfig.each() { backconf ->
      assert fileExists(backconf) : "Backend config file ${backconf} does not exist!"

      cmd += " -backend-config=${backconf}"
    }
  }
  if (config.backendKV) {
    assert (config.backendKV instanceof Map) : 'Parameter backendKV must be a map of strings.'

    config.backendKV.each() { key, value ->
      cmd += " -backend-config='${key}=${value}'"
    }
  }
  if (config.testDir) {
    assert fileExists(config.testDir) : "The test directory ${config.testDir} does not exist."

    cmd += " -test-directory=${config.testDir}"
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

void install(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform instanceof String && config.version instanceof String) : 'A required parameter is missing from the terraform.install method. Please consult the documentation for proper usage.'

  new utils().makeDirParents(config.installPath)

  // check if current version already installed
  if (fileExists("${config.installPath}/terraform")) {
    final String installedVersion = sh(label: 'Check Terraform Version', returnStdout: true, script: "${config.installPath}/terraform version").trim()
    if (installedVersion =~ config.version) {
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

String output(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
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
  if (config.raw == true) {
    cmd += ' -raw'
  }
  // must be last param
  if (config.name) {
    cmd += " ${config.name}"
  }

  // display outputs from the state
  try {
    // capture output(s)
    dir(config.dir) {
      final String outputs = sh(label: 'Terraform Output', script: cmd, returnStdout: true)
    }
  }
  catch(Exception error) {
    print 'Failure using terraform output.'
    throw error
  }

  print 'Terraform output was successful.'
  // display output
  if (config.display == true) {
    print 'Terraform outputs are displayed below:'
    print outputs
  }
  // return output
  return outputs
}

Map parse(String file) {
  // return map of parsed hcl
  return new hcl().hclToMap(file)
}

String plan(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
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
      // convert value to json if not string type
      if (value instanceof List || value instanceof Map) {
        value = writeJSON(json: value, returnText: true)
      }

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
  if (config.compactWarn == true) {
    cmd += ' -compact-warnings'
  }
  if (config.genConfig) {
    assert !fileExists(config.genConfig) : "The path at ${config.genConfig} is required to not exist prior to Terraform config generation, but the path does exist."

    cmd += "-generate-config-out=${config.genConfig}"
  }
  final String out = config.out ?: "${config.dir}/plan.tfplan"

  // generate a plan from the config directory
  try {
    // execute plan
    dir(config.dir) {
      final String planOutput = sh(label: 'Terraform Plan', script: "${cmd} -out=${out}", returnStdout: true)
      print "Plan output artifact written to: ${out}"
    }
  }
  catch(Exception error) {
    print 'Failure using terraform plan.'
    throw error
  }
  print 'Terraform plan was successful.'

  return planOutput
}

void pluginInstall(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.url instanceof String : "The required parameter 'url' was not set."
  assert config.installName instanceof String : "The required parameter 'installName' was not set."

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

void providers(String rootDir = '', String bin = 'terraform') {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (rootDir.length() == 0) {
    rootDir = env.WORKSPACE
  } else {
    assert fileExists(rootDir) : "Config directory ${rootDir} does not exist!"
  }

  // output provider information
  try {
    dir(rootDir) {
      sh(label: 'Terraform Providers Information', script: "${bin} providers")
    }
  }
  catch(Exception error) {
    print 'Failure using terraform providers.'
    throw error
  }
  print 'Terraform providers was successful.'
}

void refresh(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} refresh -no-color -input=false"

  // check for optional inputs
  if (config.varFile) {
    assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

    cmd += " -var-file=${config.varFile}"
  }
  if (config.var) {
    assert (config.var instanceof Map) : 'The var parameter must be a Map.'

    config.var.each() { var, value ->
      // convert value to json if not string type
      if (value instanceof List || value instanceof Map) {
        value = writeJSON(json: value, returnText: true)
      }

      cmd += " -var ${var}=${value}"
    }
  }
  if (config.target) {
    assert (config.target instanceof List) : 'The target parameter must be a list of strings.'

    config.target.each() { target ->
      cmd += " -target=${target}"
    }
  }
  if (config.compactWarn == true) {
    cmd += ' -compact-warnings'
  }

  // refresh the state
  try {
    dir(config.dir) {
      sh(label: 'Terraform Refresh', script: cmd)
    }
  }
  catch(Exception error) {
    print 'Failure using terraform refresh.'
    throw error
  }
  print 'Terraform refresh was successful.'
}

void state(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert (['move', 'remove', 'push', 'list', 'show', 'pull'].contains(config.command)) : "The command parameter must be one of: move, remove, list, show, pull, or push."
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'
  String cmd = "${config.bin} state"

  // optional inputs
  if (config.state) {
    assert config.command != 'push' && config.command != 'pull' : 'The state parameter is incompatible with state pushing and pulling.'
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd += " -state=${config.state}"
  }

  // perform state manipulation
  try {
    // perform different commands based upon type of state action
    switch (config.command) {
      case 'move':
        assert (config.resources instanceof Map) : 'Parameter resources must be a Map of strings for move command.';

        dir(config.dir) {
          config.resources.each() { from, to ->
            sh(label: "Terraform State Move ${from} to ${to}", script: "${cmd} mv ${from} ${to}")
          }
        }

        break;
      case 'remove':
        assert (config.resources instanceof List) : 'Parameter resources must be a list of strings for remove command.';

        dir(config.dir) {
          config.resources.each() { resource ->
            sh(label: "Terraform State Remove ${resource}", script: "${cmd} rm ${resource}")
          }
        }

        break;
      case 'push':
        assert !config.resources : 'Resources parameter is not allowed for push command.';

        dir(config.dir) {
          sh(label: 'Terraform State Push', script: "${cmd} push");
        }

        break;
      case 'list':
        assert !config.resources : 'Resources parameter is not allowed for push command.';

        dir(config.dir) {
          final String stateList = sh(label: 'Terraform State List', script: "${cmd} list", returnStdout: true)
        }

        print 'Terraform state output is as follows:'
        print stateList

        break;
      case 'show':
        assert (config.resources instanceof List) : 'Parameter resources must be a list of strings for show command.';

        dir(config.dir) {
          config.resources.each() { resource ->
            String stateShow = sh(label: "Terraform State Show ${resource}", script: "${cmd} show ${resource}", returnStdout: true)

            print 'Terraform state output is as follows:'
            print stateShow
          }
        }

        break;
      case 'pull':
          assert !config.resources : 'Resources parameter is not allowed for pull command.';

          dir(config.dir) {
            sh(label: 'Terraform State Pull', script: "${cmd} pull");
          }

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

void taint(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources instanceof List) : 'Parameter resources must be a list of strings.'
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} taint -no-color"

  // optional inputs
  if (config.state) {
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd += " -state=${config.state}"
  }
  if (config.allowMissing == true) {
    cmd += ' -allow-missing'
  }

  // taint the resources
  try {
    // taint each resource
    dir(config.dir) {
      config.resources.each() { resource ->
        sh(label: "Terraform Taint ${resource}", script: "${cmd} ${resource}")
      }
    }
  }
  catch(Exception error) {
    print 'Failure using terraform taint.'
    throw error
  }
  print 'Terraform taints were successful.'
}

String test(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} test -no-color"

  // optional inputs
  if (config.cloudRun) {
    cmd += " -cloud-run=${config.cloudRun}"
  }
  if (config.filter) {
    assert (config.filter instanceof List) : 'The filter parameter must be a list of strings.'

    config.filter.each() { filter ->
      cmd += " -filter=${filter}"
    }
  }
  if (config.json == true) {
    cmd += ' -json'
  }
  if (config.testDir) {
    assert fileExists(config.testDir) : "The test directory ${config.testDir} does not exist."

    cmd += " -test-directory=${config.testDir}"
  }
  if (config.varFile) {
    assert fileExists(config.varFile) : "The var file ${config.varFile} does not exist!"

    cmd += " -var-file=${config.varFile}"
  }
  if (config.var) {
    assert (config.var instanceof Map) : 'The var parameter must be a Map.'

    config.var.each() { var, value ->
      // convert value to json if not string type
      if (value instanceof List || value instanceof Map) {
        value = writeJSON(json: value, returnText: true)
      }

      cmd += " -var ${var}=${value}"
    }
  }
  if (config.verbose == true) {
    cmd += ' -verbose'
  }

  // execute tests
  try {
    dir(config.dir) {
      final String testOutput = sh(label: 'Terraform Test', script: cmd, returnStdout: true)
    }
  }
  catch(Exception error) {
    print 'Failure using terraform test.'
    throw error
  }
  print 'Terraform test was successful.'

  return testOutput
}

String validate(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} validate -no-color"

  // optional inputs
  if (config.json == true) {
    cmd += ' -json'
  }
  if (config.tests == false) {
    cmd += ' -no-tests'
  }
  else if (config.testDir) {
    assert fileExists(config.testDir) : "The test directory ${config.testDir} does not exist."

    cmd += " -test-directory=${config.testDir}"
  }

  // validate the config directory
  try {
    dir(config.dir) {
      final String validateOutput = sh(label: 'Terraform Validate', script: cmd, returnStdout: true)
    }
  }
  catch(Exception error) {
    print 'Failure using terraform validate.'
    throw error
  }
  print 'Terraform validate was successful.'

  return validateOutput
}

void workspace(Map config) {
  // set terraform env for automation
  env.TF_IN_AUTOMATION = true

  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  assert config.workspace instanceof String : 'The "workspace" parameter must be specified for the "workspace" method.'
  config.bin = config.bin ?: 'terraform'

  String cmd = "${config.bin} workspace select"

  // optional inputs
  if (config.create == true) {
    cmd += ' -or-create'
  }

  dir(config.dir) {
    // select workspace in terraform config directory
    try {
      sh(label: 'Terraform Workspace Select', script: "${config.cmd} ${config.workspace}")
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
