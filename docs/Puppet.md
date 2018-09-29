# Puppet

Interacts with Puppet Enterprise Software Endpoints. This library is considered experimental and users are encouraged to file issues when and where they are found.

### Dependencies

- pipeline-utility-steps plugin

### puppet.code_deploy {}
Deploys code and data with the Puppet Enterprise Code Manager. If wait is set to `true`, errors returned by Code Manager will be returned and the pipeline will fatally error.

```groovy
puppet.code_deploy {
  bin = '/usr/bin/curl' // optional executable path for curl
  environments = ['development', 'production'] // optional environments to deploy (default is to deploy all environments)
  servers = ['puppet'] // optional server hosting code manager
  token = '/var/lib/jenkins/.puppetlabs/token' // rbac token for deploying with code manager
  wait = false // optional wait for code manager to finish deployment
}
```

### puppet.task {}
Triggers the execution of a Puppet Enterprise task.

```groovy
puppet.task {
  bin = '/usr/bin/curl' // optional executable path for curl
  description = 'my task' // optional description of the job
  environment = 'production' // optional environment to execute the task on (default is production)
  noop = true // optional execute task in noop (default is false)
  params = { "action":"install", "name":"httpd" } // optional json format input parameters (default is empty)
  scope = //TODO nodes string array, query string, app string, or node_group string
  server = 'https://puppet:8170' // optional server hosting puppet orchestrator
  task = 'package' // name of the task to execute
  token = '/var/lib/jenkins/.puppetlabs/token' // rbac token for executing tasks
}
```
