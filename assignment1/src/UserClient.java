import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class UserClient {

    public static void Log(String ID,String Message) throws Exception{

        String path = "F:\\books\\COMP6231\\assignments\\assignment1\\UserLog\\" + ID + "_User.log";
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
    public static void main(String[] args) {

        try {
            int RMIPort;
            String hostName;
            String portNum = " ";

            System.out.println("Enter userID:");
            Scanner Id = new Scanner(System.in);
            String userID = Id.nextLine();
            String campus = userID.substring(0,3);

            if(userID.length() != 8) {
                System.out.println("Invalid UserID");
                System.exit(1);
            }
            if(!userID.substring(3,4).equals("U")){
                System.out.println("Invalid UserID");
                System.exit(1);
            }

            if(campus.equals("CON"))
                portNum = "1234";
            else if(campus.equals("MCG"))
                portNum = "1235";
            else if(campus.equals("MON"))
                portNum = "1236";
            else {
                System.out.println("Invalid UserID");
                System.exit(0);
            }

            InputStreamReader is = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);

            RMIPort = Integer.parseInt(portNum);
            String registryURL = "rmi://localhost:" + portNum + "/DLMS-" + campus;

            ServerInterface h = (ServerInterface)Naming.lookup(registryURL);
            System.out.println("Lookup completed " );

            if(h.userLogin(userID)){
                System.out.println("Log in successfully");
                Log(userID, getFormatDate() + " " + userID + " log in successfully");
            }
            else{
                System.out.println("Log in failed");
                Log(userID, getFormatDate() + " " + userID + " log in failed");
            }
            while(true){
                System.out.println(" ");
                System.out.println("Please Select An Operation: ");
                System.out.println("1: BorrowItem");
                System.out.println("2: FindItem");
                System.out.println("3: ReturnItem");
                System.out.println("4: Exit" + "\n");

                Scanner s = new Scanner(System.in);
                int input = s.nextInt();
                switch (input) {
                    case 1:
                        System.out.println("Enter The ItemID");
                        Scanner input1 = new Scanner(System.in);
                        String itemID = input1.nextLine();
                        System.out.println("Enter The NumberOfDays");
                        Scanner input2 = new Scanner(System.in);
                        int days = input2.nextInt();
                        String itemCampus = itemID.substring(0,3);
                        String userAction = " User ["+ userID + "] borrow item ["+itemID+"] for ["+days+"] days ---> ";
                        String result = h.borrowItem(itemCampus, userID, itemID, days);
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
                                String waitResult = h.waitInQueue(waitCampus, userID, itemID);
                                if(waitResult.equals(" ")){
                                    System.out.println(userAction2+"Failed.");
                                    Log(userID, getFormatDate() + userAction2+"Failed. ");
                                }else{
                                    System.out.println(userAction2+"Success. Position In Queue :"+waitResult);
                                    Log(userID, getFormatDate() + userAction2+"Success. Position In Queue :"+waitResult);
                                }
                            }
                        }
                        break;
                    case 2:
                        System.out.println("Enter the ItemName");
                        Scanner input3 = new Scanner(System.in);
                        String itemName = input3.nextLine();

                        String userAction3 = " User ["+ userID + "] find item ["+itemName+"] ---> ";
                        String findResult = h.findItem(userID,itemName);
                        if(findResult.isEmpty()){
                            System.out.println(userAction3+ "Failed ");
                            Log(userID, getFormatDate() + " " + userAction3+ "Failed ");
                        }
                        else {
                            System.out.println(userAction3+ "Success. All items: " + findResult);
                            Log(userID, getFormatDate() +userAction3+ "Success. All items: " + findResult);
                        }
                        break;
                    case 3:
                        System.out.println("Enter ItemID To Be Returned:");
                        Scanner input4 = new Scanner(System.in);
                        String returnItemID = input4.nextLine();
                        String returnCampus = returnItemID.substring(0,3);
                        String userAction4 = " User ["+ userID + "] return item ["+returnItemID+"] ---> ";
                        String returnResult = h.returnItem(returnCampus, userID, returnItemID);
                        if(!returnResult.isEmpty()){
                            System.out.println(userAction4+"Success");
                            Log(userID, getFormatDate() + " " + userAction4 +" Success. ");
                        }
                        else {
                            System.out.println(userAction4+"Failed ");
                            Log(userID, getFormatDate() + " " + userAction4 + "Failed ");
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
            System.out.println("Exception in UserClient: " + e);
        }

    }

}
