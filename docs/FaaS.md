# FaaS

Interacts with OpenFaaS CLI.

### Dependencies

- pipeline-utility-steps plugin (`validateTemplate`)
- OpenFaaS CLI binary executable

### faas.build()
Builds OpenFaaS function containers.

```groovy
faas.build(
  bin:      '/usr/bin/faas-cli', // optional executable path for faas-cli
  filter:   'filter_string', // optional wildcard to match with function names in yaml file (default is unused)
  noCache:  false, // optional do not use docker's build cache
  parallel: '1', // optional build in parallel to depth specified
  regex:    'regexp_string', // optional regex to match with function names in yaml file (default is unused)
  squash:   false, // optional use docker's squash flag for smaller images
  template: 'samples.yaml' // path to yaml file describing function(s)
)
```

### faas.deploy()
Deploys OpenFaaS function containers.

```groovy
faas.deploy(
  bin:      '/usr/bin/faas-cli', // optional executable path for faas-cli
  filter:   'filter_string', // optional wildcard to match with function names in yaml file (default is unused)
  label:    ['canary':'true', 'dev':'false'], // optional labels to set
  regex:    'regexp_string', // optional regex to match with function names in yaml file (default is unused)
  replace: true, // optional replace any existing function
  secret:   'dockerhuborg', // optional secure secret to give function access to
  template: 'samples.yaml', // path to yaml file describing function(s)
  update:   false // optional update existing functions
)
```

### faas.invoke()
Invokes an OpenFaaS function.

```groovy
faas.invoke(
  async:       false, // optional invoke the function asynchronously
  bin:         '/usr/bin/faas-cli', // optional executable path for faas-cli
  contentType: 'text/plain', // optional content-type HTTP header
  function:    'echo', // name of the deployed function
  header:      ['X-Callback-Url':'http://gateway:8080/function/send2slack', 'X-Ping-Url':'http://request.bin/etc'], // optional HTTP request headers
  method:      'POST', // optional HTTP request method
  query:       ['repo':'faas-cli', 'org':'openfaas'], // optional queries for request
  stdin:       'image.png', // optional stdin for function to receive
  tls:         true // optional TLS validation (enabled by default)
)
```

### faas.login()
Log in to the specified OpenFaaS gateway.

```groovy
faas.login(
  bin:      '/usr/bin/faas-cli', // optional executable path for faas-cli
  gateway:  'http://127.0.0.1:8080', // gateway URL starting with http(s)://
  password: 'password', // gateway password
  user:     'username', // gateway username
  tls:      true // enable or disable tls verification/validation
)
```

### faas.push()
Pushes the OpenFaaS function container image(s) to a remote repository. These container images must already be present in your local image cache.

```groovy
faas.push(
  bin:      '/usr/bin/faas-cli', // optional executable path for faas-cli
  filter:   'filter_string', // optional wildcard to match with function names in yaml file (default is unused)
  parallel: '1', // optional build in parallel to depth specified
  regex:    'regexp_string', // optional regex to match with function names in yaml file (default is unused)
  tag:      'latest', // override latest tag on function Docker image
  template: 'samples.yaml' // path to yaml file describing function(s)
)
```

### faas.remove()
Removes/deletes deployed OpenFaaS functions.

```groovy
faas.remove(
  bin:      '/usr/bin/faas-cli', // optional executable path for faas-cli
  filter:   'filter_string', // optional wildcard to match with function names in yaml file (default is unused)
  regex:    'regexp_string', // optional regex to match with function names in yaml file (default is unused)
  template: 'samples.yaml' // path to yaml file describing function(s)
)
```

### faas.validateTemplate(String template)
Validates template syntax.

```groovy
faas.validateTemplate('template.yaml')
```
