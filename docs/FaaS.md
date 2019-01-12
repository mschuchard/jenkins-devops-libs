# FaaS

Interacts with OpenFaaS CLI.

### Dependencies

- pipeline-utility-steps plugin (`validate_template`)

### faas.build {}
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

### faas.deploy {}
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

### faas.install {}
Locally installs a specific version of the OpenFaaS CLI.

```groovy
faas.install {
  install_path = '/usr/bin' // optional location to install faas cli
  platform = 'linux' // platform where faas cli will be installed ['linux', 'linux-arm64', 'linux-armhf', 'darwin', 'windows']
  version = '0.5.1' // version of faas cli to install
}
```

### faas.login {}
Log in to the specified OpenFaaS gateway.

```groovy
faas.login {
  bin = '/usr/bin/faas-cli' // optional executable path for faas-cli
  gateway = 'http://127.0.0.1:8080' // gateway URL starting with http(s)://
  password = 'password' // gateway password
  user = 'username' // gateway username
  tls = true // enable or disable tls verification/validation
}
```

### faas.push {}
Pushes the OpenFaaS function container image(s) to a remote repository. These container images must already be present in your local image cache.

```groovy
faas.push {
  bin = '/usr/bin/faas-cli' // optional executable path for faas-cli
  filter = 'filter_string' // optional wildcard to match with function names in yaml file (default is unused)
  parallel = '1' // optional build in parallel to depth specified
  regex = 'regexp_string' // optional regex to match with function names in yaml file (default is unused)
  tag = 'latest' // override latest tag on function Docker image
  template = 'samples.yaml' // path to yaml file describing function(s)
}
```

### faas.remove {}
Removes/deletes deployed OpenFaaS functions.

```groovy
faas.remove {
  bin = '/usr/bin/faas-cli' // optional executable path for faas-cli
  filter = 'filter_string' // optional wildcard to match with function names in yaml file (default is unused)
  regex = 'regexp_string' // optional regex to match with function names in yaml file (default is unused)
  template = 'samples.yaml' // path to yaml file describing function(s)
}
```

### faas.validate_template(String template)
Validates template syntax.

```groovy
faas.validate_template('template.yaml')
```
