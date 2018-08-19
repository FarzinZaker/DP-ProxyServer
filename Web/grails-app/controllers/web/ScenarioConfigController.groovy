package web

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class ScenarioConfigController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond ScenarioConfig.list(params), model:[scenarioConfigCount: ScenarioConfig.count()]
    }

    def show(ScenarioConfig scenarioConfig) {
        respond scenarioConfig
    }

    def create() {
        respond new ScenarioConfig(params)
    }

    @Transactional
    def save(ScenarioConfig scenarioConfig) {
        if (scenarioConfig == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (scenarioConfig.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond scenarioConfig.errors, view:'create'
            return
        }

        scenarioConfig.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'scenarioConfig.label', default: 'ScenarioConfig'), scenarioConfig.id])
                redirect scenarioConfig
            }
            '*' { respond scenarioConfig, [status: CREATED] }
        }
    }

    def edit(ScenarioConfig scenarioConfig) {
        respond scenarioConfig
    }

    @Transactional
    def update(ScenarioConfig scenarioConfig) {
        if (scenarioConfig == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (scenarioConfig.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond scenarioConfig.errors, view:'edit'
            return
        }

        scenarioConfig.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'scenarioConfig.label', default: 'ScenarioConfig'), scenarioConfig.id])
                redirect scenarioConfig
            }
            '*'{ respond scenarioConfig, [status: OK] }
        }
    }

    @Transactional
    def delete(ScenarioConfig scenarioConfig) {

        if (scenarioConfig == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        scenarioConfig.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'scenarioConfig.label', default: 'ScenarioConfig'), scenarioConfig.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'scenarioConfig.label', default: 'ScenarioConfig'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
