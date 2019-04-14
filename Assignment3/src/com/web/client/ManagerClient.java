package com.web.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.web.service.WebInterface;

public class ManagerClient {
	
	static WebInterface compInterface;

	public static void main(String[] args) throws MalformedURLException {
		String managerID;
		String campus;
		if(args.length==0){
			System.out.println("Enter managerID:");
			Scanner Id = new Scanner(System.in);
			managerID = Id.nextLine().toUpperCase();
			campus = managerID.substring(0,3);
		}else{
			managerID = args[0].toUpperCase();
			campus = managerID.substring(0,3);
		}
		int addr = 0;

		if(managerID.length() != 8) {
			System.err.println("Invalid ManagerID");
			main(new String[0]);
		}
		if(!managerID.substring(3,4).equals("M")) {
			System.err.println("Invalid ManagerID");
			main(new String[0]);
		}

		if(campus.equals("CON"))
			addr = 8080;
		else if(campus.equals("MCG"))
			addr = 8081;
		else if(campus.equals("MON"))
			addr = 8082;
		else {
			System.err.println("Invalid ManagerID");
			main(new String[0]);
		}

		URL compURL = new URL("http://localhost:"+addr+"/comp?wsdl");
		QName compQName = new QName("http://impl.service.web.com/", "ImplementationService");
		Service compService = Service.create(compURL, compQName);
		
		compInterface = compService.getPort(WebInterface.class);


		try {

			if(compInterface.managerLogin(managerID)){
				System.out.println("Log in successfully");
				Log(managerID, getFormatDate() + " Manager [" + managerID + "] log in successfully");
			}
			else{
				System.out.println("Log in failed");
				Log(managerID, getFormatDate() + " Manager [" + managerID + "] log in failed");
			}

			while(true){
				System.out.println(" ");
				System.out.println("++++++++++++++++++++++++++++++");
				System.out.println("|Please Select An Operation: |");
				System.out.println("|1: AddItem                  |");
				System.out.println("|2: RemoveItem               |");
				System.out.println("|3: ListAllAvailability      |");
				System.out.println("|4: Logout                   |");
				System.out.println("++++++++++++++++++++++++++++++");

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
						String addResult = compInterface.addItem(managerID, itemID, itemName,quantity);
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
						String removeResult = compInterface.removeItem(managerID, removeItemID, removeQuantity);
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
						String result = compInterface.listItemAvailability(managerID);
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
						main(new String[0]);
					default:
						break;
				}

			}

		}
		catch (Exception e) {
			System.err.println("Exception in ManagerClient: " + e);
			main(new String[0]);
		}

	}

	public static void Log(String ID,String Message) throws Exception{

		String path = System.getProperty("user.dir")+"\\Assignment3\\Logs\\" + ID + "_Manager.log";
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
