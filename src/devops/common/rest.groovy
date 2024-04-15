// src/devops/common/rest.groovy
package devops.common;

// imports
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.2')
import groovyx.net.http.RESTClient

// http method enum
enum HTTPMethod{
  GET,
  POST,
  PUT,
  DELETE,
}

// defines a method for interacting with rest apis
Map request(String url, HTTPMethod method = GET, Map body = [:], Map headers = [:]) {
  // initialize client and expected status code
  def client = new RESTClient(url)
  int status = 200

  // invoke helper request method depending upon interaction method
  switch(method) {
    case GET:
      response = client.get(headers: headers)
      break
    case POST:
      response = client.post(headers: headers, body: body)
      status = 201
      break
    case PUT:
      response = client.put(headers: headers, body: body)
      break
    case DELETE:
      response = client.delete(headers: headers)
      break
    default:
      throw new Exception("Invalid REST API interaction method '${method}' specified.")
  }

  // handle the response
  assert response.status == status : "Invalid response status code from the REST API: ${response.status}."
  // return the data as a list instance with map interface
  return response['reader']
}
