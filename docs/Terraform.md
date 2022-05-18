# Terraform

Interacts with Terraform. `env.TF_IN_AUTOMATION` is set to `true` for each method.

### Dependencies

- Terraform CLI binary executable >= 0.12

### terraform.apply()
Uses Terraform to apply a config. Note that if `terraform.plan(path: configDir)` was invoked before this and the `out` parameter was not specified, then the resultant plan file is in `${configDir}/plan.tfplan`. If a plan file is specified as the `configPath` parameter value, then the `vars` and `target` parameters will be ignored.

```groovy
terraform.apply(
  bin:        '/usr/bin/terraform', // optional path to terraform executable
  configPath: '/path/to/config_dir_or_plan_file', // path to config dir or plan file
  target:     ['aws_instance.example', 'aws_eip.ip'], // optional resource targets
  var:        ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile:    '/path/to/variables.tf' // optional location of variables file
)
```

### terraform.destroy()
Uses Terraform to destroy an applied config. Note that if `terraform.plan(path: configDir)` with `destroy: true` was invoked before this, then the resultant plan file is in `${configDir}/plan.tfplan`. If a plan file is specified as the `configPath` parameter value, then the vars and target parameters will be ignored.

```groovy
terraform.destroy(
  bin:        '/usr/bin/terraform', // optional path to terraform executable
  configPath: '/path/to/config_dir', // path to config dir or plan file
  target:     ['aws_instance.example', 'aws_eip.ip'], // optional resource targets
  var:        ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile:    '/path/to/variables.tf' // optional location of variables file
)
```

### terraform.fmt()
Uses Terraform to check for properly formatted code. Note that in Terraform 0.12.x the `recursive` option was added (Terraform < 0.12 automatically recursed through subdirectories). The `check` and `write` parameters are mutually exclusive, and so only one of them may be enabled at a time.

```groovy
terraform.fmt(
  bin:       '/usr/bin/terraform', // optional path to terraform executable
  check:     false, // optional check files within config dir and return an error if any files are not formatted correctly (cannot be used with `write`)
  diff:      false, // optional present a diff if any files within config dir are not formatted correctly
  dir:       '/path/to/working_config_dir', // path to working config dir
  recursive: false, // optional check subdirectories of config dir recursively
  write:     true // optional write changes directly to files that are not formatted directly (cannot be used with `check`)
)
```

### terraform.init()
Uses Terraform to initialize a working directory.

```groovy
terraform.init(
  backend:       true  // optional false to omit backend initialization
  backendConfig: ['/path/to/backend.hcl'] // optional paths to hcl files with backend configs
  bin:           '/usr/bin/terraform', // optional path to terraform executable
  dir:           '/path/to/working_config_dir', // path to working config dir
  pluginDir:     '/path/to/plugin_dir', // optional path to (presumably shared) plugin/provider installation directory
  upgrade:       false, // optional upgrade modules and plugins
)
```

### terraform.imports()
Imports existing infrastructure into your Terraform state.

```groovy
terraform.imports(
  bin:       '/usr/bin/terraform', // optional path to terraform executable
  dir:       '/path/to/config', // optional path to terraform config for provider
  resources: ['resource.name':'resource.id', 'aws_instance.this':'i-1234567890'], // resource name and id mappings to import
  provider:  'template', // optional specific provider for import
  state:     'terraform.tfstate', // optional path to the source state file
  var:       ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile:   '/path/to/variables.tf' // optional location of variables file
)
```

### terraform.output()
Reads an output variable from a Terraform state and prints the value.

```groovy
terraform.plan(
  bin:     '/usr/bin/terraform', // optional path to terraform executable
  display: false, // optional display outputs; else they will be returned as String from method
  json:    false, // optional json format output
  name:    'module.foo.server_ip_address', // optional output name
  state:   'terraform.tfstate', // optional path to the source state file
)
```

### terraform.plan()
Uses Terraform to generate an execution plan. The output plan file `plan.tfplan` will be written to the same directory as the input config directory if the `out` parameter is not specified. Otherwise, the output plan file will be written to the filesystem at the path specified in the `out` parameter. This is recommended practice to provide as an input in a Pipeline to a subsequent `apply` or `destroy` for various reasons.

```groovy
terraform.plan(
  bin:         '/usr/bin/terraform', // optional path to terraform executable
  destroy:     false, // optional generate a plan to destroy resources
  dir:         '/path/to/config_dir_or_plan_file', // path to config dir
  display:     false, // optional display plan output
  out:         'plan.tfplan', // optional plan output file path (extension must be .tfplan)
  refreshOnly: false, // optional check if remote objects match outcome of most recent apply (>= 0.15)
  replace:     ['aws_instance.example', 'aws_eip.ip'], // optional resources to unconditionally recreate in plan
  return:      false, // optional return plan output from method
  target:      ['aws_instance.example', 'aws_eip.ip'], // optional resource targets
  var:         ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile:     '/path/to/variables.tf' // optional location of variables file
)
```

### terraform.state()
Manipulate or display the Terraform state. The resources parameter should be `null` for a `push` or `list`, a list of strings for a `remove`, and a map of strings for a `move`.

```groovy
terraform.state(
  bin:       '/usr/bin/terraform', // optional path to terraform executable
  command:   'move', // state command; one of 'move', 'remove', 'list', or 'push'
  resources: ['resource.from':'resource.to', 'resource.other_from':'resource.other_to'], // resources to move
  resources: ['resource.one', 'resource.two'], // resources to remove
  state:     'terraform.tfstate' // optional path to read and save state
)
```

### terraform.taint()
Manually marks a resource as tainted. This forces a destroy and recreate on the next plan or apply.

```groovy
terraform.taint(
  bin:       '/usr/bin/terraform', // optional path to terraform executable
  resources: ['resource.name', 'other.name'], // names of the resources to taint
  state:     'terraform.tfstate' // optional path to read and save state
)
```

### terraform.validate()
Uses Terraform to validate a config directory.

```groovy
terraform.validate(
  bin:    '/usr/bin/terraform', // optional path to terraform executable
  dir:    '/path/to/config_dir', // path to config dir
  json:   false, // optional produce output in a machine-readable JSON format
  return: false, // optional return validate output from method (mostly useful with json: true)
)
```

### terraform.workspace()
Selects the Terraform workspace for a config directory. Ideally executed in Pipeline before other Terraform blocks.

```groovy
terraform.workspace(
  bin:       '/usr/bin', // optional location of terraform install
  dir:       '/path/to/config', // location of terraform config directory
  workspace: 'default' // terraform workspace to select
)
```
