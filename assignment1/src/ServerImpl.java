import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class ServerImpl extends UnicastRemoteObject implements ServerInterface {

    class Admin{
        String adminID = " ";
    }
    class User{
        String userID = " ";
        int borrowCount = 0;
    }
    class Item{
        String ID;
        String name;
        int num;
    }

    HashMap<String, Item> items = new HashMap<>();
    HashMap<String, ArrayList<String> > waitList = new HashMap<>();
    HashMap<String, ArrayList<String> > borrowedItems = new HashMap<>();
    private String Campus = " ";

    ArrayList<Admin> adminClients = new ArrayList<>();
    ArrayList<User> userClients = new ArrayList<>();

    public ServerImpl() throws java.rmi.RemoteException{
        super();
    }


    public static void Log(String serverID,String Message) throws Exception{

        String path = "F:\\books\\COMP6231\\assignments\\assignment1\\ServerLog\\" + serverID + "_Server.log";
        FileWriter fileWriter = new FileWriter(path,true);
        BufferedWriter bf = new BufferedWriter(fileWriter);
        bf.write(Message + "\n");
        bf.close();
    }

    @Override
    public RemoteRef getRef() {
        return super.getRef();
    }

    public String getFormatDate(){
        Date date = new Date();
        long times = date.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    public void StartServer(String campus) {
        Campus = campus;
        try {
            Log(Campus,getFormatDate() + " Server for " + Campus + " started");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Item i1 = new Item();
        i1.name = "a";
        i1.num = 1;
        i1.ID = Campus+"1111";
        Item i2 = new Item();
        i2.name = "b";
        i2.num = 2;
        i2.ID = Campus+"2222";
        items.put(i1.ID,i1);
        items.put(i2.ID,i2);
    }

    public boolean managerLogin(String managerID) {

        Boolean exist = false;

        for(int i = 0; i < adminClients.size(); i ++){
            if(adminClients.get(i).adminID.equals(managerID)){
                exist = true;
                break;
            }
        }

        if(!exist){
            Admin newAdmin = new Admin();
            newAdmin.adminID = managerID;
            adminClients.add(newAdmin);
        }
        System.out.println("adminClient " + managerID + " log in successfully");
        try {
            Log(Campus, getFormatDate() + " ManagerClient [" + managerID + "] log in successfully" );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean studentLogin(String studentID) throws java.rmi.RemoteException{

        Boolean exist = false;

        for(int i = 0; i < userClients.size(); i ++){
            if(userClients.get(i).userID.equals(studentID)){
                exist = true;
                break;
            }
        }
        if(!exist){
            User newStudent = new User();
            newStudent.userID = studentID;
            newStudent.borrowCount = 0;
            userClients.add(newStudent);
        }
        System.out.println("userClient " + studentID + " log in");
        try {
            Log(Campus, getFormatDate() + " UserClient " + studentID + " log in" );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //manager operations
    @Override
    public String addItem (String managerID, String itemID, String itemName, int quantity) throws RemoteException {
        String result = " ";
        if(itemID.isEmpty() || itemName.isEmpty() || quantity==0){
            return " ";
        }
        boolean added = false;
        if(incertItem(managerID, itemID, itemName, quantity)){
            added = true;
            result = getFormatDate() + " Manager [" + managerID + "] add an item successfully. "
                    + "ItemID: " + itemID + " " + "ItemName: " + itemName + " " + "ItemQuantity: " + quantity;
            try {
                Log(Campus, result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(waitList.containsKey(itemID) && waitList.get(itemID).size() > 0) {
                String lendResult = autoLend(itemID);
                if (!lendResult.equals(" ")) {
                    try {
                        System.out.println("Server auto lend item ["+itemID+"] to user ["+lendResult+"] " +
                                "success after manager add item");
                        Log(Campus, getFormatDate() + " Server auto lend item ["+itemID+"] " +
                                "to user : " + lendResult+" after manager ["+managerID+"] add item. ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        System.out.println("Server auto lend item ["+itemID+"] to user ["+lendResult+"] " +
                                "failed after manager add item");
                        Log(Campus, getFormatDate() + " Server auto lend item ["+itemID+"] " +
                                "to user failed after manager ["+managerID+"] add item. ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(!added){
            String mess1 = getFormatDate() + " Manager [" + managerID + "] add an item failed. "
                    + "ItemID: " + itemID + " " + "ItemName: " + itemName + " " + "ItemQuantity: " + quantity;
            try {
                Log(Campus, mess1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private boolean incertItem(String managerID, String itemID, String itemName, int quantity){
        synchronized(this) {
            if(quantity >= 0){
                if (items.containsKey(itemID) && itemName.equals(items.get(itemID).name)){
                    items.get(itemID).num += quantity;
                    System.out.println("Manager increase quantity of an exist item Successfully");
                    return true;
                }else if(!items.containsKey(itemID)){
                    for(HashMap.Entry<String, Item> entry : items.entrySet()){
                        if(entry.getValue().name.equals(itemName)){
                            return false;
                        }
                    }
                    Item newItem = new Item();
                    newItem.name = itemName;
                    newItem.num = quantity;
                    items.put(itemID, newItem);
                    System.out.println("Manager add a new item Successfully");
                    return true;
                }else{
                    return false;
                }
            }
            else{
                System.out.println("Manager add an item failed");
                return false;
            }
        }
    }

    @Override
    public String removeItem (String managerID, String itemID, int quantity) throws RemoteException {
        String result = " ";
        boolean remove = false;
        synchronized(this) {
            if(items.containsKey(itemID)){
                if(items.get(itemID).num >= 0){
                    if(quantity < 0 ){
                        //remove all
                        items.remove(itemID);
                        if(waitList.containsKey(itemID)){
                            waitList.remove(itemID);
                        }if(borrowedItems.containsKey(itemID)){
                            borrowedItems.remove(itemID);
                        }
                        remove = true;
                        String mess = getFormatDate() + " Manager [" + managerID + "] remove all of " +
                                "item [" + itemID + "] successfully. ";
                        items.remove(itemID);
                        if(borrowedItems.containsKey(itemID)){
                            mess += "Remove this item from borrowed list";
                            borrowedItems.remove(itemID);
                        }
                        try {
                            Log(Campus, mess);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }else if(quantity <= items.get(itemID).num) {
                        items.get(itemID).num -= quantity;
                        remove =true;
                        String mess1 = getFormatDate() + " Manager [" + managerID + "] remove ["
                                + quantity + "] of item [" + itemID + "] successfully. ";
                        if(borrowedItems.containsKey(itemID)){
                            mess1 += "Remove this item from borrowed list";
                        }
                        try {
                            Log(Campus, mess1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        result = result + "No Enough Item To Remove";
                    }
                }else{
                    result = result + "No Item Available";
                }
            }else{
                result = result + "Item Not Found!";
            }
        }
        if(!remove){
            String mess2 = getFormatDate() + " Manager [" + managerID + "] remove ["
                    + quantity + "] of item [" + itemID + "] failed: ";
            mess2 += result;
            try {
                Log(Campus, mess2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String listItemAvailability (String managerID) throws RemoteException{
        String result = " ";
        for(HashMap.Entry<String, Item> entry : items.entrySet()){
            result = result + " , " + entry.getKey() + " " + entry.getValue().name + " " + entry.getValue().num;
        }
        if(result.equals(" ")){
            System.out.println(" Manager " + managerID + " list all of item failed");
            try {
                Log(Campus, getFormatDate() + " Manager " + managerID + " list all of item failed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            System.out.println(" Manager " + managerID + " list all of item successfully. "
                    + "All Items: " + result);
            try {
                Log(Campus, getFormatDate() + " Manager " + managerID + " list all of item successfully. "
                        + "All Items: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    //user operations
    @Override
    public String borrowItem (String campusName, String userID, String itemID, int numberOfDays) throws RemoteException{
        String result = " ";
        String command = "borrowItem(" + userID + "," + itemID + "," + numberOfDays + ")";

        for(int i = 0; i < userClients.size();i ++) {
            if (userClients.get(i).userID.equals(userID)) {
                try {
                    if (campusName.equals(Campus)) {
                        result = borrowLocal(userID, itemID);
                    } else if (campusName.equals("CON")) {
                        int serverport = 2234;
                        result = UDPRequest.UDPborrowItem(command, serverport);
                    } else if (campusName.equals("MCG")) {
                        int serverport = 2235;
                        result = UDPRequest.UDPborrowItem(command, serverport);
                    } else if (campusName.equals("MON")) {
                        int serverport = 2236;
                        result = UDPRequest.UDPborrowItem(command, serverport);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(!campusName.equals(Campus)) {
                    if (result.equals(" ")) {
                        String log = " Server borrow item ["+itemID+"] for user ["+userID+"] from ["+campusName+"] >> failed <<";
                        System.out.println(log);
                        try {
                            Log(Campus, getFormatDate() + log);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String log2 = " Server borrow item ["+itemID+"] for user ["+userID+"] from ["+campusName+"] >> success <<";
                        System.out.println(log2);
                        try {
                            Log(Campus, getFormatDate() + log2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
        return result;
    }

    public String borrowLocal(String userID, String itemID){
        String result = " ";
        String userCampus = userID.substring(0,3);
        synchronized (this){
            if(!userCampus.equals(Campus)){
                for(HashMap.Entry<String, ArrayList<String>> entry : borrowedItems.entrySet()){
                    if(entry.getValue().contains(userID)){
                        break;
                    }
                }
            }
            if (items.get(itemID).num > 0) {
                if(!borrowedItems.containsKey(itemID)){
                    ArrayList<String> newBorrowedUser = new ArrayList<>();
                    newBorrowedUser.add(userID);
                    borrowedItems.put(itemID,newBorrowedUser);
                    items.get(itemID).num--;
                }else{
                    if(!borrowedItems.get(itemID).contains(userID)) {
                        borrowedItems.get(itemID).add(userID);
                        items.get(itemID).num--;
                    }
                }
                result = itemID;


            }

            if (result.equals(" ")) {
                String log = " User [" + userID + "] borrow item ["+itemID+"] >> failed <<";
                System.out.println(log);
                try {
                    Log(Campus, getFormatDate() + log);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String log2 = " User [" + userID + "] borrow item ["+itemID+"] >> success <<";
                System.out.println(log2);
                try {
                    Log(Campus, getFormatDate() + log2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public String waitInQueue(String campusName, String userID, String itemID) {
        String result = " ";
        String command = "waitInQueue(" + userID + "," + itemID + ")";
        for (User temp : userClients) {
            if (temp.userID.equals(userID)) {
                try {
                    if (campusName.equals(Campus)) {
                        result = waitInLocal(userID, itemID);
                    } else if (campusName.equals("CON")) {
                        int serverport = 2234;
                        result = UDPRequest.UDPwaitInQueue(command, serverport);
                    } else if (campusName.equals("MCG")) {
                        int serverport = 2235;
                        result = UDPRequest.UDPwaitInQueue(command, serverport);
                    } else if (campusName.equals("MON")) {
                        int serverport = 2236;
                        result = UDPRequest.UDPwaitInQueue(command, serverport);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (result.equals(" ")) {
                    return " ";
                } else {
                    return result;
                }

            }
        }
        return " ";
    }

    public String waitInLocal(String userID, String itemID){
        String result = " ";
        synchronized (this) {

            if(!waitList.containsKey(itemID)){
                ArrayList<String> users = new ArrayList<>();
                users.add(userID);
                waitList.put(itemID,users);
                result = String.valueOf(waitList.get(itemID).indexOf(userID)+1);
            }else{
                if(!waitList.get(itemID).contains(userID)){
                    waitList.get(itemID).add(userID);
                    result = String.valueOf(waitList.get(itemID).indexOf(userID)+1);
                }
            }

            if (result.equals(" ")) {
                String mess = " Server add user [" + userID + "] in wait queue of item ["+itemID+ "] failed.";
                System.out.println(mess);
                try {
                    Log(Campus, getFormatDate() + mess);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String mess1 = " Server add user [" + userID + "] in wait queue of " +
                        "item ["+itemID+ "] at position [" +result+"] success.";
                System.out.println(mess1);
                try {
                    Log(Campus, getFormatDate() + mess1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }


    @Override
    public String findItem (String userID, String itemName) throws RemoteException {
        String result = " ";
        result = findItemLocal(itemName);
        String command = "findItem(" + itemName + ")";

        try {
            if(Campus.equals("CON")){
                int serverport1 = 2235;
                int serverport2 = 2236;
                result = result + " " + UDPRequest.UDPfindItem(command, serverport1);
                result = result + " " + UDPRequest.UDPfindItem(command, serverport2);
            }
            else if(Campus.equals("MCG")){
                int serverport1 = 2234;
                int serverport2 = 2236;
                result = result + " " + UDPRequest.UDPfindItem(command, serverport1);
                result = result + " " + UDPRequest.UDPfindItem(command, serverport2);
            }
            else{
                int serverport1 = 2234;
                int serverport2 = 2235;
                result = result + " " + UDPRequest.UDPfindItem(command, serverport1);
                result = result + " " + UDPRequest.UDPfindItem(command, serverport2);
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        if(!result.equals(" ")) {
            String log =" User [" + userID + "] found all item named ["+itemName +"] successfully . Items: "+result;
            try {
                System.out.println(log);
                Log(Campus, getFormatDate() + log );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            String log1 =" User [" + userID + "] found all item named ["+itemName +"] failed. ";
            try {
                System.out.println(log1);
                Log(Campus, getFormatDate() + log1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String findItemLocal(String itemName){
        String result = " ";
        int availablenum = 0;
        String itemID = " ";
        synchronized(this) {
            for(HashMap.Entry<String,Item> entry : items.entrySet()){
                if(entry.getValue().name.equals(itemName)){
                    availablenum = availablenum + entry.getValue().num;
                    itemID = entry.getKey();
                    result = itemID + " " + Integer.toString(availablenum);
                }
            }
        }

        return result;
    }

    @Override
    public String returnItem(String campusName, String userID, String itemID) throws RemoteException {
        String result = " ";
        String command = "returnItem(" + itemID + "," + userID + ")";
        int serverport;

        try {
            if(campusName.equals(Campus)){
                result = returnLocal(itemID,userID);

            }
            else if(campusName.equals("CON")){
                serverport = 2234;
                result = UDPRequest.UDPreturnItem(command,serverport);

            }
            else if(campusName.equals("MCG")){
                serverport = 2235;
                result = UDPRequest.UDPreturnItem(command,serverport);

            }
            else if(campusName.equals("MON")){
                serverport = 2236;
                result = UDPRequest.UDPreturnItem(command,serverport);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!campusName.equals(Campus)) {
            if (!result.equals(" ")) {
                String mess = " Server return item [" + itemID + "] for user [" + userID + "] to ["
                        + campusName + "] success";
                try {
                    System.out.println(mess);
                    Log(Campus, getFormatDate() + mess);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                String mess1 = " Server return item [" + itemID + "] for user [" + userID + "] to ["
                        + campusName + "] failed";
                try {
                    System.out.println(mess1);
                    Log(Campus, getFormatDate() + mess1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public String returnLocal(String itemID,String userID) throws RemoteException {
        String result = " ";
        synchronized (this) {
            if (borrowedItems.containsKey(itemID)) {
                if (borrowedItems.get(itemID).contains(userID)) {
                    borrowedItems.get(itemID).remove(userID);
                    items.get(itemID).num++;
                    result = itemID;
                    try {
                        String mess = " User [" + userID + "] return item [" + itemID + "] success.";
                        System.out.println(mess);
                        Log(Campus, getFormatDate() + mess);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(waitList.containsKey(itemID) && waitList.get(itemID).size() > 0) {
                        String lendResult = autoLend(itemID);
                        if (!lendResult.equals(" ")) {
                            try {
                                System.out.println("Server auto lend item ["+itemID+"] Successfully");
                                Log(Campus, getFormatDate() + " Server auto lend item ["+itemID+"] " +
                                        "to user : " + lendResult+" after user ["+userID+"] return. ");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                System.out.println("Server auto lend item ["+itemID+"] Failed");
                                Log(Campus, getFormatDate() + " Server auto lend item ["
                                        +itemID+"] failed after user ["+userID+"] return. ");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        }

        if(result.equals(" ")){
            String mess1 = " User [" + userID + "] return item [" + itemID + "] failed.";
            try {
                System.out.println(mess1);
                Log(Campus, getFormatDate() + mess1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public String autoLend (String itemID) throws RemoteException {
        String result =" ";
        if (waitList.containsKey(itemID) && waitList.get(itemID).size() > 0 ) {
            int left = items.get(itemID).num;
            int pointer = 0;
            while(left > 0 && waitList.get(itemID).size() > 0 && pointer < waitList.get(itemID).size() ) {
                String waitUser = waitList.get(itemID).get(pointer);
                result = borrowLocal(waitUser, itemID);
                if(!result.equals(" ")){
                    waitList.get(itemID).remove(waitUser);
                    result += waitUser+",";
                }else{
                    pointer ++;
                }
                left = items.get(itemID).num;
            }
        }
        return result;
    }

}
