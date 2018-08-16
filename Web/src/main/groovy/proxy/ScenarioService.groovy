package proxy

import proxy.messages.Request

/**
 * Created by root on 8/14/17.
 */
class ScenarioService {

    Request userRequest

    String handle() {

        try {
            new URL("http://${userRequest.host}${userRequest.path}").text
        } catch (ignored) {
            ignored.message
        }
    }
}
