// vars/httpTest.groovy
import devops.common.utils
import devops.common.rest

void get(body) {
  Map response = new rest().request(config.url)

  print response
}

void post(body) {
  Map response = new rest().request(config.url, config.headers, config.body, 'post')

  print response
}

void put(body) {
  Map response = new rest().request(config.url, config.headers, config.body, 'put')

  print response
}

void error(body) {
  Map response = new rest().request(config.url, config.headers, config.body, 'error')

  print response
}
