package web

class AdaptationOption {

    Float value
    Configuration configuration

    static mapping = {
        sort 'value'
    }

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
