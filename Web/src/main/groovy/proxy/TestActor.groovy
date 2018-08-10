package proxy

import akka.actor.ActorRef
import akka.actor.UntypedAbstractActor
import grails.converters.JSON
import org.h2.mvstore.ConcurrentArrayList
import org.springframework.context.annotation.Scope
import proxy.messages.*

/**
 * Created by root on 8/14/17.
 */
import javax.inject.Named
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

@Named("TestActor")
@Scope("prototype")
class TestActor extends UntypedAbstractActor {

    private String adaptationId
    private String scenarioName
    private Boolean isLeader
    private ConcurrentHashMap<String, Map> scenarios
    private Float currentBandWidth
    private Float option
    private ActorRef leader
    private Integer scenariosCount
    private ActorRef parent

//    TestActor(Map parametersMap) {
//        akka.serialization.Serialization.currentSystem.withValue(context.system) {
//            def parameters = Serializer.loadObject(parametersMap.bytes) as Object[]
//            this.adaptationId = parameters[0]
//            this.scenarioName = parameters[1]
//            this.currentBandWidth = parameters[2]
//            this.option = parameters[3]
//            this.leader = parameters[4] ?: self
//            this.scenariosCount = parameters[5]
//            this.isLeader = parameters[6]
//            this.scenarios = parameters[7]
//            this.arrivalRateModel = parameters[8]
//            this.responseTimeModel = parameters[9]
//            this.arrivalRates = parameters[10]
//            this.responseTimes = parameters[11]
//            this.serversCount = parameters[12]
//            this.bandWidths = parameters[13]
//            this.lastRequests = parameters[14]
//            this.parent = parameters[15]
//        }
//    }

    TestActor(
            String adaptationId,
            String scenarioName,
            Float initialBandWidth,
            Float option,
            ActorRef leader,
            Integer scenariosCount,
            Boolean isLeader,
            ConcurrentHashMap<String, Map> scenarios,
            PredictionModel arrivalRateModel,
            PredictionModel responseTimeModel,
            ConcurrentHashMap<String, Integer> arrivalRates,
            ConcurrentHashMap<String, Integer> responseTimes,
            AtomicInteger serversCount,
            ConcurrentHashMap<String, Integer> bandWidths,
            ConcurrentLinkedQueue<Long> lastRequests,
            ActorRef parent) {
        this.adaptationId = adaptationId
        this.scenarioName = scenarioName
        this.currentBandWidth = initialBandWidth
        this.option = option
        this.leader = leader ?: self
        this.scenariosCount = scenariosCount
        this.isLeader = isLeader
        this.scenarios = scenarios
        this.arrivalRateModel = arrivalRateModel
        this.responseTimeModel = responseTimeModel
        this.serversCount = serversCount
        this.arrivalRates = arrivalRates
        this.responseTimes = responseTimes
        this.bandWidths = bandWidths
        this.lastRequests = lastRequests
        this.parent = parent
    }

    private ConcurrentLinkedQueue<Long> lastRequests
    private ownResponseTimes = new ConcurrentLinkedQueue<Integer>()

    private ConcurrentHashMap<String, Integer> arrivalRates
    private ConcurrentHashMap<String, Integer> responseTimes
    private AtomicInteger serversCount
    private ConcurrentHashMap<String, Float> bandWidths
    private actors = new ConcurrentHashMap<String, ActorRef>()

    private PredictionModel arrivalRateModel
    private PredictionModel responseTimeModel

    private lastArrivalRate = 0
    private lastResponseTime = 0

    public AtomicInteger numberOfSLAViolations = new AtomicInteger(0)
    public AtomicInteger numberOfAdaptations = new AtomicInteger(0)

    private numberOfReceivedScores = new AtomicInteger(0)
    private numberOfSLAViolationsList = new ConcurrentArrayList<Integer>()
    private numberOfAdaptationsList = new ConcurrentArrayList<Integer>()

    @Override
    void postRestart(Throwable reason) {
        println(reason.message)
    }


    @Override
    void onReceive(Object message) {

        try {
            //request
            if (message instanceof Request)
                handleMessage(message as Request)

            //scenario
            else if (message instanceof Scenario) {
                handleScenario(message as Scenario)
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

            //band width
            else if (message instanceof Bandwidth) {
                Bandwidth bandwidth = message
                bandWidths.put(bandwidth.scenario, bandwidth.value)
            }

            //inform leader
            else if (message.toString() == 'INFORM_LEADER') {
                if (!leader)
                    leader = self
                leader.tell(new Scenario(name: scenarioName), self)
            }

            //feed
            else if (message?.toString() == 'START') {
                start()
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
        catch (exception) {
            println "ERROR: ${exception.message}"
        }
    }

    private synchronized void handleScenario(Scenario scenario) {
        actors.putIfAbsent(scenario.name, sender)
        arrivalRates.putIfAbsent(scenario.name, 0)
        responseTimes.putIfAbsent(scenario.name, 0)
        bandWidths.putIfAbsent(scenario.name, 0)

        if (actors.keySet().size() == scenariosCount) {
            actors.values().each { ActorRef actor ->
                actor.tell('START', self)
            }
        }
    }

    private void handleMessage(Request request) {

        //calculate arrival rate
        if (lastRequests.size() > SystemConfig.arrivalRateWindowSize)
            lastRequests.remove()
        lastRequests.add(new Date().time)
        def times = lastRequests?.collect { it.longValue() }
        def arrivalRate = 0
        if (times?.size() > 1 && times.max() > times.min()) {
            arrivalRate = Math.round(times.size() * 1000 / (times.max() - times.min()))
            arrivalRate = Math.round(arrivalRate / SystemConfig.simulationSpeed)?.toInteger()

            //send arrival rate
            actors?.values()?.each {
                it.tell(new ArrivalRate(scenario: scenarioName, rate: arrivalRate), self)
            }
        }

        //test response time model
        def predictedResponseTime = responseTimeModel.predictNext(
                arrivalRates?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastArrivalRate ?: 0],
                responseTimes?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastResponseTime ?: 0],
                serversCount?.get())

        //forward message
        def response = [
                content: 'Test',
                time   : predictedResponseTime
        ]
        sender.tell(response.content, null)

        //send response time
        actors?.values()?.each {
            it.tell(new Bandwidth(scenario: scenarioName, value: currentBandWidth), self)
            it.tell(new ResponseTime(scenario: scenarioName, time: response.time), self)
        }

        //store last records
        lastArrivalRate = arrivalRate ?: 0
        lastResponseTime = response.time ?: 0

        //check for adaptation
        if (ownResponseTimes.size() > SystemConfig.responseTimeWindowSize)
            ownResponseTimes.remove()
        ownResponseTimes.add(response.time)
        def averageResponseTime = ownResponseTimes.sum() / ownResponseTimes.size()
        if (averageResponseTime > (scenarios[scenarioName].responseTimeSLA as Integer) * SystemConfig.adaptationSLARate) {
            if (averageResponseTime > (scenarios[scenarioName].responseTimeSLA as Integer))
                numberOfSLAViolations.incrementAndGet()
            numberOfAdaptations.incrementAndGet()
        }
    }

    private Date startTime

    private void start() {
        startTime = new Date()
        feed()
    }

    private void feed() {
        if (new Date().time - startTime.time < SystemConfig.testDuration / SystemConfig.simulationSpeed) {
            def predictedArrivalRate = arrivalRateModel.predictNext(
                    arrivalRates?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastArrivalRate ?: 0],
                    responseTimes?.collect { [key: it.key, value: it.value] }?.sort { it.key }?.collect { it.value } + [lastResponseTime ?: 0],
                    serversCount?.get()) ?: 1
            predictedArrivalRate = predictedArrivalRate * option// / (BandWidthActor.bandWidths[scenarioName] ?: 1)

            predictedArrivalRate.each {
                self.tell(new Request(), self)
            }
            Thread.sleep(Math.round(1000 / SystemConfig.simulationSpeed)?.toInteger())
            //feed
            feed()
        } else {
            sendScore()
        }
    }

    private void sendScore() {
        def score = new Score(numberOfSLAViolations: numberOfSLAViolations.get(), numberOfAdaptations: numberOfAdaptations.get())
        if (!leader)
            leader = self
        leader.tell(score, self)
    }

    private void handleScore(Score score) {

        numberOfSLAViolationsList.add(score.numberOfSLAViolations)
        numberOfAdaptationsList.add(score.numberOfAdaptations)

        //finish
        if (numberOfReceivedScores.incrementAndGet() == actors.size()) {
            def totalNumberOfSLAViolations = 0
            numberOfSLAViolationsList?.each { totalNumberOfSLAViolations += it }
            def totalNumberOfAdaptations = 0
            numberOfAdaptationsList?.each { totalNumberOfAdaptations += it }
            parent.tell(new Score(numberOfSLAViolations: totalNumberOfSLAViolations as Integer, numberOfAdaptations: totalNumberOfAdaptations as Integer, option: option, adaptationId: adaptationId), self)
        }

    }
}

