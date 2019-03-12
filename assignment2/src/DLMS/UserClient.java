package DLMS;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class UserClient {

    static CORBAInterface ServerImp;

    public static void main(String[] args) throws Throwable {

        System.out.println("Enter userID:");
        Scanner Id = new Scanner(System.in);
        String userID = Id.nextLine();
        String campus = userID.substring(0,3);
        String name = "";

        if(userID.length() != 8) {
            System.out.println("Invalid UserID");
            System.exit(1);
        }
        if(!userID.substring(3,4).equals("U")){
            System.out.println("Invalid UserID");
            System.exit(1);
        }

        if(campus.equals("CON"))
            name = "DLMSCON";
        else if(campus.equals("MCG"))
            name = "DLMSMCG";
        else if(campus.equals("MON"))
            name = "DLMSMON";
        else {
            System.out.println("Invalid UserID");
            System.exit(0);
        }

        try {

            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            ServerImp = CORBAInterfaceHelper.narrow(ncRef.resolve_str(name));

            if(ServerImp.userLogin(userID)){
                System.out.println("Log in successfully");
                Log(userID, getFormatDate() + " " + userID + " log in successfully");
            }
            else{
                System.out.println("Log in failed");
                Log(userID, getFormatDate() + " " + userID + " log in failed");
            }

            while(true){
                System.out.println("++++++++++++++++++++++++++++++");
                System.out.println("|Please Select An Operation: |");
                System.out.println("|1: BorrowItem               |");
                System.out.println("|2: FindItem                 |");
                System.out.println("|3: ReturnItem               |");
                System.out.println("|4: ListBorrowedItem         |");
                System.out.println("|5: ExchangeItem             |");
                System.out.println("|6: Exit                     |");
                System.out.println("++++++++++++++++++++++++++++++");

                Scanner s = new Scanner(System.in);
                int input = s.nextInt();
                switch (input) {
                    case 1:
                        borrowItem(userID);
                        break;
                    case 2:
                        findItem(userID);
                        break;
                    case 3:
                        returnItem(userID);
                        break;
                    case 4:
                        listBorrowedItem(userID);
                        break;
                    case 5:
                        exchangeItem(userID);
                        break;
                    case 6:
                        System.exit(6);
                    default:
                        break;
                }

            }

        }
        catch (Exception e) {
            System.out.println("Exception in UserClient: " + e);
        }

    }

    private static void borrowItem(String userID) throws Exception {
        System.out.println("Enter The ItemID");
        Scanner input1 = new Scanner(System.in);
        String itemID = input1.nextLine();
//                        System.out.println("Enter The NumberOfDays");
//                        Scanner input2 = new Scanner(System.in);
//                        int days = input2.nextInt();
        String days = String.valueOf(1);
        String itemCampus = itemID.substring(0,3);
        String userAction = " User ["+ userID + "] borrow item ["+itemID+"] for ["+days+"] days ---> ";
        String result = ServerImp.borrowItem(itemCampus, userID, itemID, days);
        if(!result.isEmpty()){
            System.out.println(userAction+"Success.");
            Log(userID, getFormatDate() + userAction+"Success.");
        }else{
            System.out.println(userAction+"Failed.");
            Log(userID, getFormatDate() + userAction+"Failed.");
            System.out.println("Do You Want To Wait In The Queue?(Y/N)");
            Scanner input5 = new Scanner(System.in);
            String response = input5.nextLine();
            String userAction2 = " User ["+ userID + "] wait in queue for item ["+itemID+"] ---> ";
            if(response.equalsIgnoreCase("y")){
                String waitCampus = itemID.substring(0,3);
                String waitResult = ServerImp.waitInQueue(waitCampus, userID, itemID);
                if(waitResult.equals(" ")){
                    System.out.println(userAction2+"Failed.");
                    Log(userID, getFormatDate() + userAction2+"Failed. ");
                }else{
                    System.out.println(userAction2+"Success. Position In Queue :"+waitResult);
                    Log(userID, getFormatDate() + userAction2+"Success. Position In Queue :"+waitResult);
                }
            }
        }
    }

    private static void findItem(String userID) throws Exception {
        System.out.println("Enter the ItemName");
        Scanner input3 = new Scanner(System.in);
        String itemName = input3.nextLine();

        String userAction3 = " User ["+ userID + "] find item ["+itemName+"] ---> ";
        String findResult = ServerImp.findItem(userID,itemName);
        if(findResult.isEmpty()){
            System.out.println(userAction3+ "Failed ");
            Log(userID, getFormatDate() + " " + userAction3+ "Failed ");
        }
        else {
            System.out.println(userAction3+ "Success. All items: " + findResult);
            Log(userID, getFormatDate() +userAction3+ "Success. All items: " + findResult);
        }
    }

    private static void returnItem(String userID) throws Exception {
        System.out.println("Enter ItemID To Be Returned:");
        Scanner input4 = new Scanner(System.in);
        String returnItemID = input4.nextLine();
        String returnCampus = returnItemID.substring(0,3);
        String userAction4 = " User ["+ userID + "] return item ["+returnItemID+"] ---> ";
        String returnResult = ServerImp.returnItem(returnCampus, userID, returnItemID);
        if(!returnResult.isEmpty()){
            System.out.println(userAction4+"Success");
            Log(userID, getFormatDate() + " " + userAction4 +" Success. ");
        }
        else {
            System.out.println(userAction4+"Failed ");
            Log(userID, getFormatDate() + " " + userAction4 + "Failed ");
        }
    }

    private static void listBorrowedItem(String userID) {
        String listResult = ServerImp.listBorrowedItem(userID);
        System.out.println("All Borrowed Items : " + listResult);
    }

    private static void exchangeItem(String userID) throws Exception {
        System.out.println("Enter oldItemID:");
        Scanner input = new Scanner(System.in);
        String oldItemID = input.nextLine();
        System.out.println("Enter newItemID:");
        String newItemID = input.nextLine();

        String userAction = "User ["+ userID +"] exchange with item ["+oldItemID+"] for item ["
                +newItemID+"] ---> ";
        String exchangeResult = ServerImp.exchangeItem(userID, newItemID, oldItemID);

        if(!exchangeResult.isEmpty()){
            System.out.println(userAction+"Success");
            Log(userID, getFormatDate() + " " + userAction +" Success. ");
        }
        else {
            System.out.println(userAction+"Failed ");
            Log(userID, getFormatDate() + " " + userAction + "Failed ");
        }
    }

    public static void Log(String ID,String Message) throws Exception{

        String path = "F:\\books\\COMP6231\\assignments\\assignment2\\UserLog\\" + ID + "_User.log";
        FileWriter fileWriter = new FileWriter(path,true);
        BufferedWriter bf = new BufferedWriter(fileWriter);
        bf.write(Message + "\n");
        bf.close();
    }

    public static String getFormatDate(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
