package ReplicaManagerOne.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import ReplicaManagerOne.ImplementRemoteInterface.MontrealClass;
import Utils.PortsAndIPs;


public class MontrealServer {
	public static MontrealClass monObjecct;
	public static String RMNo = "1";
	public static void main(String args[])
	{
		try {

			MontrealClass obj = new MontrealClass();
			monObjecct=obj;
			System.out.println("Montreal Server ready and waiting ...");
			Runnable task = () -> {
				receive(obj);
			};
			Thread thread = new Thread(task);
			thread.start();

			Runnable task2 = () -> {
				receiveFromSequencer();
			};
			Thread thread2 = new Thread(task2);
			thread2.start();

		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		System.out.println("Montreal Server Exiting ...");

	}


	private static void receive(MontrealClass obj) {
		DatagramSocket aSocket = null;
		String sendingResult = "";
		try {
			aSocket = new DatagramSocket(6666);
			byte[] buffer = new byte[1000];
			System.out.println("Montreal UDP Server 6666 Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String sentence = new String( request.getData(), 0,
						request.getLength() );
				String[] parts = sentence.split(";");
				String function = parts[0];
				String userID = parts[1];
				String itemName = parts[2];
				String itemId = parts[3];
				int numberOfDays = Integer.parseInt(parts[4]);
				if(function.equals("borrow")) {
					boolean result = obj.borrowItem(userID, itemId, numberOfDays);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("find")) {
					String result = obj.findItem(userID, itemName);
					sendingResult = result;
					sendingResult= sendingResult+";";
				}else if(function.equals("return")) {
					boolean result = obj.returnItem(userID, itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("wait")) {
					boolean result = obj.waitInQueue(userID, itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("isAvailableInLibrary")) {
					boolean result = obj.isAvailableInLibrary(itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("isBorrowed")) {
					boolean result = obj.isBorrowed(userID,itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("isAlreadyBorrowed")) {
					boolean result = obj.isAlreadyBorrowed(userID);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}
				byte[] sendData = sendingResult.getBytes();
				DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
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

	private static void receiveFromSequencer() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(PortsAndIPs.RM1_MON_PortNum);
			byte[] buffer = new byte[1000];
			System.out.println("Sequencer UDP Server "+PortsAndIPs.RM1_MON_PortNum+" Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String sentence = new String( request.getData(), 0,
						request.getLength() );
				findNextMessage(sentence);
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

	public static void findNextMessage(String sentence) {
		String message = sentence;
		String[] parts = message.split(";");
		String function = parts[0];
		String userID = parts[1];
		String itemName = parts[2];
		String itemId = parts[3];
		String newItemId = parts[4];
		int number = Integer.parseInt(parts[5]);
		System.out.println(message);
		String sendingResult ="";
		if(function.equals("addItem")) {
			sendingResult = monObjecct.addItem(userID,itemId, itemName,number);
		}else if(function.equals("removeItem")) {
			String result = monObjecct.removeItem(userID, itemId,number);
			sendingResult = result;
		}else if(function.equals("listItemAvailability")) {
			String result = monObjecct.listItemAvailability(userID);
			sendingResult = result;
		}else if(function.equals("borrowItem")) {
			boolean result = monObjecct.borrowItem(userID, itemId,number);
			sendingResult = Boolean.toString(result);
		}else if(function.equals("findItem")) {
			sendingResult = monObjecct.findItem(userID,itemName);
		}else if(function.equals("returnItem")) {
			boolean result = monObjecct.returnItem(userID,itemId);
			sendingResult = Boolean.toString(result);
		}else if(function.equals("waitInQueue")) {
			boolean result = monObjecct.waitInQueue(userID,itemId);
			sendingResult = Boolean.toString(result);
		}else if(function.equals("exchangeItem")) {
			boolean result = monObjecct.exchangeItem(userID,newItemId,itemId);
			sendingResult = Boolean.toString(result);
		}

		sendingResult= sendingResult+";"+RMNo+";"+"mon";
		int FEPortNum = Integer.valueOf(parts[7]);
		String FEIpAddress = parts[8];

		sendMessageBackToFrontend(sendingResult,FEPortNum,FEIpAddress);
	}

	public static void sendMessageBackToFrontend(String message,int FEPortNum,String FEIpAddress) {
		System.out.println("result : "+message);
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] m = message.getBytes();
			InetAddress aHost = InetAddress.getByName(FEIpAddress);

			DatagramPacket request = new DatagramPacket(m, m.length, aHost, FEPortNum);
			aSocket.send(request);
			aSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void shutDown(){
		System.exit(8);
	}
}
