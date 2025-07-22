// vars/awx.groovy
import devops.common.utils

void hostCreate(Map config) {
  // input checking
  assert config.name in String : '"name" is a required parameter for awx.hostCreate.'
  assert config.inventory in String : '"inventory" is a required parameter for awx.hostCreate.'
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
  if (config.instanceId) {
    cmd += "--instance_id ${config.instanceId}"
  }
  if (config.variables) {
    assert (config.variables in Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    final String variables = new utils().mapToJSON(config.variables)

    cmd += " --variables ${variables}"
  }

  // create a host in the inventory
  try {
    sh(label: "AWX Host Create ${config.name}", script: cmd)
  }
  catch (Exception error) {
    print 'Failure using awx host create.'
    throw error
  }
  print 'awx host create was successful.'
}

void hostDelete(String id, String bin = 'awx') {
  // delete a host in the inventory
  try {
    sh(label: "AWX Host Delete ${id}", script: "${bin} hosts delete ${id}")
  }
  catch (Exception error) {
    print 'Failure using awx host delete.'
    throw error
  }
  print 'awx host delete was successful.'
}

// helper method for create and modify
private void inventory(Map config) {
  // helpful constant
  final String capAction = config.action.capitalize()

  // input checking
  assert config.name in String : "'name' is a required parameter for inventory${capAction}."
  assert config.organization in String : "'organization' is a required parameter for inventory${capAction}."
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  String cmd = "${config.bin} inventory ${config.action} --name ${config.name} --organization ${config.organization}"

  // check for optional inputs
  if (config.description) {
    cmd += " --description ${config.description}"
  }
  if (config.kind) {
    assert ['smart', 'constructed'].contains(cconfig.kind) : 'Inventory kind parameter value must be "smart" or "constructed".'

    cmd += " --kind ${config.kind}"
  }
  if (config.hostFilter) {
    cmd += " --host_filter ${config.hostFilter}"
  }
  if (config.variables) {
    assert (config.variables in Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    final String variables = new utils().mapToJSON(config.variables)

    cmd += " --variables ${variables}"
  }
  if (config.action == 'modify') {
    assert config.inventory in String : 'inventory is a required parameter for inventoryModify'

    cmd += " ${config.inventory}"
  }

  // "something" a inventory
  try {
    sh(label: "AWX Inventory ${capAction} ${config.name}", script: cmd)
  }
  catch (Exception error) {
    print "Failure using awx inventory${config.action}."
    throw error
  }
  print "awx inventory${config.action} was successful."
}

// invokes inventory helper method
void inventoryCreate(Map config) {
  // invoke helper method with create
  config.action = 'create'
  inventory(config)
}

void inventoryDelete(String id, String bin = 'awx') {
  // delete an inventory
  try {
    sh(label: "AWX Inventory Delete ${id}", script: "${bin} inventory delete ${id}")
  }
  catch (Exception error) {
    print 'Failure using awx inventory delete.'
    throw error
  }
  print 'awx inventory delete was successful.'
}

// invokes inventory helper method
void inventoryModify(Map config) {
  // invoke helper method with modify
  config.action = 'modify'
  inventory(config)
}

void jobTemplateLaunch(Map config) {
  // input checking
  assert config.id in int : '"id" is a required parameter for awx.jobTemplateLaunch.'
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  String cmd = "${config.bin} job_templates launch"

  // check for optional inputs
  if (config.credentials) {
    assert (config.credentials in List) : 'The credentials parameter must be a list of strings.'

    cmd += " --credentials ${config.credentials.join(',')}"
  }
  if (config.executionEnv) {
    cmd += " --execution_environment ${config.executionEnv}"
  }
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
    assert (config.skipTags in List) : 'The skipTags parameter must be a List.'

    cmd += " --skip_tags ${config.skipTags.join(',')}"
  }
  if (config.extraVars) {
    assert (config.extraVars in Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    final String extraVars = new utils().mapToJSON(config.variables)

    cmd += " --extra_vars ${extraVars}"
  }

  // launch a job template job
  try {
    sh(label: "AWX Job Template Launch ${config.id}", script: "${cmd} ${config.id}")
  }
  catch (Exception error) {
    print 'Failure using awx job template launch.'
    throw error
  }
  print 'awx job template launch was successful.'
}

void projectsUpdate(Map config) {
  // input checking
  assert config.id in int : '"id" is a required parameter for awx.projectsUpdate.'
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  String cmd = "${config.bin} projects update"

  // check for optional inputs
  if (config.monitor == true) {
    cmd += ' --monitor'
  }

  // launch a project update job
  try {
    sh(label: "AWX Project Update ${config.id}", script: "${cmd} ${config.id}")
  }
  catch (Exception error) {
    print 'Failure using awx projects update.'
    throw error
  }
  print 'awx projects update was successful.'
}

void workflowJobTemplateLaunch(Map config) {
  // input checking
  assert config.id in int : '"id" is a required parameter for awx.workflowJobTemplateLaunch.'
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  String cmd = "${config.bin} workflow_job_templates launch"

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
  if (config.extraVars) {
    assert (config.extraVars in Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    final String extraVars = new utils().mapToJSON(config.variables)

    cmd += " --extra_vars ${extraVars}"
  }
  if (config.skipTags) {
    assert (config.skipTags in List) : 'The skipTags parameter must be a List.'

    cmd += " --skip_tags ${config.skipTags.join(',')}"
  }

  // launch a workflow job template job
  try {
    sh(label: "AWX Workflow Job Template Launch ${config.id}", script: "${cmd} ${config.id}")
  }
  catch (Exception error) {
    print 'Failure using awx workflow job template launch.'
    throw error
  }
  print 'awx workflow job template launch was successful.'
}
