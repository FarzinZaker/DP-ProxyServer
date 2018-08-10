package web

import proxy.SystemConfig

class Configuration {

    String name = 'Default'
    String serverAddress
    String webHostIP
    String dataHostIP
    Integer totalBandwidth = 384000
    Boolean adaptationEnabled = true
    Float adaptationSLARate = 0.9f
    Integer coolingTime = 10
    Integer arrivalRateWindowSize = 10
    Integer responseTimeWindowSize = 10
    Integer scoringViolationsWeight = 5
    Integer scoringAdaptationsWeight = 1
    Boolean testArrivalRateModel = false
    Boolean testResponseTimeModel = false
    Boolean printStatistics = true
    Integer numberOfServedRequestsBetweenAdaptations = 100
    Integer numberOfServedRequestsBetweenViolationChecks = 100
    Integer simulationSpeed = 10
    Integer testDuration = 1000
    Integer feedingSpeed = 1
    Integer feedingStepsCount = 100
    String feedingConfig = '250,220,350,650,750'

    static hasMany = [responseTimeSLAs: ScenarioConfig, adaptationOptions: AdaptationOption]

    static constraints = {
        name(nullable: true)
        serverAddress(nullable: true)
        webHostIP(nullable: true)
        dataHostIP(nullable: true)
        totalBandwidth(nullable: true)
        adaptationEnabled()
        adaptationSLARate()
        coolingTime(nullable: true)
        numberOfServedRequestsBetweenAdaptations()
        numberOfServedRequestsBetweenViolationChecks()
        arrivalRateWindowSize()
        responseTimeWindowSize()
        scoringViolationsWeight()
        scoringAdaptationsWeight()
        simulationSpeed()
        testDuration()
        testArrivalRateModel()
        testResponseTimeModel()
        printStatistics()
        responseTimeSLAs()
        adaptationOptions()
        feedingSpeed(nullable: true)
        feedingStepsCount(nullable: true)
        feedingConfig(nullable: true)
    }

    def afterUpdate() {
        updateConfiguration()
    }

    def afterInsert() {
        updateConfiguration()
    }

    def updateConfiguration() {
        SystemConfig.serverAddress = serverAddress
        SystemConfig.webHostIP = webHostIP
        SystemConfig.dataHostIP = dataHostIP
        SystemConfig.adaptationEnabled = adaptationEnabled
        SystemConfig.totalBandwidth = (totalBandwidth ?: 384000)
        SystemConfig.adaptationSLARate = adaptationSLARate
        SystemConfig.coolingTime = (coolingTime ?: 10)
        SystemConfig.numberOfServedRequestsBetweenAdaptations = numberOfServedRequestsBetweenAdaptations
        SystemConfig.numberOfServedRequestsBetweenViolationChecks = numberOfServedRequestsBetweenViolationChecks
        SystemConfig.arrivalRateWindowSize = arrivalRateWindowSize
        SystemConfig.responseTimeWindowSize = responseTimeWindowSize
        SystemConfig.scoringViolationsWeight = scoringViolationsWeight
        SystemConfig.scoringAdaptationsWeight = scoringAdaptationsWeight
        SystemConfig.simulationSpeed = simulationSpeed
        SystemConfig.testDuration = testDuration
        SystemConfig.testArrivalRateModel = testArrivalRateModel
        SystemConfig.testResponseTimeModel = testResponseTimeModel
        SystemConfig.printStatistics = printStatistics
        SystemConfig.feedingSpeed = feedingSpeed
        SystemConfig.feedingStepsCount = feedingStepsCount
        SystemConfig.feedingConfig = feedingConfig

        SystemConfig.scenarios?.clear()
        responseTimeSLAs?.each {
            SystemConfig.scenarios.putIfAbsent(it.name, [responseTimeSLA: it.responseTimeSLA, publicIP: it.publicIP, privateIP: it.privateIP, interfaceName: it.interfaceName, minBandwidth: it.minBandwidth ?: 100, feedingWeight: it.feedingWeight ?: 0.25])
        }

        SystemConfig.adaptationOptions = adaptationOptions?.collect { it.value }
    }

    @Override
    String toString() {
        name
    }
}

