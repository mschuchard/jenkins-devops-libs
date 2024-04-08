// src/devops/common/restTest.groovy
package devops.common;

import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail

class restTest extends GroovyTestCase {
  @Test
  void testGet() {
    final Map response = new rest().request('https://www.google.com')

    assert response
  }

  @Test
  void testPost() {
    final Map response = new rest().request(config.url, config.headers, config.body, 'post')

    assert response
  }

  @Test
  void testPut() {
    final Map response = new rest().request(config.url, config.headers, config.body, 'put')

    assert response
  }

  @Test
  void testError() {
    String exception = shouldFail {
      new rest().request(config.url, config.headers, config.body, 'error')
    }

    assert exception.message == "Invalid REST API interaction method 'error' specified."
  }
}
