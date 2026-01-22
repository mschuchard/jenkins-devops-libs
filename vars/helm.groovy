//vars/helm.groovy
import devops.common.utils
import devops.common.helpers

String history(Map config) {
  // input checking
  assert config.name : 'The required parameter "name" was not set.'
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'history']

  // check for optional inputs
  if (config.max) {
    cmd.addAll(['--max', config.max])
  }
  if (config.outputFormat) {
    assert (['table', 'json', 'yaml'].contains(config.outputFormat)) : 'The outputFormat parameter must be one of table, json, or yaml'

    cmd.addAll(['-o', config.outputFormat])
  }
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
  }

  // gather release revision history with helm
  try {
    cmd.add(config.name)
    final String historyOutput = sh(label: "Helm History ${config.name}", script: cmd.join(' '), returnStdout: true)

    print 'Helm history executed successfully.'

    return historyOutput
  }
  catch (hudson.AbortException error) {
    print 'Failure using helm history.'
    throw error
  }
}

void install(Map config) {
  // input checking
  assert config.name : 'The required parameter "name" was not set.'
  assert config.chart : 'The required parameter "chart" was not set.'
  if (config.version && config.devel) {
    error(message: "The 'version' and 'devel' parameters for helm.install are mutually exclusive; only one can be specified.")
  }
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'install']
  List<String> lister = [config.bin, 'list']

  // check for optional inputs
  if (config.values) {
    assert (config.values in List) : 'The values parameter must be a list of strings.'

    config.values.each { String value ->
      if (!value.contains('://')) {
        assert new helpers().validateYamlFile(value, 'value overrides file')
      }

      cmd.addAll(['-f', value])
    }
  }
  if (config.set) {
    assert (config.set in Map) : 'The set parameter must be a Map.'

    config.set.each { String var, String value ->
      cmd.addAll(['--set', "${var}=${value}"])
    }
  }
  if (config.version) {
    cmd.addAll(['--version', config.version])
  }
  else if (config.devel == true) {
    cmd.add('--devel')
  }
  if (config.dryRun == true) {
    cmd.add('--dry-run')
  }
  if (config.force == true) {
    cmd.add('--force')
  }
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
    lister.addAll(['--kube-context', config.context])
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
    lister.addAll(['--namespace', config.namespace])
  }
  if (config.createNS == true) {
    cmd.add('--create-namespace')
  }
  if (config.verify == true) {
    cmd.add('--verify')
  }
  if (config.atomic == true) {
    cmd.add('--atomic')
  }
  else if (config.wait == true) {
    cmd.add('--wait')
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister.join(' ')).trim()
  if (releaseObjList =~ config.name) {
    error(message: "Release object ${config.name} already exists!")
  }

  // install with helm
  cmd.addAll([config.name, config.chart])
  new helpers().toolExec("Helm Install ${config.name}", cmd)
}

void kubectl(String version, String installPath = '/usr/bin/') {
  new utils().makeDirParents(installPath)

  // check if current version already installed
  if (fileExists("${installPath}/kubectl")) {
    final String installedVersion = sh(label: 'Check Kubectl Version', returnStdout: true, script: "${installPath}/kubectl version").trim()
    if (installedVersion =~ version) {
      print "Kubectl version ${version} already installed at ${installPath}."
      return
    }
  }
  // otherwise download specified version
  new utils().downloadFile("https://storage.googleapis.com/kubernetes-release/release/v${version}/bin/linux/amd64/kubectl", "${installPath}/kubectl")
  sh(label: 'Kubectl Executable Permissions', script: "chmod ug+rx ${installPath}/kubectl")
  print "Kubectl successfully installed at ${installPath}/kubectl."
}

Boolean lint(Map config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : 'The required parameter "chart" was not set.'

  List<String> cmd = [config.bin, 'lint']

  // check for optional inputs
  if (config.values) {
    assert (config.values in List) : 'The values parameter must be a list of strings.'

    config.values.each { String value ->
      if (!value.contains('://')) {
        assert new helpers().validateYamlFile(value, 'value overrides file')
      }

      cmd.addAll(['-f', value])
    }
  }
  if (config.set) {
    assert (config.set in Map) : 'The set parameter must be a Map.'

    config.set.each { String var, String value ->
      cmd.addAll(['--set', "${var}=${value}"])
    }
  }
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
  }
  if (config.strict == true) {
    cmd.add('--strict')
  }

  // lint with helm
  cmd.add(config.chart)
  final int returnCode = sh(label: "Helm Lint ${config.chart}", script: cmd.join(' '), returnStatus: true)

  // return by code
  if (returnCode == 0) {
    print 'The chart successfully linted.'
    return true
  }
  else if (returnCode == 1) {
    print 'The chart failed linting.'
    return false
  }

  print 'Failure using helm lint.'
  error(message: 'Helm lint failed unexpectedly')
}

void packages(Map config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : 'The required parameter "chart" was not set.'
  assert new helpers().validateYamlFile("${config.chart}/Chart.yaml", 'chart')
  if (config.key && config.keyring) {
    error(message: "The 'key' and 'keyring' parameters for helm.packages are mutually exclusive; only one can be specified.")
  }

  List<String> cmd = [config.bin, 'package']

  // check for optional inputs
  if (config.dest) {
    new utils().makeDirParents(config.dest)

    cmd.addAll(['-d', config.dest])
  }
  if (config.key) {
    cmd.addAll(['--sign', '--key', config.key])
  }
  else if (config.keyring) {
    assert fileExists(config.keyring) : "The keyring ${config.keyring} does not exist."

    cmd.addAll(['--sign', '--keyring', config.keyring])
  }
  if (config.updateDeps == true) {
    cmd.add('-u')
  }
  if (config.version) {
    cmd.addAll(['--version', config.version])
  }

  // package with helm
  cmd.add(config.chart)
  new helpers().toolExec("Helm Package ${config.chart}", cmd)
}

void plugin(Map config) {
  // input checking
  assert (['install', 'list', 'uninstall', 'update'].contains(config.command)) : 'The argument must be one of: install, list, uninstall, or update.'
  assert (config.plugin) && (config.command != 'list') : 'The required parameter "plugin" was not set for a non-list command.'
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'plugin', config.command]

  // append plugin to cmd if not list command
  if (config.command != 'list') {
    cmd.add(config.plugin)
  }

  // manage a helm plugin
  new helpers().toolExec("Helm Plugin ${config.command.capitalize()}", cmd)
}

void push(Map config) {
  // input checking
  assert config.chart : 'The required parameter "chart" was not set.'
  assert fileExists(config.chart) : "The chart does not exist at ${config.chart}."
  assert config.remote : 'The required parameter "remote" was not set.'
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'push']

  // optional inputs
  if (config.insecure == true) {
    cmd.add('--insecure-skip-tls-verify')
  }

  // push helm chart to remote registry
  cmd.addAll([config.chart, config.remote])
  new helpers().toolExec("Helm Push ${config.chart}", cmd)
}

void registryLogin(Map config) {
  // input checking
  assert config.host : 'The required parameter "host" was not set.'
  assert config.password : 'The required parameter "password" was not set.'
  assert config.username : 'The required parameter "username" was not set.'
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'registry', 'login', '--username', config.username, '--password', config.password]

  // optional inputs
  if (config.insecure == true) {
    cmd.add('--insecure')
  }

  // login to a helm registry
  cmd.add(config.host)
  new helpers().toolExec("Helm Registry Login ${config.host}", cmd)
}

void repo(Map config) {
  // input checking
  assert config.repo : 'The required parameter "repo" was not set.'
  assert config.url : 'The required parameter "url" was not set.'
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'repo', 'add']

  // optional inputs
  if (config.insecure == true) {
    cmd.add('--insecure-skip-tls-verify')
  }
  else if ((config.ca) && (config.cert) && (config.key)) {
    cmd.addAll(['--ca-file', config.ca, '--cert-file', config.cert, '--key-file', config.key])
  }
  if (config.force == true) {
    cmd.add('--force-update')
  }
  if ((config.user) && (config.password)) {
    cmd.addAll(['--username', config.user, '--password', config.password])
  }

  // add a repo with helm
  cmd.addAll([config.repo, config.url])
  new helpers().toolExec("Helm Repo Add ${config.repo}", cmd)

  // update the repo
  new helpers().toolExec("Helm Repo Update ${config.repo}", cmd.join(' ').replaceFirst('add', 'update').split(' ') as List)
}

void rollback(Map config) {
  // input checking
  assert config.name : "The required parameter 'name' was not set."
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'rollback']
  List<String> lister = [config.bin, 'list']

  // optional inputs also applicable to lister
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
    lister.addAll(['--kube-context', config.context])
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
    lister.addAll(['--namespace', config.namespace])
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister.join(' ')).trim()
  assert releaseObjList =~ config.name : "Release object ${config.name} does not exist!"

  // optional inputs
  if (config.force == true) {
    cmd.add('--force')
  }
  if (config.hooks == false) {
    cmd.add('--no-hooks')
  }
  if (config.recreatePods == true) {
    cmd.add('--recreate-pods')
  }

  // append rollback version if specified
  if (config.version) {
    cmd.addAll([config.name, config.version])
  }
  else {
    cmd.add(config.name)
  }

  // rollback with helm
  new helpers().toolExec("Helm Rollback ${config.name}", cmd)
}

void setup(String version, String installPath = '/usr/bin/') {
  new utils().makeDirParents(installPath)

  // check if current version already installed
  if (fileExists("${installPath}/helm")) {
    final String installedVersion = sh(label: 'Check Helm Version', returnStdout: true, script: "${installPath}/helm version").trim()
    if (installedVersion =~ version) {
      print "Helm version ${version} already installed at ${installPath}."
    }
  }
  // otherwise download and untar specified version
  else {
    new utils().downloadFile("https://storage.googleapis.com/kubernetes-helm/helm-v${version}-linux-amd64.tar.gz", '/tmp/helm.tar.gz')
    sh(label: 'Untar Helm CLI', script: "tar -xzf /tmp/helm.tar.gz -C ${installPath} --strip-components 1 linux-amd64/helm")
    new utils().removeFile('/tmp/helm.tar.gz')
    print "Helm successfully installed at ${installPath}/helm."
  }
}

void show(Map config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : 'The required parameter "chart" was not set.'
  assert (['all', 'chart', 'crds', 'readme', 'values']).contains(config.info) : 'The info parameter must be one of all, chart, crds, readme, or values.'

  // show chart info
  new helpers().toolExec("Helm Show ${config.chart}", [config.bin, 'show', config.info, config.chart])
}

String status(Map config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : 'The required parameter "name" was not set.'

  List<String> cmd = [config.bin, 'status']
  List<String> lister = [config.bin, 'list']

  // check for optional inputs
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
    lister.addAll(['--kube-context', config.context])
  }
  if (config.description) {
    cmd.add('--show-desc')
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
    lister.addAll(['--namespace', config.namespace])
  }
  if (config.outputFormat) {
    assert (['table', 'json', 'yaml'].contains(config.outputFormat)) : 'The outputFormat parameter must be one of table, json, or yaml'

    cmd.addAll(['-o', config.outputFormat])
  }
  if (config.resources) {
    cmd.add('--show-resources')
  }
  if (config.revision) {
    cmd.addAll(['--revision', config.revision])
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister.join(' ')).trim()
  assert (releaseObjList =~ config.name) : "Release object ${config.name} does not exist!"

  // attempt to query a release object's status
  try {
    cmd.add(config.name)
    String status = sh(label: "Helm Status ${config.name}", script: cmd.join(' '), returnStdout: true)

    print 'Helm status executed successfully.'

    return status
  }
  catch (hudson.AbortException error) {
    print 'Failure using helm status.'
    throw error
  }
}

void test(Map config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : 'The required parameter "name" was not set.'

  List<String> cmd = [config.bin, 'test']

  // check if helm test has logging functionality (deprecated in 3, but interesting code to retain)
  final String logs = sh(label: 'Check Helm Usage', returnStdout: true, script: "${config.bin} test --help") ==~ /--logs/
  if (logs) {
    cmd.add('--logs')
  }

  // optional inputs
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
  }

  // test with helm
  try {
    cmd.add(config.name)
    sh(label: "Helm Test ${config.name}", script: cmd.join(' '))
  }
  catch (hudson.AbortException error) {
    // no longer relevant as of version 1.6.0, but still interesting code
    if (!(logs)) {
      print 'Release failed helm test. kubectl will now access the logs of the test pods and display them for debugging (unless using cleanup param).'

      if (config.cleanup == true) {
        print 'Pods have already been cleaned up and are no longer accessible.'
        return
      }

      // collect necessary information for displaying debug logs
      // first grab the status of the release as a json
      final String jsonStatus = sh(label: 'Check Release Object Status', returnStdout: true, script: "${config.bin} status -o json ${config.name}")
      // parse the json to return the status map
      final Map status = readJSON(text: jsonStatus)
      // assign the namespace to a local var for kubectl logs
      final String namespace = status['namespace']
      // iterate through results and store names of test pods
      List<String> testPods = []
      status['info']['status']['last_test_suite_run']['results'].each { result ->
        testPods.push(result['name'])
      }

      // input check default value for kubectl path
      config.kubectl = config.kubectl ?: 'kubectl'

      // iterate through test pods, display the logs for each, and then delete the test pod
      testPods.each { String testPod ->
        final String podLogs = sh(label: "List Pod Logs for ${testPod}", returnStdout: true, script: "${config.kubectl} -n ${namespace} logs ${testPod}")
        print "Logs for ${testPod} for release ${config.name} are:"
        print podLogs
        print "Removing test pod ${testPod}."
        sh(label: "Test Pod Cleanup for ${testPod}", script: "${config.kubectl} -n ${namespace} delete pod ${testPod}")
      }
    }

    error(message: 'Helm test failed with above logs.')
  }
  print 'Helm test executed successfully.'
}

void uninstall(Map config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name in String : 'The required parameter "name" was not set.'

  List<String> cmd = [config.bin, 'uninstall']
  List<String> lister = [config.bin, 'list']

  // check for optional inputs
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
    lister.addAll(['--kube-context', config.context])
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
    lister.addAll(['--namespace', config.namespace])
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister.join(' ')).trim()
  assert (releaseObjList =~ config.name) : "Release object ${config.name} does not exist!"

  // attempt to uninstall a release object
  cmd.add(config.name)
  new helpers().toolExec("Helm Uninstall ${config.name}", cmd)
}

void upgrade(Map config) {
  // input checking
  if (config.version && config.devel) {
    error(message: "The 'version' and 'devel' parameters for helm.upgrade are mutually exclusive; only one can be specified.")
  }
  assert config.chart in String : 'The required parameter "chart" was not set.'
  assert config.name in String : 'The required parameter "name" was not set.'
  config.bin = config.bin ?: 'helm'

  List<String> cmd = [config.bin, 'upgrade']
  List<String> lister = [config.bin, 'list']

  // check for optional inputs
  if (config.values) {
    assert (config.values in List) : 'The values parameter must be a list of strings.'

    config.values.each { String value ->
      if (!value.contains('://')) {
        assert new helpers().validateYamlFile(value, 'value overrides file')
      }

      cmd.addAll(['-f', value])
    }
  }
  if (config.set) {
    assert (config.set in Map) : 'The set parameter must be a Map.'

    config.set.each { String var, String value ->
      cmd.addAll(['--set', "${var}=${value}"])
    }
  }
  if (config.version) {
    cmd.addAll(['--version', config.version])
  }
  else if (config.devel == true) {
    cmd.add('--devel')
  }
  if (config.verify == true) {
    cmd.add('--verify')
  }
  if (config.atomic == true) {
    cmd.add('--atomic')
  }
  else if (config.wait == true) {
    cmd.add('--wait')
  }
  if (config.install == true) {
    cmd.add('--install')

    if (config.createNS == true) {
      cmd.add('--create-namespace')
    }
  }
  if (config.dryRun == true) {
    cmd.add('--dry-run')
  }
  if (config.context) {
    cmd.addAll(['--kube-context', config.context])
    lister.addAll(['--kube-context', config.context])
  }
  if (config.namespace) {
    cmd.addAll(['--namespace', config.namespace])
    lister.addAll(['--namespace', config.namespace])
  }

  // check release object presence if install param is not true (i.e. false or null)
  if (!(config.install == true)) {
    final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister.join(' ')).trim()
    assert releaseObjList =~ config.name : "Release object ${config.name} does not exist!"
  }

  // upgrade with helm
  cmd.addAll([config.name, config.chart])
  new helpers().toolExec("Helm Upgrade ${config.name}", cmd)
}

Boolean verify(String chartPath, String helmPath = 'helm') {
  // input checking
  assert fileExists(chartPath) : "The chart at ${chartPath} does not exist."

  // verify helm chart
  final int returnCode = sh(label: "Helm Verify ${chartPath}", script: "${helmPath} verify ${chartPath}", returnStatus: true)

  // return by code
  if (returnCode == 0) {
    print "The chart at ${chartPath} successfully verified."
    return true
  }
  else if (returnCode == 1) {
    print "The chart at ${chartPath} failed verification."
    return false
  }

  print 'Failure using helm verify'
  error(message: 'Helm verify failed unexpectedly')
}
