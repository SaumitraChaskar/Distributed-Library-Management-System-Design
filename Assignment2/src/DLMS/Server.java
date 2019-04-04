package DLMS;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;

import java.net.DatagramSocket;
import java.util.Scanner;

public class Server {

    public static void main(String[] args) {

        int udpPortNum = 0;
        String campus;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Campus");
        campus = sc.nextLine();
        String name = "";

        try{

            ORB orb = ORB.init(args, null);
            POA rootPoa = (POA)orb.resolve_initial_references("RootPOA");
            rootPoa.the_POAManager().activate();

            ServerImp serverImp = new ServerImp();
//            serverImp.setORB(orb);

            org.omg.CORBA.Object ref = rootPoa.servant_to_reference(serverImp);

            CORBAInterface href = CORBAInterfaceHelper.narrow(ref);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            switch (campus) {
                case "CON":
                    name = "DLMSCON";
                    udpPortNum = 2234;
                    break;
                case "MCG":
                    name = "DLMSMCG";
                    udpPortNum = 2235;
                    break;
                case "MON":
                    name = "DLMSMON";
                    udpPortNum = 2236;
                    break;
                default:
                    System.out.println("Server started failed");
                    System.exit(0);
            }

            NameComponent path[] = ncRef.to_name( name );
            ncRef.rebind(path, href);
            System.out.println("DLMS ready and waiting ...");
            DatagramSocket serversocket = new DatagramSocket(udpPortNum);
            startListening(campus, serverImp, serversocket);
            serverImp.StartServer(campus);
            orb.run();

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




