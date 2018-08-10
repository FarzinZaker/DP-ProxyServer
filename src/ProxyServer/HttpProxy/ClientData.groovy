package ProxyServer.HttpProxy

import ProxyServer.IRequestFilter
import groovy.transform.Immutable

import java.nio.channels.SocketChannel

/**
 * Created by root on 8/9/17.
 */
public class ClientData {

    SocketChannel clientSocket
    InetAddress originAddress
    int originPort
    IRequestFilter filter
    String request
}
