# Terraform

Interacts with Terraform. `env.TF_IN_AUTOMATION` is set to `true` for each method.

### Dependencies

- unzip package (`install`)
- pipeline-utility-steps plugin (`install`)

### terraform.apply {}
Uses Terraform to apply a config. Note that if `terraform.plan { path = config_dir }` was invoked before this, the resulting plan file is in `config_dir/plan.tfplan`. If a plan file is specified as the `config_path`, then vars cannot be specified.

```groovy
terraform.apply {
  bin         = '/usr/bin/terraform' // optional path to terraform executable
  config_path = '/path/to/config_dir_or_plan_file' // path to config dir or plan file
  target      = ['aws_instance.example', 'aws_eip.ip'] // optional resource targets
  var         = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file    = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.destroy {}
Uses Terraform to destroy an applied config. Note that if `terraform.plan { path = config_dir }` with `destroy = true` was invoked before this, the resulting plan file is in `config_dir/plan.tfplan`.

```groovy
terraform.destroy {
  bin         = '/usr/bin/terraform' // optional path to terraform executable
  config_path = '/path/to/config_dir' // path to config dir or plan file
  target      = ['aws_instance.example', 'aws_eip.ip'] // optional resource targets
  var         = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file    = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.fmt {}
Uses Terraform to check for properly formatted code. Note that in Terraform
0.12.x the `recursive` option was added (Terraform < 0.12 automatically
recursed through subdirectories). The `check` and `write` parameters are
mutually exclusive, so only one of them may be enabled at a time.

```groovy
terraform.fmt {
  bin        = '/usr/bin/terraform' // optional path to terraform executable
  check      = true // optional check files within config dir and return an error if any files are not formatted correctly (cannot be used with `write`)
  diff       = true // optional present a diff if any files within config dir are not formatted correctly
  dir        = '/path/to/working_config_dir' // path to working config dir
  recursive  = false // optional check subdirectories of config dir recursively (only available in Terraform 0.12 and greater)
  write      = false // optional write changes directly to files that are not formatted directly (cannot be used with `check`)
}
```

### terraform.init {}
Uses Terraform to initialize a working directory.

```groovy
terraform.init {
  bin        = '/usr/bin/terraform' // optional path to terraform executable
  dir        = '/path/to/working_config_dir' // path to working config dir
  plugin_dir = '/path/to/plugin_dir' // optional path to (presumably shared) plugin/provider installation directory
  upgrade    = false // optional upgrade modules and plugins
}
```

### terraform.imports {}
Imports existing infrastructure into your Terraform state.

```groovy
terraform.imports {
  bin       = '/usr/bin/terraform' // optional path to terraform executable
  dir       = '/path/to/config' // optional path to terraform config for provider
  resources = ['resource.name', 'other.name'] // names of the resources to import
  provider  = 'template' // optional specific provider for import
  state     = 'terraform.tfstate' // optional path to the source state file
  var       = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file  = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.install {}
Locally installs a specific version of Terraform.

```groovy
terraform.install {
  install_path = '/usr/bin' // optional location to install terraform
  platform     = 'linux_amd64' // platform where terraform will be installed
  version      = '0.11.7' // version of terraform to install
}
```

### terraform.plan {}
Uses Terraform to generate an execution plan. The plan file `plan.tfplan` will be written to the same directory as the input config directory. This is mostly useful in a Pipeline for validating the config set and then speeding up a subsequent `apply` or `destroy` by providing an input plan file.

```groovy
terraform.plan {
  bin      = '/usr/bin/terraform' // optional path to terraform executable
  destroy  = false // optional generate a plan to destroy resources
  dir      = '/path/to/config_dir_or_plan_file' // path to config dir
  display  = false // optional display plan output
  target   = ['aws_instance.example', 'aws_eip.ip'] // optional resource targets
  var      = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.plugin_install {}
Locally installs a Terraform plugin. Note that these plugins need to be named appropriately in the nomenclature of `terraform-provider-NAME_vX.Y.Z`. The remotely stored plugin should also be zipped or an executable.

```groovy
terraform.plugin_install {
  install_loc  = '~/.terraform.d/plugins' // optional path to install plugin into
  install_name = 'terraform-provisioner-foo_v1.0.0' // post-install name of plugin
  url          = 'https://github.com/org/terraform-provisioner-foo/releases/download/v1.0.0/terraform-provisioner-foo-v1.0.0-linux-amd64' // url to retrieve plugin from
}
```

### terraform.state {}
Manipulate the Terraform state. The resources parameter should be `null` for a `push`, an array of strings for a `remove`, and an array of two element arrays of strings for a `move`.

```groovy
terraform.state {
  bin       = '/usr/bin/terraform' // optional path to terraform executable
  cmd       = 'move' // state command; one of 'move', 'remove', or 'push'
  resources = [['resource.from', 'resource.to'], ['resource.other_from', 'resource.other_to']] // resources to move
  resources = ['resource.one', 'resource.two'] // resources to remove
  state     = 'terraform.tfstate' // optional path to read and save state
}
```

### terraform.taint {}
Manually marks a resource as tainted. This forces a destroy and recreate on the next plan or apply.

```groovy
terraform.taint {
  bin       = '/usr/bin/terraform' // optional path to terraform executable
  module    = 'my-module' // optional module path where the resource lives
  resources = ['resource.name', 'other.name'] // names of the resources to taint
  state     = 'terraform.tfstate' // optional path to read and save state
}
```

### terraform.validate {}
Uses Terraform to validate a config directory. Note that Terraform >= 0.12 does not allow variables to be input or checked in this method, and those parameters will be ignored for that version range.

```groovy
terraform.validate {
  bin        = '/usr/bin/terraform' // optional path to terraform executable
  check_vars = true // optional check whether all required variables have been specified
  dir        = '/path/to/config_dir' // path to config dir
  var        = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file   = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.workspace {}
Selects the Terraform workspace for a config directory. Ideally executed in Pipeline before other Terraform blocks.

```groovy
terraform.workspace {
  bin       = '/usr/bin' // optional location of terraform install
  dir       = '/path/to/config' // location of terraform config directory
  workspace = 'default' // terraform workspace to select
}
```
