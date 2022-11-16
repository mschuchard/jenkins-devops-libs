//vars/helm.groovy
import devops.common.utils

void install(config) {
  // input checking
  assert config.name : 'The required parameter "name" was not set.'
  assert config.chart : 'The required parameter "chart" was not set.'
  config.bin = config.bin ?: 'helm'

  String cmd = "${config.bin} install"
  String lister = "${config.bin} list"

  // check for optional inputs
  if (config.values) {
    assert (config.values instanceof List) : 'The values parameter must be a list of strings.'

    config.values.each() { value ->
      if (!(value ==~ /:\/\//)) {
        assert readYaml(value) instanceof String : "Value overrides file ${value} does not exist or is not a valid YAML file!"
      }

      cmd += " -f ${value}"
    }
  }
  if (config.set) {
    assert (config.set instanceof Map) : 'The set parameter must be a Map.'

    config.set.each() { var, value ->
      cmd += " --set ${var}=${value}"
    }
  }
  if (config.dryRun == true) {
    cmd += ' --dry-run'
  }
  if (config.context) {
    cmd += " --kube-context ${config.context}"
    lister += " --kube-context ${config.context}"
  }
  if (config.namespace) {
    cmd += " --namespace ${config.namespace}"
    lister += " --namespace ${config.namespace}"
  }
  if (config.verify == true) {
    cmd += ' --verify'
  }
  if (config.wait == true) {
    cmd += ' --wait'
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
  if (releaseObjList =~ config.name) {
    throw new Exception("Release object ${config.name} already exists!")
  }

  // install with helm
  try {
    sh(label: 'Helm Install', script: "${cmd} ${config.name} ${config.chart}")
  }
  catch(Exception error) {
    print 'Failure using helm install.'
    throw error
  }
  print 'Helm install executed successfully.'
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

void lint(config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : 'The required parameter "chart" was not set.'

  String cmd = "${config.bin} lint"

  // check for optional inputs
  if (config.values) {
    assert (config.values instanceof List) : 'The values parameter must be a list of strings.'

    config.values.each() { value ->
      if (!(value ==~ /:\/\//)) {
        assert readYaml(value) instanceof String : "Value overrides file ${value} does not exist or is not a valid YAML file!"
      }

      cmd += " -f ${value}"
    }
  }
  if (config.set) {
    assert (config.set instanceof Map) : 'The set parameter must be a Map.'

    config.set.each() { var, value ->
      cmd += " --set ${var}=${value}"
    }
  }
  if (config.context) {
    cmd += " --kube-context ${config.context}"
  }
  if (config.namespace) {
    cmd += " --namespace ${config.namespace}"
  }
  if (config.strict == true) {
    cmd += ' --strict'
  }

  // lint with helm
  try {
    final String lintOutput = sh(label: 'Helm Lint', returnStdout: true, script: "${cmd} ${config.chart}")

    if (!(lintOutput)) {
      print 'No errors or warnings from helm lint.'
    }
    else {
      print 'Helm lint output is:'
      print lintOutput
    }
  }
  catch(Exception error) {
    print 'Chart failed helm lint.'

    // if the chart caused the error, then give more information about that
    if (lintOutput) {
      print 'Output of helm lint is:'
      print lintOutput
    }

    throw error
  }
  print 'Helm lint executed successfully.'
}

void packages(config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : 'The required parameter "chart" was not set.'
  assert readYaml("${config.chart}/Chart.yaml") instanceof String : "The supplied path ${config.chart} to the chart does not contain a valid Chart.yaml!"

  String cmd = "${config.bin} package"

  // check for optional inputs
  if (config.dest) {
    new utils().makeDirParents(config.dest)

    cmd += " -d ${config.dest}"
  }
  if (config.key) {
    cmd += " --sign --key ${config.key}"
  }
  else if (config.keyring) {
    assert readYaml(config.keyring) instanceof String : "The keyring ${config.keyring} does not exist or is not a valid YAML file."

    cmd += " --sign --keyring ${config.keyring}"
  }
  if (config.updateDeps == true) {
    cmd += ' -u'
  }
  if (config.version) {
    cmd += " --version ${config.version}"
  }

  // package with helm
  try {
    sh(label: 'Helm Package', script: "${cmd} ${config.chart}")
  }
  catch(Exception error) {
    print 'Failure using helm package.'
    throw error
  }
  print 'Helm package command was successful.'
}

void plugin(config) {
  // input checking
  assert (['install', 'list', 'uninstall', 'update'].contains(config.command)) : 'The argument must be one of: install, list, uninstall, or update.'
  assert (config.plugin) && (config.command != 'list') : 'The required parameter "plugin" was not set for a non-list command.'
  config.bin = config.bin ?: 'helm'

  String cmd = "${config.bin} plugin ${config.command}"

  // append plugin to cmd if not list command
  if (config.command != 'list') {
    cmd += " ${config.plugin}"
  }

  // manage a helm plugin
  try {
    sh(label: 'Helm Plugin', script: cmd)
  }
  catch(Exception error) {
    print "Failure using helm plugin ${config.command}."
    throw error
  }
  print "Helm plugin ${config.command} executed successfully."
}

void repo(config) {
  // input checking
  assert config.repo : 'The required parameter "repo" was not set.'
  assert config.url : 'The required parameter "url" was not set.'
  config.bin = config.bin ?: 'helm'

  String cmd = "${config.bin} repo add"

  // optional inputs
  if (config.insecure) {
    cmd += ' --insecure-skip-tls-verify'
  }
  else if ((config.ca) && (config.cert) && (config.key)) {
    cmd += " --ca-file ${config.ca} --cert-file ${config.cert} --key-file ${config.key}"
  }
  if ((config.user) && (config.password)) {
    cmd += " --username ${config.user} --password ${config.password}"
  }

  // add a repo with helm
  try {
    sh(label: 'Helm Repo Add', script: "${cmd} ${config.repo} ${config.url}")
  }
  catch(Exception error) {
    print 'Failure using helm repo add.'
    throw error
  }
  print 'Helm repo add executed successfully.'
}

void rollback(config) {
  // input checking
  assert config.version : "The required parameter 'version' was not set."
  assert config.name : "The required parameter 'name' was not set."
  config.bin = config.bin ?: 'helm'

  String cmd = "${config.bin} rollback"
  String lister = "${config.bin} list"

  // optional inputs
  if (config.context) {
    cmd += " --kube-context ${config.context}"
    lister += " --kube-context ${config.context}"
  }
  if (config.namespace) {
    cmd += " --namespace ${config.namespace}"
    lister += " --namespace ${config.namespace}"
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
  assert releaseObjList =~ config.name : "Release object ${config.name} does not exist!"

  // rollback with helm
  try {
    sh(label: 'Helm rollback', script: "${cmd} ${config.name} ${config.version}")
  }
  catch(Exception error) {
    print 'Failure using helm rollback.'
    throw error
  }
  print 'Helm rollback command was successful.'
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

void show(config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : 'The required parameter "chart" was not set.'
  assert config.info : 'The required parameter "info" was not set.'
  assert (['all', 'chart', 'readme', 'values']).contains(config.info) : "The info parameter must be one of all, chart, readme, or values."

  // show chart info
  try {
    sh(label: 'Helm Show', script: "${config.bin} ${config.info} ${config.chart}")
  }
  catch(Exception error) {
    print 'Failure using helm show.'
    throw error
  }
  print 'Helm show executed successfully.'
}

void status(config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : 'The required parameter "name" was not set.'

  String cmd = "${config.bin} status"
  String lister = "${config.bin} list"

  // check for optional inputs
  if (config.context) {
    cmd += " --kube-context ${config.context}"
    lister += " --kube-context ${config.context}"
  }
  if (config.namespace) {
    cmd += " --namespace ${config.namespace}"
    lister += " --namespace ${config.namespace}"
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
  assert (releaseObjList =~ config.name) : "Release object ${config.name} does not exist!"

  // attempt to query a release object's status
  try {
    sh(label: 'Helm Status', script: "${cmd} ${config.name}")
  }
  catch(Exception error) {
    print 'Failure using helm status.'
    throw error
  }
  print 'Helm status executed successfully.'
}

void test(config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : 'The required parameter "name" was not set.'

  String cmd = "${config.bin} test"

  // check if helm test has logging functionality (deprecated in 3, but interesting code to retain)
  final String logs = sh(label: 'Check Helm Usage', returnStdout: true, script: "${config.bin} test --help") ==~ /--logs/
  if (logs) {
    cmd += ' --logs'
  }

  // optional inputs
  if (config.cleanup == true) {
    cmd += ' --cleanup'
  }
  if (config.parallel == true) {
    cmd += ' --parallel'
  }
  if (config.context) {
    cmd += " --kube-context ${config.context}"
  }
  if (config.namespace) {
    cmd += " --namespace ${config.namespace}"
  }

  // test with helm
  try {
    sh(label: 'Helm Test', script: "${cmd} ${config.name}")
  }
  catch(Exception error) {
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
      status['info']['status']['last_test_suite_run']['results'].each() { result ->
        testPods.push(result['name'])
      }

      // input check default value for kubectl path
      config.kubectl = config.kubectl ?: 'kubectl'

      // iterate through test pods, display the logs for each, and then delete the test pod
      testPods.each() { testPod ->
        final String podLogs = sh(label: "List Pod Logs for ${testPod}", returnStdout: true, script: "${config.kubectl} -n ${namespace} logs ${testPod}")
        print "Logs for ${testPod} for release ${config.name} are:"
        print podLogs
        print "Removing test pod ${testPod}."
        sh(label: "Test Pod Cleanup for ${testPod}", script: "${config.kubectl} -n ${namespace} delete pod ${testPod}")
      }
    }

    throw new Exception('Helm test failed with above logs.')
  }
  print 'Helm test executed successfully.'
}

void uninstall(config) {
  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : 'The required parameter "name" was not set.'

  String cmd = "${config.bin} uninstall"
  String lister = "${config.bin} list"

  // check for optional inputs
  if (config.context) {
    cmd += " --kube-context ${config.context}"
    lister += " --kube-context ${config.context}"
  }
  if (config.namespace) {
    cmd += " --namespace ${config.namespace}"
    lister += " --namespace ${config.namespace}"
  }

  // check release object
  final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
  assert (releaseObjList =~ config.name) : "Release object ${config.name} does not exist!"

  // attempt to uninstall a release object
  try {
    sh(label: 'Helm Uninstall', script: "${cmd} ${config.name}")
  }
  catch(Exception error) {
    print 'Failure using helm uninstall.'
    throw error
  }
  print 'Helm uninstall executed successfully.'
}

void upgrade(config) {
  // input checking
  assert config.chart : 'The required parameter "chart" was not set.'
  assert config.name : 'The required parameter "name" was not set.'
  config.bin = config.bin ?: 'helm'

  String cmd = "${config.bin} upgrade"
  String lister = "${config.bin} list"

  // check for optional inputs
  if (config.values) {
    assert (config.values instanceof List) : 'The values parameter must be a list of strings.'

    config.values.each() { value ->
      if (!(value ==~ /:\/\//)) {
        assert readYaml(value) instanceof String : "Value overrides file ${value} does not exist or is not a valid YAML file!"
      }

      cmd += " -f ${value}"
    }
  }
  if (config.set) {
    assert (config.set instanceof Map) : 'The set parameter must be a Map.'

    config.set.each() { var, value ->
      cmd += " --set ${var}=${value}"
    }
  }
  if (config.verify == true) {
    cmd += ' --verify'
  }
  if (config.wait == true) {
    cmd += ' --wait'
  }
  if (config.install == true) {
    cmd += ' --install'
  }
  if (config.dryRun == true) {
    cmd += ' --dry-run'
  }
  if (config.context) {
    cmd += " --kube-context ${config.context}"
    lister += " --kube-context ${config.context}"
  }
  if (config.namespace) {
    cmd += " --namespace ${config.namespace}"
    lister += " --namespace ${config.namespace}"
  }

  // check release object presence if install param is not true (i.e. false or null)
  if (!(config.install == true)) {
    final String releaseObjList = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
    assert releaseObjList =~ config.name : "Release object ${config.name} does not exist!"
  }

  // upgrade with helm
  try {
    sh(label: 'Helm Upgrade', script: "${cmd} ${config.name} ${config.chart}")
  }
  catch(Exception error) {
    print 'Failure using helm upgrade.'
    throw error
  }
  print 'Helm upgrade executed successfully.'
}

Boolean verify(String chartPath, String helmPath = 'helm') {
  // input checking
  assert fileExists(chartPath) : "The chart at ${chartPath} does not exist."

  // verify helm chart
  try {
    sh(label: 'Helm Verify', script: "${helmPath} verify ${chartPath}")
    print "The chart at ${chartPath} successfully verified."
    return true
  }
  catch(Exception error) {
    print "The chart at ${chartPath} failed verification."
    return false
  }
}
