# Packer

Interacts with Packer.

### Dependencies

- unzip package (`install`)
- pipeline-utility-steps plugin (`install`)

### packer.build()
Uses Packer to build an artifact from a template.

```groovy
packer.build(
  bin:      '/usr/bin/packer', // optional location of packer install
  only:     ['foo', 'bar', 'baz'], // optional builder names to build
  template: '/path/to/template.json', // location of packer template
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
  template: '/path/to/template.json', // location of packer template
  write:     false // optional write changes directly to files that are not formatted directly (cannot be used with `check`)
)
```

### packer.inspect(String template, String bin = '/usr/bin/packer')
Inspects a template and parses and outputs the components a template defines.

```groovy
packer.inspect('/path/to/template.json', '/usr/local/bin/packer')
```

### packer.install()
Locally installs a specific version of Packer.

```groovy
packer.install(
  install_path: '/usr/bin', // optional location to install packer
  platform:     'linux_amd64', // platform where packer will be installed
  version:      '1.4.5' // version of packer to install
)
```

### packer.plugin_install(String url, String install_loc)
Locally installs a Packer plugin. Note that these plugins need to either be installed in an executable path, or in the same directory as `packer`, and need to be named appropriately. The remotely stored plugin should also be zipped or an executable.

```groovy
packer.plugin_install('https://github.com/YaleUniversity/packer-provisioner-goss/releases/download/v0.3.0/packer-provisioner-goss-v0.3.0-linux-amd64', '/usr/bin/packer-provisioner-goss')
```

### packer.validate()
Uses Packer to validate a build template.

```groovy
packer.validate(
  bin:      '/usr/bin/packer', // optional location of packer install
  only:     ['foo', 'bar', 'baz'], // optional builder names to build
  template: '/path/to/template.json', // location of packer template
  var:      ['foo':'bar', 'bar':'baz'], // optional variable setting
  var_file: '/path/to/variables.json' // optional location of variables file
)
```
