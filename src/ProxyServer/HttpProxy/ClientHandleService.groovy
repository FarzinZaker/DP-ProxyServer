package ProxyServer.HttpProxy

import ProxyServer.IRequestFilter
import com.sun.xml.internal.ws.util.ASCIIUtility

import java.nio.channels.SocketChannel
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by root on 8/9/17.
 */
class ClientHandleService {

    SocketChannel clientSocket
    InetAddress originAddress
    int originPort
    IRequestFilter filter
    String request

    void setClientData(ClientData clientData) {
        clientSocket = clientData.clientSocket
        originAddress = clientData.originAddress
        originPort = clientData.originPort
        filter = clientData.filter
        request = clientData.request
    }

    private SocketChannel originSocket
    private Map<SocketChannel, List<byte[]>> dataMap

    private final static Logger logger = Logger.getLogger(ClientHandler.class.getName())

    def ClientHandler() {
        originSocket = null
        dataMap = new HashMap<SocketChannel, List<byte[]>>()
    }

    void handle() {
        try {
            InetSocketAddress isa = new InetSocketAddress(originAddress.getHostName(), originPort)

            originSocket = SocketChannel.open()
            originSocket.connect(isa)

            // String no_records= extractNoRecords(request)
            String client_ip = (clientSocket.socket().getRemoteSocketAddress()).toString()
            client_ip = Utils.extractIP(client_ip)

            if (request == null)
                return

            URI uri = extractURI(request)

            if (uri == null)
                return

            UUID uuid = UUID.randomUUID()
            // onRequest in doFilter
            if (!filter.doFilter(uri, uuid, client_ip))
                return
            request = modifyRequest(request)

            sendRequest(originSocket.socket(), request)

            copy(originSocket, clientSocket, uri.getPath() + uri.getQuery())

            filter.onReply(uuid, client_ip)
        } catch (Exception ex) {
            Utils.printExceptionInfo(ex, "handle")
        } finally {
            safeClose(originSocket)
            safeClose(clientSocket)
        }
    }

    private static void sendRequest(Socket socket, String request) throws IOException {
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        outWriter.write(request)
        outWriter.flush()
    }

    /*
     * in http1.1, the end of message is not defined, it is the responisbility
     * of the client/server to detect the end of message if the message is not
     * chuncked due to large message size the message is chuncked here we chek
     * if the message is chuncked or not if not we use the content-length and
     * if chuncked, we use the string ("\n0\r\n\r")to detect end of chunck
     */

    private static void copy(SocketChannel source, SocketChannel destination, String url) throws InterruptedException {
        int contCount = 0
        int contLen = 0
        int chuncked = 0
        try {

            InputStream streamSource = source.socket().getInputStream()
            OutputStream streamDestination = destination.socket().getOutputStream()
            // source.socket().setSoTimeout(200)
            // destination.socket().setSoTimeout(400)
            byte[] buffer = new byte[4096]
            int bytesRead = 0
            while ((bytesRead = streamSource.read(buffer)) > 0) {
                streamDestination.write(buffer, 0, bytesRead)
                String s = ASCIIUtility.toString(buffer, 0, bytesRead)
                if (findContentLength(s) != -1) {
                    contLen = findContentLength(s)

                }
                if (chuncked == 0 & chunckedCheck(s) == 1) {
                    chuncked = 1
                }
                //is message is chuncked
                if (chuncked == 1) {
                    if (s.contains("\n0\r\n\r\n")) {
                        break
                    }

                }

                contCount += contentCharCount(s)
                //if message content is equal to contLen
                if (contCount >= contLen & chuncked != 1) {
                    break
                }

            }

        }

        catch (IOException e) {
        }

    }

    private static int findContentLength(String s) {
        int index1 = s.indexOf("Content-Length")
        int index2 = s.indexOf(":", index1)
        int index3 = s.indexOf("\n", index2)
        if (index1 != -1) {
            int conLen = Integer.valueOf((s.substring(index2 + 1, index3).replaceAll("\\s+", "")))
            // System.out.println("contentLengh="+conLen)
            return conLen
        } else
            return -1
    }
// "\r\n\r\n" shows the end of header and start of body
    private static int contentCharCount(String s) {
        int index1 = s.indexOf("\r\n\r\n")
        if (index1 != -1) {
            s = s.substring(index1 + "\r\n\r\n".length())
        }
        //System.out.println(s.length())
        return s.length()

    }

    private static int chunckedCheck(String s) {
        if (s.contains("chunked")) {
            return 1
        }
        return 0
    }

    private static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (Exception ex) {
                Utils.printExceptionInfo(ex, "safeClose (Closeable closeable)")
            }
        }
    }

    private String modifyRequest(String request) {

        String newRequest = ""

        String[] lines = request.split("\n")

        for (String line : lines) {

            if (line.toLowerCase().startsWith("host:")) {
                line = "Host: " + originAddress.getHostName() + ":" + originPort
            }
            newRequest += line + "\n"
        }

        newRequest += "\n"

        return newRequest

    }

    private URI extractURI(String msg) {
        String[] tokens = msg.split(" ")
        try {
            return new URI("http://" + this.originAddress.getHostName() + ":" + this.originPort + tokens[1])

        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, "BUG failed construct URI", e)
            e.printStackTrace()
        }
        System.err.println("Failed URI creation")
        return null
    }
}
