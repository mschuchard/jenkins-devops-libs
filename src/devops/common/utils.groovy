// src/devops/common/utils.groovy
package devops.common

// imports
import com.cloudbees.groovy.cps.NonCPS
import hudson.FilePath
import jenkins.model.Jenkins

// checks input value for default value use if not set
def defaultInput(input, defaultValue) {
  return input ?: defaultValue
}

// removes file
@NonCPS
void removeFile(String file) {
  // delete a file on the master or build node
  env['NODE_NAME'] == 'master' ? new File(file).delete() : new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), file).delete()
}

// downloads file using httpRequest step
void downloadFile(String url, String dest) {
  // attempt to download the file
  try {
    Map response = httpRequest(
      url: url,
      httpMode: 'GET',
      outputFile: dest,
      quiet: true,
      validResponseCodes: '200:399'
    )

    print "Downloaded ${url} to ${dest} (HTTP ${response.status})"
  } catch (Exception error) {
    print "Failed to download ${url} to ${dest} (HTTP ${response.status})"
    throw error
  }
}

// downloads file (java/groovy)
@NonCPS
void deprecatedDownloadFile(String url, String dest) {
  // establish the file download for the master or the build node
  File file = env['NODE_NAME'] == 'master' ? new File(dest) : new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), dest)

  // download the file and close the ostream
  file.newOutputStream() << new URL(url).openStream()
  file.close()
}

// recursively creates directory with serializable steps
void makeDirParents(String dir) {
  // normalize path
  String normalizedDir = dir.replaceAll('\\\\', '/')

  // check if directory already exists
  if (fileExists(normalizedDir)) {
    print "Directory at ${normalizedDir} already exists on node."
    return
  }

  // create the directory(ies)
  dir(normalizedDir) {
    print "Created directory at ${normalizedDir}"
  }
}

// functionally equivalent to unix mkdir -p (java/groovy)
@NonCPS
void deprecatedMakeDirParents(String dir) {
  // ascertain directory on jenkins master or build agent/node
  File file = env['NODE_NAME'] == 'master' ? new File(dir) : new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), dir)

  // short circuit if directory exists
  if (file.exists()) {
    print "Directory at ${dir} already exists on node."
    return
  }

  // create the directory(ies)
  file.mkdirs()
  print "Created directory at ${dir}"
}

// converts content map to json string
String mapToJSON(Map content) {
  return writeJSON(json: content, returnText: true)
}

// converts closure body to config map, or returns same config map
// bridges gap between users of older DSL and newer DSL
Map paramsConverter(body) {
  // initialize config
  Map config = [:]

  // if we received older DSL, convert it into config map
  if (body in Closure) {
    // evaluate the body block and collect configuration into the object
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
  }
  // if newer DSL, return same map as the config
  else if (body in Map) {
    config = body
  }
  // params are invalid type
  else {
    throw new Exception('The parameter inputs are an invalid type. They must either be a Closure or Map. Consult the documentation for more information.')
  }

  return config
}
// example usage: void globalVarMethod(body)
// where body is closure or map
// Map config = new utils().paramsConverter(body)

// the following methods handle parameter subcommand generation
// the various wrapper methods all rely upon subCommand for actual functionality, and the various definitions exist primarily for type checking

String listParam(List param, String cmdArg) {
  return subCommand(param, cmdArg)
}

String mapParam(Map param, String cmdArg) {
  return subCommand(param, cmdArg)
}

String stringBoolParams(Map paramCmdArg) {
  // initialize aggregate sub command
  String aggregateSubCommand = ''

  // iterate through map of params and corresponding command arguments
  paramCmdArg.each { param, cmdArg ->
    // build aggregated sub command
    aggregateSubCommand += subCommand(param, cmdArg)
  }

  return aggregateSubCommand
}

private String subCommand(param, String cmdArg) {
  // initialize subcommand string
  String subCmd = ''

  // immediately verify param is not null
  if (param) {
    // different behavior based on param type
    switch (param) {
      case Map:
        // iterate through param value pairs and concatenate full arg and value pairs to subcommand
        param.each { paramValueName, paramValue ->
          subCmd += " ${cmdArg}${paramValueName}=${paramValue}"
        }
        break
      case List:
        // iterate through param values and concatenate full arg and value to subcommand
        param.each { paramValue ->
          subCmd += " ${cmdArg}${paramValue}"
        }
        break
      case String:
        // build aggregate sub command with consecutive subCommand returns
        subCmd += " ${cmdArg}${param}"
        break
      case Boolean:
        // build aggregate sub command with consecutive subCommand returns
        subCmd += " ${cmdArg}"
        break
      default:
        throw new Exception("Unexpected parameter type '${param.getClass()}' for command argument '${cmdArg}'.")
    }
  }

  return subCmd
}
