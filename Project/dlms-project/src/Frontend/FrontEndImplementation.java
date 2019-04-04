package Frontend;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Utils.CONSTANTS;
import Utils.PortsAndIPs;
import org.omg.CORBA.ORB;

import ServerObjectInterfaceApp.ServerObjectInterfacePOA;

import static java.lang.Thread.getAllStackTraces;
import static java.lang.Thread.sleep;

public class FrontEndImplementation extends ServerObjectInterfacePOA {
    private ORB orb;
    private static HashMap<String, Integer> incorrectRM = new HashMap<>();
    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    private void receiveFromAllRM(String request, ArrayList<String> result) {
        Runnable task = () -> {
            sendMessageAndReceive(request, result);
        };
        Thread thread = new Thread(task);
        thread.start();

    }

    private void sendMessageAndReceive(String dataFromClient, ArrayList<String> result) {
        ArrayList<DatagramPacket> packets = new ArrayList<>();
        HashMap<String, Boolean> rmCheck = new HashMap<>();
        HashMap<String, Boolean> serverCheck = new HashMap<>();
        serverCheck.put("CON", false);
        serverCheck.put("MCG", false);
        serverCheck.put("MON", false);
        rmCheck.put("1",false);
        rmCheck.put("2",false);
        rmCheck.put("3",false);

        String methodName = dataFromClient.split(";")[0];

        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, PortsAndIPs.Sequencer_PortNum);
            aSocket.send(request);

            //set time out
            aSocket.setSoTimeout(CONSTANTS.FE_TIME_OUT);

            byte[] buffer = new byte[1000];

            while (packets.size() < CONSTANTS.TOTAL_REPLICA) {
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(reply);
                synchronized (this) {
                    packets.add(reply);
                }
                String mess = new String(reply.getData(), 0, reply.getLength());
                String[] parts = mess.split(";");
                String rmNum = parts[1];
                String serverName = parts[2];
                serverCheck.put(serverName, true);
                rmCheck.put(rmNum,true);
                System.out.println("FE receive reply in method : "+methodName +" from  RM " +rmNum+" , server "+serverName );
            }

            //check reply nums
            if (packets.size() < CONSTANTS.TOTAL_REPLICA) {
                ArrayList<String> lostRMs = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : rmCheck.entrySet()) {
                    if (!entry.getValue()) {
                        for (Map.Entry<String, Boolean> entry1 : serverCheck.entrySet()) {
                            if (!entry1.getValue()) {
                                lostRMs.add("crash"+entry.getKey()+";"+entry1.getKey()+";"+methodName);
                                System.out.println("Lost Reply From RM "+entry.getKey()+" server "+entry1);

                            }
                        }
                    }
                }
                for(String info : lostRMs) {
                    informAllRMs(info);
                }
            } else {//receive all three packets
                chooseCorrectResult(packets, result);
            }

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
    private void chooseCorrectResult(ArrayList<DatagramPacket> packets, ArrayList<String> result) {
        assert (packets.size() == CONSTANTS.TOTAL_REPLICA) : "Wrong packets size: " + packets.size();
        String[] reply = new String[CONSTANTS.TOTAL_REPLICA];
        String[] rmNames = new String[CONSTANTS.TOTAL_REPLICA];
        String[] serverNames = new String[CONSTANTS.TOTAL_REPLICA];
        for (int i = 0; i < packets.size(); i++) {
            String[] parts = new String(packets.get(i).getData(), 0, packets.get(i).getLength()).split(";");
            reply[i] = parts[0];
            rmNames[i] = parts[1];
            serverNames[i] = parts[2];
        }
        findMajorityElement(reply, packets.size(), rmNames, serverNames, result);
    }
    private void findMajorityElement(String[] reply, int n, String[] rmNames, String[] serverNames, ArrayList<String> result) {
        String major = null;
        // check if A[i] is majority element or not
        for (int i = 0; i <= n / 2; i++) {
            int count = 1;
            for (int j = i + 1; j < n; j++) {
                if (reply[j].equals(reply[i])) {
                    count++;
                }
            }

            if (count > n / 2) {
                major = reply[i];
                //check diff result rm
                ArrayList<String> diffRMs = new ArrayList<>();
                if (count < n) {
                    for (int k = 0; k < reply.length; k++) {
                        if (!reply[k].equals(major)) {
                            diffRMs.add("fault"+";"+rmNames[k]+";"+serverNames[k]);
                            System.out.println("Different Results From RM :" + rmNames[k]+" server " + serverNames[k]);
//                            checkConsecutiveErrorRM(diffRMs);
                            for(String diff:diffRMs){
                                informAllRMs(diff);
                            }
                        }
                    }

                }
            }

        }
        System.out.println("major :" + major);
        if (major != null) {
            result.add(major);
        } else {
            System.out.println("no major");
            //TODO:no major
        }

    }
//    private void checkConsecutiveErrorRM(ArrayList<String> rmNames) {
//        if (incorrectRM.isEmpty()) {
//            for (String s : rmNames)
//                incorrectRM.put(s, 1);
//        } else {
//            for (Map.Entry<String, Integer> entry : incorrectRM.entrySet()) {
//                String rmName = entry.getKey();
//                if (rmNames.contains(rmName)) {
//                    if (incorrectRM.get(rmName) >= 3) {
//                        informAllRMs();//TODO:found incorrect rm
//                        incorrectRM.remove(rmName);
//                    } else {
//                        incorrectRM.put(rmName, incorrectRM.get(rmName) + 1);
//                    }
//                } else {
//                    incorrectRM.remove(rmName);
//                }
//            }
//            for (String newName : rmNames) {
//                if (!incorrectRM.containsKey(newName)) {
//                    incorrectRM.put(newName, 1);
//                }
//            }
//        }
//    }

    private void informAllRMs(String information) {
        DatagramSocket fSocket = null;
        try {
            fSocket = new DatagramSocket();
            byte[] message = information.getBytes();
            InetAddress aHost = InetAddress.getByName(PortsAndIPs.RM_Feedback_IPAddress);
            DatagramPacket feedback = new DatagramPacket(message,message.length,aHost,PortsAndIPs.RM_Feedback_Portnum);
            fSocket.send(feedback);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public String addItem(String managerId, String itemID, String itemName, int quantity) {
        String request = "addItem" + ";" + managerId + ";" + itemName + ";" + itemID + ";" + null + ";" + quantity + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.get(0);
    }

    public String removeItem(String managerID, String itemID, int quantity) {
        String request = "removeItem" + ";" + managerID + ";" + null + ";" + itemID + ";" + null + ";" + quantity + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.get(0);
    }

    public String listItemAvailability(String managerID) {
        String request = "listItemAvailability" + ";" + managerID + ";" + null + ";" + null + ";" + null + ";" + 0 + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.get(0);

    }

    public boolean borrowItem(String userID, String itemID, int numberOfDay) {
        String request = "borrowItem" + ";" + userID + ";" + null + ";" + itemID + ";" + null + ";" + numberOfDay + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Boolean.valueOf(result.get(0));
    }

    public String findItem(String userID, String itemName) {
        String request = "findItem" + ";" + userID + ";" + itemName + ";" + null + ";" + null + ";" + 0 + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.get(0);
    }

    public boolean returnItem(String userID, String itemID) {
        String request = "returnItem" + ";" + userID + ";" + null + ";" + itemID + ";" + null + ";" + 0 + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Boolean.valueOf(result.get(0));
    }

    public boolean waitInQueue(String userID, String itemID) {
        String request = "waitInQueue" + ";" + userID + ";" + null + ";" + itemID + ";" + null + ";" + 0 + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Boolean.valueOf(result.get(0));
    }

    public boolean exchangeItem(String userID, String newItemID, String oldItemID) {
        String request = "exchangeItem" + ";" + userID + ";" + null + ";" + oldItemID + ";" + newItemID + ";" + 0 + ";";
        ArrayList<String> result = new ArrayList<>();
        receiveFromAllRM(request, result);
        while (result.size() == 0) {
            System.out.println("Waiting for result to return ...");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return Boolean.valueOf(result.get(0));
    }


    // implement shutdown() method
    public void shutdown() {
        orb.shutdown(false);
    }
}