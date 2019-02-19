// src/devops/common/utils.groovy
package devops.common;

// imports
import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonOutput

// checks input value for default value use if not set
def default_input(input, default_value) {
  return input == null ? default_value : input
}

// removes file
@NonCPS
def remove_file(String file) {
  new File(file).delete()
}

// downloads file
@NonCPS
def download_file(String url, String dest) {
  def file = new File(dest).newOutputStream()
  file << new URL(url).openStream()
  file.close()
}

// converts content map to json string
def map_to_json(Map content) {
  return JsonOutput.toJson(content)
}

//http://vertx.io/docs/groovydoc/io/vertx/groovy/core/file/FileSystem.html
