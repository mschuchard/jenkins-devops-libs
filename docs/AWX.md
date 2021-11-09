# AWX/Ansible Tower

Interacts with AWX/Ansible Tower endpoints via the AWX CLI. Note that you should set the environment variable `TOWER_OAUTH_TOKEN` and `TOWER_HOST` in your pipeline with `environment { TOWER_OAUTH_TOKEN = '6E5SXhld7AMOhpRveZsLJQsfs9VS8U' }` for targeting and authentication. Alternatively, you can use the Credentials Binding plugin for the token, and then wrap code within a `withCredentials` block as per normal. Also alternatively, you can use the plugin with the environment directive like `environment { TOWER_OAUTH_TOKEN = credentials('tower-oauth-token') }`. This library is considered experimental and users are encouraged to file issues when and where they are found.

### Dependencies

- AWX CLI binary executable
- AWX or Tower installation

### awx.hostCreate()

Uses AWX to create a host in an inventory.

```groovy
awx.hostCreate(
  bin:         '/usr/bin/awx', // optional path to awx executable
  description: 'my host', // optional description of the host
  enabled:     true, // optional is host available and online for running jobs
  inventory:   'my_inventory', // ID of the associated inventory
  name:        'foo.bar.com', // name of the host
  variables:   ['foo': 'bar', 'baz': 1] // optional host variables
)
```

### awx.hostDelete()

Uses AWX to delete a host in an inventory.

```groovy
awx.hostDelete('foo.bar.com', '/usr/local/bin/awx') // the ID (or unique name) of the host for first argument
```

### awx.inventoryCreate()

Uses AWX to create an inventory.

```groovy
awx.inventoryCreate(
  bin:          '/usr/bin/awx', // optional path to awx executable
  description:  'an inventory', // optional description of this inventory
  hostFilter:   'myhosts*', // optional filter that will be applied to the hosts of this inventory
  name:         'my_inventory', // name of this inventory
  organization: 'my_org', // organization containing this inventory
  smart:        false, // optional smart inventory
  variables:    ['foo': 'bar', 'baz': 1] // optional inventory variables
)
```

### awx.inventoryDelete()

Uses AWX to delete an inventory.

```groovy
awx.inventoryDelete('my_inventory', '/usr/local/bin/awx') // the ID (or unique name) of the inventory for first argument
```

### awx.inventoryModify()

Uses AWX to modify an inventory.

```groovy
awx.inventoryModify(
  bin:          '/usr/bin/awx', // optional path to awx executable
  description:  'an inventory', // optional description of this inventory
  hostFilter:   'myhosts*', // optional filter that will be applied to the hosts of this inventory
  name:         'my_inventory', // name of this inventory
  organization: 'my_org', // organization containing this inventory
  smart:        false, // optional smart inventory
  variables:    ['foo': 'bar', 'baz': 1] // optional inventory variables
)
```

### awx.jobTemplateLaunch()

Uses AWX to launch a job from a job template.

```groovy
awx.jobTemplateLaunch(
  bin:        '/usr/bin/awx', // optional path to awx executable
  extraVars: ['foo': 'bar', 'baz': 1], // optional extra variables
  id:         5, // job template id
  inventory:  2, // optional ID of the associated inventory
  jobType:   'run', // optional job type (run or check)
  limit:      'hosts*.com', // optional host limit
  monitor:    false, // optional wait until launched job finishes
  skipTags:  ['skipper', 'to_skip'] // optional tags to skip
)
```

### awx.projectsUpdate()

Uses AWX to launch a project update job.

```groovy
awx.projectsUpdate(
  bin:     '/usr/bin/awx', // optional path to awx executable
  id:      3, // project id
  monitor: false // optional wait until project update job finishes
)
```

### awx.workflowJobTemplateLaunch()

Uses AWX to launch a workflow job from a workflow job template.

```groovy
awx.jobTemplateLaunch(
  bin:        '/usr/bin/awx', // optional path to awx executable
  extraVars: ['foo': 'bar', 'baz': 1], // optional extra variables
  id:         5, // workflow job template id
  inventory:  2, // optional ID of the associated inventory
  monitor:    false, // optional wait until launched job finishes
)
```
