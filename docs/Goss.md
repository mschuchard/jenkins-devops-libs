# GoSS

Interacts with GoSS.

### Dependencies

- `pipeline-utility-steps plugin` (`validateGossfile`)
- GoSS CLI binary executable >= 0.3.0
- DGoSS CLI binary exeuctable (`validateDocker`)

### goss.render()
Renders a single valid GoSSfile from separated individual test files. This method will return the rendered GoSSfile as a String, and does not perform any further actions with the rendered content.

```groovy
goss.render(
  bin:        '/usr/bin/goss', // optional executable path for goss
  debug:      false, // optional print rendered golang template prior to gossfile
  gossfile:   'goss.yaml', // optional location of gossfile with included directive
  package:    null, // optional package type to use (apk, dpkg, pacman, rpm)
  vars:       'vars.yaml', // optional YAML or JSON vars file to use with gossfile
  varsInline: ['name':'value', 'name2':'value2'] // optional inline vars to use with gossfile (overwrites 'vars')
)
```

### goss.server()
Creates a persistent REST API endpoint with GoSS.

```groovy
goss.server(
  bin:        '/usr/bin/goss', // optional executable path for goss
  cache:      '5s', // optional time to cache the results
  endpoint:   '/healthz', // optional endpoint to expose
  format:     'rspecish', // optional formatter to use for output
  formatOpts: 'perfdata', // optional extra formatter options (perfdata, pretty, or verbose)
  gossfile:   'goss.yaml', // optional location of gossfile
  logLevel:   'info', // optional logging verbosity level; one of 'error', 'warn', 'info', 'debug', or 'trace'
  maxConcur:  '50', // optional maximum number of tests to run concurrently
  package:    null, // optional package type to use (apk, dpkg, pacman, rpm)
  port:       '8080', // optional specified port to listen on
  vars:       'vars.yaml', // optional YAML or JSON vars file to use with gossfile
  varsInline: ['name':'value', 'name2':'value2'] // optional inline vars to use with gossfile (overwrites 'vars')
)
```

### goss.validate()
Locally executes a `gossfile` with GoSS. This method will return a `Boolean` type indicating whether the validation was successful (`true`) or not (`false`).

```groovy
goss.validate(
  bin:          '/usr/bin/goss', // optional executable path for goss
  format:       'rspecish', // optional formatter to use for output
  formatOpts:   'perfdata', // optional extra formatter options (perfdata, pretty, sort, or verbose)
  gossfile:     'goss.yaml', // optional location of gossfile
  logLevel:   'info', // optional logging verbosity level; one of 'error', 'warn', 'info', 'debug', or 'trace'
  maxConcur:    '50', // optional maximum number of tests to run concurrently
  package:      null, // optional package type to use (apk, dpkg, pacman, rpm)
  retryTimeout: '0s', // optional retry on failure so long as elapsed + `sleep` time is less than this value
  sleep:        '1s', // optional time to sleep between retries (ignored unless `retryTimeout` also specified)
  vars:         'vars.yaml', // optional YAML or JSON vars file to use with gossfile
  varsInline:   ['name':'value', 'name2':'value2'] // optional inline vars to use with gossfile (overwrites 'vars')
)
```

### goss.validateDocker()
Locally executes a `gossfile` in a Docker container with DGoSS.
Note that dgoss [environment variables](https://github.com/aelsabbahy/goss/tree/master/extras/dgoss#environment-vars-and-defaults) should be set in the `environment` block of a `Jenkinsfile` and will not be provided as as part of the interface to this method. That is also the process for providing arguments to goss when running inside the dgoss wrapper, so goss arguments cannot be directly interfaced in this method. Also note that dgoss runs a container, but does not stop the running container, so you may want to wrap the code inside a `Image.withRun{}` block for safety.

```groovy
goss.validateDocker(
  bin:   '/usr/bin/dgoss', // optional executable path for dgoss
  flags: ['JENKINS_OPTS':'--httpPort=8080 --httpsPort=-1', 'JAVA_OPTS':'-Xmx1048m'], // optional flags for container run
  image: 'alpine:latest' // docker image to run container from
)
```

### goss.validateGossfile(String gossfile)
Validates `gossfile` syntax. This method will return a `Boolean` type indicating whether the validation was successful (`true`) or not (`false`).

```groovy
goss.validateGossfile('gossfile.yaml')
```
