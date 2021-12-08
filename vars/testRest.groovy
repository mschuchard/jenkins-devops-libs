// vars/httpTest.groovy
import devops.common.rest

void get(config) {
  Map response = new rest().request(config.url)

  print response
}

void post(config) {
  Map response = new rest().request(config.url, config.headers, config.body, 'post')

  print response
}

void put(config) {
  Map response = new rest().request(config.url, config.headers, config.body, 'put')

  print response
}

void error(config) {
  Map response = new rest().request(config.url, config.headers, config.body, 'error')

  print response
}
