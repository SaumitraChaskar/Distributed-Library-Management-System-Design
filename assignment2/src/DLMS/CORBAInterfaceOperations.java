package DLMS;


/**
* DLMS/CORBAInterfaceOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从DLMS.idl
* 2019年3月12日 星期二 上午05时10分58秒 EDT
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
