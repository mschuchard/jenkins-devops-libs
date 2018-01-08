# Terraform

Interacts with Terraform.

### Dependencies

- unzip package (`install`)
- pipeline-utility-steps plugin (`install`)
- devops.common.utils

### terraform.apply(String config_path, String bin = 'terraform')
Uses Terraform to apply a config.

```groovy
terraform.apply('path/to/config_dir_or_plan_file')
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
  version = '0.10.8' // version of terraform to install
}
```

### terraform.plan(String dir, String bin = 'terraform')
Uses Terraform to generate an execution plan. The plan file `plan.tfplan` will be written to the same directory as the input config directory. This is mostly useful in a Pipeline for validating the config set and then speeding up a subsequent `apply` by providing an input plan file.

```groovy
terraform.plan(String '/path/to/config_dir')
```

### terraform.validate(String dir, String bin = 'terraform')
Uses Terraform to validate a config directory.

```groovy
terraform.plan(String '/path/to/config_dir')
```
