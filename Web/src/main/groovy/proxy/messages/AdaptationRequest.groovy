package proxy.messages

import akka.actor.ActorRef

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by root on 8/23/17.
 */
class AdaptationRequest implements Serializable {

    String id = UUID.randomUUID()?.toString()
    Integer priority
    ConcurrentHashMap<Float, ConcurrentHashMap<String, Float>> options
    ConcurrentHashMap<Float, ActorRef> leaders
    Integer scenariosCount
}
