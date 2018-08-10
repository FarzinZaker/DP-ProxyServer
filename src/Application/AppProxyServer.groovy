package Application

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props;

import java.net.InetAddress;
import java.util.ResourceBundle;

import ProxyServer.IServiceClassRequestObserver;
import ProxyServer.ProxyControlServer;
import ProxyServer.RequestFilter;
import ProxyServer.HttpProxy.ForwardServer;
import ProxyServer.RequestObserver.ServiceClassRequestObserver;

public class AppProxyServer {
    static void ConfigureTheRequestObserver(IServiceClassRequestObserver theObserver) {
        ResourceBundle myResources = ResourceBundle.getBundle("resources.RequestObserver");

        int scenariosCount = Integer.parseInt(myResources.getString("scenarios.count"));
        for (int i = 0; i < scenariosCount; ++i) {
            String name = myResources.getString("scenarios." + i + ".name");
            String pattern = myResources.getString("scenarios." + i + ".pattern");

            theObserver.AddServiceClass(name, pattern);
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        ProgramSettings settings = new ProgramSettings();
        Application.CommandLineHandler.CommandLineParse(settings, args);
        // start the control server
        ProxyControlServer controlServer = new ProxyControlServer(settings.monitorPort);
        controlServer.start();

        // start the request observer
        ServiceClassRequestObserver requestObserver = new ServiceClassRequestObserver();
        ConfigureTheRequestObserver(requestObserver);
        requestObserver.start();

        // start the forward server
        RequestFilter requestFilter = new RequestFilter(requestObserver);
        controlServer.SetRequestFilter(requestFilter);
        InetAddress remoteAddress = InetAddress.getByName(settings.remoteAddress);
        ForwardServer forwardServer = new ForwardServer(remoteAddress, settings.remotePort, settings.localPort, requestFilter);
        forwardServer.start();
    }
}
