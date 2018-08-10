package web

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.Patterns
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import grails.converters.JSON

import groovyx.net.http.Method
import proxy.RemoteController
import proxy.BandWidthActor
import proxy.ScenarioActor
import proxy.SystemConfig
import proxy.messages.OwnResponseTime
import proxy.messages.Request
import proxy.messages.Scenario
import scala.concurrent.Await

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ProxyController {

    ActorSystem actorSystem
    final Object systemLock = new Object()
    def actors = new ConcurrentHashMap<String, ActorRef>()

    def get() {
        proxifyRequest(Method.GET)
    }

    def put() {
        proxifyRequest(Method.PUT)
    }

    def delete() {
        proxifyRequest(Method.DELETE)
    }

    def post() {
        proxifyRequest(Method.POST)
    }

    def rt() {
        handleResponseTime()
    }

    private def proxifyRequest(Method method) {
        prepareParams()
        //initialize actor system
        synchronized (systemLock) {
            if (!actorSystem) {
                def cl = this.class.classLoader
                actorSystem = ActorSystem.create("ForwardServer", ConfigFactory.load(cl), cl)
//            actorSystem = ActorSystem.create("ForwardServer", ConfigFactory.load(cl), cl)
            }
        }

//        def scenarioName = (request?.servletPath?.split('/')?.findAll { it } + params?.findAll { it.key != 'rt' }?.sort { it.key }?.collect { "${it.key}=${it.value}" })?.join("\$")
        def scenarioName = request?.servletPath?.split('/')?.findAll { it }?.find()
        def actorRef = createActor(scenarioName)
        //forward request
        def request = new Request(method: method, host: SystemConfig.loadBalancerAddress, path: request?.servletPath, params: params)
        def timeout = new Timeout(60, TimeUnit.SECONDS)
        def result = 'TIME OUT'
        try {
            def future = Patterns.ask(actorRef, request, timeout)
            result = Await.result(future, timeout.duration())?.toString()
        }
        catch (ignored) {
        }
        render result
    }

    private def prepareParams() {
        params.remove('controller')
        params.remove('action')
    }

    private def handleResponseTime() {
        prepareParams()
//        def scenarioName = (request?.servletPath?.split('/')?.findAll { it }[1..2] + params?.findAll { it.key != 'rt' }?.sort { it.key }?.collect { "${it.key}=${it.value}" })?.join("\$")
        def scenarioName = request?.servletPath?.split('/')?.findAll { it }?.last()
        def actorRef = createActor(scenarioName)
        actorRef.tell(new OwnResponseTime(value: params.rt?.toInteger()), actorRef)
        render ''
    }

    private AtomicInteger idGenerator = new AtomicInteger(0)

    private synchronized ActorRef createActor(String scenarioName) {
        def actorName = "Scenario_${scenarioName}"

        synchronized (systemLock) {
            if (!actorSystem) {
                def cl = this.class.classLoader
                actorSystem = ActorSystem.create("ForwardServer", ConfigFactory.load(cl), cl)
            }
        }

        ActorRef actorRef = actorSystem.actorFor("user/${actorName}")
        if (actorRef?.terminated) {
            actorRef = actorSystem.actorOf(Props.create(ScenarioActor, idGenerator.incrementAndGet(), scenarioName), actorName)

            bandWidthActor.tell(new Scenario(name: scenarioName), actorRef)
            //inform other actors
            actors?.each {
                it.value.tell(new Scenario(name: scenarioName), actorRef)
                actorRef.tell(new Scenario(name: it.key), it.value)
            }
            actors?.putIfAbsent(scenarioName, actorRef)
        }
        actorRef
    }

    private synchronized ActorRef getBandWidthActor() {
        synchronized (systemLock) {
            if (!actorSystem) {
                def cl = this.class.classLoader
                actorSystem = ActorSystem.create("ForwardServer", ConfigFactory.load(cl), cl)
            }
        }
        ActorRef actorRef = actorSystem.actorFor("user/bandWidth")
        if (actorRef?.terminated) {
            actorRef = actorSystem.actorOf(Props.create(BandWidthActor), 'bandWidth')
        }
        actorRef
    }

    def ignore() {
        render ''
    }

    def report() {
        def report = SystemConfig.lastReport.clone() as Map
        report.put('time', SystemConfig.currentFeedingStep.get())
        render(report as JSON)
    }

    def createTestActor() {
    }

    def data() {
        def report = SystemConfig.lastReport.clone() as Map

        report.put('step', SystemConfig.currentFeedingStep.get())

        def mean = SystemConfig.feedingStepsCount / 2
        //variance
        long n = 0;
        double m = 0;
        double s = 0.0;
        def population = 1..SystemConfig.feedingStepsCount
        population.each {
            n++;
            double delta = it - m
            m += delta / n;
            s += delta * (it - m)
        }
        def variance = s / n
        def result = [
                scenarios: []
        ]

        def numberOfUsers = SystemConfig.feedingConfig.split(',')[Math.round(SystemConfig.currentFeedingStep.get() / SystemConfig.feedingStepsCount).toInteger()].toInteger()
        def x = SystemConfig.currentFeedingStep.get() % SystemConfig.feedingStepsCount
        SystemConfig.scenarios.each {
            def average = (it.value.feedingWeight as Float) * numberOfUsers
            def scenario = [
                    url            : it.key,
                    responseTimeSLA: it.value.responseTimeSLA,
                    privateIP      : it.value.privateIP,
                    interface      : it.value.interfaceName,
                    users          : Math.round(Math.pow(Math.exp(-(((x - mean) * (x - mean)) / ((2 * variance)))), 1 / (0.1 * Math.sqrt(2 * Math.PI))) * average).toInteger(),
                    responseTime   : report.responseTimes?.get(it.key),
                    arrivalRate    : report.arrivalRates?.get(it.key) ?: 0,
                    violations     : report.violationsCount?.get(it.key) ?: 0
            ]
            result.scenarios << scenario
        }
        result.put('utilization', report.utilization)
        result.put('configuration', [
                totalBandwidth        : SystemConfig.totalBandwidth,
                adaptationSLARate     : SystemConfig.adaptationSLARate,
                coolingTime           : SystemConfig.coolingTime,
                arrivalRateWindowSize : Math.round(SystemConfig.arrivalRateWindowSize / 2)?.toInteger(),
                responseTimeWindowSize: Math.round(SystemConfig.responseTimeWindowSize / 2)?.toInteger()
        ])
        render(result as JSON)
    }

    def prepareSlave() {

        synchronized (systemLock) {
            if (!actorSystem) {
                def cl = this.class.classLoader
                actorSystem = ActorSystem.create("ForwardServer", ConfigFactory.load(cl), cl)
            }
        }

        render 'DONE'
    }


    def reset() {
        SystemConfig.currentFeedingStep.set(0)
        def cl = this.class.classLoader
        synchronized (systemLock) {
            actorSystem = ActorSystem.create("ForwardServer", ConfigFactory.load(cl), cl)
        }
        ScenarioActor.numberOfSLAViolations.set(0)
        ScenarioActor.numberOfAdaptations.set(0)
        ScenarioActor.numberOfFinishedAdaptations.set(0)
        ScenarioActor.lastResponseTimes = new ConcurrentHashMap<String, Integer>()
        ScenarioActor.lastArrivalRates = new ConcurrentHashMap<String, Integer>()
        ScenarioActor.adaptationsCount = new ConcurrentHashMap<String, Integer>()
        ScenarioActor.violationsCount = new ConcurrentHashMap<String, Integer>()
        BandWidthActor.bandWidths = new ConcurrentHashMap<String, Float>()
        BandWidthActor.realBandWidth = new ConcurrentHashMap<String, Float>()
        SystemConfig.scenarios.keySet().each {
            BandWidthActor.bandWidths.putIfAbsent(it, 1)
            BandWidthActor.realBandWidth.putIfAbsent(it, Math.round(SystemConfig.totalBandwidth))
            RemoteController.setBW(it, Math.round(SystemConfig.totalBandwidth)?.toInteger())
        }
        render "DONE!"
    }
}
