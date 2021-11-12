// vars/awx.groovy
import devops.common.utils

void hostCreate(body) {
  // input checking
  assert config.name : '"name" is a required parameter for awx.hostCreate.'
  assert config.inventory : '"inventory" is a required parameter for awx.hostCreate.'
  config.bin = config.bin ?: 'awx'

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
    String variables = new utils().mapToJSON(config.variables)

    cmd += " --variables ${variables}"
  }

  // create a host in the inventory
  try {
    sh(label: 'AWX Host Create', script: cmd)
  }
  catch(Exception error) {
    print 'Failure using awx host create.'
    throw error
  }
  print 'awx host create was successful.'
}

void hostDelete(String id, String bin = 'awx') {
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
    String variables = new utils().mapToJSON(config.variables)

    cmd += " --variables ${variables}"
  }

  // "something" a inventory
  try {
    sh(label: "AWX Inventory ${config.action}", script: cmd)
  }
  catch(Exception error) {
    print "Failure using awx inventory ${config.action}."
    throw error
  }
  print "awx inventory ${config.action} was successful."
}

// invokes inventory helper method
void inventoryCreate(body) {
  // invoke helper method with create
  config.action = 'create'
  inventory(config)
}

void inventoryDelete(String id, String bin = 'awx') {
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
void inventoryModify(body) {
  // invoke helper method with modify
  config.action = 'modify'
  inventory(config)
}

void jobTemplateLaunch(body) {
  // input checking
  assert config.id : '"id" is a required parameter for awx.jobTemplateLaunch.'
  config.bin = config.bin ?: 'awx'

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
  if (config.jobType) {
    assert config.jobType in ['run', 'check'] : 'jobType parameter must be one of "run" or "check"'

    cmd += " --job_type ${config.jobType}"
  }
  if (config.skipTags) {
    assert (config.skipTags instanceof List) : 'The skipTags parameter must be a List.'

    cmd += " --skip_tags ${config.skipTags.join(',')}"
  }
  if (config.extraVars) {
    assert (config.extraVars instanceof Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    String extraVars = new utils().mapToJSON(config.variables)

    cmd += " --extra_vars ${extraVars}"
  }

  // launch a job template job
  try {
    sh(label: 'AWX Job Template Launch', script: "${cmd} ${config.id}")
  }
  catch(Exception error) {
    print 'Failure using awx job template launch.'
    throw error
  }
  print 'awx job template launch was successful.'
}

void projectsUpdate(body) {
  // input checking
  assert config.id : '"id" is a required parameter for awx.projectsUpdate.'
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  String cmd = "${config.bin} projects update"

  // check for optional inputs
  if (config.monitor == true) {
    cmd += ' --monitor'
  }

  // launch a project update job
  try {
    sh(label: 'AWX Project Update', script: "${cmd} ${config.id}")
  }
  catch(Exception error) {
    print 'Failure using awx projects update.'
    throw error
  }
  print 'awx projects update was successful.'
}

void workflowJobTemplateLaunch(body) {
  // input checking
  assert config.id : '"id" is a required parameter for awx.workflowJobTemplateLaunch.'
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  String cmd = "${config.bin} workflow_job_templates launch"

  // check for optional inputs
  if (config.monitor == true) {
    cmd += ' --monitor'
  }
  if (config.inventory) {
    cmd += " --inventory ${config.inventory}"
  }
  if (config.extraVars) {
    assert (config.extraVars instanceof Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    String extraVars = new utils().mapToJSON(config.variables)

    cmd += " --extra_vars ${extraVars}"
  }

  // launch a workflow job template job
  try {
    sh(label: 'AWX Workflow Job Template Launch', script: "${cmd} ${config.id}")
  }
  catch(Exception error) {
    print 'Failure using awx workflow job template launch.'
    throw error
  }
  print 'awx workflow job template launch was successful.'
}
