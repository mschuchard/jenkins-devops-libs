# Terraform

Interacts with Terraform. `env.TF_IN_AUTOMATION` is set to `true` for each method. Note that OpenTofu can also be used with these by assigning a value to the `bin` parameter for each method that is the path to the OpenTofu binary executable.

### Dependencies

- Terraform CLI binary executable >= 1.0

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
  dir:       env.WORKSPACE, // optional path to working config dir
  recursive: false, // optional check subdirectories of config dir recursively
  write:     true // optional write changes directly to files that are not formatted directly (cannot be used with `check`)
)
```

### terraform.graph()
Uses Terraform to produce a representation of the dependency graph between different objects in the current configuration and state. The resulting DOT graph is written as `graph.gv` (GraphViz extension) in the current working directory when this method is invoked.

```groovy
terraform.graph(
  bin:        '/usr/bin/terraform', // optional path to terraform executable
  dir:        '/path/to/working_config_dir', // optional path to working config dir (mutually exclusive with plan)
  drawCycles: false, // optional highlight any cycles in the graph with colored edges
  plan:       'plan.tfplan', // optional path to plan file for rendering (mutually exclusive with dir)
  type:       'plan', // optional type of graph to output; valid arguments are plan, plan-refresh-only, plan-destroy, or apply (apply is default if plan parameter also specified)
)
```

### terraform.init()
Uses Terraform to initialize a working directory.

```groovy
terraform.init(
  backend:       true // optional false to omit backend initialization
  backendConfig: ['/path/to/backend.hcl'] // optional paths to hcl files with backend configs
  backendKV:     ['address':'demo.consul.io', 'scheme':'https'], // optional key-value pairs for backend settings
  bin:           '/usr/bin/terraform', // optional path to terraform executable
  dir:           env.WORKSPACE, // optional path to working config dir
  forceCopy:     false, // optional suppress prompts about copying state data when initializating a new state backend
  migrateState:  false, // optional reconfigure a backend and attempt to migrate any existing state
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
Reads an output variable from a Terraform state and returns the value as a String.

```groovy
terraform.output(
  bin:     '/usr/bin/terraform', // optional path to terraform executable
  dir:     env.WORKSPACE, // optional path to config dir
  display: false, // optional display outputs
  json:    false, // optional JSON format String return
  name:    'module.foo.server_ip_address', // optional output name
  state:   'terraform.tfstate', // optional path to the source state file
)
```

### terraform.parse(String template)
Provides a thin wrapper around [HCL4j](https://github.com/bertramdev/hcl4j) for inputting a Terraform config, and returning a `Map` representing the parsed HCL2. Note this requires local installation of the HCL4j dependency, and therefore the agent must have sufficient permissions to do so.

```groovy
parsedMap = terraform.parse('/path/to/config.tf')
```

### terraform.plan()
Uses Terraform to generate an execution plan. The output plan file `plan.tfplan` will be written to the same directory as the input config directory if the `out` parameter is not specified. Otherwise, the output plan file will be written to the filesystem at the path specified in the `out` parameter. This is recommended practice to provide as an input in a Pipeline to a subsequent `apply` or `destroy` for various reasons.

```groovy
terraform.plan(
  bin:         '/usr/bin/terraform', // optional path to terraform executable
  destroy:     false, // optional generate a plan to destroy resources
  dir:         env.WORKSPACE, // optional path to config dir
  display:     false, // optional display plan output
  genConfig:   'config.tf', // optional hcl generation for import blocks (>= 1.5)
  out:         'plan.tfplan', // optional plan output file path (extension must be .tfplan)
  refreshOnly: false, // optional check if remote objects match outcome of most recent apply (>= 0.15)
  replace:     ['aws_instance.example', 'aws_eip.ip'], // optional resources to unconditionally recreate in plan
  return:      false, // optional return plan output from method as String
  target:      ['aws_instance.example', 'aws_eip.ip'], // optional resource targets
  var:         ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile:     '/path/to/variables.tf' // optional location of variables file
)
```

### terraform.providers(String rootDir, String bin = 'terraform')
Prints out a tree of modules in the referenced configuration annotated with their provider requirements.

```groovy
terraform.providers('/path/to/root_module_dir')
```

### terraform.refresh()
Update the state file of your infrastructure with metadata that matches the physical resources they are tracking.

```groovy
terraform.refresh(
  bin:     '/usr/bin/terraform', // optional path to terraform executable
  dir:     env.WORKSPACE, // optional path to config dir
  target:  ['aws_instance.example', 'aws_eip.ip'], // optional resource targets
  var:     ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile: '/path/to/variables.tf' // optional location of variables file
)
```

### terraform.state()
Manipulate or display the Terraform state. The resources parameter should be `null` for a `push` or `list`, a list of strings for a `remove`, and a map of strings for a `move`.

```groovy
terraform.state(
  bin:       '/usr/bin/terraform', // optional path to terraform executable
  command:   'move', // state command; one of 'move', 'remove', 'list', or 'push'
  dir:       env.WORKSPACE, // optional path to config dir
  resources: ['resource.from':'resource.to', 'resource.other_from':'resource.other_to'], // resources to move
  resources: ['resource.one', 'resource.two'], // resources to remove
  state:     'terraform.tfstate' // optional path to read and save state
)
```

### terraform.taint()
Manually marks a resource as tainted. This forces a destroy and recreate on the next plan or apply. Note this is generally deprecated in favor of the `replace` parameter in the `plan` method.

```groovy
terraform.taint(
  bin:       '/usr/bin/terraform', // optional path to terraform executable
  dir:       env.WORKSPACE, // optional path to config dir
  resources: ['resource.name', 'other.name'], // names of the resources to taint
  state:     'terraform.tfstate' // optional path to read and save state
)
```

### terraform.test()
Uses Terraform to execute experimental automated integration testing of shared modules. **This method usage will correlate to a recent version of Terraform as this subcommand changes greatly between versions.**

```groovy
terraform.test(
  bin:      '/usr/bin/terraform', // optional path to terraform executable
  cloudRun: 'app.terraform.io/:ORG/:MODULE_NAME/:PROVIDER', // optional source of a private module in a registry to execute tests remotely against via terraform cloud
  dir:      env.WORKSPACE, // optional path to config dir
  filter:   ['machine.tf', 'network.tf'], // optional list of test files to execute
  json:     false, // optional produce output in a machine-readable JSON format
  return:   false, // optional return test output from method (mostly useful with json: true)
  testDir:  'tests', // optional terraform test directory
  var:      ['foo':'bar', 'bar':'baz'], // optional variable setting
  varFile:  '/path/to/variables.tf' // optional location of variables file
  verbose:  false, // optional print plan or state for each test as it executes
)
```

### terraform.validate()
Uses Terraform to validate a config directory. **This subcommand's usage varies greatly between different versions of Terraform, and therefore not all parameters may be supported in your utilized version.**

```groovy
terraform.validate(
  bin:     '/usr/bin/terraform', // optional path to terraform executable
  dir:     env.WORKSPACE, // optional path to config dir
  json:    false, // optional produce output in a machine-readable JSON format
  return:  false, // optional return validate output from method (mostly useful with json: true)
  testDir: 'tests', // optional terraform test directory
  tests:   true, // optional validate test files
)
```

### terraform.workspace()
**`create` requires version >= 1.4**

Selects the Terraform workspace for a config directory. Ideally executed in Pipeline before other Terraform blocks.

```groovy
terraform.workspace(
  bin:       '/usr/bin', // optional location of terraform install
  create:    false, // optionally create the workspace if it does not exist
  dir:       env.WORKSPACE, // optional location of terraform config directory
  workspace: 'default' // terraform workspace to select
)
```
