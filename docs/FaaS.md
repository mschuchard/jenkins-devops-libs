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
  pull:     false, // optional force re-pull of base images
  regex:    'regexp_string', // optional regex to match with function names in yaml file (default is unused)
  squash:   false, // optional use docker's squash flag for smaller images
  tag:      'latest', // optional tag override for function image
  template: 'samples.yaml' // path to yaml file describing function(s)
)
```

### faas.deploy()
Deploys OpenFaaS function containers.

```groovy
faas.deploy(
  bin:       '/usr/bin/faas-cli', // optional executable path for faas-cli
  filter:    'filter_string', // optional wildcard to match with function names in yaml file (default is unused)
  gateway:   'http://127.0.0.1:8080', // optional gateway url with protocol
  label:     ['canary':'true', 'dev':'false'], // optional labels to set
  namespace: 'default', // optional namespace of the function
  regex:     'regexp_string', // optional regex to match with function names in yaml file (default is unused)
  replace:   true, // optional replace any existing function (mutually exclusive with update)
  secret:    'dockerhuborg', // optional secure secret to give function access to
  template:  'samples.yaml', // path to yaml file describing function(s)
  tls:       true // optional TLS validation
  update:    false // optional update existing functions (mutually exclusive with replace)
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
  gateway:     'http://127.0.0.1:8080', // optional gateway url with protocol
  header:      ['X-Callback-Url':'http://gateway:8080/function/send2slack', 'X-Ping-Url':'http://request.bin/etc'], // optional HTTP request headers
  method:      'POST', // optional HTTP request method
  namespace:   'default', // optional namespace of the function
  query:       ['repo':'faas-cli', 'org':'openfaas'], // optional queries for request
  stdin:       'image.png', // optional stdin for function to receive
  tls:         true // optional TLS validation
)
```

### faas.list()
List OpenFaaS functions. The output function list and information is also returned by this method as a `String`.

```groovy
faas.list(
  bin:       '/usr/bin/faas-cli', // optional executable path for faas-cli
  gateway:   'http://127.0.0.1:8080', // optional gateway url with protocol
  namespace: 'default', // optional namespace of the function
  quiet:     false, // optional display only the function's id (mutually exclusive with verbose)
  sort:      'name', // optional sort category (valid: 'name' or 'invocations')
  tls:       true // optional TLS validation
  verbose:   false, // optional display extra function information (mutually exclusive with quiet)
)
```

### faas.login()
Log in to the specified OpenFaaS gateway.

```groovy
faas.login(
  bin:      '/usr/bin/faas-cli', // optional executable path for faas-cli
  gateway:  'http://127.0.0.1:8080', // optional gateway url with protocol
  password: 'password', // gateway password
  user:     'admin', // optional gateway username
  tls:      true // optional TLS validation
)
```

### faas.logs()
Fetch logs for a given function name. The logs are returned by this method as a `String`.

```groovy
faas.logs(
  bin:       '/usr/bin/faas-cli', // optional executable path for faas-cli
  gateway:   'http://127.0.0.1:8080', // optional gateway url with protocol
  instance:  false, // optional print the function instance name/id
  name:      'sadpanda', // function name for which to retrieve logs
  namespace: 'default', // optional namespace of the function
  format:    'plain', // optional return format (plain, keyvalue, or json)
  since:     '5s', // optional return logs newer than relative duration
  tls:       true // optional TLS validation
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
  bin:       '/usr/bin/faas-cli', // optional executable path for faas-cli
  filter:    'filter_string', // optional wildcard to match with function names in yaml file (default is unused)
  gateway:   'http://127.0.0.1:8080', // optional gateway url with protocol
  namespace: 'default', // optional namespace of the function
  regex:     'regexp_string', // optional regex to match with function names in yaml file (default is unused)
  template:  'samples.yaml' // path to yaml file describing function(s)
  tls:       true // optional TLS validation
)
```

### faas.validateTemplate(String template)
Validates template syntax. This method will return a `Boolean` type indicating whether the validation was successful (`true`) or not (`false`).

```groovy
faas.validateTemplate('template.yaml')
```
