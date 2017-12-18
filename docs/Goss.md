# Goss

Interacts with Goss.

### Dependencies

- pipeline-utility-steps (`validate_gossfile`)

### goss.validate {}
Locally executes a gossfile with goss.

```groovy
goss.validate {
  gossfile = 'goss.yaml' // optional location of gossfile
  format = 'rspecish' // optional formatter to use for output
  path = '/usr/bin/goss' // optional executable path for goss
  vars = 'vars.yaml' // optional vars file to use with gossfile
}
```

### goss.install(String version, String install_path = '/usr/bin')
Locally installs a specific version of goss.

```groovy
goss.install('0.3.5', '/usr/local/bin/')
```

### goss.server{}
Creates a persistent REST API endpoint with goss.

```groovy
goss.server {
  endpoint = '/healthz' // optional endpoint to expose
  gossfile = 'goss.yaml' // optional location of gossfile
  format = 'rspecish' // optional formatter to use for output
  port = '8080' // optional specified port to listen on
  path = '/usr/bin/goss' // optional executable path for goss
  vars = 'vars.yaml' // optional vars file to use with gossfile
}
```

### goss.validate_gossfile(String gossfile)
Validates gossfile syntax.

```groovy
goss.validate_gossfile('gossfile.yaml')
```
