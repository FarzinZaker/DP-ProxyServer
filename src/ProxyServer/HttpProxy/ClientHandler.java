package ProxyServer.HttpProxy;

import ProxyServer.IRequestFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import akka.actor.ActorRef;
import akka.actor.Props;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import Framework.Diagnostics.Trace;
import Framework.Diagnostics.Trace.TraceLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClientHandler extends Thread {
	private final SocketChannel clientSocket;
	private SocketChannel originSocket;
	private final InetAddress originAddr;
	private final int originPort;
	private final IRequestFilter filter;
	private Map<SocketChannel, List<byte[]>> dataMap;
	final private static int BUFFER_SIZE = 65536; // 8

	public static Connection connection;
	public static ConnectionFactory factory;
	public static Channel channel;
	public static boolean singleton = false;
	public static String QUEUE_NAME;

	private final static Logger logger = Logger.getLogger(ClientHandler.class.getName());

	public ClientHandler(SocketChannel connectionSocket, InetAddress inetAddr, int port, IRequestFilter filter) {
		this.clientSocket = connectionSocket;
		this.originAddr = inetAddr;
		this.originPort = port;
		this.filter = filter;
		originSocket = null;
		dataMap = new HashMap<SocketChannel, List<byte[]>>();
		start();
		// init_rabbit();
	}

	private void init_rabbit() {
		if (singleton == false) {
			QUEUE_NAME = "proxy-python";
			factory = new ConnectionFactory();
			// connect to Nasim's machine to send rabbitmq msg
			String remote_host_rabit = "10.253.0.54";
			// String remote_host_rabit="localhost";
			factory.setHost(remote_host_rabit);
			factory.setUsername("nasim");
			factory.setPassword("nasim");
			try {
				connection = factory.newConnection();
				channel = connection.createChannel();
				channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			} catch (IOException ex) {
				printExceptionInfo(ex, "initQeueu");
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		singleton = true;
	}

	@Override
	public void run() {
		try {
			InetSocketAddress isa = new InetSocketAddress(originAddr.getHostName(), originPort);

			originSocket = SocketChannel.open();
			originSocket.connect(isa);

			String request = readRequest(clientSocket.socket());

			// String no_records= extractNoRecords(request);
			String client_ip = (clientSocket.socket().getRemoteSocketAddress()).toString();
			client_ip = extractIP(client_ip);

			if (request == null)
				return;

			URI uri = extractURI(request);

			if (uri == null)
				return;

			UUID uuid = UUID.randomUUID();
			// onRequest in doFilter
			if (!filter.doFilter(uri, uuid, client_ip))
				return;
			request = modifyRequest(request);
			// long start = System.currentTimeMillis();
			sendRequest(originSocket.socket(), request);
			// copy(originSocket, clientSocket, uri.getPath() +
			// uri.getQuery(),Integer.valueOf(no_records));
			copy(originSocket, clientSocket, uri.getPath() + uri.getQuery());

			// long end = System.currentTimeMillis();
			// long rt = end - start;
			// String client_ip =
			// (clientSocket.socket().getRemoteSocketAddress()).toString();
			// JSONObject joClient = new JSONObject();
			// // JSONArray array = new JSONArray();
			// joClient.put("Client_ip", client_ip);
			// joClient.put("Response_time", rt);
			// joClient.put("request", request);
			// String message = joClient.toString();
			// // System.out.println(message);
			// // connection = factory.newConnection();
			// // channel = connection.createChannel();
			// // channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			// // System.out.println(channel.toString());
			// // String message= client_ip +","+ rt;
			// channel.basicPublish("", QUEUE_NAME, null,
			// message.getBytes("UTF-8"));
			//// Trace.WriteLine(TraceLevel.DEBUG, "Channel is [%s], Message is
			// [%s].", channel.toString(), message);
			filter.onReply(uuid, client_ip);
		} catch (Exception ex) {
			printExceptionInfo(ex, "run");
		} finally {
			safeClose(originSocket);
			safeClose(clientSocket);
		}
		super.run();
	}

	public String extractIP(String ipStr) {
		// ipStr=/192.168.1.1:40489
		int slashIndex = ipStr.indexOf("/");
		int colIndex = ipStr.indexOf(":");
		return ipStr.substring(slashIndex + 1, colIndex);
	}

	public String extractNoRecords(String req) {
		// ipStr=/192.168.1.1:40489
		int equalIndex = req.indexOf("=");
		int HIndex = req.indexOf("H");
		return req.substring(equalIndex + 1, HIndex - 1);
	}

	private void printExceptionInfo(Exception ex, String methodName) {
		System.out.println("Exception catch by: " + methodName);
		System.out.println("Exception message: " + ex.getLocalizedMessage());
		System.out.println("Exception cause: " + ex.getCause());
		System.out.println("Exception class: " + ex.getClass().getSimpleName());
		// logger.log(Level.WARNING,"Exception" + ex.getLocalizedMessage(), ex);
		ex.printStackTrace();
	}

	private void safeClose(Selector selector) {
		try {
			if ((selector == null) || (!selector.isOpen()))
				return;

			selector.close();
		} catch (IOException ex) {
			printExceptionInfo(ex, "safeClose socket");
		}
	}

	private void sendRequest(Socket socket, String request) throws IOException {
		BufferedWriter outWriter = null;
		outWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		outWriter.write(request);
		outWriter.flush();

	}

	public int roundNoRecords(int noRec) {
		if (noRec < 100) {
			return (noRec / 10) * 10;

		} else if (noRec < 1000) {
			return (noRec / 100) * 100;
		} else if (noRec < 10000) {
			return (noRec / 1000) * 1000;

		} else if (noRec < 100000) {
			return (noRec / 10000) * 10000;
		}

		return 0;
	}
	/*
	 * in http1.1, the end of message is not defined, it is the responisbility
	 * of the client/server to detect the end of message; if the message is not
	 * chuncked due to large message size the message is chuncked; here we chek
	 * if the message is chuncked or not; if not we use the content-length and
	 * if chuncked, we use the string ("\n0\r\n\r")to detect end of chunck
	 */

	private void copy(SocketChannel source, SocketChannel destination, String url) throws InterruptedException {
		int contCount = 0;
		int contLen = 0;
		int chuncked = 0;
		try {

			InputStream streamSource = source.socket().getInputStream();
			OutputStream streamDestination = destination.socket().getOutputStream();
			// source.socket().setSoTimeout(200);
			// destination.socket().setSoTimeout(400);
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while ((bytesRead = streamSource.read(buffer)) > 0) {
				streamDestination.write(buffer, 0, bytesRead);
				String s = byteArrToString(buffer, bytesRead);
				if (findContentLength(s) != -1) {
					contLen = findContentLength(s);

				}
				if (chuncked == 0 & chunckedCheck(s) == 1) {
					chuncked = 1;
				}
				//is message is chuncked
				if (chuncked == 1) {
					if (s.contains("\n0\r\n\r\n")) {
						break;
					}

				}

				contCount += contentCharCount(s);
				//if message content is equal to contLen
				if (contCount >= contLen & chuncked != 1) {
					break;
				}

			}

		}

		catch (IOException e) {
			// e.printStackTrace();
			// e.getMessage();
		}
		// long end=System.currentTimeMillis();
		// long time=end -start;
		// System.out.println("time on copy:"+time);

	}

	private String byteArrToString(byte[] buffer, int br) {
		char[] chArr = new char[br];
		for (int i = 0; i < br; i++) {
			chArr[i] = (char) buffer[i];
		}
		String s = new String(chArr);
		//System.out.println("String:" + s);
		return s;
	}

	private int findContentLength(String s) {
		int index1 = s.indexOf("Content-Length");
		int index2 = s.indexOf(":", index1);
		int index3 = s.indexOf("\n", index2);
		if (index1 != -1) {
			int conLen = Integer.valueOf((s.substring(index2 + 1, index3).replaceAll("\\s+", "")));
			// System.out.println("contentLengh="+conLen);
			return conLen;
		} else
			return -1;
	}
// "\r\n\r\n" shows the end of header and start of body 
	private int contentCharCount(String s) {
		int index1 = s.indexOf("\r\n\r\n");
		if (index1 != -1) {
			s = s.substring(index1 + "\r\n\r\n".length());
		}
		//System.out.println(s.length());
		return s.length();

	}

	private int chunckedCheck(String s) {
		if (s.contains("chunked")) {
			return 1;
		}
		return 0;
	}

	private void copy1(SocketChannel source, SocketChannel destination, String url, int noRrecords)
			throws InterruptedException {
		try {
			InputStream streamSource = source.socket().getInputStream();
			OutputStream streamDestination = destination.socket().getOutputStream();
			// source.socket().setSoTimeout(200);
			// destination.socket().setSoTimeout(400);
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			// IOUtils.copy(streamSource,streamDestination);
			// //streamDestination.close();
			// streamSource.close();
			int roundNoRec = roundNoRecords(noRrecords);

			while (true) {
				streamDestination.flush();
				bytesRead = streamSource.read(buffer);
				if (bytesRead == -1) {
					streamDestination.close();
					break;
				}
				if (buffer.length == 0) {
					streamDestination.close();
					break;
				}

				// System.out.println(bytesRead);
				String bufStr = new String(buffer);
				// System.out.print(bufStr);
				if (bufStr.contains(noRrecords + "\t")) {
					int indexOflastRec = bufStr.indexOf(noRrecords + "\t");
					int indexOfLastEnter = bufStr.indexOf("\n", indexOflastRec);
					bufStr = bufStr.substring(0, indexOfLastEnter + 3);
					// System.out.println(bufStr);
					byte[] buff = bufStr.getBytes();
					bytesRead = buff.length;
					streamDestination.write(buff, 0, bytesRead);
					streamDestination.close();
					break;
				}

				streamDestination.write(buffer, 0, bytesRead);

			}

			// streamSource.read(buffer) //will block the thread because there
			// is no indication if the server is done transmitting.
			// one method is to set setSoTimeout() on the read operation of
			// socket.

			// while ((bytesRead = streamSource.read(buffer)) > 0) {
			// streamDestination.write(buffer, 0, bytesRead);
			// //System.out.println(bytesRead);
			//
			// }

		}

		catch (IOException e) {
			// e.printStackTrace();
			// e.getMessage();
		}

	}

	private void copy2(SocketChannel source, SocketChannel destination, String url) {
		long start = System.currentTimeMillis();
		Selector selector = null;
		try {
			selector = Selector.open();
			source.configureBlocking(false);
			destination.configureBlocking(false);
			source.socket().setTcpNoDelay(true);
			destination.socket().setTcpNoDelay(true);
			SelectionKey sourceKey = source.register(selector, SelectionKey.OP_READ);
			SelectionKey destinationKey = destination.register(selector, SelectionKey.OP_READ);

			registerInWriteQueue(source);
			registerInWriteQueue(destination);
			boolean isCopying = true;
			boolean isSourceOpen = true;
			while (isCopying) {
				// System.out.println("[Response Time 7 after while] [" + id +"]
				// [" + url + "]: " + (System.currentTimeMillis() - start) );
				// start = System.currentTimeMillis();

				selector.select();
				for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) {
					SelectionKey key = i.next();
					i.remove();

					if (!key.isValid()) {
						continue;
					}

					if (key.isReadable()) {
						// System.out.println("[Response Time 08] [" + id +"] ["
						// + url + "]: " + (System.currentTimeMillis() - start)
						// + "[ before read ]");
						// start = System.currentTimeMillis();
						byte[] data = read(key);
						// System.out.println("[Response Time 09] [" + id +"] ["
						// + url + "]: " + (System.currentTimeMillis() - start)
						// + "[" + (data == null ? "null" : "(" + data.length +
						// ")" + new String(data)+ "]"));
						// start = System.currentTimeMillis();
						if (sourceKey == key) {

							if (data != null) {
								addWriteQueue(destinationKey, data);
							} else {
								isSourceOpen = false;
							}

						} else {
							if (data == null) {
								isCopying = false;
								break;
							}

							addWriteQueue(sourceKey, data);
						}

					}

					if (key.isValid() && key.isWritable()) {
						// System.out.println("[Response Time 10] [" + id +"] ["
						// + url + "]: " + (System.currentTimeMillis() - start)
						// + "[ before write ]");
						// start = System.currentTimeMillis();
						boolean isPendingData = write(key);
						// System.out.println("[Response Time 11] [" + id +"] ["
						// + url + "]: " + (System.currentTimeMillis() - start)
						// + "[ after write]");
						// start = System.currentTimeMillis();
						if ((key == destinationKey) && (!isSourceOpen) && (!isPendingData)) {
							isCopying = false;
							break;
						}

					}
				}
			}
		} catch (Exception ex) {
			printExceptionInfo(ex, "copy ");

		} finally {
			safeClose(selector);
		}
		long end = System.currentTimeMillis();
		long time = end - start;
		System.out.println("time on second copy:" + time);
	}

	private void registerInWriteQueue(SocketChannel channel) {
		dataMap.put(channel, new ArrayList<byte[]>());
	}

	private byte[] read(SelectionKey key) {
		if (!key.isValid())
			return null;

		SocketChannel channel = (SocketChannel) key.channel();

		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		int numRead = -1;
		try {
			numRead = channel.read(buffer);
		} catch (IOException ex) {
			printExceptionInfo(ex, " read socket is " + key.channel());
		}

		if (numRead == -1) {
			safeClose(channel);
			key.cancel();
			return null;
		}
		byte[] data = new byte[numRead];
		System.arraycopy(buffer.array(), 0, data, 0, numRead);
		return data;
	}

	private boolean write(SelectionKey key) {
		if (!key.isValid())
			return false;

		SocketChannel channel = (SocketChannel) key.channel();
		List<byte[]> pendingData = this.dataMap.get(channel);

		if (pendingData.isEmpty())
			return false;

		byte[] item = pendingData.remove(0);
		try {
			channel.write(ByteBuffer.wrap(item));
		} catch (IOException ex) {
			printExceptionInfo(ex, "write socket is " + key.channel());
		}

		if (pendingData.isEmpty()) {
			key.interestOps(SelectionKey.OP_READ);
			return false;
		} else {
			return true;
		}
	}

	private void addWriteQueue(SelectionKey key, byte[] data) {
		SocketChannel channel = (SocketChannel) key.channel();
		List<byte[]> pendingData = this.dataMap.get(channel);
		pendingData.add(data);
		key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
	}

	private void safeClose(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception ex) {
				printExceptionInfo(ex, "safeClose (Closeable closeable)");
			}
		}
	}

	private String readRequest(Socket socket) throws IOException {
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

	private String modifyRequest(String request) {

		String newRequest = "";

		String[] lines = request.split("\n");

		for (String line : lines) {

			if (line.toLowerCase().startsWith("host:")) {
				line = "Host: " + originAddr.getHostName() + ":" + originPort;
			}
			newRequest += line + "\n";
		}

		newRequest += "\n";

		return newRequest;

	}

	private URI extractURI(String msg) {
		String[] tokens = msg.split(" ");
		try {
			return new URI("http://" + this.originAddr.getHostName() + ":" + this.originPort + tokens[1]);

		} catch (URISyntaxException e) {
			logger.log(Level.SEVERE, "BUG failed construct URI", e);
			e.printStackTrace();
		}
		System.err.println("Failed URI creation");
		return null;
	}
}
