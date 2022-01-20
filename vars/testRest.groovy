// vars/httpTest.groovy
import devops.common.rest

void get(config) {
  final Map response = new rest().request(config.url)

  print response
}

void post(config) {
  final Map response = new rest().request(config.url, config.headers, config.body, 'post')

  print response
}

void put(config) {
  final Map response = new rest().request(config.url, config.headers, config.body, 'put')

  print response
}

void error(config) {
  final Map response = new rest().request(config.url, config.headers, config.body, 'error')

  print response
}
