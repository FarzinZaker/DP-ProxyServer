package proxy.messages

import akka.actor.ActorRef

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by root on 8/23/17.
 */
class StartCommand implements Serializable {

    ConcurrentHashMap<String, ActorRef> actors
}
