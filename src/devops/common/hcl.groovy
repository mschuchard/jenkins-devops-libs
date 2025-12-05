// src/devops/common/hcl.groovy
package devops.common;

// imports
import jenkins.model.Jenkins
import com.cloudbees.groovy.cps.NonCPS
@Grab('com.bertramlabs.plugins:hcl4j:0.9.1')
import com.bertramlabs.plugins.hcl4j.HCLParser

// wrapper method for returning a map from a hcl file
@NonCPS
Map hclToMap(String filePath) {
  // load the file from the jenkins master or the build agent/node
  File file = env['NODE_NAME'] == 'master' ? new File(filePath) : new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), filePath);

  // verify file exists
  if (!(file.exists())) {
    print "File does not exist at ${filePath}"
    throw new FileNotFoundException("HCL file does not exist")
   }

  // return map from parsed hcl-formatted string
  return new HCLParser().parse(file, 'UTF-8');
}
