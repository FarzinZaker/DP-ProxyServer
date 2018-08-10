package ProxyServer.HttpProxy

/**
 * Created by root on 8/9/17.
 */
import akka.actor.UntypedAbstractActor
import org.springframework.context.annotation.Scope
import javax.inject.Named

@Named("ClientHandleActor")
@Scope("prototype")
class ClientHandleActor extends UntypedAbstractActor {

    @Override
    void onReceive(Object message) throws Exception {
        if (message instanceof ClientData) {
            ClientData clientData = message
            handleClient(clientData)
        } else {
            unhandled(message);
        }
    }


    private static void handleClient(ClientData clientData) {
        new ClientHandleService(clientData: clientData).handle()
    }
}
