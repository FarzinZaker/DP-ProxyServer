package proxy

import groovyx.net.http.RESTClient
import proxy.messages.Request

import static groovyx.net.http.ContentType.URLENC

/**
 * Created by root on 8/14/17.
 */
class ScenarioService {

    Request userRequest

    String handle() {
//        def client = new RESTClient(userRequest.host)
//        def postBody = userRequest.params
//        def response = client.get(path: userRequest.path + '?' + userRequest.params?.collect { "${it.key}=${it.value}" }?.join('&'), requestContentType: URLENC)
//        response.data?.toString()

        try {
//            new URL("${userRequest.host}${userRequest.path}?${userRequest.params?.collect { "${it.key}=${it.value}" }?.join('&')}").text
            new URL("http://${userRequest.host}${userRequest.path}").text
        } catch (ignored) {
            ignored.message
        }
    }
}
