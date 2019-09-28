// vars/packer.groovy
import devops.common.utils

def build(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.template : 'The required template parameter was not set.'

  config.bin = !config.bin ? 'packer' : config.bin

  assert fileExists(config.template) : "The template file ${config.template} does not exist!"

  // create artifact with packer
  try {
    String cmd = "${config.bin} build -color=false"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof List) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }
    if (config.only) {
      assert (config.only instanceof List) : 'The only parameter must be an array of strings.'

      cmd += " -only=${config.only.join(',')}"
    }

    sh "${cmd} ${config.template}"
  }
  catch(Exception error) {
    print 'Failure using packer build.'
    throw error
  }
  print 'Packer build artifact created successfully.'
}

def inspect(String template, String bin = '/usr/bin/packer') {
  // input checking
  assert fileExists(bin) : "A file does not exist at ${bin}."
  assert fileExists(template) : "A file does not exist at ${template}."

  // inspect the packer template
  try {
    sh "${bin} inspect ${template}"
  }
  catch(Exception error) {
    print 'Failure inspecting the template.'
    throw error
  }
}

def install(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  config.install_path = !config.install_path ? '/usr/bin' : config.install_path
  assert (config.platform && config.version) : 'A required parameter is missing from the packer.install method. Please consult the documentation for proper usage.'
  assert fileExists(config.install_path) : "The desired installation path at ${config.install_path} does not exist."

  // check if current version already installed
  if (fileExists("${config.install_path}/packer")) {
    String installed_version = sh(returnStdout: true, script: "${config.install_path}/packer version").trim()
    if (installed_version ==~ config.version) {
      print "Packer version ${config.version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().download_file("https://releases.hashicorp.com/packer/${config.version}/packer_${config.version}_${config.platform}.zip", 'packer.zip')
  unzip(zipFile: 'packer.zip', dir: config.install_path)
  new utils().remove_file('packer.zip')
  print "Packer successfully installed at ${config.install_path}/packer."
}

def plugin_install(String url, String install_loc) {
  // determine number of elements in loc up to final slash
  String elem_count = new File(install_loc).name.lastIndexOf('/')
  // return file path up to final slash element
  String install_dir = new File(install_loc).name.take(elem_count)

  // check if plugin dir exists and create if not
  if (!(fileExists(install_dir))) {
    new File(install_dir).mkdir()
  }

  // check if plugin already installed
  if (fileExists(install_loc)) {
    print "Packer plugin already installed at ${install_loc}."
    return
  }
  // otherwise download and install plugin
  if (url ==~ /\.zip$/) {
    // append zip extension to avoid filename clashes
    install_loc = "${install_loc}.zip"
  }
  new utils().download_file(url, install_loc)
  if (url ==~ /\.zip$/) {
    unzip(zipFile: install_loc)
    new utils().remove_file(install_loc)
  }
  else {
    sh "chmod ug+rx ${install_loc}"
  }
  print "Packer plugin successfully installed at ${install_loc}."
}

def validate(Closure body) {
  // evaluate the body block and collect configuration into the object
  Map config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.template : 'The required template parameter was not set.'

  config.bin = !config.bin ? 'packer' : config.bin

  assert fileExists(config.template) : "The template file ${config.template} does not exist!"

  // validate template with packer
  try {
    String cmd = "${config.bin} validate"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof List) : 'The var parameter must be an array of strings.'

      config.var.each() { var ->
        cmd += " -var ${var}"
      }
    }
    if (config.only) {
      assert (config.only instanceof List) : 'The only parameter must be an array of strings.'

      cmd += " -only=${config.only.join(',')}"
    }

    sh "${cmd} ${config.template}"
  }
  catch(Exception error) {
    print 'Failure using packer validate.'
    throw error
  }
  print 'Packer validate executed successfully.'
}
