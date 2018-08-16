package web

class ScenarioConfig {

    String name
    Integer responseTimeSLA
    String interfaceName
    Configuration configuration

    static mapping = {
        sort 'interfaceName'
    }

    static constraints = {
        name()
        responseTimeSLA()
        interfaceName(nullable: true)
    }

    def afterUpdate() {
        configuration?.updateConfiguration()
    }

    def afterInsert() {
        configuration?.updateConfiguration()
    }

    @Override
    String toString() {
        "${name} [${responseTimeSLA}] - ${interfaceName}"
    }
}
