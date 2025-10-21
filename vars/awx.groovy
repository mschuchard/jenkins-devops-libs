// vars/awx.groovy
import devops.common.utils

void hostCreate(Map config) {
  // input checking
  assert config.name in String : '"name" is a required parameter for awx.hostCreate.'
  assert config.inventory in String : '"inventory" is a required parameter for awx.hostCreate.'
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  List<String> cmd = [config.bin, 'hosts', 'create', '--name', config.name, '--inventory', config.inventory]

  // check for optional inputs
  if (config.description) {
    cmd.addAll(['--description', config.description])
  }
  if (config.enabled == true) {
    cmd.add('--enabled')
  }
  if (config.instanceId) {
    cmd.addAll(['--instance_id', config.instanceId])
  }
  if (config.variables) {
    assert (config.variables in Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    final String variables = new utils().mapToJSON(config.variables)

    cmd.addAll(['--variables', variables])
  }

  // create a host in the inventory
  try {
    sh(label: "AWX Host Create ${config.name}", script: cmd.join(' '))
  }
  catch (hudson.AbortException error) {
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
  catch (hudson.AbortException error) {
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
  List<String> cmd = [config.bin, 'inventory', config.action, '--name', config.name, '--organization', config.organization]

  // check for optional inputs
  if (config.description) {
    cmd.addAll(['--description', config.description])
  }
  if (config.kind) {
    assert ['smart', 'constructed'].contains(config.kind) : 'Inventory kind parameter value must be "smart" or "constructed".'

    cmd.addAll(['--kind', config.kind])
  }
  if (config.hostFilter) {
    cmd.addAll(['--host_filter', config.hostFilter])
  }
  if (config.variables) {
    assert (config.variables in Map) : 'The variables parameter must be a Map.'

    // convert variables map to json for input
    final String variables = new utils().mapToJSON(config.variables)

    cmd.addAll(['--variables', variables])
  }
  if (config.action == 'modify') {
    assert config.inventory in String : 'inventory is a required parameter for inventoryModify'

    cmd.add(config.inventory)
  }

  // "something" a inventory
  try {
    sh(label: "AWX Inventory ${capAction} ${config.name}", script: cmd.join(' '))
  }
  catch (hudson.AbortException error) {
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
  catch (hudson.AbortException error) {
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
  assert config.id in Integer : '"id" is a required parameter for awx.jobTemplateLaunch.'
  config.bin = config.bin ?: 'awx'

  // initialize the base command
  List<String> cmd = [config.bin, 'job_templates', 'launch']

  // check for optional inputs
  if (config.credentials) {
    assert (config.credentials in List) : 'The credentials parameter must be a list of strings.'

    cmd.addAll(['--credentials', config.credentials.join(',')])
  }
  if (config.executionEnv) {
    cmd.addAll(['--execution_environment', config.executionEnv])
  }
  if (config.monitor == true) {
    cmd.add('--monitor')
  }
  if (config.limit) {
    cmd.addAll(['--limit', config.limit])
  }
  if (config.inventory) {
    cmd.addAll(['--inventory', config.inventory])
  }
  if (config.jobType) {
    assert config.jobType in ['run', 'check'] : 'jobType parameter must be one of "run" or "check"'

    cmd.addAll(['--job_type', config.jobType])
  }
  if (config.skipTags) {
    assert (config.skipTags in List) : 'The skipTags parameter must be a List.'

    cmd.addAll(['--skip_tags', config.skipTags.join(',')])
  }
  if (config.extraVars) {
    assert (config.extraVars in Map) : 'The extraVars parameter must be a Map.'

    // convert variables map to json for input
    final String extraVars = new utils().mapToJSON(config.extraVars)

    cmd.addAll(['--extra_vars', extraVars])
  }

  // launch a job template job
  try {
    cmd.add(config.id)
    sh(label: "AWX Job Template Launch ${config.id}", script: cmd.join(' '))
  }
  catch (hudson.AbortException error) {
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
  List<String> cmd = [config.bin, 'projects', 'update']

  // check for optional inputs
  if (config.monitor == true) {
    cmd.add('--monitor')
  }

  // launch a project update job
  try {
    cmd.add(config.id)
    sh(label: "AWX Project Update ${config.id}", script: cmd.join(' '))
  }
  catch (hudson.AbortException error) {
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
  List<String> cmd = [config.bin, 'workflow_job_templates', 'launch']

  // check for optional inputs
  if (config.monitor == true) {
    cmd.add('--monitor')
  }
  if (config.limit) {
    cmd.addAll(['--limit', config.limit])
  }
  if (config.inventory) {
    cmd.addAll(['--inventory', config.inventory])
  }
  if (config.extraVars) {
    assert (config.extraVars in Map) : 'The extraVars parameter must be a Map.'

    // convert variables map to json for input
    final String extraVars = new utils().mapToJSON(config.extraVars)

    cmd.addAll(['--extra_vars', extraVars])
  }
  if (config.skipTags) {
    assert (config.skipTags in List) : 'The skipTags parameter must be a List.'

    cmd.addAll(['--skip_tags', config.skipTags.join(',')])
  }

  // launch a workflow job template job
  try {
    cmd.add(config.id)
    sh(label: "AWX Workflow Job Template Launch ${config.id}", script: cmd.join(' '))
  }
  catch (hudson.AbortException error) {
    print 'Failure using awx workflow job template launch.'
    throw error
  }
  print 'awx workflow job template launch was successful.'
}
