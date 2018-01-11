# FaaS

Interacts with FaaS.

### Dependencies

- pipeline-utility-steps plugin (`validate_template`)

### faas.install
Locally installs a specific version of FaaS CLI.

```groovy
faas.install {
  install_path = '/usr/bin' // optional location to install faas cli
  platform = '' // platform where faas cli will be installed ['linux', 'linux-arm64', 'linux-armhf', 'darwin', 'windows']
  version = '0.5.1' // version of faas cli to install
}
```
