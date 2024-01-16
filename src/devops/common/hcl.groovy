// src/devops/common/hcl.groovy
package devops.common;

// imports
@Grab('com.bertramlabs.plugins:hcl4j:0.7.3')
import com.bertramlabs.plugins.hcl4j.HCLParser;

// wrapper method for returning a map from a hcl file
Map hclToMap(String filePath) {
  // load the file from the jenkins master
  if (env['NODE_NAME'].equals('master')) {
    final file = new File(filePath);
  }
  // load the file from the build agent/node
  else {
    final file = new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), filePath)
  }

  // return map from parsed hcl-formatted string
  return new HCLParser().parse(file, 'UTF-8');
}
