//vars/helm.groovy
import devops.common.utils

void install(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.name : "The required parameter 'name' was not set."
  assert config.chart : "The required parameter 'chart' was not set."
  config.bin = config.bin ?: 'helm'

  // install with helm
  try {
    String cmd = "${config.bin} install"
    String lister = "${config.bin} list"

    if (config.values) {
      assert (config.values instanceof List) : 'The values parameter must be a list of strings.'

      config.values.each() { value ->
        if (!(value ==~ /:\/\//)) {
          assert fileExists(value) : "Value overrides file ${value} does not exist!"
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
      lister += " --kube-context ${config.context}"
    }
    if (config.namespace) {
      cmd += " --namespace ${config.namespace}"
      lister += " --namespace ${config.namespace}"
    }
    if (config.verify == true) {
      cmd += " --verify"
    }

    // check release object
    String release_obj_list = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
    if (release_obj_list ==~ config.name) {
      throw new Exception("Release object ${config.name} already exists!")
    }

    sh(label: 'Helm Install', script: "${cmd} ${config.name} ${config.chart}")
  }
  catch(Exception error) {
    print 'Failure using helm install.'
    throw error
  }
  print 'Helm install executed successfully.'
}

void kubectl(String version, String install_path = '/usr/bin/') {
  assert fileExists(install_path) : "The desired installation path at ${install_path} does not exist."

  // check if current version already installed
  if (fileExists("${install_path}/kubectl")) {
    String installed_version = sh(label: 'Check Kubectl Version', returnStdout: true, script: "${install_path}/kubectl version").trim()
    if (installed_version ==~ version) {
      print "Kubectl version ${version} already installed at ${install_path}."
      return
    }
  }
  // otherwise download specified version
  new utils().downloadFile("https://storage.googleapis.com/kubernetes-release/release/v${version}/bin/linux/amd64/kubectl", "${install_path}/kubectl")
  sh(label: 'Kubectl Executable Permissions', script: "chmod ug+rx ${install_path}/kubectl")
  print "Kubectl successfully installed at ${install_path}/kubectl."
}

void lint(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : "The required parameter 'chart' was not set."

  // lint with helm
  try {
    String cmd = "${config.bin} lint"

    if (config.values) {
      assert (config.values instanceof List) : 'The values parameter must be a list of strings.'

      config.values.each() { value ->
        if (!(value ==~ /:\/\//)) {
          assert fileExists(value) : "Value overrides file ${value} does not exist!"
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
      cmd += " --strict"
    }

    String lint_output = sh(label: 'Helm Lint', returnStdout: true, script: "${cmd} ${config.chart}")

    if (!(lint_output)) {
      print 'No errors or warnings from helm lint.'
    }
    else {
      print 'Helm lint output is:'
      print lint_output
    }
  }
  catch(Exception error) {
    print 'Chart failed helm lint.'

    // if the chart caused the error, then give more information about that
    if (lint_output) {
      print 'Output of helm lint is:'
      print lint_output
    }

    throw error
  }
  print 'Helm lint executed successfully.'
}

void packages(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.chart : "The required parameter 'chart' was not set."
  assert fileExists("${config.chart}/Chart.yaml") : "The supplied path ${config.chart} to the chart does not contain a Chart.yaml!"

  // package with helm
  try {
    String cmd = "${config.bin} package"

    if (config.dest) {
      assert fileExists(config.dest) : "The destination directory ${config.dest} for the chart archive does not exist!"

      cmd += " -d ${config.dest}"
    }
    if (config.key) {
      cmd += " --sign --key ${config.key}"
    }
    else if (config.keyring) {
      assert fileExists(config.keyring) : "The keyring ${config.keyring} does not exist."

      cmd += " --sign --keyring ${config.keyring}"
    }
    if (config.update_deps == true) {
      cmd += " -u"
    }
    if (config.version) {
      cmd += " --version ${config.version}"
    }

    sh(label: 'Helm Package', script: "${cmd} ${config.chart}")
  }
  catch(Exception error) {
    print 'Failure using helm package.'
    throw error
  }
  print 'Helm package command was successful.'
}

void plugin(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  assert (['install', 'list', 'uninstall', 'update'].contains(config.command)) : "The argument must be one of: install, list, uninstall, or update."
  assert (config.plugin) && (config.command != 'list') : "The required parameter 'plugin' was not set for a non-list command."
  config.bin = config.bin ?: 'helm'

  // manage a helm plugin
  try {
    String cmd = "${config.bin} plugin ${config.command}"

    // append plugin to cmd if not list command
    if (config.command != 'list') {
      cmd += " ${config.plugin}"
    }

    sh(label: 'Helm Plugin', script: cmd)
  }
  catch(Exception error) {
    print "Failure using helm plugin ${config.command}."
    throw error
  }
  print "Helm plugin ${config.command} executed successfully."
}

void repo(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.repo : "The required parameter 'repo' was not set."
  assert config.url : "The required parameter 'url' was not set."
  config.bin = config.bin ?: 'helm'

  // add a repo with helm
  try {
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

    sh(label: 'Helm Repo Add', script: "${cmd} ${config.repo} ${config.url}")
  }
  catch(Exception error) {
    print 'Failure using helm repo add.'
    throw error
  }
  print 'Helm repo add executed successfully.'
}

void rollback(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.version : "The required parameter 'version' was not set."
  assert config.name : "The required parameter 'name' was not set."
  config.bin = config.bin ?: 'helm'

  // rollback with helm
  try {
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
    String release_obj_list = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
    assert release_obj_list ==~ config.name : "Release object ${config.name} does not exist!"

    sh(label: 'Helm rollback', script: "${cmd} ${config.name} ${config.version}")
  }
  catch(Exception error) {
    print 'Failure using helm rollback.'
    throw error
  }
  print 'Helm rollback command was successful.'
}

void setup(String version, String install_path = '/usr/bin/') {
  assert fileExists(install_path) : "The desired installation path at ${install_path} does not exist."

  // check if current version already installed
  if (fileExists("${install_path}/helm")) {
    String installed_version = sh(label: 'Check Helm Version', returnStdout: true, script: "${install_path}/helm version").trim()
    if (installed_version ==~ version) {
      print "Helm version ${version} already installed at ${install_path}."
    }
  }
  // otherwise download and untar specified version
  else {
    new utils().downloadFile("https://storage.googleapis.com/kubernetes-helm/helm-v${version}-linux-amd64.tar.gz", '/tmp/helm.tar.gz')
    sh(label: 'Untar Helm CLI', script: "tar -xzf /tmp/helm.tar.gz -C ${install_path} --strip-components 1 linux-amd64/helm")
    new utils().removeFile('/tmp/helm.tar.gz')
    print "Helm successfully installed at ${install_path}/helm."
  }
}

void status(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : "The required parameter 'name' was not set."

  // attempt to query a release object's status
  try {
    String cmd = "${config.bin} status"
    String lister = "${config.bin} list"

    if (config.context) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
    }
    if (config.namespace) {
      cmd += " --namespace ${config.namespace}"
      lister += " --namespace ${config.namespace}"
    }

    // check release object
    String release_obj_list = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
    assert (release_obj_list ==~ config.name) : "Release object ${config.name} does not exist!"

    sh(label: 'Helm Status', script: "${cmd} ${config.name}")
  }
  catch(Exception error) {
    print 'Failure using helm status.'
    throw error
  }
  print 'Helm status executed successfully.'
}

void test(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : "The required parameter 'name' was not set."

  // test with helm
  try {
    String cmd = "${config.bin} test"

    // check if helm test has logging functionality (deprecated in 3, but interesting code to retain)
    String logs = sh(label: 'Check Helm Usage', returnStdout: true, script: "${config.bin} test --help") ==~ /--logs/
    if (logs) {
      cmd += " --logs"
    }

    // optional inputs
    if (config.cleanup == true) {
      cmd += " --cleanup"
    }
    if (config.parallel == true) {
      cmd += " --parallel"
    }
    if (config.context) {
      cmd += " --kube-context ${config.context}"
    }
    if (config.namespace) {
      cmd += " --namespace ${config.namespace}"
    }

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
      String json_status = sh(label: 'Check Release Object Status', returnStdout: true, script: "${config.bin} status -o json ${config.name}")
      // parse the json to return the status map
      Map status = readJSON(text: json_status)
      // assign the namespace to a local var for kubectl logs
      String namespace = status['namespace']
      // iterate through results and store names of test pods
      List<String> test_pods = []
      status['info']['status']['last_test_suite_run']['results'].each() { result ->
        test_pods.push(result['name'])
      }

      // input check default value for kubectl path
      config.kubectl = config.kubectl ?: 'kubectl'

      // iterate through test pods, display the logs for each, and then delete the test pod
      test_pods.each() { test_pod ->
        String logs = sh(label: 'List Pod Logs', returnStdout: true, script: "${config.kubectl} -n ${namespace} logs ${test_pod}")
        print "Logs for ${test_pod} for release ${config.name} are:"
        print logs
        print "Removing test pod ${test_pod}."
        sh(label: 'Test Pod Cleanup', script: "${config.kubectl} -n ${namespace} delete pod ${test_pod}")
      }
    }

    throw new Exception('Helm test failed with above logs.')
  }
  print 'Helm test executed successfully.'
}

void uninstall(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ?: 'helm'
  assert config.name : "The required parameter 'name' was not set."

  // attempt to uninstall a release object
  try {
    String cmd = "${config.bin} uninstall"
    String lister = "${config.bin} list"

    if (config.context) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
    }
    if (config.namespace) {
      cmd += " --namespace ${config.namespace}"
      lister += " --namespace ${config.namespace}"
    }

    // check release object
    String release_obj_list = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
    assert (release_obj_list ==~ config.name) : "Release object ${config.name} does not exist!"

    sh(label: 'Helm Uninstall', script: "${cmd} ${config.name}")
  }
  catch(Exception error) {
    print 'Failure using helm uninstall.'
    throw error
  }
  print 'Helm uninstall executed successfully.'
}

void upgrade(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.chart : "The required parameter 'chart' was not set."
  assert config.name : "The required parameter 'name' was not set."
  config.bin = config.bin ?: 'helm'

  // upgrade with helm
  try {
    String cmd = "${config.bin} upgrade"
    String lister = "${config.bin} list"

    if (config.values) {
      assert (config.values instanceof List) : 'The values parameter must be a list of strings.'

      config.values.each() { value ->
        if (!(value ==~ /:\/\//)) {
          assert fileExists(value) : "Value overrides file ${value} does not exist!"
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
    if (config.install == true) {
      cmd += ' --install'
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
      String release_obj_list = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
      assert release_obj_list ==~ config.name : "Release object ${config.name} does not exist!"
    }

    sh(label: 'Helm Upgrade', script: "${cmd} ${config.name} ${config.chart}")
  }
  catch(Exception error) {
    print 'Failure using helm upgrade.'
    throw error
  }
  print 'Helm upgrade executed successfully.'
}
