// src/devops/common/testRest.groovy
package devops.common;

import org.junit.jupiter.api.Test
import groovy.test.GroovyTestCase

class restTest extends GroovyTestCase {
  @Test
  void testGet(config) {
    final Map response = new rest().request(config.url)

    assert response
  }

  @Test
  void testPost(config) {
    final Map response = new rest().request(config.url, config.headers, config.body, 'post')

    assert response
  }

  @Test
  void testPut(config) {
    final Map response = new rest().request(config.url, config.headers, config.body, 'put')

    assert response
  }

  @Test
  void testError(config) {
    String msg = shouldFail {
      new rest().request(config.url, config.headers, config.body, 'error')
    }

    assert msg == "Invalid REST API interaction method 'error' specified."
  }
}
