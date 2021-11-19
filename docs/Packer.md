# Packer

Interacts with Packer. The `template` argument must generally be a `pkr.json` template, `pkr.hcl` template, or a directory containing Packer templates and configs.

### Dependencies

- Packer CLI binary executable >= 1.5

### packer.build()
Uses Packer to build an artifact from a template.

```groovy
packer.build(
  bin:      '/usr/bin/packer', // optional location of packer install
  force:    false, // optional force a build to continue if artifacts exist and deletes existing artifacts
  only:     ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to build
  onError:  "default", // optional "default" cleanup, "abort", "ask", or "run-cleanup-provisioner"
  template: '/path/to/template.pkr.json', // location of packer template file or templates directory
  var:      ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile: '/path/to/variables.json' // optional location of variables file
)
```

### packer.fmt()
Uses Packer to check for properly canonically formatted code.

```groovy
packer.fmt(
  bin:      '/usr/bin/packer', // optional location of packer install
  check:    false, // optional check template and return an error if file is not formatted correctly (cannot be used with `write`)
  diff:     false, // optional present a diff if the template is not formatted correctly
  template: '/path/to/template_dir', // location of packer templates directory
  write:    true // optional write changes directly to files that are not formatted directly (cannot be used with `check`)
)
```

### packer.init()
**Requires Packer version >= 1.7**

Uses Packer to install all the missing plugins required in a Packer template directory.

```groovy
packer.init(
  bin:     '/usr/bin/packer', // optional location of packer install
  dir:     '/path/to/template_dir', // location of packer templates directory
  upgrade: false // optional update installed plugins to the latest available version within the specified constraints
)
```

### packer.inspect(String template, String bin = '/usr/bin/packer')
Inspects a template and parses and outputs the components a template defines.

```groovy
packer.inspect('/path/to/template.pkr.json', '/usr/local/bin/packer')
```

### packer.validate()
Uses Packer to validate a build template.

```groovy
packer.validate(
  bin:      '/usr/bin/packer', // optional location of packer install
  only:     ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to validate
  template: '/path/to/template.pkr.hcl', // // location of packer template file or templates directory
  var:      ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile: '/path/to/variables.json' // optional location of variables file
)
```
