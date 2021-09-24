# Packer

Interacts with Packer.The `template` argument must generally be a `pkr.json` template, `pkr.hcl` template, or a directory containing them.

### Dependencies

- Packer CLI binary executable >= 1.5

### packer.build()
Uses Packer to build an artifact from a template.

```groovy
packer.build(
  bin:      '/usr/bin/packer', // optional location of packer install
  force:    false, // optional force a build to continue if artifacts exist and deletes existing artifacts
  only:     ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to build
  on_error: "default", // optional "default" cleanup, "abort", "ask", or "run-cleanup-provisioner"
  template: '/path/to/template.pkr.json', // location of packer template(s)
  var:      ['foo':'bar', 'bar':'baz'], // optional variable setting
  var_file: '/path/to/variables.json' // optional location of variables file
)
```

### packer.fmt()
Uses Packer to check for properly canonically formatted code.

```groovy
packer.fmt(
  bin:      '/usr/bin/packer', // optional location of packer install
  check:     false, // optional check template and return an error if file is not formatted correctly (cannot be used with `write`)
  diff:      false, // optional present a diff if the template is not formatted correctly
  template: '/path/to/template_dir', // location of packer template(s)
  write:     true // optional write changes directly to files that are not formatted directly (cannot be used with `check`)
)
```

### packer.init()
Uses Packer to install all the missing plugins required in a Packer template directory.

```groovy
packer.init(
  bin:     '/usr/bin/packer', // optional location of packer install
  dir:     '/path/to/template_dir', // location of packer template directory
  upgrade: false // optional update installed plugins to the latest available version within the specified constraints
)
```

### packer.inspect(String template, String bin = '/usr/bin/packer')
Inspects a template and parses and outputs the components a template defines.

```groovy
packer.inspect('/path/to/template.pkr.json', '/usr/local/bin/packer')
```

### packer.install()
**Deprecated**:
Please use software provisioning, configuration management, or containerized build agents instead. This method will be removed completely in 2.0.0.

Locally installs a specific version of Packer.

```groovy
packer.install(
  install_path: '/usr/bin', // optional location to install packer
  platform:     'linux_amd64', // platform where packer will be installed
  version:      '1.6.6.' // version of packer to install
)
```

### packer.plugin_install(String url, String install_loc)
**Deprecated**:
Please use software provisioning, configuration management, or containerized build agents instead. This method will be removed completely in 2.0.0.

Locally installs a Packer plugin. Note that these plugins need to either be installed in an executable path, or in the same directory as `packer`, and need to be named appropriately. The remotely stored plugin should also be zipped or an executable.

```groovy
packer.plugin_install('https://github.com/YaleUniversity/packer-provisioner-goss/releases/download/v1.4.0/packer-provisioner-goss-v1.4.0-linux-amd64', '/usr/bin/packer-provisioner-goss')
```

### packer.validate()
Uses Packer to validate a build template.

```groovy
packer.validate(
  bin:      '/usr/bin/packer', // optional location of packer install
  only:     ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to validate
  template: '/path/to/template.pkr.hcl', // location of packer template(s)
  var:      ['foo':'bar', 'bar':'baz'], // optional variable setting
  var_file: '/path/to/variables.json' // optional location of variables file
)
```
