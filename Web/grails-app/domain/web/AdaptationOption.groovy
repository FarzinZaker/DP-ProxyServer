package web

class AdaptationOption {

    Float value
    Configuration configuration

    static constraints = {
        value()
    }

    def afterUpdate() {
        configuration?.updateConfiguration()
    }

    def afterInsert() {
        configuration?.updateConfiguration()
    }

    @Override
    String toString(){
        value
    }
}
