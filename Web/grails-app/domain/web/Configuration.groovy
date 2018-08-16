package web

import proxy.SystemConfig

class Configuration {

    String name = 'Default'
    String feederAddress = '10.1.0.21'
    String proxyServerMasterAddress = '10.1.0.31'
    String proxyServerSlaveAddress = '10.1.0.32'
    String loadBalancerAddress = '10.1.0.41'
    String webServer1Address = '10.1.0.51'
    String webServer2Address = '10.1.0.52'
    String webServer3Address = '10.1.0.53'
    String databaseAddress = '10.1.0.61'
    Integer simulationSteps = 100

    static hasMany = [responseTimeSLAs: ScenarioConfig, adaptationOptions: AdaptationOption]

    static constraints = {
        name(nullable: true)
        feederAddress(nullable: true)
        proxyServerMasterAddress(nullable: true)
        proxyServerSlaveAddress(nullable: true)
        loadBalancerAddress(nullable: true)
        webServer1Address(nullable: true)
        webServer2Address(nullable: true)
        webServer3Address(nullable: true)
        databaseAddress(nullable: true)
        simulationSteps(inList: [1, 25, 50, 75, 100])
        responseTimeSLAs()
        adaptationOptions()
    }

    def afterUpdate() {
        updateConfiguration()
    }

    def afterInsert() {
        updateConfiguration()
    }

    def updateConfiguration() {
        SystemConfig.feederAddress = feederAddress
        SystemConfig.proxyServerMasterAddress = proxyServerMasterAddress
        SystemConfig.proxyServerSlaveAddress = proxyServerSlaveAddress
        SystemConfig.loadBalancerAddress = load()
        SystemConfig.webServer1Address = webServer1Address
        SystemConfig.webServer2Address = webServer2Address
        SystemConfig.webServer3Address = webServer3Address
        SystemConfig.databaseAddress = databaseAddress

        SystemConfig.simulationSteps = simulationSteps

        SystemConfig.scenarios?.clear()
        responseTimeSLAs?.each {
            SystemConfig.scenarios.putIfAbsent(it.name, [responseTimeSLA: it.responseTimeSLA, interfaceName: it.interfaceName])
        }

        SystemConfig.adaptationOptions = adaptationOptions?.collect { it.value }
    }

    @Override
    String toString() {
        name
    }
}

