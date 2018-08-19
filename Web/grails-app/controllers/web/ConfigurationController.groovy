package web

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class ConfigurationController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Configuration.list(params), model:[configurationCount: Configuration.count()]
    }

    def show(Configuration configuration) {
        respond configuration
    }

    def create() {
        respond new Configuration(params)
    }

    @Transactional
    def save(Configuration configuration) {
        if (configuration == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (configuration.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond configuration.errors, view:'create'
            return
        }

        configuration.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'configuration.label', default: 'Configuration'), configuration.id])
                redirect configuration
            }
            '*' { respond configuration, [status: CREATED] }
        }
    }

    def edit(Configuration configuration) {
        respond configuration
    }

    @Transactional
    def update(Configuration configuration) {
        if (configuration == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (configuration.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond configuration.errors, view:'edit'
            return
        }

        configuration.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'configuration.label', default: 'Configuration'), configuration.id])
                redirect configuration
            }
            '*'{ respond configuration, [status: OK] }
        }
    }

    @Transactional
    def delete(Configuration configuration) {

        if (configuration == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        configuration.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'configuration.label', default: 'Configuration'), configuration.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'configuration.label', default: 'Configuration'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
