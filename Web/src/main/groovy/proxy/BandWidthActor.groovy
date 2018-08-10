package proxy

import akka.actor.ActorRef
import akka.actor.UntypedAbstractActor
import grails.util.Environment
import org.springframework.context.annotation.Scope
import proxy.messages.BandWidthData
import proxy.messages.Bandwidth
import proxy.messages.Scenario

import javax.inject.Named
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by root on 8/31/17.
 */

@Named("BandWidthActor")
@Scope("prototype")
class BandWidthActor extends UntypedAbstractActor {

    private actors = new ConcurrentHashMap<String, ActorRef>()
    static bandWidths = new ConcurrentHashMap<String, Float>()
    static realBandWidth = new ConcurrentHashMap<String, Integer>()

    @Override
    void onReceive(Object message) throws Throwable {

        //scenario
        if (message instanceof Scenario) {
            Scenario scenario = message
            actors.putIfAbsent(scenario.name, sender)
            bandWidths.putIfAbsent(scenario.name, 1.0f)
            realBandWidth.putIfAbsent(scenario.name, (SystemConfig.totalBandwidth)?.toInteger())
        }

        //band width
        else if (message instanceof Bandwidth) {
            Bandwidth bandwidth = message
            bandWidths.put(bandwidth.scenario, bandwidth.value)
        }

        //get
        else if (message?.toString() == 'GET') {
            sender?.tell(new BandWidthData(bandWidths: bandWidths), self)
        }

        //get
        else if (message?.toString() == 'GET_REAL') {
            sender?.tell(new BandWidthData(bandWidths: realBandWidth), self)
        }

        //set
        else if (message instanceof BandWidthData) {
            setBandWidths(message as BandWidthData)
        }
    }

    private synchronized setBandWidths(BandWidthData data) {

        //record bandwidths
        data.bandWidths?.each {
            bandWidths.put(it.key, it.value)
        }

        if (!Environment.isDevelopmentMode()) {

            //apply bandwidth on network
//            def weightSum = bandWidths.values().sum { it } as Float
//            println "SUM BANDWIDTH WEIGHT: ${weightSum}"
//            println "BANDWIDTH WEIGHT: ${bandWidths as JSON}"
//            def availableBandwidth = SystemConfig.totalBandwidth - (SystemConfig.scenarios.values().collect { it.minBandwidth }.sum() as Integer)
            bandWidths.each { bandWidth ->
                def newBandwidth = Math.round((bandWidth.value as Float) * SystemConfig.totalBandwidth)?.toInteger()
                def minBandwidth = (SystemConfig.scenarios[bandWidth.key].minBandwidth as Integer) * ScenarioActor.lastArrivalRates[bandWidth.key] * 2
                if (newBandwidth < minBandwidth)
                    newBandwidth = minBandwidth
                realBandWidth.put(bandWidth.key, newBandwidth)
            }

//            println "REAL WEIGHT: ${realBandWidth as JSON}"
            realBandWidth.each { bandWidth ->
                RemoteController.setBW(bandWidth.key, bandWidth.value)
            }
        }
    }
}
