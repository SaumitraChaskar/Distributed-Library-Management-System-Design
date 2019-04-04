package ReplicaManagerThree;


import Utils.PortsAndIPs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.server.ExportException;
import java.text.ParseException;


public class DLMS_Concordia_Server
{
	public static DLMS_Concordia_Implementation concordiaObjecct;
	public static String RMNo = "3";

	public static void main(String args[]) throws ExportException
	{
		try 
		{
			

			DLMS_Concordia_Implementation obj = new DLMS_Concordia_Implementation();
			concordiaObjecct =obj;

			System.out.println("Concordia Server ready and waiting ...");
			
			Runnable task= () ->{
				try {
					connect_CON_UDP_Server(obj);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	}

	private static void connect_CON_UDP_Server(DLMS_Concordia_Implementation conimplPublic) throws IOException, ParseException, Exception
	{
		DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		String returnvalue = null;

		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);

			String sentence = new String(receivePacket.getData());
			String[] parts =sentence.split(":");
			String part1 = parts[0];
			String part2 = parts[1];
			String part3 = parts[2];
			System.out.println("RECEIVED:"+part1+":"+part2+":"+part3);

			if(part1.equals("0"))
			{
				returnvalue = Boolean.toString(conimplPublic.borrowItem(part2,part3,0));

			}
			if(part1.equals("1"))
			{
				returnvalue = Boolean.toString(conimplPublic.returnItem(part2,part3));

			}
			if(part1.equals("2"))
			{
				returnvalue = conimplPublic.findBook(part2,part3);

			}
			if(part1.equals("3"))
			{
				returnvalue = conimplPublic.exchangeCheck1(part2,part3);
			}
			if(part1.equals("4"))
			{
				returnvalue = conimplPublic.exchangeCheck2(part2,part3);
			}

			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			returnvalue = returnvalue+":";
			sendData = returnvalue.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, returnvalue.length(), IPAddress, port);
			serverSocket.send(sendPacket);
		}


	}
	
	//------------------------- Project codes ------------------
	
	private static void receiveFromSequencer() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(PortsAndIPs.RM3_CON_PortNum);
			byte[] buffer = new byte[1000];
			System.out.println("Sequencer UDP Server "+PortsAndIPs.RM3_CON_PortNum+" Started............");
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
			sendingResult = concordiaObjecct.addItem(userID,itemId, itemName,number);
		}else if(function.equals("removeItem")) {
			String result = concordiaObjecct.removeItem(userID, itemId,number);
			sendingResult = result;
		}else if(function.equals("listItemAvailability")) {
			String result = concordiaObjecct.listItemAvailability(userID);
			sendingResult = result;
		}else if(function.equals("borrowItem")) {
			boolean result = concordiaObjecct.borrowItem(userID, itemId,number);
			sendingResult = Boolean.toString(result);
		}else if(function.equals("findItem")) {
			sendingResult = concordiaObjecct.findItem(userID,itemName);
		}else if(function.equals("returnItem")) {
			boolean result = concordiaObjecct.returnItem(userID,itemId);
			sendingResult = Boolean.toString(result);
		}else if(function.equals("waitInQueue")) {
			boolean result = concordiaObjecct.waitInQueue(userID,itemId);
			sendingResult = Boolean.toString(result);
		}else if(function.equals("exchangeItem")) {
			boolean result = concordiaObjecct.exchangeItem(userID,newItemId,itemId);
			sendingResult = Boolean.toString(result);
		}

		sendingResult= sendingResult+";"+RMNo+";"+"con";
		int FEPortNum = Integer.valueOf(parts[7]);
		String FEIpAddress = parts[8];

		sendMessageBackToFrontend(sendingResult,FEPortNum,FEIpAddress);
	}
	
	public static void sendMessageBackToFrontend(String message,int FEPortNum,String FEIpAddress) {
		System.out.println("Result : "+message);
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


}
