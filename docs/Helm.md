# Helm

Interacts with Helm. Note that you should set the environment variable `KUBECONFIG` in your pipeline with `environment { KUBECONFIG = '/path/to/.kube/config' }` as the `jenkins` user probably does not have one in its home directory, and Helm requires a valid kube config for all commands. Alternatively, you can use the `kubeconfigFile` or `kubeconfigContent` bindings for the Credentials Binding plugin, and then wrap code within a `withCredentials` block as per normal. Also alternatively, you can combine the two like `environment { KUBECONFIG = credentials('my-kubeconfig') }`

Also please note direct Kubernetes support will never exist in this library. The reason for this is that the Kubernetes Java client requires exactly specifying the API version and object type altered during modifications. This limitation is not encountered in the Golang client and its associated tools (e.g. `kubectl`), and the reason is [well explained here](https://github.com/kubernetes-client/java/issues/611#issuecomment-509106822). This makes the implementation significantly more cumbersome. The workaround for this has already been excellently implemented by Fabric8 in their library, so no compelling reason exists to attempt to implement something similar in this library.

### Dependencies

- Helm CLI binary executable >= 3.0
- Kubernetes cluster

### helm.history()
Prints historical revisions for a given release.

```groovy
helm.history(
  bin:          '/usr/bin/helm', // optional executable path for helm
  context:      'default', // optional kube-context from kube config
  max:          256, // optional maximum number of revisions to include in history (default 256)
  name:         'happy-panda', // required name for the release object
  namespace:    'default', // optional namespace for the installed release object
  outputFormat: 'table', // optional format for output (table, json, or yaml)
  return:       false, // optional return history output from method as String
)
```

### helm.install()
Performs an installation with Helm onto the Kubernetes cluster.

```groovy
helm.install(
  atomic:    false, // optional deletes the installation on failure
  bin:       '/usr/bin/helm', // optional executable path for helm
  chart:     'chart', // chart repository, local archive, directory, or url to install
  context:   'default', // optional kube-context from kube config
  createNS:  false, // optional create the release namespace if not present
  devel:     false, // optional also use development versions (mutually exclusive with version)
  dryRun:    false, // optional simulate an install
  force:     false, // optional force resource updates through replacement strategy
  name:      'happy-panda', // required name for the installed release object
  namespace: 'default', // optional namespace for the installed release object
  set:       ['key1':'val1', 'key2':'val2'], // optional set input values
  values:    ['config.yaml'], // optional value overrides yaml file or url
  verify:    true, // optional verify the provenance of the chart
  version:   'latest', // optional chart version to install (mutually exclusive with devel)
  wait:      false, // optional wait until everything is in a ready state
)
```

### helm.lint()
Runs a series of tests to verify that the chart is well-formed.

```groovy
helm.lint(
  bin:           '/usr/bin/helm', // optional executable path for helm
  chart:         'chart', // chart repository, local archive, directory, or url to install
  context:       'default', // optional kube-context from kube config
  namespace:     'default', // optional namespace for the installed release object
  set:           ['foo':'bar', 'bar':'baz'], // optional value override
  strict:        false // optional fail on warnings
  values:        ['config.yaml'], // optional value overrides yaml file or url
  withSubcharts: false, // optional lin dependent charts
)
```

### helm.packages()
Package a chart directory into a chart archive.

```groovy
helm.packages(
  bin:        '/usr/bin/helm', // optional executable path for helm
  chart:      'path/to/chart', // absolute or relative path to chart
  dest:       '.', // optional location to write the chart
  key:        'foo', // optional sign the package with this key name (mutually exclusive with keyring)
  keyring:    '/home/dir/.gnupg/pubring.gpg', // optional sign the package with the public keyring at this location (mutually exclusive with key)
  updateDeps: false, // optional update dependencies from requirements prior to packaging
  version:    '1.0.0' // optional version set for the chart
)
```

### helm.plugin()
Manage client-side Helm plugins.

```groovy
helm.plugin(
  bin:     '/usr/bin/helm', // optional executable path for helm
  command: 'install', // plugin command; one of 'install', 'list', 'uninstall', or 'update'
  plugin:  'https://github.com/adamreese/helm-env' // targeted plugin (unless 'list' command)
)
```

### helm.push
Upload a chart to a registry.

```groovy
helm.push(
  bin:      '/usr/bin/helm', // optional executable path for helm
  chart:    'helm-chart-1.0.0.tgz', // absolute or relative path to packaged chart
  insecure: false, // optional skip tls certificate checks for the chart upload
  remote:   'oci://my.registry.com/', // address of the remote chart registry
)
```

### helm.registryLogin
Authenticate to a remote registry.

```groovy
helm.registryLogin(
  bin:      '/usr/bin/helm', // optional executable path for helm
  host:     'https://helm.registry.com', // registry host address
  insecure: false, // optional allow connections to TLS registry without certs
  password: 'pass', // registry password or identity token
  username: 'user', // registry username
)
```

### helm.repo()
Add and update a Helm chart repository. The repository will update if it has already been added.

```groovy
helm.repo(
  bin:      '/usr/local/bin/helm', // optional executable path
  ca:       '/path/to/crt.ca', // optional path to CA bundle to verify certificates of HTTPS servers
  cert:     '/path/to/ca.crt', // optional path to HTTPS client SSL certificate file
  force:    false, // optional replace/overwrite the repo if it exists
  insecure: false, // optional skip tls certificate checks
  key:      '/path/to/rsa.key', // optional path to HTTPS client SSL key file
  password: 'mypassword', // optional chart repository password
  repo:     'stable', // name of the chart repository
  url:      'https://kubernetes-charts.storage.googleapis.com', // url of the chart repository
  user:     'myuser' // optional chart repository username
)
```

### helm.rollback()
Roll back the release object to a previous release with Helm.

```groovy
helm.rollback(
  bin:          '/usr/local/bin/helm', // optional executable path for helm
  context:      'default', // optional kube-context from kube config
  force:        false, // optional force resource update through delete/recreate if needed
  hooks:        true, // optional hooks run during rollback
  name:         'happy-panda', // release object name to rollback
  namespace:    'default', // optional namespace for the rolled back release object
  recreatePods: false, // optional performs pods restart for the resource if applicable
  version:      'previous' // optional version of release-object to rollback to
)
```

### helm.show()
Show information about a chart.

```groovy
helm.show(
  bin:   '/usr/local/bin/helm', // optional executable path for helm
  chart: 'chart', // chart repository, local archive, directory, or url to display
  info:  'all', // info to display; one of 'all', 'chart', 'crds', 'readme', or 'values'
)
```

### helm.status()
Shows the status of a named release.

```groovy
helm.status(
  bin:       '/usr/bin/helm', // optional executable path for helm
  name:      'happy-panda', // name for the release object to be queried
  context:   'default' // optional kube-context from kube config
  namespace: 'default' // optional namespace for the queried release object
)
```

### helm.test()
Executes the tests for a release.

```groovy
helm.test(
  bin:       '/usr/bin/helm', // optional executable path for helm
  cleanup:   false, // optional delete test pods upon completion
  context:   'default', // optional kube-context from kube config
  kubectl:   '/usr/bin/kubectl', // optional executable path for kubectl
  name:      'happy-panda', // name of a deployed release
  namespace: 'default' // optional namespace for the queried release object
  parallel:  false // optional run test pods in parallel
)
```

### helm.uninstall()
Uninstall the release object from Kubernetes with Helm.

```groovy
helm.uninstall(
  bin:       '/usr/bin/helm', // optional executable path for helm
  name:      'happy-panda', // name for the release object to be deleted
  context:   'default' // optional kube-context from kube config
  namespace: 'default' // optional namespace for the uninstalled release object
)
```

### helm.upgrade()
Updates and/or changes the configuration of a release with Helm.

```groovy
helm.upgrade(
  atomic:    false, // optional rolls back changes made in case of failed upgrade
  bin:       '/usr/bin/helm', // optional executable path for helm
  chart:     'chart', // chart repository, local archive, directory, or url to upgrade
  context:   'default', // optional kube-context from kube config
  createNS:  false, // optional create the release namespace if not present and install occurs
  devel:     false, // optional also use development versions (mutually exclusive with version)
  dryRun:    false, // optional simulate an upgrade
  force:     false, // optional force resource updates through replacement strategy
  install:   false, // optional install if release not already present
  name:      'happy-panda', // name of the upgraded release object
  namespace: 'default', // optional namespace for the upgraded release object
  set:       ['key1':'val1', 'key2':'val2'], // optional set input values
  values:    ['config.yaml'], // optional value overrides yaml file or url
  verify:    true, // optional verify the provenance of the chart
  version:   'latest', // optional chart version to install (mutually exclusive with devel)
  wait:      false, // optional wait until everything is in a ready state
)
```

### helm.verify(String chartPath, String helmPath = 'helm')
Verify that the given chart has a valid provenance file. This can be used to verify a local chart. This method will return a `Boolean` type indicating whether the verification was successful (`true`) or not (`false`).

```groovy
helm.verify('/path/to/chart.tar.gz', '/usr/bin/helm')
```
