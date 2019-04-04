package assignment3;

import java.net.DatagramSocket;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {

        int udpPortNum = 0;
        String campus;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Campus");
        campus = sc.nextLine();

        try{
            ServerImp serverImp = new ServerImp();
            switch (campus) {
                case "CON":
                    udpPortNum = 2234;
                    break;
                case "MCG":
                    udpPortNum = 2235;
                    break;
                case "MON":
                    udpPortNum = 2236;
                    break;
                default:
                    System.out.println("Server started failed");
                    System.exit(0);
            }

            System.out.println("DLMS ready and waiting ...");
            DatagramSocket serversocket = new DatagramSocket(udpPortNum);
            startListening(campus, serverImp, serversocket);
            serverImp.StartServer(campus);
        }
        catch (Exception re) {
            System.out.println("Exception in Server.main: " + re);
        }
    }

    private static void startListening(String campusName, ServerImp campusSever, DatagramSocket SeverSocket) {

        String threadName = campusName + "listen";
        Listening listen = new Listening(threadName, SeverSocket, campusSever);
        listen.start();
    }

}




