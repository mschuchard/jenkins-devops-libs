//vars/helm.groovy
import devops.common.utils

void delete(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ? config.bin : 'helm'
  assert config.name : "The required parameter 'name' was not set."

  // determine subcommand
  subcmd = config.new_cmd ? 'uninstall' : 'delete'

  // attempt to delete a release object
  try {
    String cmd = "${config.bin} ${subcmd}"
    String lister = "${config.bin} list"

    if (config.context) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
    }

    // check release object
    String release_obj_list = sh(label: 'List Release Objects', returnStdout: true, script: lister).trim()
    assert (release_obj_list ==~ config.name) : "Release object ${config.name} does not exist!"

    sh(label: 'Helm Delete', script: "${cmd} ${config.name}")
  }
  catch(Exception error) {
    print 'Failure using helm delete.'
    throw error
  }
}

void install(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ? config.bin : 'helm'
  assert config.chart : "The required parameter 'chart' was not set."

  // version check and required param check
  helm_help = sh(label: 'Check Helm Version', script: "${config.bin} --help", returnStdout: true)
  if (!(helm_help =~ /init/)) {
    assert config.name : "The required parameter 'name' was not set."
  }

  // install with helm
  try {
    String cmd = helm_help =~ /init/ ? "${config.bin} install" : "${config.bin} install ${config.name}"
    String lister = "${config.bin} list"

    if (config.values) {
      assert (config.values instanceof List) : 'The values parameter must be an array of strings.'

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
    // TODO: when dropping 2 support, use else for generate-name flag
    if ((config.name) && (helm_help =~ /init/)) {
      cmd += " --name ${config.name}"
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
    if ((config.name) && (release_obj_list ==~ config.name)) {
      throw new Exception("Release object ${config.name} already exists!")
    }

    sh(label: 'Helm Install', script: "${cmd} ${config.chart}")
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
  config.bin = config.bin ? config.bin : 'helm'
  assert config.chart : "The required parameter 'chart' was not set."

  // lint with helm
  try {
    String cmd = "${config.bin} lint"

    if (config.values) {
      assert (config.values instanceof List) : 'The values parameter must be an array of strings.'

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
  config.bin = config.bin ? config.bin : 'helm'
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

void repo(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.repo : "The required parameter 'repo' was not set."
  assert config.url : "The required parameter 'url' was not set."
  config.bin = config.bin ? config.bin : 'helm'

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
  config.bin = config.bin ? config.bin : 'helm'

  // rollback with helm
  try {
    String cmd = "${config.bin} rollback"
    String lister = "${config.bin} list"

    // optional inputs
    if (config.context) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
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
  // initialize helm if version < 3
  if (version ==~ /^2\./) {
    // if tiller already installed, then only initialize the helm client
    if (!(fileExists("${env.HOME}/.helm"))) {
      sh(label: 'Helm Init Client', script: "${install_path}/helm init --client-only ")
      print "Helm successfully initialized."
    }
    // otherwise fully initialize helm
    else {
      try {
        sh(label: 'Helm Init', script: "${install_path}/helm init")
      }
      catch(Exception error) {
        print 'Failure initializing helm.'
        throw error
      }
      print "Helm and Tiller successfully initialized."
    }
  }
}

void test(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  config.bin = config.bin ? config.bin : 'helm'
  assert config.name : "The required parameter 'name' was not set."

  // test with helm
  try {
    String cmd = "${config.bin} test"

    // check if helm test has logging functionality
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

    sh(label: 'Helm Test', script: "${cmd} ${config.name}")
  }
  catch(Exception error) {
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
      List test_pods = []
      status['info']['status']['last_test_suite_run']['results'].each() { result ->
        test_pods.push(result['name'])
      }

      // input check default value for kubectl path
      config.kubectl = config.kubectl ? config.kubectl : 'kubectl'

      // iterate through test pods, display the logs for each, and then delete the test pod
      test_pods.each() { test_pod ->
        logs = sh(label: 'List Pod Logs', returnStdout: true, script: "${config.kubectl} -n ${namespace} logs ${test_pod}")
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

  // redirect to delete method with key value indicating helm 3
  config['new_cmd'] = true
  delete(config)
}

void upgrade(body) {
  // evaluate the body block, and collect configuration into the object
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.chart : "The required parameter 'chart' was not set."
  assert config.name : "The required parameter 'name' was not set."
  config.bin = config.bin ? config.bin : 'helm'

  // upgrade with helm
  try {
    String cmd = "${config.bin} upgrade"
    String lister = "${config.bin} list"

    if (config.values) {
      assert (config.values instanceof List) : 'The values parameter must be an array of strings.'

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
