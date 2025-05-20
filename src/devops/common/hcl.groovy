// src/devops/common/hcl.groovy
package devops.common;

// imports
import jenkins.model.Jenkins
@Grab('com.bertramlabs.plugins:hcl4j:0.9.1')
import com.bertramlabs.plugins.hcl4j.HCLParser

// wrapper method for returning a map from a hcl file
Map hclToMap(String filePath) {
  def file = null;

  // load the file from the jenkins master
  if (env['NODE_NAME'].equals('master')) {
    // verify file exists
    if (!(File(filePath).exists())) {
      print "File does not exist at ${filePath}"
      throw new FileNotFoundException("HCL file does not exist")
    }

    final file = new File(filePath);
  }
  // load the file from the build agent/node
  else {
    // verify file exists
    if (!(FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), dir).exists())) {
      print "File does not exist at ${filePath}"
      throw new FileNotFoundException("HCL file does not exist")
    }

    final file = new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), filePath);
  }

  // return map from parsed hcl-formatted string
  return new HCLParser().parse(file, 'UTF-8');
}
