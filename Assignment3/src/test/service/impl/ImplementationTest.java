package test.service.impl;

import com.web.service.impl.Implementation;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class ImplementationTest {
    private Implementation implTest;
    private String managerID;
    private String managerID2;
    private String studentID;
    private String studentID2;
    private String itemID;
    private String itemID2;
    private String itemName;
    private String itemName2;
    private String quantity;
    private String campusName;
    private String days;

    @Before
    public void setUp(){
        managerID = "";
        managerID2 = "";
        studentID = "";
        studentID2 = "";
        itemID = "";
        itemID2 = "";
        itemName = "";
        itemName2 = "";
        quantity = "";
        campusName = "";
        days = "1";
        implTest = new Implementation();
        implTest.StartServer("CON");
    }

    @Test
    public void addItemTest(){

        managerID = "CONM0000";
        itemID = "CON0000";
        itemName = "Test";
        quantity = "9999";
        assertFalse(implTest.addItem(managerID,itemID,itemName,quantity).isEmpty());
    }

    @Test
    public void removeItemTest(){
        managerID = "CONM0000";
        itemID = "CON0000";
        itemName = "Test";
        quantity = "9999";
        implTest.addItem(managerID,itemID,itemName,quantity);

        assertFalse(implTest.removeItem(managerID,itemID,quantity).isEmpty());
    }

    @Test
    public void listItemAvailabilityTest(){
        managerID = "CONM0000";
        itemID = "CON0000";
        itemName = "Test";
        quantity = "9999";
        implTest.addItem(managerID,itemID,itemName,quantity);

        assertFalse(implTest.listItemAvailability(managerID).isEmpty());
    }

    @Test
    public void borrowTest(){
        managerID = "CONM0000";
        itemID = "CON0000";
        itemName = "Test";
        quantity = "9999";
        implTest.addItem(managerID,itemID,itemName,quantity);

        campusName = "CON";
        studentID = "CONU0000";
        assertFalse(implTest.borrowLocal(studentID,itemID).isEmpty());
    }

    @Test
    public void findItemLocalTest(){
        managerID = "CONM0000";
        itemID = "CON0000";
        itemName = "Test";
        quantity = "9999";
        implTest.addItem(managerID,itemID,itemName,quantity);

        assertFalse(implTest.findItemLocal(itemName).isEmpty());
    }

    @Test
    public void returnItemTest(){
        managerID = "CONM0000";
        itemID = "CON0000";
        itemName = "Test";
        quantity = "9999";
        implTest.addItem(managerID,itemID,itemName,quantity);

        campusName = "CON";
        studentID = "CONU0000";
        implTest.borrowLocal(studentID,itemID);

        assertFalse(implTest.returnItem(campusName,studentID,itemID).isEmpty());
    }

    @Test
    public void waitInQueueTest(){
        managerID = "CONM0000";
        itemID = "CON0000";
        itemName = "Test";
        quantity = "9999";
        implTest.addItem(managerID,itemID,itemName,quantity);

        campusName = "CON";
        studentID = "CONU0000";
        implTest.borrowLocal(studentID,itemID);

        studentID2 = "CONU9999";
        assertFalse(implTest.waitInQueue(campusName,studentID2,itemID).isEmpty());
    }

    @Test
    public void exchangeItemTest(){
        managerID = "CONM0000";
        managerID2 = "CONM0001";
        itemID = "CON0000";
        itemID2 = "CON0001";
        itemName = "Test";
        itemName2 = "Test2";
        quantity = "1";
        implTest.addItem(managerID,itemID,itemName,quantity);
        implTest.addItem(managerID2,itemID2,itemName2,quantity);

        campusName = "CON";
        studentID = "CONU0000";
        implTest.borrowLocal(studentID,itemID);

        assertFalse(implTest.exchangeItem(studentID,itemID2,itemID).isEmpty());

    }
}
