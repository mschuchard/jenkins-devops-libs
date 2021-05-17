# AWX/Ansible Tower

Interacts with AWX/Ansible Tower endpoints via the AWX CLI. This library is considered experimental and users are encouraged to file issues when and where they are found.

### Dependencies

None

### awx.host_create()

Uses AWX to create a host in an inventory.

```groovy
awx.host_create(
  description: 'my host', // optional description of the host
  enabled:     true, // optional is host available and online for running jobs
  inventory:   'my_inventory', // ID of the associated inventory
  name:        'foo.bar.com', // name of the host
  variables:   ['foo': 'bar', 'baz': 1], // optional host variables
)
```
