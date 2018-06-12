# FaaS

Interacts with OpenFaaS CLI.

### Dependencies

- pipeline-utility-steps plugin (`validate_template`)

### faas.build
Builds OpenFaaS function containers.

```groovy
faas.build {
  bin = '/usr/bin/faas-cli' // optional executable path for faas-cli
  filter = 'filter_string' // optional wildcard to match with function names in yaml file (default is unused)
  no_cache = false // optional do not use docker's build cache
  parallel = '1' // optional build in parallel to depth specified
  regex = 'regexp_string' // optional regex to match with function names in yaml file (default is unused)
  squash = false // optional use docker's squash flag for smaller images
  template = 'samples.yaml' // path to yaml file describing function(s)
}
```

### faas.deploy
Deploys OpenFaaS function containers.

```groovy
faas.deploy {
  bin = '/usr/bin/faas-cli' // optional executable path for faas-cli
  filter = 'filter_string' // optional wildcard to match with function names in yaml file (default is unused)
  label = 'canary=true' // optional label to set
  regex = 'regexp_string' // optional regex to match with function names in yaml file (default is unused)
  replace = true // optional replace any existing function
  secret = 'dockerhuborg' // optional secure secret to give function access to
  template = 'samples.yaml' // path to yaml file describing function(s)
  update = false // optional update existing functions
}
```

### faas.install
Locally installs a specific version of the OpenFaaS CLI.

```groovy
faas.install {
  install_path = '/usr/bin' // optional location to install faas cli
  platform = 'linux' // platform where faas cli will be installed ['linux', 'linux-arm64', 'linux-armhf', 'darwin', 'windows']
  version = '0.5.1' // version of faas cli to install
}
```

### faas.validate_template(String template)
Validates template syntax.

```groovy
faas.validate_template('template.yaml')
```
