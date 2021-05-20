// vars/awx.groovy
import devops.common.utils

void host_create(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.name : '"name" is a required parameter for awx.host_create.'
  assert config.inventory : '"inventory" is a required parameter for awx.host_create.'

  config.bin = config.bin ?: 'awx'

  // create a host in the inventory
  try {
    // initialize the base command
    String cmd = "${config.bin} hosts create --name ${config.name} --inventory ${config.inventory}"

    // check for optional inputs
    if (config.description) {
      cmd += " --description ${config.description}"
    }
    if (config.enabled == true) {
      cmd += ' --enabled'
    }
    if (config.variables) {
      assert (config.variables instanceof Map) : 'The variables parameter must be a Map.'

      // convert variables map to json for input
      variables = new utils().mapToJSON(config.variables)

      cmd += " --variables ${variables}"
    }

    sh(label: 'AWX Host Create', script: cmd)
  }
  catch(Exception error) {
    print 'Failure using awx host create.'
    throw error
  }
  print 'awx host create was successful.'
}

void host_delete(String id, String bin = 'awx') {
  // delete a host in the inventory
  try {
    sh(label: 'AWX Host Delete', script: "${config.bin} hosts delete ${id}")
  }
  catch(Exception error) {
    print 'Failure using awx host delete.'
    throw error
  }
  print 'awx host delete was successful.'
}
