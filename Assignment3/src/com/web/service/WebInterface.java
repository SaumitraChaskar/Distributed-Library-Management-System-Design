package com.web.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WebInterface {

	public boolean enrollCourse(String courseId);
	
	public int add(int a, int b);

	String addItem (String managerID, String itemID, String itemName, String quantity);

	String removeItem (String managerID, String itemID, String quantity);

	String listItemAvailability (String managerID);

	String borrowItem (String campusName, String userID, String itemID, String numberOfDays);

	String findItem (String userID, String itemName);

	String returnItem(String campusName, String userID, String itemID);

	String waitInQueue(String campusName, String userID, String itemID);

	String listBorrowedItem(String userID);

	String exchangeItem (String studentID, String newItemID, String oldItemID);

	boolean managerLogin(String adminID);
	boolean userLogin(String studentID);

}
