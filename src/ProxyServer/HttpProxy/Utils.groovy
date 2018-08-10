package ProxyServer.HttpProxy

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by root on 8/9/17.
 */
class Utils {

    static String readRequest(Socket socket) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        StringBuilder total = new StringBuilder();
        String line = "";
        while (true) {
            line = rd.readLine();

            if (line == null)
                return null;

            if (line.isEmpty()) {
                total.append("" + "\n");
                return total.toString();
            }
            total.append(line + "\n");
        }
    }

    static String extractPath(String msg) {
        String[] tokens = msg?.split(" ");
        if (tokens)
            try {
                return tokens[1];

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        System.err.println("Failed path extration");
        return null;
    }

    static String extractIP(String ipStr) {
        // ipStr=/192.168.1.1:40489
        int slashIndex = ipStr.indexOf("/")
        int colIndex = ipStr.indexOf(":")
        return ipStr.substring(slashIndex + 1, colIndex)
    }

    static void printExceptionInfo(Exception ex, String methodName) {
        System.out.println("Exception catch by: " + methodName)
        System.out.println("Exception message: " + ex.getLocalizedMessage())
        System.out.println("Exception cause: " + ex.getCause())
        System.out.println("Exception class: " + ex.getClass().getSimpleName())
        // logger.log(Level.WARNING,"Exception" + ex.getLocalizedMessage(), ex)
        ex.printStackTrace()
    }
}
