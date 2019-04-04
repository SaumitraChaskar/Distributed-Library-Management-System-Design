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
	private static final String rmName = "1";

	private static boolean testMode = false;
	private static String testServer = "";
	private static boolean recovering = false;
	private static int consecutiveError = 0;
	private static ArrayList<String> history = new ArrayList<>();

	private static Thread conServer;
	private static Thread mcgServer;
	private static Thread monServer;
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

		Runnable task3 = () ->{
            try {
                ConcordiaServer.main(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
		conServer = new Thread(task3);
		conServer.start();

        Runnable task4 = () ->{
            try {
                McgillServer.main(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
		mcgServer= new Thread(task4);
        mcgServer.start();

        Runnable task5 = () ->{
            try {
                MontrealServer.main(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
		monServer = new Thread(task5);
        monServer.start();

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
                        System.out.println("Waiting for recovering ...");
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
				history.add(message);
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
		if(testMode){
			System.out.println("Test mode start");
			if(libraryPrefix.equalsIgnoreCase(testServer)){
				simulateFailure(message);
			}
			testMode = false;
		}else {

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
	}

	private static void simulateFailure(String message) {
		String[] parts = message.split(";");
		String serverName = parts[1].substring(0, Math.min(parts[1].length(), 3)).toLowerCase();
		String randomResult = "Random result"+";"+"1"+";"+serverName;
		int port = Integer.valueOf(message.split(";")[7]);
		String hostName = message.split(";")[8];
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messageByte = randomResult.getBytes();
			InetAddress aHost = InetAddress.getByName(hostName);
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
			System.out.println("Handling fault result...");
			if(consecutiveError >=3){
				System.out.println("three consecutive incorrect result");
				crashHandle(parts);
			}
		}else{
			consecutiveError = 0;
		}
	}

	public static void crashHandle(String[] parts){
		recovering = true;
		String rmNum = parts[1];
		String serverName = parts[2];
		if(rmNum.equalsIgnoreCase(rmName)) {
			if (serverName.equalsIgnoreCase("con")) {
				conServer.interrupt();
				Runnable task = () -> {
					try {
						ConcordiaServer.main(new String[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				conServer = new Thread(task);
				conServer.start();
				System.out.println("handle con server crash!");

			} else if (serverName.equalsIgnoreCase("mcg")) {
				mcgServer.interrupt();
				Runnable task = () -> {
					try {
						McgillServer.main(new String[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				mcgServer = new Thread(task);
				mcgServer.start();
				System.out.println("handle mcg server crash!");
			} else if (serverName.equalsIgnoreCase("mon")) {
				monServer.interrupt();
				Runnable task = () -> {
					try {
						MontrealServer.main(new String[0]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				monServer = new Thread(task);
				monServer.start();
				System.out.println("handle mon server crash!");
			}
			recoverServerData(serverName);

		}
	}

	private static void recoverServerData(String serverName) {
		System.out.println("Recovering data for server "+serverName);
		for(String h:history){
			String targetServer = h.split(";")[1];
			if(targetServer.equalsIgnoreCase(serverName)){
				sendMessage(targetServer,h);
			}
		}
		recovering = false;
		System.out.println("Recover done");
	}
}