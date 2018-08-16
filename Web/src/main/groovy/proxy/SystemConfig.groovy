package proxy

import grails.util.Environment

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by root on 8/31/17.
 */
class SystemConfig {

    //configuration Options
    static feederAddress = '10.1.0.21'
    static proxyServerMasterAddress = '10.1.0.31'
    static proxyServerSlaveAddress = '10.1.0.32'
    static loadBalancerAddress = '10.1.0.41'
    static webServer1Address = '10.1.0.51'
    static webServer2Address = '10.1.0.52'
    static webServer3Address = '10.1.0.53'
    static databaseAddress = '10.1.0.61'
    static ConcurrentHashMap<String, Map> scenarios = [
            'basket' : [responseTimeSLA: 500, interfaceName: 'p2'],
            'product': [responseTimeSLA: 800, interfaceName: 'p1'],
            'browse' : [responseTimeSLA: 1200, interfaceName: 'p0'],
            'static' : [responseTimeSLA: 1000, interfaceName: 'p3']
    ]
    static adaptationOptions = [0.25f, 0.5f, 0.75f, 1.0f]
    static Long simulationSteps = 100

    //algorithm parameters (do not modify)
    static totalBandwidth = 384000
    static adaptationSLARate = 0.9
    static coolingTime = 10
    static proxyServerCoresCount = 4
    static arrivalRateWindowSize = 10
    static responseTimeWindowSize = 10
    static scoringViolationsWeight = 5
    static scoringAdaptationsWeight = 1
    static numberOfServedRequestsBetweenAdaptations = 100
    static numberOfServedRequestsBetweenViolationChecks = 100
    static Integer simulationSpeed = 10
    static Integer feedingSpeed = 3600
    static Integer feedingStepsCount = 720

    //runtime flags
    static AtomicInteger currentFeedingStep = new AtomicInteger(0)
    static def lastReport = [:]

}
