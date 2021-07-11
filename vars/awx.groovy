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

// helper method for create and modify
void inventory(config) {
  // input checking
  assert config.name : "'name' is a required parameter for awx.inventory_${config.action}."
  assert config.organization : "'organization' is a required parameter for awx.inventory_${config.action}."

  config.bin = config.bin ?: 'awx'

  // "something" a inventory
  try {
    // initialize the base command
    String cmd = "${config.bin} inventory ${config.action} --name ${config.name} --organization ${config.organization}"

    // check for optional inputs
    if (config.description) {
      cmd += " --description ${config.description}"
    }
    if (config.smart == true) {
      cmd += " --kind smart"
    }
    if (config.hostFilter) {
      cmd += " --host_filter ${config.hostFilter}"
    }
    if (config.variables) {
      assert (config.variables instanceof Map) : 'The variables parameter must be a Map.'

      // convert variables map to json for input
      variables = new utils().mapToJSON(config.variables)

      cmd += " --variables ${variables}"
    }
    sh(label: "AWX Inventory ${config.action}", script: cmd)
  }
  catch(Exception error) {
    print "Failure using awx inventory ${config.action}."
    throw error
  }
  print "awx inventory ${config.action} was successful."
}

// invokes inventory helper method
void inventory_create(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // invoke helper method with create
  config.action = 'create'
  inventory(config)
}

void inventory_delete(String id, String bin = 'awx') {
  // delete an inventory
  try {
    sh(label: 'AWX Inventory Delete', script: "${config.bin} inventory delete ${id}")
  }
  catch(Exception error) {
    print 'Failure using awx inventory delete.'
    throw error
  }
  print 'awx inventory delete was successful.'
}

// invokes inventory helper method
void inventory_modify(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // invoke helper method with modify
  config.action = 'modify'
  inventory(config)
}

void job_template_launch(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.id : '"id" is a required parameter for awx.job_template_launch.'

  config.bin = config.bin ?: 'awx'

  // launch a job template job
  try {
    // initialize the base command
    String cmd = "${config.bin} job_templates launch"

    // check for optional inputs
    if (config.monitor == true) {
      cmd += ' --monitor'
    }
    if (config.limit) {
      cmd += " --limit ${config.limit}"
    }
    if (config.inventory) {
      cmd += " --inventory ${config.inventory}"
    }
    if (config.job_type) {
      assert config.job_type in ['run', 'check'] : 'job_type parameter must be one of "run" or "check"'

      cmd += " --job_type ${config.job_type}"
    }
    if (config.skip_tags) {
      assert config.skip_tags instanceof List) : 'The skip_tags parameter must be a List.'

      cmd += " --skip_tags ${config.skip_tags.join(',')}"
    }
    if (config.extra_vars) {
      assert (config.extra_vars instanceof Map) : 'The variables parameter must be a Map.'

      // convert variables map to json for input
      extra_vars = new utils().mapToJSON(config.variables)

      cmd += " --extra_vars ${extra_vars}"
    }

    sh(label: 'AWX Job Template Launch', script: "${cmd} ${config.id}")
  }
  catch(Exception error) {
    print 'Failure using awx job template launch.'
    throw error
  }
  print 'awx job template launch was successful.'
}

void projects_update(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.id : '"id" is a required parameter for awx.projects_update.'

  config.bin = config.bin ?: 'awx'

  // launch a project update job
  try {
    // initialize the base command
    String cmd = "${config.bin} projects update"

    // check for optional inputs
    if (config.monitor == true) {
      cmd += ' --monitor'
    }

    sh(label: 'AWX Project Update', script: "${cmd} ${config.id}")
  }
  catch(Exception error) {
    print 'Failure using awx projects update.'
    throw error
  }
  print 'awx projects update was successful.'
}

void workflow_job_template_launch(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  // input checking
  assert config.id : '"id" is a required parameter for awx.workflow_job_template_launch.'

  config.bin = config.bin ?: 'awx'

  // launch a workflow job template job
  try {
    // initialize the base command
    String cmd = "${config.bin} workflow_job_templates launch"

    // check for optional inputs
    if (config.monitor == true) {
      cmd += ' --monitor'
    }
    if (config.inventory) {
      cmd += " --inventory ${config.inventory}"
    }
    if (config.extra_vars) {
      assert (config.extra_vars instanceof Map) : 'The variables parameter must be a Map.'

      // convert variables map to json for input
      extra_vars = new utils().mapToJSON(config.variables)

      cmd += " --extra_vars ${extra_vars}"
    }

    sh(label: 'AWX Workflow Job Template Launch', script: "${cmd} ${config.id}")
  }
  catch(Exception error) {
    print 'Failure using awx workflow job template launch.'
    throw error
  }
  print 'awx workflow job template launch was successful.'
}
