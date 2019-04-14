package sequencer;

import Utils.PortsAndIPs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sequencer {
    private static int sequencerId = 1;

    public static void main(String[] args) {
        int portNum = PortsAndIPs.Sequencer_PortNum;
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(portNum);
            byte[] buffer = new byte[1000];
            System.out.println("Sequencer UDP Server "+portNum +" Started............");
            while (true) {
                DatagramPacket FERequest = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(FERequest);
                String request_message = new String(FERequest.getData(), 0, FERequest.getLength());
                System.out.println("Receive Message: "+request_message);

                sendMessageToRM(request_message,FERequest);
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

    public static void sendMessageToRM(String request_message,DatagramPacket FERequest) {
        request_message += sequencerId;
        sequencerId ++;
        request_message += ";";
        request_message += FERequest.getPort();
        request_message += ";";
        request_message += FERequest.getAddress().getHostName();

        DatagramSocket aSocket;
        int RMPort = PortsAndIPs.RM_Group_PortNum;
        try {
            aSocket = new DatagramSocket();
            byte[] messages = request_message.getBytes();
            InetAddress aHost = InetAddress.getByName(PortsAndIPs.RMGroupIPAddress);

            DatagramPacket SeqRequest = new DatagramPacket(messages, messages.length, aHost, RMPort);
            aSocket.send(SeqRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}