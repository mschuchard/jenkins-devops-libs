# Packer

Interacts with Packer. The `template` argument must generally be a `pkr.json` template, `pkr.hcl` template, or a directory containing Packer templates and configs.

### Dependencies

- Packer CLI binary executable >= 1.7

### packer.build()
Uses Packer to build an artifact from a template or template directory.

```groovy
packer.build(
  bin:      '/usr/bin/packer', // optional location of packer install
  except:   ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to ignore during build (mutually exclusive with only)
  force:    false, // optional force a build to continue if artifacts exist and deletes existing artifacts
  only:     ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to build (mutually exclusive with except)
  onError:  'default', // optional 'default' cleanup, 'abort', 'ask', or 'run-cleanup-provisioner'
  template: '/path/to/template.pkr.json', // location of packer template file or templates directory
  var:      ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile: '/path/to/variables.json' // optional location of variables file
)
```

### packer.fmt()
Uses Packer to check for properly canonically formatted code. This method will return a `Boolean` type indicating whether the format check was successful (`true`) or not (`false`). Note that if `check` is `false` then the return will always be `true`.

```groovy
packer.fmt(
  bin:       '/usr/bin/packer', // optional location of packer install
  check:     false, // optional check template and return an error if file is not formatted correctly (cannot be used with `write`)
  diff:      false, // optional present a diff if the template is not formatted correctly
  recursive: false, // optional also process files in subdirectories
  template:  '/path/to/template_dir', // location of packer templates directory
  write:     true // optional write changes directly to files that are not formatted directly (cannot be used with `check`)
)
```

### packer.init()
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

### packer.parse(String template)
Provides a thin wrapper around [HCL4j](https://github.com/bertramdev/hcl4j) for inputting a Packer template or config, and returning a `Map` representing the parsed HCL2. Note this requires local installation of the HCL4j dependency, and therefore the agent must have sufficient permissions to do so.

```groovy
parsedMap = packer.parse('/path/to/template.pkr.hcl')
```

### packer.pluginsInstall()
Uses Packer to install the most recent compatible Packer plugin matching the version constraint. When the version parameter is omitted, then the most recent version will be installed. `packer.init()` with a config file is generally recommended instead of this method.

```groovy
packer.pluginsInstall(
  bin:     '/usr/bin/packer', // optional location of packer install
  plugin:  'github.com/hashicorp/happycloud',
  force:   false, // optional force reinstallation of plugins
  version: 'v1.2.3', // optional version of plugin to install
)
```

### packer.pluginsRemove()
Uses Packer to remove all Packer plugins matching the version constraint for the current OS and architecture. When the version parameter is omitted all installed versions will be removed. `packer.init()` with a config file is generally recommended instead of this method.

```groovy
packer.pluginsRemove(
  bin:     '/usr/bin/packer', // optional location of packer install
  plugin:  'github.com/hashicorp/happycloud',
  version: 'v1.2.3', // optional version of plugin to install
)
```

### packer.plugins()
Uses Packer to interact with plugins and display information about them.

```groovy
packer.plugins(
  bin:     '/usr/bin/packer', // optional location of packer install
  command: 'installed', // one of 'installed' or 'required'
  dir:     '/path/to/template_dir', // location of directory with packer config (required for 'required' command)
)
```

### packer.validate()
**`evalData` and `warnUndeclVar` require Packer version >= 1.8.5**

Uses Packer to validate a build template or template directory. This method will return a `Boolean` type indicating whether the validation was successful (`true`) or not (`false`).

```groovy
packer.validate(
  bin:           '/usr/bin/packer', // optional location of packer install
  evalData:      false, // optional evaluate datasources during validation
  except:   ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to ignore during build (mutually exclusive with only)
  only:     ['source.*.foo', 'source.bar.*', 'baz'], // optional builder names to build (mutually exclusive with except)
  syntaxOnly:    false, // optional only check syntax and do not verify config
  template:      '/path/to/template.pkr.hcl', // // location of packer template file or templates directory
  var:           ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile:       '/path/to/variables.json' // optional location of variables file
  warnUndeclVar: true, // optional warn on user variable files containing undeclared variables
)
```
