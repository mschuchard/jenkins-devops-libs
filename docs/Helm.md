# Helm

Interacts with Helm. Note that you should set the environment variable `KUBECONFIG` in your pipeline with `environment { KUBECONFIG = '/path/to/.kube/config' }` as the `jenkins` user probably does not have one in its home directory and helm requires a valid kube config for all commands.

### Dependencies

- tar package (`setup`)
- pipeline-utility-steps plugin (`setup`)

### helm.delete {}
Delete the release object from Kubernetes with helm.

```groovy
helm.delete {
  bin     = '/usr/bin/helm' // optional executable path for helm
  name    = 'happy-panda' // name for the release object to be deleted
  context = 'default' // optional kube-context from kube config
}
```

### helm.install {}
Performs an installation with helm onto the Kubernetes cluster.

```groovy
helm.install {
  bin       = '/usr/bin/helm' // optional executable path for helm
  chart     = 'chart' // chart repository, local archive, directory, or url to install
  context   = 'default' // optional kube-context from kube config
  name      = 'happy-panda' // optional name for the installed release object
  namespace = 'default' // optional namespace for the installed release object
  values    = ['config.yaml'] // optional value overrides yaml file or url
  verify    = true // verify the provenance of the chart
  set       = ['foo=bar', 'bar=baz'] // optional value override
}
```

### helm.lint {}
Runs a series of tests to verify that the chart is well-formed.

```groovy
helm.lint {
  bin       = '/usr/bin/helm' // optional executable path for helm
  chart     = 'chart' // chart repository, local archive, directory, or url to install
  context   = 'default' // optional kube-context from kube config
  namespace = 'default' // optional namespace for the installed release object
  values    = ['config.yaml'] // optional value overrides yaml file or url
  set       = ['foo=bar', 'bar=baz'] // optional value override
  strict    = false // optional fail on warnings
}
```

### helm.package {}
Package a chart directory into a chart archive.

```groovy
helm.package {
  bin         = '/usr/bin/helm' // optional executable path for helm
  chart       = 'path/to/chart' // absolute or relative path to chart
  dest        = '.' // optional location to write the chart
  key         = 'foo' // optional sign the package with this key name (mutually exclusive with keyring)
  keyring     = '/home/dir/.gnupg/pubring.gpg' // optional sign the package with the public keyring at this location (mutually exclusive with key)
  update_deps = false // optional update dependencies from requirements prior to packaging
  version     = '1.0.0' // optional version set for the chart
}
```

### helm.rollback {}
Roll back the release object to a previous release with helm.

```groovy
helm.rollback {
  bin     = '/usr/local/bin/helm' // optional executable path for helm
  context = 'default' // optional kube-context from kube config
  name    = 'happy-panda' // release object name to rollback
  version = '1' // version of release-object to rollback to
}
```

### helm.setup(String version, String install_path = '/usr/bin')
Locally installs a specific version of helm and then initializes helm and installs tiller. If helm is already installed at the specified version, then helm is initialized for the jenkins user if it has not been already. It is strongly recommended to manage this with a software provisioner instead, but this can be helpful for quick one-offs. Also, it is sometimes necessary to initialize helm for the jenkins user, and this will rectify that situation.

```groovy
helm.setup('2.9.1', '/usr/local/bin')
```

### helm.test {}
Runs the tests for a release. If the tests fail and `cleanup` is false, then the logs for the test pods will be displayed (this is still undergoing functionality verification/testing) and then the pods will be deleted (both by `kubectl`).

```groovy
helm.test {
  bin      = '/usr/bin/helm' // optional executable path for helm
  cleanup  = false // optional delete test pods upon completion
  context  = 'default' // optional kube-context from kube config
  kubectl  = '/usr/bin/kubectl' // optional executable path for kubectl
  name     = 'happy-panda' // name of a deployed release
  parallel = false // optional run test pods in parallel
}
```

### helm.upgrade {}
Updates and/or changes the configuration of a release with helm.

```groovy
helm.upgrade {
  bin       = '/usr/bin/helm' // optional executable path for helm
  chart     = 'chart' // chart repository, local archive, directory, or url to upgrade
  context   = 'default' // optional kube-context from kube config
  name      = 'happy-panda' // name of the upgraded release object
  namespace = 'default' // optional namespace for the upgraded release object
  values    = ['config.yaml'] // optional value overrides yaml file or url
  verify    = true // verify the provenance of the chart
  set       = ['foo=bar', 'bar=baz'] // optional value override
}
```
