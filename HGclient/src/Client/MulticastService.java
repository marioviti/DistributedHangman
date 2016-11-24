package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MulticastService {
	
	private int PORT;
	private InetAddress GROUP_IP;
	private InetAddress LOCAL_IP;
	private MulticastSocket socket;
	private byte[] sendBuffer;
	private byte[] recBuffer;
	private DatagramPacket packet;
	
	/**
	 * Wrapper for Multicast UDP socket
	 * 
	 * @param ifaceIP
	 * @param groupIP
	 * @param port
	 * @throws UnknownHostException
	 */
	public MulticastService(String ifaceIP,String groupIP, int port) throws UnknownHostException {
		System.out.println("me: "+ ifaceIP);
		System.out.println("group: "+ groupIP);

		setSocketAddress(ifaceIP,groupIP,port);
		sendBuffer = new byte[256];
		packet = new DatagramPacket(sendBuffer,sendBuffer.length);
	}
	
	private void setSocketAddress(String ifaceIP, String groupIP, int port) throws UnknownHostException {
		PORT = port;
		GROUP_IP = InetAddress.getByName(groupIP);
		LOCAL_IP = InetAddress.getByName(ifaceIP);
	}
	
	/**
	 * use to join the group
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SocketException
	 */
	public void join() throws UnknownHostException, IOException, SocketException {
		InetAddress iface = LOCAL_IP; 
		MulticastSocket socket = new MulticastSocket(PORT);
		InetAddress address = GROUP_IP;
		socket.setInterface(iface);
		socket.joinGroup(address);
		//socket.setLoopbackMode(true);
		this.socket = socket; 
	}

	/**
	 * send data to the group
	 * 
	 * @param dString
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void send(String dString) throws UnknownHostException, IOException { 
		byte[] buf;
		buf = dString.getBytes();
		packet = new DatagramPacket(buf, buf.length, GROUP_IP, PORT);
		socket.send(packet); 
	}
	
	/**
	 * receive data from the group
	 * 
	 * @return
	 * @throws IOException
	 */
	public String receive() throws IOException {
		packet = new DatagramPacket(sendBuffer, sendBuffer.length);
		socket.receive(packet);
	
		String received = new String(packet.getData(), 0, packet.getLength());
		  
		System.err.println("\ndata: " + received);
		System.err.println("\nsrc: " + packet.getAddress());
		System.err.println("\ndest: " + LOCAL_IP+"\n");
		return received;
	}

}
