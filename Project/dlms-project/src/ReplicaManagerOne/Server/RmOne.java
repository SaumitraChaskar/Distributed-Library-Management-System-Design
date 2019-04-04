package ReplicaManagerOne.Server;

import ReplicaManagerOne.Map.Message;
import ReplicaManagerOne.Map.MessageComparator;
import Utils.PortsAndIPs;

import java.io.IOException;
import java.net.*;
import java.util.*;

import static java.lang.Thread.sleep;

public class RmOne {
	private static int nextSequence = 1;
	private static PriorityQueue<Message> pq = new PriorityQueue<Message>(20, new MessageComparator());

	private static boolean testMode = false;
	private static String testServer = "";
    private static boolean recovering = false;
    private static final String rmName = "1";
	private static int consecutiveError = 0;
	public static void main(String[] args) {
		
		Runnable task = () -> {
			receive();
		};
		Thread thread = new Thread(task);
		thread.start();
		Runnable task2 = () ->{
		    receiveFeedback();
        };
		Thread thread1 = new Thread(task2);
		thread1.start();

		sendMessage("CONM1111", "Test");
		sendMessage("MCGM1111", "Test");
		sendMessage("MONM1111", "Test");
		if(args.length != 0){
			testMode = true;
			testServer = args[0].toUpperCase();
		}
	}

	private static void receive() {
		MulticastSocket aSocket = null;
		try {

			aSocket = new MulticastSocket(PortsAndIPs.RM_Group_PortNum);

			aSocket.joinGroup(InetAddress.getByName(PortsAndIPs.RMGroupIPAddress));

			byte[] buffer = new byte[1000];
			System.out.println("RM1 UDP Server "+PortsAndIPs.RM_Group_PortNum+" Started............");

			while (true) {
			    while(recovering){
                    try {
                        sleep(1000);
                        System.out.println("Recovering from failure");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);

				String sentence = new String( request.getData(), 0,
						request.getLength() );
				String[] parts = sentence.split(";");

				int sequencerId = Integer.parseInt(parts[6]);

				Message message = new Message(sentence,sequencerId);
				pq.add(message);
				findNextMessage();

				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(),
						request.getPort());
				aSocket.send(reply);
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
	
	public static void findNextMessage() {
		Iterator<Message> itr = pq.iterator();
		while (itr.hasNext()) {
			Message request = itr.next();
			if(request.getsequenceId()==nextSequence) {
				nextSequence = nextSequence+1;
				String message = request.getMessage();
				String[] parts = message.split(";");
				String userID = parts[1]; 
				
				System.out.println(message);

				sendMessage(userID,message);
				
			}
		} 			 
	}

	public static void sendMessage(String userID , String message) {
		String libraryPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		int port=0;
		if(libraryPrefix.equals("con")) {
			port = PortsAndIPs.RM1_CON_PortNum;
		}else if(libraryPrefix.equals("mcg")) {
			port = PortsAndIPs.RM1_MCG_PortNum;
		}else if(libraryPrefix.equals("mon")) {
			port = PortsAndIPs.RM1_MON_PortNum;
		}

		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messageByte = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(messageByte, messageByte.length, aHost, port);
			aSocket.send(request);
			System.out.println(message);
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

	}

	private static void receiveFeedback() {
		MulticastSocket fSocket = null;
		try {
			fSocket = new MulticastSocket(PortsAndIPs.RM_Feedback_Portnum);
			fSocket.joinGroup(InetAddress.getByName(PortsAndIPs.RM_Feedback_IPAddress));
			byte[] buffer = new byte[1000];
			System.out.println("RM1 receive feedbacks at port :"+PortsAndIPs.RM_Feedback_Portnum );

			while (true){
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				fSocket.receive(request);

				String sentence = new String( request.getData(), 0,
						request.getLength() );
				String[] parts = sentence.split(";");
				String erroType = parts[0];
				if(erroType.equalsIgnoreCase("crash")){
					crashHandle(parts);
				}else if(erroType.equalsIgnoreCase("fault")){
					faultHandle(parts);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void faultHandle(String[] parts) {
		String rmNum = parts[1];
		if(rmNum.equalsIgnoreCase(rmName)){
			consecutiveError ++;
			if(consecutiveError >=3){
				System.out.println("three consecutive incorrect result");
				crashHandle(parts);
			}
		}else{
			consecutiveError = 0;
		}
	}

	public static void crashHandle(String[] parts){
		String rmNum = parts[1];
		String serverName = parts[2];
		if(rmNum.equalsIgnoreCase(rmName)) {
			//TODO : setting data after restart replica
			if (serverName.equalsIgnoreCase("con")) {
				Runnable task = () -> {
					try {
						ConcordiaServer.main(new String[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				Thread handleThread = new Thread(task);
				handleThread.start();
				System.out.println("handle con server crash!");
			} else if (serverName.equalsIgnoreCase("mcg")) {
				Runnable task = () -> {
					try {
						McgillServer.main(new String[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				Thread handleThread = new Thread(task);
				handleThread.start();
				System.out.println("handle mcg server crash!");
			} else if (serverName.equalsIgnoreCase("mon")) {
				Runnable task = () -> {
					try {
						MontrealServer.main(new String[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				Thread handleThread = new Thread(task);
				handleThread.start();
				System.out.println("handle mon server crash!");
			}

		}
	}
}