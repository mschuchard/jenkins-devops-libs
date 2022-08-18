// src/devops/common/hcl.groovy
package devops.common;

// imports
@Grab('com.bertramlabs.plugins:hcl4j:0.4.4')
import com.bertramlabs.plugins.hcl4j.HCLParser;

Map hclToMap(String filePath) {
  def file = null;

  // load the file from the jenkins master
  if (env['NODE_NAME'].equals('master')) {
    final file = new File(file);
  }
  // load the file from the build agent/node
  else {
    final file = new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), file)
  }

  // return map from parsed hcl-formatted string
  return new HCLParser().parse(file, 'UTF-8');
}
