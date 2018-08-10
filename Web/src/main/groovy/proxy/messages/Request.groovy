package proxy.messages

import groovyx.net.http.Method

import javax.servlet.ServletOutputStream

/**
 * Created by root on 8/14/17.
 */
class Request implements Serializable {

    Method method
    String host
    String path
    Map params
}
