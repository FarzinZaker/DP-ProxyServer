package web

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class AdaptationOptionController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond AdaptationOption.list(params), model:[adaptationOptionCount: AdaptationOption.count()]
    }

    def show(AdaptationOption adaptationOption) {
        respond adaptationOption
    }

    def create() {
        respond new AdaptationOption(params)
    }

    @Transactional
    def save(AdaptationOption adaptationOption) {
        if (adaptationOption == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (adaptationOption.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond adaptationOption.errors, view:'create'
            return
        }

        adaptationOption.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'adaptationOption.label', default: 'AdaptationOption'), adaptationOption.id])
                redirect adaptationOption
            }
            '*' { respond adaptationOption, [status: CREATED] }
        }
    }

    def edit(AdaptationOption adaptationOption) {
        respond adaptationOption
    }

    @Transactional
    def update(AdaptationOption adaptationOption) {
        if (adaptationOption == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (adaptationOption.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond adaptationOption.errors, view:'edit'
            return
        }

        adaptationOption.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'adaptationOption.label', default: 'AdaptationOption'), adaptationOption.id])
                redirect adaptationOption
            }
            '*'{ respond adaptationOption, [status: OK] }
        }
    }

    @Transactional
    def delete(AdaptationOption adaptationOption) {

        if (adaptationOption == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        adaptationOption.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'adaptationOption.label', default: 'AdaptationOption'), adaptationOption.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'adaptationOption.label', default: 'AdaptationOption'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
