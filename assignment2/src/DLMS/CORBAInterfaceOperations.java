package DLMS;


/**
* DLMS/CORBAInterfaceOperations.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��DLMS.idl
* 2019��3��12�� ���ڶ� ����05ʱ10��58�� EDT
*/

public interface CORBAInterfaceOperations 
{
  String addItem (String managerID, String itemID, String itemName, String quantity);
  String removeItem (String managerID, String itemID, String quantity);
  String listItemAvailability (String managerID);
  String borrowItem (String campusName, String userID, String itemID, String numberOfDays);
  String findItem (String userID, String itemName);
  String returnItem (String campusName, String userID, String itemID);
  String waitInQueue (String campusName, String userID, String itemID);
  boolean managerLogin (String adminID);
  boolean userLogin (String studentID);
  String listBorrowedItem (String userID);
  String exchangeItem (String studentID, String newItemID, String oldItemID);
} // interface CORBAInterfaceOperations
