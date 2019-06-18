// src/devops/common/utils.groovy
package devops.common;

// imports
import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonOutput
import hudson.FilePath
import jenkins.model.Jenkins

// checks input value for default value use if not set
def default_input(input, default_value) {
  return input == null ? default_value : input
}

// removes file
@NonCPS
void remove_file(String file) {
  // delete a file off of the master
  if (env['NODE_NAME'].equals('master')) {
    new File(file).delete();
  }
  // delete a file off of the build node
  else {
    new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), file).delete();
  }
}

// downloads file
@NonCPS
void download_file(String url, String dest) {
  def file = null;
  // establish the file download for the master
  if (env['NODE_NAME'].equals('master')) {
    file = new File(dest).newOutputStream();
  }
  // establish the file download for the build node
  else {
    file = new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), dest).newOutputStream();
  }
  // download the file and close the ostream
  file << new URL(url).openStream();
  file.close();
}

// converts content map to json string
String map_to_json(Map content) {
  return JsonOutput.toJson(content);
}
