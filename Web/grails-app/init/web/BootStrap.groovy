package web

import grails.util.Environment
import proxy.RemoteController
import proxy.ScenarioActor
import proxy.SystemConfig


class BootStrap {

    def init = { servletContext ->
        Configuration.list()?.find()?.updateConfiguration()
        ScenarioActor.numberOfSLAViolations.set(0)
        ScenarioActor.numberOfAdaptations.set(0)
        ScenarioActor.numberOfFinishedAdaptations.set(0)

        if (new File('/etc/bwc/master')?.text?.trim() != '1') {
            Thread.start {
                Thread.sleep(10000)
                new URL('http://127.0.0.1:8080/prepareSlave').text
            }
            println "ROLE: SLAVE"
        } else {
            if (!Environment.isDevelopmentMode()) {
                SystemConfig.scenarios.keySet().each {
                    RemoteController.setBW(it, SystemConfig.totalBandwidth)
                }
            }
            println "ROLE: MASTER"
        }
    }
    def destroy = {
    }
}
