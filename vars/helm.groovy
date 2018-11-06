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
  if (config.name == null) {
    throw new Exception("The required parameter 'name' was not set.")
  }

  // attempt to delete a release object
  lister = config.context == null ? "${config.bin} list" : "${config.bin} --kube-context ${config.context} list"
  release_obj_list = sh(returnStdout: true, script: lister).trim()

  if (release_obj_list =~ config.name) {
    try {
      cmd = "${config.bin} delete"

      if (config.context != null) {
        cmd += " --kube-context ${config.context}"
      }

      sh "${cmd} ${config.name}"
    }
    catch(Exception error) {
      print 'Failure using helm delete.'
      throw error
    }
  }
  else {
    throw new Exception("Release object ${config.name} does not exist!")
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
  if (config.chart == null) {
    throw new Exception("The required parameter 'chart' was not set.")
  }
  lister = config.context == null ? "${config.bin} list" : "${config.bin} --kube-context ${config.context} list"
  release_obj_list = sh(returnStdout: true, script: lister).trim()
  if ((config.name != null) && (release_obj_list =~ config.name)) {
    throw new Exception("Release object ${config.name} already exists!")
  }

  // install with helm
  try {
    cmd = "${config.bin} install"

    if (config.values != null) {
      if (!fileExists(config.values)) {
        throw new Exception("Overrides file ${config.values} does not exist!")
      }
      cmd += " -f ${config.values}"
    }
    if (config.set != null) {
      if (!(config.set instanceof String[])) {
        throw new Exception('The set parameter must be an array of strings.')
      }
      config.set.each() { kv ->
        cmd += " --set ${kv}"
      }
    }
    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
    }
    if (config.name != null) {
      cmd += " --name ${config.name}"
    }
    if (config.namespace != null) {
      cmd += " --namespace ${config.namespace}"
    }
    if (config.verify == true) {
      cmd += " --verify"
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
  if (config.chart == null) {
    throw new Exception("The required parameter 'chart' was not set.")
  }

  // lint with helm
  try {
    cmd = "${config.bin} lint"

    if (config.values != null) {
      if (!fileExists(config.values)) {
        throw new Exception("Overrides file ${config.values} does not exist!")
      }
      cmd += " -f ${config.values}"
    }
    if (config.set != null) {
      if (!(config.set instanceof String[])) {
        throw new Exception('The set parameter must be an array of strings.')
      }
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

def rollback(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.version == null) {
    throw new Exception("The required parameter 'version' was not set.")
  }
  if (config.name == null) {
    throw new Exception("The required parameter 'name' was not set.")
  }
  config.bin = config.bin == null ? 'helm' : config.bin
  lister = config.context == null ? "${config.bin} list" : "${config.bin} --kube-context ${config.context} list"
  release_obj_list = sh(returnStdout: true, script: lister).trim()
  if (!(release_obj_list =~ config.name)) {
    throw new Exception("Release object ${config.name} does not exist!")
  }

  // rollback with helm
  try {
    cmd = "${config.bin} rollback"

    if (config.context != null) {
      cmd += " --kube-context ${config.context}"
    }

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

def upgrade(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.chart == null) {
    throw new Exception("The required parameter 'chart' was not set.")
  }
  if (config.name == null) {
    throw new Exception("The required parameter 'name' was not set.")
  }
  config.bin = config.bin == null ? 'helm' : config.bin
  lister = config.context == null ? "${config.bin} list" : "${config.bin} --kube-context ${config.context} list"
  release_obj_list = sh(returnStdout: true, script: lister).trim()
  if (!(release_obj_list =~ config.name)) {
    throw new Exception("Release object ${config.name} does not exist!")
  }

  // upgrade with helm
  try {
    cmd = "${config.bin} upgrade"

    if (config.values != null) {
      if (!fileExists(config.values)) {
        throw new Exception("Overrides file ${config.values} does not exist!")
      }
      cmd += " -f ${config.values}"
    }
    if (config.set != null) {
      if (!(config.set instanceof String[])) {
        throw new Exception('The set parameter must be an array of strings.')
      }
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
    if (config.verify == true) {
      cmd += " --verify"
    }

    sh "${cmd} ${config.name} ${config.chart}"
  }
  catch(Exception error) {
    print 'Failure using helm upgrade.'
    throw error
  }
  print 'Helm upgrade executed successfully.'
}
