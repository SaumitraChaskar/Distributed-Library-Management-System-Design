package DLMS;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ManagerClient {
    static CORBAInterface ServerImp;

    public static void main(String args[]) throws Throwable{

        System.out.println("Enter managerID:");
        Scanner Id = new Scanner(System.in);
        String managerID = Id.nextLine();
        String campus = managerID.substring(0,3);
        String name = "";

        if(managerID.length() != 8) {
            System.out.println("Invalid ManagerID");
            System.exit(1);
        }
        if(!managerID.substring(3,4).equals("M")) {
            System.out.println("Invalid ManagerID");
            System.exit(1);
        }

        if(campus.equals("CON"))
            name = "DLMSCON";
        else if(campus.equals("MCG"))
            name = "DLMSMCG";
        else if(campus.equals("MON"))
            name = "DLMSMON";
        else {
            System.out.println("Invalid ManagerID");
            System.exit(0);
        }

        try {

            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef =
                    orb.resolve_initial_references("NameService");
            NamingContextExt ncRef =
                    NamingContextExtHelper.narrow(objRef);
            ServerImp = CORBAInterfaceHelper.narrow(ncRef.resolve_str(name));

            if(ServerImp.managerLogin(managerID)){
                System.out.println("Log in successfully");
                Log(managerID, getFormatDate() + " " + managerID + " log in successfully");
            }
            else{
                System.out.println("Log in failed");
                Log(managerID, getFormatDate() + " " + managerID + " log in failed");
            }

            while(true){
                System.out.println(" ");
                System.out.println("Please Select An Operation: ");
                System.out.println("1: AddItem");
                System.out.println("2: RemoveItem");
                System.out.println("3: ListAllAvailability");
                System.out.println("4: Exit" + "\n");

                Scanner s = new Scanner(System.in);
                int input = s.nextInt();
                switch (input) {
                    case 1:
                        System.out.println("Enter Add ItemID");
                        Scanner input1 = new Scanner(System.in);
                        String itemID = input1.nextLine();
                        System.out.println("Enter ItemName");
                        Scanner input2 = new Scanner(System.in);
                        String itemName = input2.nextLine();
                        System.out.println("Enter ItemQuantity");
                        Scanner input3 = new Scanner(System.in);
                        String quantity = input3.nextLine();
                        String addAction = " Manager ["+managerID+"] add ["+quantity+"] " +
                                "of item ["+itemID+"] ["+itemName+"] to server ---> ";
                        String addResult = ServerImp.addItem(managerID, itemID, itemName,quantity);
                        if(!addResult.isEmpty()) {
                            System.out.println(addAction+"Success");
                            Log(managerID, getFormatDate()+addAction+"Success");
                            break;
                        }
                        else{
                            System.out.println(addAction+"Failed");
                            Log(managerID, getFormatDate()+addAction+"Failed");
                            break;
                        }

                    case 2:
                        System.out.println("Enter Remove ItemID");
                        Scanner input4 = new Scanner(System.in);
                        String removeItemID = input4.nextLine();
                        System.out.println("Enter ItemQuantity(Remove all if Quantity < 0)");
                        Scanner input5 = new Scanner(System.in);
                        String removeQuantity = input5.nextLine();
                        String addAction2 = " Manager ["+managerID+"] remove ["+removeQuantity+"] " +
                                "of item ["+removeItemID+"] from server ---> ";
                        String removeResult = ServerImp.removeItem(managerID, removeItemID, removeQuantity);
                        if(removeResult.isEmpty()){
                            System.out.println(addAction2+"Success");
                            Log(managerID, getFormatDate()+addAction2+"Success");
                        }
                        else{
                            System.out.println(addAction2+"Failed");
                            Log(managerID, getFormatDate() + addAction2+"Failed");
                        }
                        break;
                    case 3:
                        String addAction3 = " Manager ["+managerID+"] list all items in server ---> ";
                        String result = ServerImp.listItemAvailability(managerID);
                        if(!result.isEmpty()){
                            System.out.println(addAction3+"Success. All Items: "+result);
                            Log(managerID, getFormatDate() + addAction3+"Success. All Items: "+result);
                        }
                        else{
                            System.out.println(addAction3+"Failed");
                            Log(managerID, getFormatDate() + addAction3+"Failed");
                        }
                        break;
                    case 4:
                        System.exit(4);
                    default:
                        break;
                }

            }

        }
        catch (Exception e) {
            System.out.println("Exception in ManagerClient: " + e);
        }
    }

    public static void Log(String ID,String Message) throws Exception{

        String path = "F:\\books\\COMP6231\\assignments\\assignment2\\ManagerLog\\" + ID + "_Manager.log";
        FileWriter fileWriter = new FileWriter(path,true);
        BufferedWriter bf = new BufferedWriter(fileWriter);
        bf.write(Message + "\n");
        bf.close();
    }

    public static String getFormatDate(){
        Date date = new Date();
        long times = date.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }
}
