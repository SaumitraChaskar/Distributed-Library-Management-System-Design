package com.web.client;

import com.web.service.WebInterface;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class StudentClient {
	
	static WebInterface compInterface;

	public static void main(String[] args) throws MalformedURLException {
		System.out.println("Enter StudentID:");
		Scanner Id = new Scanner(System.in);
		String studentID = Id.nextLine().toUpperCase();
		String campus = studentID.substring(0,3);
		int addr = 0;

		if(studentID.length() != 8) {
			System.err.println("Invalid StudentID");
			main(new String[0]);
		}
		if(!studentID.substring(3,4).equals("U")){
			System.err.println("Invalid StudentID");
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

			if(compInterface.userLogin(studentID)){
				System.out.println("Log in successfully");
				Log(studentID, getFormatDate() + " Student[" + studentID + "] log in successfully");
			}
			else{
				System.out.println("Log in failed");
				Log(studentID, getFormatDate() + " Student[" + studentID + "] log in failed");
			}

			while(true){
				System.out.println(" ");
				System.out.println("++++++++++++++++++++++++++++++");
				System.out.println("|Please Select An Operation: |");
				System.out.println("|1: BorrowItem               |");
				System.out.println("|2: FindItem                 |");
				System.out.println("|3: ReturnItem               |");
				System.out.println("|4: ListBorrowedItem         |");
				System.out.println("|5: ExchangeItem             |");
				System.out.println("|6: Logout                   |");
				System.out.println("++++++++++++++++++++++++++++++");

				Scanner s = new Scanner(System.in);
				int input = s.nextInt();
				switch (input) {
					case 1:
						borrowItem(studentID);
						break;
					case 2:
						findItem(studentID);
						break;
					case 3:
						returnItem(studentID);
						break;
					case 4:
						listBorrowedItem(studentID);
						break;
					case 5:
						exchangeItem(studentID);
						break;
					case 6:
						main(new String[0]);
					default:
						break;
				}

			}

		}
		catch (Exception e) {
			System.err.println("Exception in StudentClient: " + e);
			main(new String[0]);
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
		String userAction = " Student ["+ userID + "] borrow item ["+itemID+"] for ["+days+"] days ---> ";
		String result = compInterface.borrowItem(itemCampus, userID, itemID, days);
		if(!result.isEmpty()){
			System.out.println(userAction+"Success.");
			Log(userID, getFormatDate() + userAction+"Success.");
		}else{
			System.out.println(userAction+"Failed.");
			Log(userID, getFormatDate() + userAction+"Failed.");
			System.out.println("Do You Want To Wait In The Queue?(Y/N)");
			Scanner input5 = new Scanner(System.in);
			String response = input5.nextLine();
			String userAction2 = " Student ["+ userID + "] wait in queue for item ["+itemID+"] ---> ";
			if(response.equalsIgnoreCase("y")){
				String waitCampus = itemID.substring(0,3);
				String waitResult = compInterface.waitInQueue(waitCampus, userID, itemID);
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

		String userAction3 = " Student ["+ userID + "] find item ["+itemName+"] ---> ";
		String findResult = compInterface.findItem(userID,itemName);
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
		String userAction4 = " Student ["+ userID + "] return item ["+returnItemID+"] ---> ";
		String returnResult = compInterface.returnItem(returnCampus, userID, returnItemID);
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
		String listResult = compInterface.listBorrowedItem(userID);
		System.out.println("All Borrowed Items : " + listResult);
	}

	private static void exchangeItem(String userID) throws Exception {
		System.out.println("Enter oldItemID:");
		Scanner input = new Scanner(System.in);
		String oldItemID = input.nextLine();
		System.out.println("Enter newItemID:");
		String newItemID = input.nextLine();

		String userAction = "Student ["+ userID +"] exchange with item ["+oldItemID+"] for item ["
				+newItemID+"] ---> ";
		String exchangeResult = compInterface.exchangeItem(userID, newItemID, oldItemID);

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

		String path = System.getProperty("user.dir")+"\\Assignment3\\Logs\\" + ID + "_Student.log";
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
