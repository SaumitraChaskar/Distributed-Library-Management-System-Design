import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

    public static void main(String[] args) {

        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String portNum = " ", registryURL = " ";
        int udpportnum = 0;
        try{

            if(args[0].equals("CON")){
                portNum = "1234";
                udpportnum = 2234;
            }
            else if(args[0].equals("MCG")){
                portNum = "1235";
                udpportnum = 2235;
            }
            else if(args[0].equals("MON")){
                portNum = "1236";
                udpportnum = 2236;
            }
            else {
                System.out.println("Server started failed");
                System.exit(0);
            }
            int RMIPortNum = Integer.parseInt(portNum);
            startRegistry(RMIPortNum);
            ServerImpl obj = new ServerImpl();
            registryURL = "rmi://localhost:" + portNum + "/DLMS-" + args[0];
            Naming.rebind(registryURL, obj);
            System.out.println ("Server registered.  Registry currently contains:");
            listRegistry(registryURL);
            System.out.println("DLMS Server ready.");

            DatagramSocket serversocket = new DatagramSocket(udpportnum);
            startlistening(args[0], obj, udpportnum, serversocket);
            obj.StartServer(args[0]);
        }
        catch (Exception re) {
            System.out.println("Exception in Server.main: " + re);
        }
    }


    private static void startlistening(String campusName, ServerImpl campusSever, int UDPlistenPort, DatagramSocket SeverSocket) throws Exception {

        String threadName = campusName + "listen";
        Listening listen = new Listening(threadName, SeverSocket, campusSever);
        listen.start();
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException {

        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list( );
        } catch (RemoteException e) {
            //System.out.println ("RMI registry cannot be located at port " + RMIPortNum);
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            System.out.println("RMI registry created at port " + RMIPortNum);
        }
    }

    private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {

        String [ ] names = Naming.list(registryURL);
        for (int i=0; i < names.length; i++)
            System.out.println(names[i]);
    }
}




