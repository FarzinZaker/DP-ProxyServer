package proxy

import akka.actor.*
import akka.remote.RemoteScope
import grails.converters.JSON
import org.h2.mvstore.ConcurrentArrayList
import org.springframework.context.annotation.Scope
import proxy.messages.*
import reactor.jarjar.jsr166e.extra.AtomicDouble
import web.UtilizationJob

import javax.inject.Named;

/**
 * Created by root on 8/14/17.
 */
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Named("ScenarioActor")
@Scope("prototype")
class ScenarioActor extends UntypedAbstractActor {

    private Integer id
    private String scenarioName

    ScenarioActor(Integer id, String scenarioName) {
        this.id = id
        this.scenarioName = scenarioName
        arrivalRateModel = new PredictionModel()
        responseTimeModel = new PredictionModel()
        violationsCount.putIfAbsent(scenarioName, 0)
        adaptationsCount.putIfAbsent(scenarioName, 0)
    }

    static lastResponseTimes = new ConcurrentHashMap<String, Integer>()
    static lastArrivalRates = new ConcurrentHashMap<String, Integer>()
    static adaptationsCount = new ConcurrentHashMap<String, Integer>()
    static violationsCount = new ConcurrentHashMap<String, Integer>()
    static serversCount = new AtomicInteger(1)

    private lastRequests = new ConcurrentLinkedQueue<Long>()
    private ownResponseTimes = new ConcurrentLinkedQueue<Integer>()

    private arrivalRates = new ConcurrentHashMap<String, Integer>()
    private responseTimes = new ConcurrentHashMap<String, Integer>()
    private bandWidths = new ConcurrentHashMap<String, Float>()
    private actors = new ConcurrentHashMap<String, ActorRef>()
    private lastArrivalRate = 0
    private lastResponseTime = 0

    private PredictionModel arrivalRateModel
    private PredictionModel responseTimeModel

    private arrivalRateVariances = new ConcurrentHashMap<Integer, Integer>()
    private responseTimeVariances = new ConcurrentHashMap<Integer, Integer>()

    static AtomicBoolean adapting = new AtomicBoolean(false)
    static AtomicInteger lastAdaptationTime = new AtomicInteger(0)
    private AtomicInteger numberOfRequestsServedAfterAdaptation = new AtomicInteger(0)
    private AtomicInteger numberOfRequestsServedAfterViolation = new AtomicInteger(0)
    private AtomicLong numberOfSecondsAfterAdaptation = new AtomicLong(new Date().time)
    private AtomicLong numberOfSecondsAfterViolation = new AtomicLong(new Date().time)

    public static AtomicInteger numberOfSLAViolations = new AtomicInteger(0)
    public static AtomicInteger numberOfAdaptations = new AtomicInteger(0)
    public static AtomicLong numberOfRequests = new AtomicLong(0)
    public static AtomicLong numberOfDeliveredRequests = new AtomicLong(0)
    public static AtomicInteger numberOfFinishedAdaptations = new AtomicInteger(0)
    public static AtomicDouble proxyServerMasterUtilization = new AtomicDouble(0)
    public static AtomicDouble proxyServerSlaveUtilization = new AtomicDouble(0)
    public static AtomicDouble loadBalancerUtilization = new AtomicDouble(0)
    public static AtomicDouble databaseUtilization = new AtomicDouble(0)
    public static AtomicDouble webServer1Utilization = new AtomicDouble(0)
    public static AtomicDouble webServer2Utilization = new AtomicDouble(0)
    public static AtomicDouble webServer3Utilization = new AtomicDouble(0)

    private testActors = new ConcurrentHashMap<String, ConcurrentHashMap<Float, ActorRef>>()

    private numberOfSLAViolationsInTest = new ConcurrentHashMap<String, ConcurrentHashMap<Float, Integer>>()
    private numberOfAdaptationsInTest = new ConcurrentHashMap<String, ConcurrentHashMap<Float, Integer>>()

    private Date adaptationStartTime
    public static ConcurrentArrayList<Long> adaptationTimes = new ConcurrentArrayList<>()
    public static AtomicDouble averageAdaptationTime = new AtomicDouble(0)

    @Override
    void onReceive(Object message) throws Exception {

        //request
        if (message instanceof Request)
            handleMessage(message as Request)

        //adaptation request
        else if (message instanceof AdaptationRequest)
            handleAdaptationRequest(message as AdaptationRequest)

        //scenario
        else if (message instanceof Scenario) {
            Scenario scenario = message
            actors.putIfAbsent(scenario.name, sender)
            arrivalRates.putIfAbsent(scenario.name, 0)
            responseTimes.putIfAbsent(scenario.name, 0)
            bandWidths.putIfAbsent(scenario.name, 1.0f)
        }

        //request rate
        else if (message instanceof ArrivalRate) {
            ArrivalRate requestRate = message
            arrivalRates.put(requestRate.scenario, requestRate.rate)
        }

        //response time
        else if (message instanceof ResponseTime) {
            ResponseTime responseTime = message
            responseTimes.put(responseTime.scenario, responseTime.time)
        }

        //response time
        else if (message instanceof OwnResponseTime) {
            handleOwnResponseTime((message as OwnResponseTime).value)
        }

        //band width
        else if (message instanceof Bandwidth) {
            Bandwidth bandwidth = message
            bandWidths.put(bandwidth.scenario, bandwidth.value)
        }

        //score
        else if (message instanceof Score) {
            handleScore(message as Score)
        }

        //unhandled
        else {
            unhandled(message)
        }
    }

    private void handleMessage(Request request) {
        if (adapting.get()) {
            sender.tell('ADAPTING', null)
            return
        }

        numberOfRequests.incrementAndGet()
        //calculate arrival rate
        if (lastRequests.size() > SystemConfig.arrivalRateWindowSize)
            lastRequests.remove()
        lastRequests.add(new Date().time)
        def times = lastRequests?.collect { it.longValue() }
        def arrivalRate = 0
        def now = new Date().time
        if (times?.size() > 1 && now > times.min()) {
            arrivalRate = Math.round(times.size() * 1000 / (now - times.min()))?.toInteger()

            //send arrival rate
            actors?.values()?.each {
                it.tell(new ArrivalRate(scenario: scenarioName, rate: arrivalRate), self)
                it.tell(new Bandwidth(scenario: scenarioName, value: BandWidthActor.bandWidths[scenarioName]), self)
            }

            lastArrivalRates.put(scenarioName, arrivalRate)
        }

        //forward message
        def response = new ScenarioService(userRequest: request).handle()
        sender.tell(response, null)

        //train models
        if (arrivalRates?.size() == actors?.size() && responseTimes?.size() == actors?.size() && bandWidths?.size() == actors?.size()) {
            arrivalRateModel.train(
                    arrivalRates?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastArrivalRate ?: 0],
                    responseTimes?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastResponseTime ?: 0],
                    serversCount?.get(),
                    arrivalRate ?: 0)
        }

        //store last records
        lastArrivalRate = arrivalRate ?: 0
    }

    private void startAdaptation() {

        adaptationStartTime = new Date()

        //setup adaptation options
        def optionsMap = new ConcurrentHashMap<Float, ConcurrentHashMap<String, Float>>()
        def bws = BandWidthActor.bandWidths
        SystemConfig.adaptationOptions?.each { option ->
            Float nextBandWidth = 1 / bws[scenarioName]
            optionsMap.putIfAbsent(option, new ConcurrentHashMap<String, Float>())
            actors?.each { actor ->
                def scenario = actor.key
                def bw = bws[scenario] / option
                optionsMap[option].putIfAbsent(scenario, bw.toFloat())
            }
            optionsMap[option].putIfAbsent(scenarioName, nextBandWidth)
        }


        def adaptationRequest = new AdaptationRequest(
                priority: Math.round(lastResponseTime * 100 / (SystemConfig.scenarios[scenarioName].responseTimeSLA as Integer)) * 100 + id,
                options: optionsMap,
                scenariosCount: actors?.keySet()?.size())
        createTestActors(adaptationRequest, true)
        adaptationRequest.leaders = testActors.get(adaptationRequest.id)

        actors?.values()?.each { ActorRef actor ->
            actor.tell(adaptationRequest, self)
        }
    }

    private void handleAdaptationRequest(AdaptationRequest adaptationRequest) {

        if (sender.path() != self.path())
            createTestActors(adaptationRequest)
        testActors.get(adaptationRequest.id).values().each { ActorRef testActor ->
            testActor.tell('INFORM_LEADER', self)
        }
    }

    private void createTestActors(AdaptationRequest adaptationRequest, Boolean isLeader = false) {

        testActors.putIfAbsent(adaptationRequest.id, new ConcurrentHashMap<Float, ActorRef>())
        adaptationRequest.options.each { option ->

            def server = UtilizationJob.getLocalCPUUtilization() < 90 * SystemConfig.proxyServerCoresCount ? SystemConfig.proxyServerMasterAddress : SystemConfig.proxyServerSlaveAddress

            Address address = new Address("akka.tcp", "ForwardServer", server, 2552)

            try {
                def testActor = context.actorOf(Props.create(
                        TestActor,
                        adaptationRequest.id,
                        scenarioName,
                        option.value[scenarioName],
                        option.key as Float,
                        adaptationRequest.leaders?.get(option.key),
                        adaptationRequest.scenariosCount,
                        isLeader,
                        SystemConfig.scenarios,
                        arrivalRateModel,
                        responseTimeModel,
                        arrivalRates,
                        responseTimes,
                        serversCount,
                        bandWidths,
                        lastRequests,
                        self
                ).withDeploy(new Deploy(new RemoteScope(address))), UUID.randomUUID().toString())

                testActors.get(adaptationRequest.id).put(option.key as Float, testActor)

            } catch (exception) {
                exception.printStackTrace()
            }
        }
    }

    private void handleScore(Score score) {

        numberOfSLAViolationsInTest.putIfAbsent(score.adaptationId, new ConcurrentHashMap<Float, Integer>())
        numberOfSLAViolationsInTest.get(score.adaptationId).putIfAbsent(score.option, score.numberOfSLAViolations)


        numberOfAdaptationsInTest.putIfAbsent(score.adaptationId, new ConcurrentHashMap<Float, Integer>())
        numberOfAdaptationsInTest.get(score.adaptationId).putIfAbsent(score.option, score.numberOfAdaptations)

        if (SystemConfig.adaptationOptions.size() == numberOfAdaptationsInTest.get(score.adaptationId)?.size() &&
                SystemConfig.adaptationOptions.size() == numberOfSLAViolationsInTest.get(score.adaptationId)?.size()) {

            applyAdaptation(score.adaptationId)

            if (score.numberOfSLAViolations > 2)
                increaseNumberOfServers()
            else if (score.numberOfSLAViolations == 0)
                decreaseNumberOfServers()

            numberOfSLAViolationsInTest.remove(score.adaptationId)
            numberOfAdaptationsInTest.remove(score.adaptationId)
            testActors.remove(score.adaptationId)
            adapting.set(false)
            lastAdaptationTime.set(SystemConfig.currentFeedingStep.get())
            def duration = new Date().time - adaptationStartTime.time
            adaptationTimes.add(duration)

            def sum = 0
            def count = 0
            adaptationTimes.each {
                sum += it
                count++
            }
            averageAdaptationTime.set(sum / (count + 1))
        }
    }

    private void increaseNumberOfServers() {
        if (serversCount?.get() == 1) {
            RemoteController.executeCommand('', "sudo sed -i 's/#server 10.1.0.52:8080/server 10.1.0.52:8080/g' /etc/nginx/nginx.conf")
            serversCount.incrementAndGet()
            println "SERVERS COUNT INCREASED: ${serversCount?.get()}"
        } else if (serversCount?.get() == 2) {
            RemoteController.executeCommand('', "sudo sed -i 's/#server 10.1.0.53:8080/server 10.1.0.53:8080/g' /etc/nginx/nginx.conf")
            serversCount.incrementAndGet()
            println "SERVERS COUNT INCREASED: ${serversCount?.get()}"
        }
    }

    private void decreaseNumberOfServers() {
        if (serversCount?.get() == 2) {
            RemoteController.executeCommand('', "sudo sed -i 's/server 10.1.0.52:8080/#server 10.1.0.52:8080/g' /etc/nginx/nginx.conf")
            serversCount.decrementAndGet()
            println "SERVERS COUNT DECREASED: ${serversCount?.get()}"
        } else if (serversCount?.get() == 3) {
            RemoteController.executeCommand('', "sudo sed -i 's/server 10.1.0.53:8080/#server 10.1.0.53:8080/g' /etc/nginx/nginx.conf")
            serversCount.decrementAndGet()
            println "SERVERS COUNT DECREASED: ${serversCount?.get()}"
        }
    }

    private void applyAdaptation(String adaptationId) {

        Float selectedAdaptationOption
        def selectedAdaptationScore = Integer.MAX_VALUE
        numberOfAdaptationsInTest[adaptationId].each { testResult ->
            if (!selectedAdaptationOption)
                selectedAdaptationOption = testResult.key

            def score = testResult.value * SystemConfig.scoringAdaptationsWeight + numberOfSLAViolationsInTest[adaptationId][testResult.key] * SystemConfig.scoringViolationsWeight
            if (score < selectedAdaptationScore) {
                selectedAdaptationOption = testResult.key
                selectedAdaptationScore = score
            }
        }

        //ask others to update their bandwidth
        def newBandWidths = new ConcurrentHashMap<String, Float>()
        def bws = BandWidthActor.bandWidths
        Float nextBandWidth = 1
        actors?.each { actor ->
            def scenario = actor.key
            def bw = bws[scenario] / selectedAdaptationOption
            newBandWidths.putIfAbsent(scenario, bw.toFloat())
        }
        newBandWidths.putIfAbsent(scenarioName, nextBandWidth)
        bandWidthActor.tell(new BandWidthData(bandWidths: newBandWidths), self)
    }

    private void handleOwnResponseTime(Integer responseTime) {

        numberOfDeliveredRequests.incrementAndGet()

        //train model
        responseTimeModel.train(
                arrivalRates?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastArrivalRate ?: 0],
                responseTimes?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastResponseTime ?: 0],
                serversCount?.get(),
                responseTime ?: 0)
        lastResponseTime = responseTime ?: 0

        //send response time
        actors?.values()?.each {
            it.tell(new ResponseTime(scenario: scenarioName, time: responseTime), self)
        }

        //check for adaptation
        if (ownResponseTimes.size() > SystemConfig.responseTimeWindowSize)
            ownResponseTimes.remove()
//        if (!adapting.get())
        ownResponseTimes.add(responseTime)
        def averageResponseTime = ownResponseTimes.sum() / ownResponseTimes.size()
        lastResponseTimes.put(scenarioName, Math.round(averageResponseTime).toInteger())
        def now = new Date().time

        if (now - numberOfSecondsAfterViolation.get() > SystemConfig.numberOfServedRequestsBetweenViolationChecks * 1000 && averageResponseTime > (SystemConfig.scenarios[scenarioName].responseTimeSLA as Integer) && lastAdaptationTime.get() + SystemConfig.coolingTime < SystemConfig.currentFeedingStep.get() && !adapting.get()) {
            numberOfSecondsAfterViolation.set(now)
            numberOfSLAViolations.incrementAndGet()
            violationsCount.put(scenarioName, violationsCount.get(scenarioName) + 1)
        }

        if (loadBalancerUtilization.get() < 90 && now - numberOfSecondsAfterAdaptation.get() > SystemConfig.numberOfServedRequestsBetweenAdaptations * 1000 && averageResponseTime > (SystemConfig.scenarios[scenarioName].responseTimeSLA as Integer) * SystemConfig.adaptationSLARate && lastAdaptationTime.get() + SystemConfig.coolingTime < SystemConfig.currentFeedingStep.get() && adapting.compareAndSet(false, true)) {
            numberOfSecondsAfterAdaptation.set(now)
            adaptationsCount.put(scenarioName, adaptationsCount.get(scenarioName) + 1)
            startAdaptation()
        }
    }

    private synchronized ActorRef getBandWidthActor() {
        ActorRef actorRef = context.system.actorFor("user/bandWidth")
        if (actorRef?.terminated) {
            actorRef = context.system.actorOf(Props.create(BandWidthActor), 'bandWidth')
        }
        actorRef
    }
}

