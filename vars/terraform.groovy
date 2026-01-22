// vars/terraform.groovy
import devops.common.utils
import devops.common.helpers
import devops.common.hcl

private void execute(Map config) {
  // input checking
  assert config.configPath in String : "'configPath' is a required parameter for terraform.${config.action}."
  assert fileExists(config.configPath) : "Terraform config/plan ${config.configPath} does not exist!"
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'apply', '-input=false', '-no-color', '-auto-approve']

  // check if a directory was passed for the config path
  if (!(config.configPath ==~ /\.tfplan$/)) {
    cmd.addAll(new helpers().varSubCmd(config))

    if (config.target) {
      assert (config.target in List) : 'The target parameter must be a list of strings.'

      config.target.each { String target ->
        cmd.add("-target=${target}")
      }
    }
  }
  if (config.compactWarn == true) {
    cmd.add('-compact-warnings')
  }
  if (config.action == 'destroy') {
    cmd.add('-destroy')
  }

  // apply the config
  if (config.configPath ==~ /\.tfplan$/) {
    cmd.add(config.configPath)

    withEnv(['TF_IN_AUTOMATION=true']) {
      new helpers().toolExec("Terraform Apply ${config.configPath}", cmd)
    }
  }
  else {
    dir(config.configPath) {
      withEnv(['TF_IN_AUTOMATION=true']) {
        new helpers().toolExec("Terraform Apply ${config.configPath}", cmd)
      }
    }
  }
}

void apply(Map config) {
  // invoke helper method with apply
  config.action = 'apply'
  execute(config)
}

void destroy(Map config) {
  // invoke helper method with destroy
  config.action = 'destroy'
  execute(config)
}

Boolean fmt(Map config) {
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  if (config.write && config.check) {
    error(message: "The 'write' and 'check' options for terraform.fmt are mutually exclusive; only one can be specified.")
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'fmt', '-no-color']

  // check for optional inputs
  if (config.recursive == true) {
    cmd.add('-recursive')
  }
  if (config.diff == true) {
    cmd.add('-diff')
  }
  if (config.check == true) {
    cmd.add('-check')
  }
  // incompatible with above
  else if (config.write == true) {
    cmd.add('-write')
  }

  // canonically format the code
  int fmtStatus
  dir(config.dir) {
    withEnv(['TF_IN_AUTOMATION=true']) {
      fmtStatus = sh(label: 'Terraform Format', returnStatus: true, script: cmd.join(' '))
    }
  }

  // report if formatting check detected issues
  if (fmtStatus != 0) {
    // the format check failed
    if (config.check == true) {
      print 'Terraform fmt has detected formatting errors.'
      return false
    }

    // the format command failed unexpectedly
    print 'Failure using terraform fmt.'
    error(message: 'terraform fmt failed unexpectedly; check logs for details')
  }

  print 'Terraform fmt was successful.'
  return true
}

void graph(Map config) {
  // input checking
  if (config.plan && config.dir) {
    error(message: "The 'plan' and 'dir' parameters for terraform.graph are mutually exclusive; only one can be specified.")
  }
  else if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  List<String> cmd = [config.bin ?: 'terraform']

  // check for plan versus dir target
  if (config.plan) {
    cmd.add("graph -plan=${config.plan}")
  }
  else {
    // cannot cleanly use dir step for this, and also because of later graph file write
    cmd.addAll(["-chdir=${config.dir}", 'graph'])
  }

  // check for optional inputs
  if (config.type) {
    assert (['plan', 'plan-refresh-only', 'plan-destroy', 'apply'].contains(config.type)) : 'The type parameter must be one of: plan, plan-refresh-only, plan-destroy, or apply.'

    cmd.add("-type=${config.type}")
  }
  if (config.drawCycles == true) {
    cmd.add('-draw-cycles')
  }

  String dotGraph
  try {
    withEnv(['TF_IN_AUTOMATION=true']) {
      dotGraph = sh(label: 'Terraform Graph', script: cmd.join(' '), returnStdout: true)
    }
  }
  catch (hudson.AbortException error) {
    print 'Failure using terraform graph.'
    throw error
  }
  print 'Terraform graph was successful. Writing graph output to "graph.gv" in current working directory.'

  writeFile(file: 'graph.gv', text: dotGraph)
}

void imports(Map config) {
  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources in Map) : 'Parameter resources must be a map of strings.'
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'import', '-no-color', '-input=false']

  // check for optional inputs
  cmd.addAll(new helpers().varSubCmd(config))

  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"

    cmd.add("-config=${config.dir}")
  }
  if (config.provider) {
    cmd.add("-provider=${config.provider}")
  }
  if (config.state) {
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd.add("-state=${config.state}")
  }

  // import each resource
  withEnv(['TF_IN_AUTOMATION=true']) {
    config.resources.each { String name, String id ->
      List<String> resourceCmd = cmd + ["'${name}'", id]
      new helpers().toolExec("Terraform Import ${name}", resourceCmd)
    }
  }
}

void init(Map config) {
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'init', '-input=false', '-no-color']

  // check for optional inputs
  if (config.pluginDir) {
    new utils().makeDirParents(config.pluginDir)

    cmd.add("-plugin-dir=${config.pluginDir}")
  }
  if (config.upgrade == true) {
    cmd.add('-upgrade')
  }
  if (config.backend == false) {
    cmd.add('-backend=false')
  }
  if (config.migrateState == true) {
    cmd.add('-migrate-state')
  }
  if (config.forceCopy == true) {
    cmd.add('-force-copy')
  }
  if (config.backendConfig) {
    assert (config.backendConfig in List) : 'Parameter backendConfig must be a list of strings.'

    config.backendConfig.each { String backconf ->
      assert fileExists(backconf) : "Backend config file ${backconf} does not exist!"

      cmd.add("-backend-config=${backconf}")
    }
  }
  if (config.backendKV) {
    assert (config.backendKV in Map) : 'Parameter backendKV must be a map of strings.'

    config.backendKV.each { String key, String value ->
      cmd.add("-backend-config='${key}=${value}'")
    }
  }
  if (config.testDir) {
    assert fileExists(config.testDir) : "The test directory ${config.testDir} does not exist."

    cmd.add("-test-directory=${config.testDir}")
  }

  // initialize the working config directory
  dir(config.dir) {
    withEnv(['TF_IN_AUTOMATION=true']) {
      new helpers().toolExec("Terraform Init ${config.dir}", cmd)
    }
  }
}

void install(Map config) {
  // input checking
  config.installPath = config.installPath ? config.installPath : '/usr/bin'
  assert (config.platform in String && config.version in String) : 'A required parameter is missing from the terraform.install method. Please consult the documentation for proper usage.'

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
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'output', '-no-color']

  // check for optional inputs
  if (config.state) {
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd.add("-state=${config.state}")
  }
  if (config.json == true) {
    cmd.add('-json')
  }
  if (config.raw == true) {
    cmd.add('-raw')
  }
  // must be last param
  if (config.name) {
    cmd.add(config.name)
  }

  // display outputs from the state
  String outputs
  try {
    // capture output(s)
    dir(config.dir) {
      withEnv(['TF_IN_AUTOMATION=true']) {
        outputs = sh(label: 'Terraform Output', script: cmd.join(' '), returnStdout: true)
      }
    }
  }
  catch (hudson.AbortException error) {
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
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'plan', '-no-color', '-input=false']

  // check for optional inputs
  cmd.addAll(new helpers().varSubCmd(config))

  if (config.target) {
    assert (config.target in List) : 'The target parameter must be a list of strings.'

    config.target.each { String target ->
      cmd.add("-target=${target}")
    }
  }
  if (config.replace) {
    assert (config.replace in List) : 'The replace parameter must be a list of strings.'

    config.replace.each { String resource ->
      cmd.add("-replace=${resource}")
    }
  }
  if (config.destroy == true) {
    cmd.add('-destroy')
  }
  if (config.refreshOnly == true) {
    cmd.add('-refresh-only')
  }
  if (config.compactWarn == true) {
    cmd.add('-compact-warnings')
  }
  if (config.genConfig) {
    assert !fileExists(config.genConfig) : "The path at ${config.genConfig} is required to not exist prior to Terraform config generation, but the path does exist."

    cmd.add("-generate-config-out=${config.genConfig}")
  }
  final String out = config.out ?: "${config.dir}/plan.tfplan"

  // generate a plan from the config directory
  String planOutput
  try {
    // execute plan
    dir(config.dir) {
      cmd.add("-out=${out}")
      withEnv(['TF_IN_AUTOMATION=true']) {
        planOutput = sh(label: 'Terraform Plan', script: cmd.join(' '), returnStdout: true)
      }
      print "Plan output artifact written to: ${out}"
    }
  }
  catch (hudson.AbortException error) {
    print 'Failure using terraform plan.'
    throw error
  }
  print 'Terraform plan was successful.'

  return planOutput
}

void pluginInstall(Map config) {
  // input checking
  assert config.url in String : "The required parameter 'url' was not set."
  assert config.installName in String : "The required parameter 'installName' was not set."

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
  // input checking
  if (rootDir.length() == 0) {
    rootDir = env.WORKSPACE
  } else {
    assert fileExists(rootDir) : "Config directory ${rootDir} does not exist!"
  }

  // output provider information
  dir(rootDir) {
    withEnv(['TF_IN_AUTOMATION=true']) {
      new helpers().toolExec('Terraform Providers Information', [bin, 'providers'])
    }
  }
}

void refresh(Map config) {
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'refresh', '-no-color', '-input=false']

  // check for optional inputs
  cmd.addAll(new helpers().varSubCmd(config))

  if (config.target) {
    assert (config.target in List) : 'The target parameter must be a list of strings.'

    config.target.each { String target ->
      cmd.add("-target=${target}")
    }
  }
  if (config.compactWarn == true) {
    cmd.add('-compact-warnings')
  }

  // refresh the state
  dir(config.dir) {
    withEnv(['TF_IN_AUTOMATION=true']) {
      new helpers().toolExec("Terraform Refresh ${config.dir}", cmd)
    }
  }
}

void state(Map config) {
  // input checking
  assert (['move', 'remove', 'push', 'list', 'show', 'pull'].contains(config.command)) : 'The command parameter must be one of: move, remove, list, show, pull, or push.'
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'
  List<String> cmd = [config.bin, 'state']

  // optional inputs
  if (config.state) {
    assert config.command != 'push' && config.command != 'pull' : 'The state parameter is incompatible with state pushing and pulling.'
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd.add("-state=${config.state}")
  }

  // perform state manipulation
  dir(config.dir) {
    // perform different commands based upon type of state action
    switch (config.command) {
      case 'move':
        assert (config.resources in Map) : 'Parameter resources must be a Map of strings for move command.'

        config.resources.each { String from, String to ->
          List<String> moveCmd = cmd + ['mv', from, to]
          withEnv(['TF_IN_AUTOMATION=true']) {
            new helpers().toolExec("Terraform State Move ${from} to ${to}", moveCmd)
          }
        }
        break
      case 'remove':
        assert (config.resources in List) : 'Parameter resources must be a list of strings for remove command.'

        config.resources.each { String resource ->
          List<String> removeCmd = cmd + ['rm', resource]
          withEnv(['TF_IN_AUTOMATION=true']) {
            new helpers().toolExec("Terraform State Remove ${resource}", removeCmd)
          }
        }
        break
      case 'push':
        assert !config.resources : 'Resources parameter is not allowed for list command.'
        withEnv(['TF_IN_AUTOMATION=true']) {
          new helpers().toolExec("Terraform State Push ${config.dir}", cmd + ['push'])
        }
        break
      case 'list':
        assert !config.resources : 'Resources parameter is not allowed for push command.'

        try {
          withEnv(['TF_IN_AUTOMATION=true']) {
            String stateList = sh(label: 'Terraform State List', script: (cmd + ['list']).join(' '), returnStdout: true)
          }
          print 'Terraform state output is as follows:'
          print stateList
        }
        catch (hudson.AbortException error) {
          print 'Failure using terraform state manipulation.'
          throw error
        }
        break
      case 'show':
        assert (config.resources in List) : 'Parameter resources must be a list of strings for show command.'

        config.resources.each { String resource ->
          try {
            List<String> showCmd = cmd + ['show', resource]
            String stateShow = sh(label: "Terraform State Show ${resource}", script: showCmd.join(' '), returnStdout: true)
            print 'Terraform state output is as follows:'
            print stateShow
          }
          catch (hudson.AbortException error) {
            print 'Failure using terraform state manipulation.'
            throw error
          }
        }
        break
      case 'pull':
        assert !config.resources : 'Resources parameter is not allowed for pull command.'
        new helpers().toolExec("Terraform State Pull ${config.dir}", cmd + ['pull'])
        break
      default:
        // should never reach this because of above assert
        error(message: "Unknown Terraform state command ${config.command} specified.")
    }
  }
  print 'Terraform state manipulation was successful.'
}

void taint(Map config) {
  // input checking
  assert config.resources : 'Parameter resources must be specified.'
  assert (config.resources in List) : 'Parameter resources must be a list of strings.'
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'taint', '-no-color']

  // optional inputs
  if (config.state) {
    assert fileExists(config.state) : "The state file at ${config.state} does not exist."

    cmd.add("-state=${config.state}")
  }
  if (config.allowMissing == true) {
    cmd.add('-allow-missing')
  }

  // taint each resource
  dir(config.dir) {
    config.resources.each { String resource ->
      List<String> taintCmd = cmd + [resource]
      withEnv(['TF_IN_AUTOMATION=true']) {
        new helpers().toolExec("Terraform Taint ${resource}", taintCmd)
      }
    }
  }
}

String test(Map config) {
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'test', '-no-color']

  // optional inputs
  if (config.cloudRun) {
    cmd.add("-cloud-run=${config.cloudRun}")
  }
  if (config.filter) {
    assert (config.filter in List) : 'The filter parameter must be a list of strings.'

    config.filter.each { String filter ->
      cmd.add("-filter=${filter}")
    }
  }
  if (config.json == true) {
    cmd.add('-json')
  }
  if (config.testDir) {
    assert fileExists(config.testDir) : "The test directory ${config.testDir} does not exist."

    cmd.add("-test-directory=${config.testDir}")
  }
  cmd.addAll(new helpers().varSubCmd(config))

  if (config.verbose == true) {
    cmd.add('-verbose')
  }

  // execute tests
  String testOutput
  try {
    dir(config.dir) {
      withEnv(['TF_IN_AUTOMATION=true']) {
        testOutput = sh(label: 'Terraform Test', script: cmd.join(' '), returnStdout: true)
      }
    }
  }
  catch (hudson.AbortException error) {
    print 'Failure using terraform test.'
    throw error
  }
  print 'Terraform test was successful.'

  return testOutput
}

String validate(Map config) {
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'validate', '-no-color']

  // optional inputs
  if (config.json == true) {
    cmd.add('-json')
  }
  if (config.tests == false) {
    cmd.add('-no-tests')
  }
  else if (config.testDir) {
    assert fileExists(config.testDir) : "The test directory ${config.testDir} does not exist."

    cmd.add("-test-directory=${config.testDir}")
  }

  // validate the config directory
  String validateOutput
  try {
    dir(config.dir) {
      withEnv(['TF_IN_AUTOMATION=true']) {
        validateOutput = sh(label: 'Terraform Validate', script: cmd.join(' '), returnStdout: true)
      }
    }
  }
  catch (hudson.AbortException error) {
    print 'Failure using terraform validate.'
    throw error
  }
  print 'Terraform validate was successful.'

  return validateOutput
}

void workspace(Map config) {
  // input checking
  if (config.dir) {
    assert fileExists(config.dir) : "Config directory ${config.dir} does not exist!"
  }
  else {
    config.dir = env.WORKSPACE
  }
  assert config.workspace in String : 'The "workspace" parameter must be specified for the "workspace" method.'
  config.bin = config.bin ?: 'terraform'

  List<String> cmd = [config.bin, 'workspace', 'select']

  // optional inputs
  if (config.create == true) {
    cmd.add('-or-create')
  }

  dir(config.dir) {
    // select workspace in terraform config directory
    try {
      cmd.add(config.workspace)
      withEnv(['TF_IN_AUTOMATION=true']) {
        new helpers().toolExec("Terraform Workspace Select ${config.workspace}", cmd)
      }
    }
    catch (hudson.AbortException error) {
      print 'Failure using terraform workspace select. The available workspaces and your current workspace are as follows:'

      final String workspaces = sh(label: 'Terraform Workspace List', script: "${config.bin} workspace list", returnStdout: true)
      print workspaces

      throw error
    }
  }
}
