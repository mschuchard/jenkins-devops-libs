// src/devops/common/utilsTest.groovy
package devops.common

import org.junit.Test

class utilsTest {

  @Test
  void testDefaultInput() {
    final String override = new utils().defaultInput('foo', 'bar')

    assert override == 'foo'

    final String backup = new utils().defaultInput(null, 'bar')

    assert backup == 'bar'
  }

  @Test
  void testMapToJSON() {
    final String json = mapToJSON([['foo':'bar'], ['bar':'baz']])

    assert json
  }

}
