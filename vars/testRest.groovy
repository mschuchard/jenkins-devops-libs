// vars/httpTest.groovy
import devops.common.utils
import devops.common.rest

void get(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  Map response = new rest().request(config.url)

  print response
}

void post(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  Map response = new rest().request(config.url, config.headers, config.body, 'post')

  print response
}

void put(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  Map response = new rest().request(config.url, config.headers, config.body, 'put')

  print response
}

void error(body) {
  // pass in params body and ensure proper config of type map
  Map config = new utils().paramsConverter(body)

  Map response = new rest().request(config.url, config.headers, config.body, 'error')

  print response
}
