package proxy

import grails.util.Environment

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by root on 8/31/17.
 */
class SystemConfig {

    static ConcurrentHashMap<String, Map> scenarios = [
            'user$select': [responseTimeSLA: 200, publicIP: '', privateIP: '192.180.253.1', interfaceName: 'p0', minBandwidth: 100, feedingWeight: 0.5],
            'user$update': [responseTimeSLA: 200, publicIP: '', privateIP: '192.180.252.1', interfaceName: 'p1', minBandwidth: 100, feedingWeight: 0.35],
            'user$insert': [responseTimeSLA: 200, publicIP: '', privateIP: '192.180.253.2', interfaceName: 'p0', minBandwidth: 100, feedingWeight: 0.1],
            'user$pi'    : [responseTimeSLA: 200, publicIP: '', privateIP: '192.180.252.2', interfaceName: 'p1', minBandwidth: 100, feedingWeight: 0.05]
    ]
    static serverAddress = 'http://localhost:8080'
    static webHostIP = ''
    static dataHostIP = ''
    static totalBandwidth = 384000
    static adaptationEnabled = true
    static adaptationOptions = [0.75f, 1.0f, 1.25f, 1.5f]
    static adaptationSLARate = 0.9
    static coolingTime = 10
    static arrivalRateWindowSize = 10
    static responseTimeWindowSize = 10
    static scoringViolationsWeight = 5
    static scoringAdaptationsWeight = 1
    static testArrivalRateModel = false
    static testResponseTimeModel = false
    static printStatistics = true
    static numberOfServedRequestsBetweenAdaptations = 100
    static numberOfServedRequestsBetweenViolationChecks = 100

    static Integer simulationSpeed = 10
    static Long testDuration = 1000

    static Integer feedingSpeed = 1
    static Integer feedingStepsCount = 100
    static String feedingConfig = '250,22,350,650,750'

    static AtomicInteger currentFeedingStep = new AtomicInteger(0)
    static def lastReport = [:]


    static String getFeederAddress() {
        Environment.isDevelopmentMode() ? '35.170.51.118' : '10.1.0.21'
    }

    static String getProxyServerMasterAddress() {
        Environment.isDevelopmentMode() ? '127.0.0.1' : '10.1.0.31'
    }

    static String getProxyServerSlaveAddress() {
        Environment.isDevelopmentMode() ? '18.213.192.91' : '10.1.0.32'
    }

    static String getLoadBalancerAddress() {
        Environment.isDevelopmentMode() ? '34.234.172.129' : '10.1.0.41'
    }

    static String getWebServer1Address() {
        Environment.isDevelopmentMode() ? '34.201.17.235' : '10.1.0.51'
    }

    static String getWebServer2Address() {
        Environment.isDevelopmentMode() ? '35.172.33.107' : '10.1.0.52'
    }

    static String getWebServer3Address() {
        Environment.isDevelopmentMode() ? '34.200.231.169' : '10.1.0.53'
    }

    static String getDatabaseAddress() {
        Environment.isDevelopmentMode() ? '34.205.174.86' : '10.1.0.61'
    }

    static Integer getProxyServerCoresCount() {
        4
    }

//    static List<String> getScenarios() {
//        scenarios.keySet()?.toList()
//    }

    static String getInterface(String scenario) {
        scenarios[scenario].interfaceName
    }
}
