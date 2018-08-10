package web

import grails.util.Environment
import proxy.RemoteController
import proxy.BandWidthActor
import proxy.ScenarioActor
import proxy.SystemConfig

import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FeedJob {
    def concurrent = false

    static triggers = {
        simple repeatInterval: 1000l, startDelay: 10000l  // execute job once in 5 seconds
    }

    def execute() {

        if (new File('/etc/bwc/master')?.text?.trim() != '1')
            return

        if (SystemConfig.currentFeedingStep.get() > SystemConfig.feedingStepsCount) {
//            println "FEEDING: FINISHED"
            if (SystemConfig.currentFeedingStep.get() == SystemConfig.feedingStepsCount + 1) {
                def directory = new File('/var/log/bwc')
                if (!directory.exists())
                    directory.mkdirs()

                String zipFileName = "/var/log/bwc/bwc-${new Date().toString()?.replace(' ', '-')?.replace(':', '-')}.zip"
                String inputDir = "/tmp/bwc"
                def filesCount = 0
                new File(inputDir).eachFile() { file ->
                    if (file.name.endsWith('.csv')) {
                        filesCount++
                    }
                }

                if (filesCount > 0) {
                    ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(zipFileName))
                    new File(inputDir).eachFile() { file ->
                        if (file.name.endsWith('.csv')) {
                            zipFile.putNextEntry(new ZipEntry(file.getName()))
                            def buffer = new byte[file.size()]
                            file.withInputStream { i ->
                                def l = i.read(buffer)
                                if (l > 0) {
                                    zipFile.write(buffer, 0, l)
                                }
                            }
                        }
                        zipFile.closeEntry()
                    }
                    zipFile.close()

                    new File(inputDir).eachFile() { file ->
                        if (file.name.endsWith('.csv')) {
                            file.delete()
                        }
                    }
                }
            }
            return
        }

        if (ScenarioActor.adapting?.get())
            return

//        10.times {
        println "FEEDING: ${SystemConfig.currentFeedingStep.get() + 1}/${SystemConfig.feedingStepsCount}"
        def threads = new ArrayList<Thread>()
        SystemConfig.scenarios.eachWithIndex { scenario ->
            threads.add(new Thread() {
                void run() {
                    try {
                        feedScenario(scenario.key as String, SystemConfig.currentFeedingStep.get(), scenario.value.interfaceName as String)
                    } catch (exception) {
                        println(exception.message)
                    }
                }
            })
        }
//            SystemConfig.scenarios.sort { it.key }.eachWithIndex { scenario, Integer index ->
//                threads.add(new Thread() {
//                    void run() {
//                        try {
////                            feedScenario(scenario.value.publicIP as String, scenario.value.interfaceName as String, index, scenario.value.feedingWeight as Float, SystemConfig.currentFeedingStep.get())
//                            feedScenario(scenario.key as String, SystemConfig.currentFeedingStep.get())
//                        } catch (exception) {
//                            println(exception.message)
//                        }
//                    }
//                })
//            }
        threads.each {
            it.start()
        }
        threads.each {
            it.join()
        }
//        }

//        println "COMPLETED ${SystemConfig.currentFeedingStep.getAndIncrement()}"
        SystemConfig.currentFeedingStep.getAndIncrement()

        logReport()
    }

    private void feedScenario(String scenarioName, Integer step, String inet) {
        if (Environment.isDevelopmentMode()) {
            def process = "java -jar /Personal/DevDesk/BWC/Feeder/out/artifacts/Feeder_jar/Feeder.jar ${scenarioName} ${540} ${step} - ${SystemConfig.proxyServerMasterAddress}".execute()
            process.waitFor()
//            println process.text
        } else
            RemoteController.executeCommand(SystemConfig.feederAddress, "java -jar ~/feeders/${inet?.replace('p', '')}/Feeder.jar ${scenarioName} ${3600} ${step} ${inet} ${SystemConfig.proxyServerMasterAddress}:8080")
    }


    static lastNumberOfRequest = 0
    static AtomicBoolean firstReport = new AtomicBoolean(true)

    private static void logReport() {

        def numberOfRequests = ScenarioActor.numberOfRequests.get()
        if (numberOfRequests != lastNumberOfRequest) {
            def recordIndexer = SystemConfig.currentFeedingStep.get()
            SystemConfig.lastReport = [
                    bandwidth                : BandWidthActor.realBandWidth?.sort { it.key },
                    bandwidthWeights         : BandWidthActor.bandWidths?.sort { it.key },
                    responseTimes            : ScenarioActor.lastResponseTimes?.sort { it.key },
                    arrivalRates             : ScenarioActor.lastArrivalRates?.sort { it.key },
                    adaptationsCount         : [count: ScenarioActor.adaptationsCount?.values()?.sum { it }],
                    violationsCount          : ScenarioActor.violationsCount?.sort { it.key },
                    serversCount             : [count: ScenarioActor.serversCount?.get()],
                    averageAdaptationDuration: [duration: ScenarioActor.averageAdaptationTime.get()],
                    utilization              : [
                            dataBase         : ScenarioActor.databaseUtilization.get() / 100,
                            webServer1       : ScenarioActor.webServer1Utilization.get() / 100,
                            webServer2       : ScenarioActor.webServer2Utilization.get() / 100,
                            webServer3       : ScenarioActor.webServer3Utilization.get() / 100,
                            loadBalancer     : ScenarioActor.loadBalancerUtilization.get() / 100,
                            proxyServerMaster: ScenarioActor.proxyServerMasterUtilization.get() / 100,
                            proxyServerSlave : ScenarioActor.proxyServerMasterUtilization.get() / 100
                    ]
            ]
            lastNumberOfRequest = numberOfRequests

            if (firstReport.getAndSet(false)) {
                def directory = new File('/tmp/bwc')
                directory.deleteDir()
            }

            def directory = new File('/tmp/bwc')
            if (!directory.exists())
                directory.mkdirs()
            SystemConfig.lastReport.each {
                def file = new File("/tmp/bwc/${it.key}.csv")
                if (!file.exists())
                    file.createNewFile()
                file.append("${recordIndexer}," + it.value.values().join(',') + '\r\n')
            }
        }
    }
}
