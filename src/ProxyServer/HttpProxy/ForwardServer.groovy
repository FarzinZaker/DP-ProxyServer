package ProxyServer.HttpProxy

import akka.actor.ActorPath
import akka.actor.ActorRef
import akka.actor.ActorRefProvider
import akka.actor.ActorSystem
import akka.actor.ActorSystemImpl
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.InternalActorRef
import akka.actor.Props
import akka.actor.Scheduler
import akka.actor.Terminated
import akka.dispatch.Dispatchers
import akka.dispatch.Mailboxes
import akka.event.EventStream
import akka.event.LoggingAdapter
import com.typesafe.config.ConfigFactory
import scala.Function0
import scala.collection.Iterable
import scala.concurrent.Await
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import ProxyServer.IRequestFilter

import static akka.pattern.Patterns.ask;

public class ForwardServer extends Thread {
    ServerSocketChannel server;
    boolean isRunning;

    InetAddress remoteAddress;
    int remotePort;
    int localPort;
    IRequestFilter filter;

    ActorSystem actorSystem

    @Override
    public void run() {
        server = null;
        isRunning = true;
        Selector selector = null;

        //initializing the actor system
        def cl = this.class.classLoader
        actorSystem = ActorSystem.create('ForwardServer', ConfigFactory.load(cl), cl)

//        try {
        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(localPort));
        server.configureBlocking(false);

        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
                SelectionKey key = i.next();
                i.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    // accept connection
                    SocketChannel client = server.accept();
                    if (client == null) {
                        // this can happen in non-blocking mode
                        return;
                    }

//						new ClientHandler(client, remoteAddress, remotePort, filter);

                    //creating Client Handle Actor
                    def request = Utils.readRequest(client.socket())
                    if(request) {
                        def path = Utils.extractPath(request)
                        def actorName = "ClientHandler_${path?.split('/')?.findAll { it }[0..1]?.join("\$")}"
                        def actorRef = actorSystem.actorFor("user/${actorName}")
                        if (actorRef?.terminated)
                            actorRef = actorSystem.actorOf(Props.create(ClientHandleActor), actorName)
                        def clientData = new ClientData(clientSocket: client, originAddress: remoteAddress, originPort: remotePort, filter: filter, request: request)
                        actorRef.tell(clientData, null)
                    }
                }
            }

            if (!isRunning)
                break;
        }
//        }
//        catch (Throwable e) {
//            throw new RuntimeException("Server failure: " + e.getMessage());
//        }
//        finally {
        try {
            // selector.close();
            server.socket().close();
            server.close();
        }
        catch (Exception e) {
            // do nothing - server failed
        }
//        }
    }

    public ForwardServer(InetAddress remoteAddress, int remotePort, int localPort, IRequestFilter filter) {
        this.localPort = localPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.filter = filter;

        this.setName("Thread - Forward Server");
    }

    public void shutdown() {
        isRunning = false;
    }
}
