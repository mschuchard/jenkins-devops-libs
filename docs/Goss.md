# Goss

Interacts with Goss.

### Dependencies

- pipeline-utility-steps plugin (`validate_gossfile`)

### goss.install(String version, String install_path = '/usr/bin')
Locally installs a specific version of goss.

```groovy
goss.install('0.3.5', '/usr/local/bin/')
```

### goss.install_dgoss(String version, String install_path = '/usr/bin')
Locally installs a specific version of dgoss.

```groovy
goss.install_dgoss('0.3.5', '/usr/local/bin/')
```

### goss.server{}
Creates a persistent REST API endpoint with goss.

```groovy
goss.server {
  bin = '/usr/bin/goss' // optional executable path for goss
  endpoint = '/healthz' // optional endpoint to expose
  gossfile = 'goss.yaml' // optional location of gossfile
  format = 'rspecish' // optional formatter to use for output
  port = '8080' // optional specified port to listen on
  vars = 'vars.yaml' // optional vars file to use with gossfile
}
```

### goss.validate {}
Locally executes a gossfile with goss.

```groovy
goss.validate {
  bin = '/usr/bin/goss' // optional executable path for goss
  gossfile = 'goss.yaml' // optional location of gossfile
  format = 'rspecish' // optional formatter to use for output
  vars = 'vars.yaml' // optional vars file to use with gossfile
}
```

### goss.validate_gossfile(String gossfile)
Validates gossfile syntax.

```groovy
goss.validate_gossfile('gossfile.yaml')
```
