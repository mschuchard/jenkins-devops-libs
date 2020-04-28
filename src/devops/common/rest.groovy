// src/devops/common/rest.groovy
package devops.common;

// imports
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.2')
import groovyx.net.http.RESTClient

// defines a method for interacting with rest apis
Map request(String url, Map headers = [:], Map body = [:], String method = 'get') {
  // initialize client
  def client = new RESTClient(url)

  // invoke helper request method depending upon interaction method
  switch(method) {
    case 'get':
      response = client.get(headers: headers, body: body)
      break
    case 'post':
      response = client.post(headers: headers, body: body)
      break
    case 'put':
      response = client.put(headers: headers, body: body)
      break
    default:
      throw new Exception("Invalid REST API interaction method ${method} specified.")
  }

  // handle the response
  assert response.status == 200 : "Invalid response status code from the REST API: ${response.status}."
  // return the data as a list instance with map interface
  return response['reader']
}
