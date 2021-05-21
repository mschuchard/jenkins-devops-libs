# AWX/Ansible Tower

Interacts with AWX/Ansible Tower endpoints via the AWX CLI. Note that you should set the environment variable `TOWER_OAUTH_TOKEN` and `TOWER_HOST` in your pipeline with `environment { TOWER_OAUTH_TOKEN = '6E5SXhld7AMOhpRveZsLJQsfs9VS8U' }` for targeting and authentication. Alternatively, you can use the Credentials Binding plugin for the token, and then wrap code within a `withCredentials` block as per normal. Also alternatively, you can use the plugin with the environment directive like `environment { TOWER_OAUTH_TOKEN = credentials('tower-oauth-token') }`. This library is considered experimental and users are encouraged to file issues when and where they are found.

### Dependencies

None

### awx.host_create()

Uses AWX to create a host in an inventory.

```groovy
awx.host_create(
  bin:         '/usr/bin/awx', // optional path to awx executable
  description: 'my host', // optional description of the host
  enabled:     true, // optional is host available and online for running jobs
  inventory:   'my_inventory', // ID of the associated inventory
  name:        'foo.bar.com', // name of the host
  variables:   ['foo': 'bar', 'baz': 1] // optional host variables
)
```

### awx.host_delete()

Uses AWX to delete a host in an inventory.

```groovy
awx.host_delete('foo.bar.com', '/usr/local/bin/awx') // the ID (or unique name) of the host for first argument
```

### awx.job_template_launch()

Uses AWX to launch a job from a job template.

```groovy
awx.job_template_launch(
  bin:        '/usr/bin/awx', // optional path to awx executable
  extra_vars: ['foo': 'bar', 'baz': 1], // optional extra variables
  id:         5, // job template id
  inventory:  2, // optional ID of the associated inventory
  job_type:   'run', // optional job type (run or check)
  limit:      'hosts*.com', // optional host limit
  monitor:    false, // optional wait until launched job finishes
  skip_tags:  ['skipper', 'to_skip'] // optional tags to skip
)
```
