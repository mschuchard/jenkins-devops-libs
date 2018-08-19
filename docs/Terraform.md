# Terraform

Interacts with Terraform.

### Dependencies

- unzip package (`install`)
- pipeline-utility-steps plugin (`install`)

### terraform.apply {}
Uses Terraform to apply a config. Note that if `terraform.plan { path = config_dir }` was invoked before this, the resulting plan file is in `config_dir/plan.tfplan`.

```groovy
terraform.apply {
  bin = '/usr/bin/terraform' // optional path to terraform executable
  config_path = '/path/to/config_dir_or_plan_file' // path to config dir or plan file
  target = ['aws_instance.example', 'aws_eip.ip'] // optional resource targets
  var = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.destroy {}
Uses Terraform to destroy an applied config.

```groovy
terraform.destroy {
  bin = '/usr/bin/terraform' // optional path to terraform executable
  dir = '/path/to/config_dir' // path to config dir
  target = ['aws_instance.example', 'aws_eip.ip'] // optional resource targets
  var = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.init(String dir, String bin = 'terraform')
Uses Terraform to initialize a working directory.

```groovy
terraform.init('/path/to/working_config_dir')
```

### terraform.install {}
Locally installs a specific version of Terraform.

```groovy
terraform.install {
  install_path = '/usr/bin' // optional location to install terraform
  platform = 'linux_amd64' // platform where terraform will be installed
  version = '0.11.7' // version of terraform to install
}
```

### terraform.plan {}
Uses Terraform to generate an execution plan. The plan file `plan.tfplan` will be written to the same directory as the input config directory. This is mostly useful in a Pipeline for validating the config set and then speeding up a subsequent `apply` by providing an input plan file.

```groovy
terraform.plan {
  bin = '/usr/bin/terraform' // optional path to terraform executable
  dir = '/path/to/config_dir_or_plan_file' // path to config dir
  var = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.plugin_install(String url, String install_name)
Locally installs a Terraform plugin. Note that these plugins need to be named appropriately in the nomenclature of `terraform-provider-NAME_vX.Y.Z`. The remotely stored plugin should also be zipped or an executable.

```groovy
terraform.plugin_install('https://github.com/org/terraform-provisioner-foo/releases/download/v1.0.0/terraform-provisioner-foo-v1.0.0-linux-amd64', 'terraform-provisioner-foo_v1.0.0')
```

### terraform.validate {}
Uses Terraform to validate a config directory.

```groovy
terraform.validate {
  bin = '/usr/bin/terraform' // optional path to terraform executable
  dir = '/path/to/config_dir' // path to config dir
  target = ['aws_instance.example', 'aws_eip.ip'] // optional resource targets
  var = ['foo=bar', 'bar=baz'] // optional variable setting
  var_file = '/path/to/variables.tf' // optional location of variables file
}
```

### terraform.workspace {}
Selects the Terraform workspace for a config directory. Ideally executed in Pipeline before other Terraform blocks.

```groovy
terraform.workspace {
  bin = '/usr/bin' // optional location of terraform install
  dir = '/path/to/config' // location of terraform config directory
  workspace = 'default' // terraform workspace to select
}
```
