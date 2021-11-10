# Goss

Interacts with Goss.

### Dependencies

- pipeline-utility-steps plugin (`validateGossfile`)
- GoSS CLI binary executable
- DGoSS CLI binary exeuctable (`validateDocker`)

### goss.install(String version, String installPath = '/usr/bin')
**Deprecated**:
Please use software provisioning, configuration management, or containerized build agents instead. This method will be removed completely in 2.0.0.

Locally installs a specific version of goss.

```groovy
goss.install('0.3.13', '/usr/local/bin/')
```

### goss.installDgoss(String version, String installPath = '/usr/bin')
**Deprecated**:
Please use software provisioning, configuration management, or containerized build agents instead. This method will be removed completely in 2.0.0.

Locally installs a specific version of dgoss.

```groovy
goss.installDgoss('0.3.13', '/usr/local/bin/')
```

### goss.server()
Creates a persistent REST API endpoint with goss.

```groovy
goss.server(
  bin:      '/usr/bin/goss', // optional executable path for goss
  endpoint: '/healthz', // optional endpoint to expose
  gossfile: 'goss.yaml', // optional location of gossfile
  format:   'rspecish', // optional formatter to use for output
  port:     '8080', // optional specified port to listen on
  vars:     'vars.yaml' // optional vars file to use with gossfile
)
```

### goss.validate()
Locally executes a gossfile with goss.

```groovy
goss.validate(
  bin:      '/usr/bin/goss', // optional executable path for goss
  gossfile: 'goss.yaml', // optional location of gossfile
  format:   'rspecish', // optional formatter to use for output
  vars:     'vars.yaml' // optional vars file to use with gossfile
)
```

### goss.validateDocker()
Locally executes a gossfile in a Docker container with dgoss.
Note that dgoss [environment variables](https://github.com/aelsabbahy/goss/tree/master/extras/dgoss#environment-vars-and-defaults) should be set in the `environment` block of a `Jenkinsfile` and will not be provided as as part of the interface to this method. That is also the process for providing arguments to goss when running inside the dgoss wrapper, so goss arguments cannot be directly interfaced in this method. Also note that dgoss runs a container, but does not stop the running container, so you may want to wrap the code inside a `Image.withRun{}` block for safety.

```groovy
goss.validateDocker(
  bin:   '/usr/bin/dgoss', // optional executable path for dgoss
  flags: ['JENKINS_OPTS':'--httpPort=8080 --httpsPort=-1', 'JAVA_OPTS':'-Xmx1048m'], // optional flags for container run
  image: 'alpine:latest' // docker image to run container from
)
```

### goss.validateGossfile(String gossfile)
Validates gossfile syntax.

```groovy
goss.validateGossfile('gossfile.yaml')
```
