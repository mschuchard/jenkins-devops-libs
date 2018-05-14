# Helm

Interacts with Helm. Note that you should set the environment variable `KUBECONFIG` in your pipeline with `environment { KUBECONFIG = '/path/to/.kube/config' }` as the `jenkins` user probably does not have one in its home directory and helm requires a valid kube config.

### Dependencies

- tar package (`setup`)
- pipeline-utility-steps plugin (`setup`)

### helm.delete(String 'release-object', String bin = 'helm')
Delete the release object from Kubernetes with helm.

```groovy
helm.delete('happy-panda')
```

### helm.install {}
Performs an installation with helm onto the Kubernetes cluster.

```groovy
helm.install {
  bin = '/usr/bin/helm' // optional executable path for helm
  chart = 'chart' // chart repository, local archive, directory, or url to install
  name = 'happy-panda' // optional name for the installed release object
  values = 'config.yaml' // optional value overrides yaml file
  set = 'foo=bar' // optional value override
}
```

### helm.rollback {}
Roll back the release object to a previous release with helm.

```groovy
helm.rollback {
  bin = '/usr/local/bin/helm' // optional executable path for helm
  name = 'happy-panda' // release object name to rollback
  version = '1' // version of release-object to rollback to
}
```

### helm.setup(String version, String install_path = '/usr/bin')
Locally installs a specific version of helm and then initializes helm and tiller.

```groovy
helm.setup('2.8.2', '/usr/local/bin')
```

### helm.upgrade {}
Updates and/or changes the configuration of a release with helm.

```groovy
helm.upgrade {
  bin = '/usr/bin/helm' // optional executable path for helm
  chart = 'chart' // chart repository, local archive, directory, or url to upgrade
  name = 'happy-panda' // name of the upgraded release object
  values = 'config.yaml' // optional value overrides yaml file
  set = 'foo=bar' // optional value override
}
```
