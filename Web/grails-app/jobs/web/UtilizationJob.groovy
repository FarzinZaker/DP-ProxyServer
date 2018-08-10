package web

import grails.util.Environment
import proxy.RemoteController
import proxy.ScenarioActor
import proxy.SystemConfig

import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class UtilizationJob {

    def concurrent = false

    static triggers = {
        simple repeatInterval: 3000l, startDelay: 10000  // execute job once in 5 seconds
    }

    def execute() {

        if(new File('/etc/bwc/master')?.text?.trim() != '1')
            return

        def value = 0

        if (!Environment.isDevelopmentMode()) {
            value = getCPUUtilization(SystemConfig.proxyServerMasterAddress)
            if (value > 0)
                ScenarioActor.proxyServerMasterUtilization.set(value)

            value = getCPUUtilization(SystemConfig.proxyServerSlaveAddress)
            ScenarioActor.proxyServerSlaveUtilization.set(value)
        } else {
            value = getLocalCPUUtilization()
            if (value > 0)
                ScenarioActor.proxyServerMasterUtilization.set(value)

            ScenarioActor.proxyServerSlaveUtilization.set(0)
        }

        value = getCPUUtilization(SystemConfig.loadBalancerAddress)
        if (value > 0)
            ScenarioActor.loadBalancerUtilization.set(value)

        value = getCPUUtilization(SystemConfig.webServer1Address)
        if (value > 0)
            ScenarioActor.webServer1Utilization.set(value)

        value = getCPUUtilization(SystemConfig.webServer2Address)
        if (value > 0)
            ScenarioActor.webServer2Utilization.set(value)

        value = getCPUUtilization(SystemConfig.webServer3Address)
        if (value > 0)
            ScenarioActor.webServer3Utilization.set(value)

        value = getCPUUtilization(SystemConfig.databaseAddress)
        if (value > 0)
            ScenarioActor.databaseUtilization.set(value)
    }


    private static double getCPUUtilization(String ip) {
        RemoteController.executeCommand(ip, "echo \$[100-\$(vmstat 1 2|tail -1|awk '{print \$15}')]")?.toDouble()
    }


    static Double getLocalCPUUtilization() {
        def value = 0D
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true)
            if (method.getName().startsWith("get") && method.getName().endsWith("CpuLoad")
                    && Modifier.isPublic(method.getModifiers()))
                try {
                    value += method.invoke(operatingSystemMXBean)?.toString()?.toDouble()
                } catch (ignored) {
                }
        }
        Math.round(value * 100)?.toDouble()
    }
}
