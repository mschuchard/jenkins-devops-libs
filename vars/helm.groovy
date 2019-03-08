//vars/helm.groovy
import devops.common.utils

def delete(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'helm' : config.bin
  assert config.name != null : "The required parameter 'name' was not set."

  // attempt to delete a release object
  try {
    cmd = "${config.bin} delete"
    lister = "${config.bin} list"

    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
    }

    // check release object
    release_obj_list = sh(returnStdout: true, script: lister).trim()
    assert (release_obj_list =~ config.name) : "Release object ${config.name} does not exist!"

    sh "${cmd} ${config.name}"
  }
  catch(Exception error) {
    print 'Failure using helm delete.'
    throw error
  }
}

def install(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'helm' : config.bin
  assert config.chart != null : "The required parameter 'chart' was not set."

  // install with helm
  try {
    cmd = "${config.bin} install"
    lister = "${config.bin} list"

    if (config.values != null) {
      assert (config.values instanceof String[]) : 'The values parameter must be an array of strings.'

      config.values.each() { value ->
        assert fileExists(value) : "Value overrides file ${value} does not exist!"

        cmd += " -f ${value}"
      }
    }
    if (config.set != null) {
      assert (config.set instanceof String[]) : 'The set parameter must be an array of strings.'

      config.set.each() { kv ->
        cmd += " --set ${kv}"
      }
    }
    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
    }
    if (config.name != null) {
      cmd += " --name ${config.name}"
    }
    if (config.namespace != null) {
      cmd += " --namespace ${config.namespace}"
      lister += " --namespace ${config.namespace}"
    }
    if (config.verify == true) {
      cmd += " --verify"
    }

    // check release object
    release_obj_list = sh(returnStdout: true, script: lister).trim()
    if ((config.name != null) && (release_obj_list =~ config.name)) {
      throw new Exception("Release object ${config.name} already exists!")
    }

    sh "${cmd} ${config.chart}"
  }
  catch(Exception error) {
    print 'Failure using helm install.'
    throw error
  }
  print 'Helm install executed successfully.'
}

def lint(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'helm' : config.bin
  assert config.chart != null : "The required parameter 'chart' was not set."

  // lint with helm
  try {
    cmd = "${config.bin} lint"

    if (config.values != null) {
      assert (config.values instanceof String[]) : 'The values parameter must be an array of strings.'

      config.values.each() { value ->
        assert fileExists(value) : "Value overrides file ${value} does not exist!"

        cmd += " -f ${value}"
      }
    }
    if (config.set != null) {
      assert (config.set instanceof String[]) : 'The set parameter must be an array of strings.'

      config.set.each() { kv ->
        cmd += " --set ${kv}"
      }
    }
    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
    }
    if (config.namespace != null) {
      cmd += " --namespace ${config.namespace}"
    }
    if (config.strict == true) {
      cmd += " --strict"
    }

    lint_output = sh(returnStdout: true, script: "${cmd} ${config.chart}")

    if (lint_output == '') {
      print 'No errors or warnings from helm lint.'
    }
    else {
      print 'Helm lint output is:'
      print lint_output
    }
  }
  catch(Exception error) {
    print 'Chart failed helm lint. Output of helm lint is:'
    print lint_output
    throw error
  }
  print 'Helm lint executed successfully.'
}

def package(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'helm' : config.bin
  assert config.chart != null : "The required parameter 'chart' was not set."
  assert fileExists("${config.chart}/Chart.yaml") : "The supplied path ${config.chart} to the chart does not contain a Chart.yaml!"

  // package with helm
  try {
    cmd = "${config.bin} package"

    if (config.dest != null) {
      assert fileExists(config.dest) : "The destination directory ${config.dest} for the chart archive does not exist!"

      cmd += " -d ${config.dest}"
    }
    if (config.key != null) {
      cmd += " --sign --key ${config.key}"
    }
    if (config.update_deps == true) {
      cmd += " -u"
    }
    if (config.version != null) {
      cmd += " --version ${config.version}"
    }

    sh "${cmd} ${config.chart}"
  }
  catch(Exception error) {
    print 'Failure using helm package.'
    throw error
  }
  print 'Helm package command was successful.'
}

def rollback(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.version != null : "The required parameter 'version' was not set."
  assert config.name != null : "The required parameter 'name' was not set."
  config.bin = config.bin == null ? 'helm' : config.bin

  // rollback with helm
  try {
    cmd = "${config.bin} rollback"
    lister = "${config.bin} list"

    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
    }

    // check release object
    release_obj_list = sh(returnStdout: true, script: lister).trim()
    assert release_obj_list =~ config.name : "Release object ${config.name} does not exist!"

    sh "${cmd} ${config.name} ${config.version}"
  }
  catch(Exception error) {
    print 'Failure using helm rollback.'
    throw error
  }
  print 'Helm rollback command was successful.'
}

def setup(String version, String install_path = '/usr/bin/') {
  // check if current version already installed
  if (fileExists("${install_path}/helm")) {
    installed_version = sh(returnStdout: true, script: "${install_path}/helm version").trim()
    if (installed_version =~ version) {
      print "Helm version ${version} already installed at ${install_path}."
    }
  }
  // otherwise download and untar specified version
  else {
    new utils().download_file("https://storage.googleapis.com/kubernetes-helm/helm-v${version}-linux-amd64.tar.gz", '/tmp/helm.tar.gz')
    sh "tar -xzf /tmp/helm.tar.gz -C ${install_path} --strip-components 1 linux-amd64/helm"
    new utils().remove_file('/tmp/helm.tar.gz')
    print "Helm successfully installed at ${install_path}/helm."
    // and then initialize helm
    try {
      sh "${install_path}/helm init"
    }
    catch(Exception error) {
      print 'Failure initializing helm.'
      throw error
    }
    print "Helm and Tiller successfully initialized."
  }
  if (!(fileExists("${install_path}/.helm"))) {
    sh "${install_path}/helm init --client-only "
    print "Helm successfully initialized."
  }
}

def test(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.bin = config.bin == null ? 'helm' : config.bin
  assert config.name != null : "The required parameter 'name' was not set."

  // test with helm
  try {
    cmd = "${config.bin} test"

    // optional inputs
    if (config.cleanup == true) {
      cmd += " --cleanup"
    }
    if (config.parallel == true) {
      cmd += " --parallel"
    }
    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
    }

    sh "${cmd} ${config.name}"
  }
  catch(Exception error) {
    print 'Release failed helm test. kubectl will now access the logs of the test pods and display them for debugging (unless using cleanup param).'

    if (config.cleanup == true) {
      print 'Pods have already been cleaned up and are no longer accessible.'
      return
    }

    // collect necessary information for displaying debug logs
    // first grab the status of the release as a json
    json_status = sh(returnStdout: true, script: "${config.bin} status -o json ${config.name}")
    // parse the json to return the status hash
    status = readJSON(text: json_status)
    // assign the namespace to a local var for kubectl logs
    namespace = status['namespace']
    // iterate through results and store names of test pods
    test_pods = []
    status['info']['status']['last_test_suite_run']['results'].each() { result ->
      test_pods.push(result['name'])
    }

    // input check default value for kubectl path
    config.kubectl = config.kubectl == null ? 'kubectl' : config.kubectl

    // iterate through test pods, display the logs for each, and then delete the test pod
    test_pods.each() { test_pod ->
      logs = sh(returnStdout: true, script: "${config.kubectl} -n ${namespace} logs ${test_pod}")
      print "Logs for ${test_pod} for release ${config.name} are:"
      print logs
      print "Removing test pod ${test_pod}."
      sh "${config.kubectl} -n ${namespace} delete pod ${test_pod}"
    }

    throw new Exception('Helm test failed with above logs.')
  }
  print 'Helm test executed successfully.'
}

def upgrade(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.chart != null : "The required parameter 'chart' was not set."
  assert config.name != null : "The required parameter 'name' was not set."
  config.bin = config.bin == null ? 'helm' : config.bin

  // upgrade with helm
  try {
    cmd = "${config.bin} upgrade"
    lister = "${config.bin} list"

    if (config.values != null) {
      assert (config.values instanceof String[]) : 'The values parameter must be an array of strings.'

      config.values.each() { value ->
        assert fileExists(value) : "Value overrides file ${value} does not exist!"

        cmd += " -f ${value}"
      }
    }
    if (config.set != null) {
      assert (config.set instanceof String[]) : 'The set parameter must be an array of strings.'

      config.set.each() { kv ->
        cmd += " --set ${kv}"
      }
    }
    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
      lister += " --kube-context ${config.context}"
    }
    if (config.namespace != null) {
      cmd += " --namespace ${config.namespace}"
      lister += " --namespace ${config.namespace}"
    }
    if (config.verify == true) {
      cmd += " --verify"
    }

    // check release object
    release_obj_list = sh(returnStdout: true, script: lister).trim()
    assert release_obj_list =~ config.name : "Release object ${config.name} does not exist!"

    sh "${cmd} ${config.name} ${config.chart}"
  }
  catch(Exception error) {
    print 'Failure using helm upgrade.'
    throw error
  }
  print 'Helm upgrade executed successfully.'
}
