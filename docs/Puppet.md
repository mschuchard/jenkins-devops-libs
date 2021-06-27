# Puppet Enterprise

Interacts with Puppet Enterprise endpoints. This library is considered experimental and users are encouraged to file issues when and where they are found.

### Dependencies

- pipeline-utility-steps plugin
- http_request plugin

### puppet.code_deploy()
Deploys code and data with the Puppet Enterprise Code Manager. If wait is set to `true`, errors returned by Code Manager will be returned and cause the pipeline to fatally error.

```groovy
puppet.code_deploy(
  credentials_id: 'pe_token', // token bindings credentials id for rbac token; mutually exclusive with token
  environments:   ['development', 'production'], // optional environments to deploy (default is to deploy all environments)
  servers:        ['puppet'], // optional server hosting code manager
  tokenFile:      '/var/lib/jenkins/.puppetlabs/token', // rbac token file location for deploying with code manager; mutually exclusive with credential_id
  wait:           false // optional wait for code manager to finish deployment
)
```

### puppet.task()
Triggers the execution of a Puppet Enterprise task via the Puppet Enterprise Orchestrator.

```groovy
puppet.task(
  credentials_id: 'pe_token', // token bindings credentials id for rbac token; mutually exclusive with token
  description:    'my task', // optional description of the job
  environment:    'production', // optional environment to execute the task on (default is production)
  noop:           true, // optional execute task in noop (default is false)
  params:         ['action':'install', 'name':'httpd'], // optional input parameters (default is empty)
  scope:          ['node1.example.com', 'node2.example.com'], // scope for deployment (if string, will be passed as `node_group` or `application`; if array of strings, will be passed as `nodes` or `query`; internal logic attempts to correctly determine which)
  server:         'puppet', // optional server hosting puppet orchestrator
  task:           'package', // name of the task to execute
  tokenFile:      '/var/lib/jenkins/.puppetlabs/token' // rbac token file location for deploying with code manager; mutually exclusive with credential_id
)
```

### puppet.token()
Generates a RBAC token for use with Puppet Enterprise endpoints, and saves it as a file in the default location (`~/.puppetlabs/token`). Recommended to use `withCredentials` bindings for `usernamePassword` in conjunction with this.

```groovy
puppet.token(
  password: 'password', // password for the rbac token
  path:     '$HOME/.puppetlabs', // optional path to save rbac token to
  secure:   true, // optional verify ssl connection
  server:   'puppet', // optional server hosting puppet server
  username: 'username' // username for the rbac token
)
```
