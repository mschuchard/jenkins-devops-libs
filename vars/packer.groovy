// vars/packer.groovy
import devops.common.utils

void build(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  config.bin = config.bin ?: 'packer'

  // create artifact with packer
  try {
    String cmd = "${config.bin} build -color=false"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof Map) : 'The var parameter must be a Map.'

      config.var.each() { var, value ->
        cmd += " -var ${var}=${value}"
      }
    }
    if (config.only) {
      assert (config.only instanceof List) : 'The only parameter must be a list of strings.'

      cmd += " -only=${config.only.join(',')}"
    }
    if (config.force == true) {
      cmd += " -force"
    }
    if (config.on_error) {
      assert (['default', 'abort', 'ask', 'run-cleanup-provisioner'].contains(config.on_error)) : "The argument must be one of: default, abort, ask, or run-cleanup-provisioner."

      cmd += " -on-error=${config.on_error}"
    }

    sh(label: 'Packer Build', script: "${cmd} ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using packer build.'
    throw error
  }
  print 'Packer build artifact created successfully.'
}

void fmt(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"

  if (config.write && config.check) {
    throw new Exception("The 'write' and 'check' options for packer.fmt are mutually exclusive - only one can be enabled.")
  }
  config.bin = config.bin ?: 'packer'

  try {
    String cmd = "${config.bin} fmt"

    if (config.diff == true) {
      cmd += " -diff"
    }
    if (config.check == true) {
      cmd += " -check"
    }
    // incompatible with above
    else if (config.write == true) {
      cmd += " -write"
    }

    fmt_status = sh(label: 'Packer Format', returnStatus: true, script: "${cmd} ${config.template}")

    // report if formatting check detected issues
    if ((config.check == true) && (fmt_status != 0)) {
      print 'Packer fmt has detected formatting errors.'
    }
  }
  catch(Exception error) {
    print 'Failure using packer fmt.'
    throw error
  }
  print 'Packer fmt was successful.'
}

void inspect(String template, String bin = '/usr/bin/packer') {
  // input checking
  assert fileExists(template) : "A file does not exist at ${template}."

  // inspect the packer template
  try {
    sh(label: 'Packer Inspect', script: "${bin} inspect ${template}")
  }
  catch(Exception error) {
    print 'Failure inspecting the template.'
    throw error
  }
  print 'Packer inspect was successful'
}

void install(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  config.install_path = config.install_path ? config.install_path : '/usr/bin'
  assert (config.platform && config.version) : 'A required parameter ("platform" or "version") is missing from the packer.install method. Please consult the documentation for proper usage.'
  assert fileExists(config.install_path) : "The desired installation path at ${config.install_path} does not exist."

  // check if current version already installed
  if (fileExists("${config.install_path}/packer")) {
    String installed_version = sh(label: 'Check Packer Version', returnStdout: true, script: "${config.install_path}/packer version").trim()
    if (installed_version ==~ config.version) {
      print "Packer version ${config.version} already installed at ${config.install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().downloadFile("https://releases.hashicorp.com/packer/${config.version}/packer_${config.version}_${config.platform}.zip", 'packer.zip')
  unzip(zipFile: 'packer.zip', dir: config.install_path)
  new utils().removeFile('packer.zip')
  print "Packer successfully installed at ${config.install_path}/packer."
}

void plugin_install(String url, String install_loc) {
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
  new utils().downloadFile(url, install_loc)
  if (url ==~ /\.zip$/) {
    unzip(zipFile: install_loc)
    new utils().removeFile(install_loc)
  }
  else {
    sh(label: 'Packer Plugin Executable Permissions', script: "chmod ug+rx ${install_loc}")
  }
  print "Packer plugin successfully installed at ${install_loc}."
}

void validate(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.template : 'The required template parameter was not set.'
  assert fileExists(config.template) : "The template file or templates directory ${config.template} does not exist!"
  config.bin = config.bin ?: 'packer'

  // validate template with packer
  try {
    String cmd = "${config.bin} validate"

    // check for optional inputs
    if (config.var_file) {
      assert fileExists(config.var_file) : "The var file ${config.var_file} does not exist!"

      cmd += " -var_file=${config.var_file}"
    }
    if (config.var) {
      assert (config.var instanceof Map) : 'The var parameter must be a Map.'

      config.var.each() { var, value ->
        cmd += " -var ${var}=${value}"
      }
    }
    if (config.only) {
      assert (config.only instanceof List) : 'The only parameter must be a list of strings.'

      cmd += " -only=${config.only.join(',')}"
    }

    sh(label: 'Packer Validate', script: "${cmd} ${config.template}")
  }
  catch(Exception error) {
    print 'Failure using packer validate.'
    throw error
  }
  print 'Packer validate executed successfully.'
}
