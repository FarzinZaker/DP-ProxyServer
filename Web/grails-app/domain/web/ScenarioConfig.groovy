package web

class ScenarioConfig {

    String name
    Integer responseTimeSLA
    String privateIP
    String publicIP
    String interfaceName
    Integer minBandwidth
    Float feedingWeight
    Configuration configuration

    static constraints = {
        name()
        responseTimeSLA()
        privateIP()
        publicIP(nullable: true)
        interfaceName(nullable: true)
        minBandwidth(nullable: true)
        feedingWeight(nullable: true)
    }

    def afterUpdate() {
        configuration?.updateConfiguration()
    }

    def afterInsert() {
        configuration?.updateConfiguration()
    }

    @Override
    String toString() {
        "${name}[${responseTimeSLA}]: ${publicIP} - ${interfaceName} - ${feedingWeight}"
    }
}
