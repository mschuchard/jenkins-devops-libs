# Puppet Enterprise

Interacts with Puppet Enterprise endpoints. This library is considered experimental and users are encouraged to file issues when and where they are found.

### Dependencies

- `http_request` plugin
- Puppet Enterprise installation

### puppet.codeDeploy()
Deploys code and data with the Puppet Enterprise Code Manager. If wait is set to `true`, then errors returned by Code Manager will be returned and cause the pipeline to fatally error.

```groovy
puppet.codeDeploy(
  credentialsId: 'pe_token', // token bindings credentials id for rbac token; mutually exclusive with token
  environments:  ['development', 'production'], // optional environments to deploy (default is to deploy all environments)
  port:          8170, // optional code manager api endpoint port
  servers:       ['puppet'], // optional servers hosting code manager
  tokenFile:     '/var/lib/jenkins/.puppetlabs/token', // rbac token file location for deploying with code manager; mutually exclusive with credentialsId
  wait:          false // optional wait for code manager to finish deployment
)
```

### puppet.plan()
Triggers the execution of a Puppet plan via the Puppet Enterprise Orchestrator.

```groovy
puppet.planRun(
  credentialsId: 'pe_token', // token bindings credentials id for rbac token; mutually exclusive with token
  description:   'my plan run', // optional description of the job
  environment:   'production', // optional environment to load the plan from (default is production)
  params:        ['nodes':['node1.example.com', 'node2.example.com']], // optional parameters for the plan to use
  planName:      'canary', // name of the plan to run
  port:          8143, // optional orchestrator api endpoint port
  server:        'puppet', // optional server hosting puppet orchestrator
  timeout:       3600, // optional maximum number of seconds allowed for the plan to run
  tokenFile:     '/var/lib/jenkins/.puppetlabs/token', // rbac token file location for deploying with code orchestrator; mutually exclusive with credentialsId
  userdata:      ['foo':'bar'] // optional arbitrary key/value data supplied to the job
)
```

### puppet.task()
Triggers the execution of a Puppet Enterprise task via the Puppet Enterprise Orchestrator.

```groovy
puppet.task(
  credentialsId: 'pe_token', // token bindings credentials id for rbac token; mutually exclusive with token
  description:   'my task', // optional description of the job
  environment:   'production', // optional environment to execute the task on (default is production)
  noop:          true, // optional execute task in noop (default is false)
  params:        ['action':'install', 'name':'httpd'], // optional input parameters (default is empty)
  port:          8170, // optional orchestrator api endpoint port
  scope:         ['node1.example.com', 'node2.example.com'], // scope for deployment (if string, will be passed as `node_group` or `application`; if array of strings, will be passed as `nodes` or `query`; internal logic attempts to correctly determine which)
  server:        'puppet', // optional server hosting puppet orchestrator
  task:          'package', // name of the task to execute
  tokenFile:     '/var/lib/jenkins/.puppetlabs/token' // rbac token file location for deploying with orchestrator; mutually exclusive with credentialId
)
```

### puppet.token()
Generates a RBAC token for use with Puppet Enterprise endpoints, and saves it as a file in the default location (`~/.puppetlabs/token`). Recommended to use `withCredentials` bindings for `usernamePassword` in conjunction with this.

```groovy
puppet.token(
  password: 'password', // password for the rbac token
  path:     '$HOME/.puppetlabs', // optional path to save rbac token to
  port:     4433, // optional puppet server api endpoint port
  secure:   true, // optional verify ssl connection
  server:   'puppet', // optional server hosting puppet server
  username: 'username' // username for the rbac token
)
```
