package proxy

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import grails.util.Environment


class RemoteController {

    static void setBW(String IP, String iface, double rate) {
        if (rate < 1)
            rate = 1
        String command1 = "sudo ovs-vsctl set interface " + iface + " ingress_policing_rate=" + Math.round(rate)
        double burst_size = (rate) / 10
        String command2 = "sudo ovs-vsctl set interface " + iface + " ingress_policing_burst=" + Math.round(burst_size)
        executeCommand(IP, command1)
        executeCommand(IP, command2)
    }

    static void setBW(String scenario, double rate) {
        if (rate < 1)
            rate = 1
        if (Environment.isDevelopmentMode()) {
            def file = new File("/Personal/Bandwidth/${scenario}")
            if (!file.exists())
                file.createNewFile()
            file.write(rate?.toString())
        } else {
            def ip = SystemConfig.feederAddress
            def inFace = SystemConfig.scenarios[scenario].interfaceName ?: 'p0'
            setBW(ip, inFace, rate)
        }
    }

    static int[] getBW(String IP, String iface) {
        int[] bw = [0, 0]
        String command1 = "sudo ovs-vsctl get interface " + iface + " ingress_policing_rate"
        String rate = executeCommand(IP, command1)
        bw[0] = Integer.parseInt(rate.trim())
        bw[1] = bw[0] / 10
        return bw
    }

    static int[] getBW(String scenario) {
        def ip = SystemConfig.feederAddress
        def inFace = SystemConfig.scenarios[scenario].interfaceName ?: 'p0'
        getBW(ip, inFace)
    }

    private static String user = 'ubuntu'
    private static String privateKey = Environment.isDevelopmentMode() ? '/Personal/DevDesk/DPPackage/DPKeyPair.pem' : '/home/DPKeyPair.pem'

    static String executeCommand(String ip, String command) {
        try {
            def client = new JSch()
            client.addIdentity(privateKey)
            def session = client.getSession(user, ip)
            def config = new Properties()
            config.put("StrictHostKeyChecking", "no")
            session.setConfig(config)
            session.connect()
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec")
            InputStream input = channelExec.getInputStream()
            channelExec.setCommand(command)
            channelExec.connect()
            BufferedReader reader = new BufferedReader(new InputStreamReader(input))
            String line

            def result = ''
            while ((line = reader.readLine()) != null) {
                result += line
            }

            int exitStatus = channelExec.getExitStatus()
            channelExec.disconnect()
            session.disconnect()

            return result
        }
        catch (ignored) {
            println "${ip}:${command} - ${ignored.message}"
            '0'
        }
    }
}
