//vars/helm.groovy
import devops.common.utils

def delete(String release_obj, String bin = 'helm') {
  // attempt to delete a release object
  release_obj_list = sh(returnStdout: true, script: "${bin} list").trim()
  if (release_obj_list =~ release_obj) {
    try {
      sh "${bin} delete ${release_obj}"
    }
    catch(Exception error) {
      print 'Failure using helm delete.'
      throw error
    }
  }
  else {
    throw new Exception("Release object ${release_obj} does not exist!")
  }
}

def install(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if (config.chart == null) {
    throw new Exception("The required parameter 'chart' was not set.")
  }
  release_obj_list = sh(returnStdout: true, script: "${config.bin} list").trim()
  if ((config.name != null) && (release_obj_list =~ config.name)) {
    throw new Exception("Release object ${config.name} already exists for ${config.chart}!")
  }
  if ((config.values != null) && (!fileExists(config.values))) {
    throw new Exception("Overrides file ${config.values} does not exist!")
  }
  config.bin = config.bin == null ? 'helm' : config.bin

  // install with helm
  try {
    cmd = "${config.bin} install"

    if (config.values != null) {
      cmd += " -f ${config.values}"
    }
    if (config.set != null) {
      cmd += " --set ${config.set}"
    }
    if (config.name != null) {
      cmd += " ${config.name}"
    }

    sh "${cmd} ${config.chart}"
  }
  catch(Exception error) {
    print 'Failure using helm install.'
    throw error
  }
  print 'Helm install executed successfully.'
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
  release_obj_list = sh(returnStdout: true, script: "${config.bin} list").trim()
  if (release_obj_list =~ config.name) {
    config.bin = config.bin == null ? 'helm' : config.bin
  }
  else {
    throw new Exception("Release object ${config.name} does not exist!")
  }

  // rollback with helm
  try {
    sh "${config.bin} rollback ${config.name} ${config.version}"
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
      return
    }
  }
  // otherwise download and untar specified version
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
  print "Helm successfully initialized."
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
  release_obj_list = sh(returnStdout: true, script: "${config.bin} list").trim()
  if (release_obj_list =~ config.name) {
    config.bin = config.bin == null ? 'helm' : config.bin
  }
  else {
    throw new Exception("Release object ${config.name} does not exist!")
  }
  if ((config.values != null) && (!fileExists(config.values))) {
    throw new Exception("Overrides file ${config.values} does not exist!")
  }

  // upgrade with helm
  try {
    cmd = "${config.bin} upgrade"

    if (config.values != null) {
      cmd += " -f ${config.values}"
    }
    if (config.set != null) {
      cmd += " --set ${config.set}"
    }

    sh "${cmd} ${config.name} ${config.chart}"
  }
  catch(Exception error) {
    print 'Failure using helm upgrade.'
    throw error
  }
  print 'Helm upgrade executed successfully.'
}
