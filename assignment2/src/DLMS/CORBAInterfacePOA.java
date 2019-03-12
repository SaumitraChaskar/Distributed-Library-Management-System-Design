package DLMS;


/**
* DLMS/CORBAInterfacePOA.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从DLMS.idl
* 2019年3月12日 星期二 上午05时10分58秒 EDT
*/

public abstract class CORBAInterfacePOA extends org.omg.PortableServer.Servant
 implements DLMS.CORBAInterfaceOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("addItem", new java.lang.Integer (0));
    _methods.put ("removeItem", new java.lang.Integer (1));
    _methods.put ("listItemAvailability", new java.lang.Integer (2));
    _methods.put ("borrowItem", new java.lang.Integer (3));
    _methods.put ("findItem", new java.lang.Integer (4));
    _methods.put ("returnItem", new java.lang.Integer (5));
    _methods.put ("waitInQueue", new java.lang.Integer (6));
    _methods.put ("managerLogin", new java.lang.Integer (7));
    _methods.put ("userLogin", new java.lang.Integer (8));
    _methods.put ("listBorrowedItem", new java.lang.Integer (9));
    _methods.put ("exchangeItem", new java.lang.Integer (10));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // DLMS/CORBAInterface/addItem
       {
         String managerID = in.read_string ();
         String itemID = in.read_string ();
         String itemName = in.read_string ();
         String quantity = in.read_string ();
         String $result = null;
         $result = this.addItem (managerID, itemID, itemName, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // DLMS/CORBAInterface/removeItem
       {
         String managerID = in.read_string ();
         String itemID = in.read_string ();
         String quantity = in.read_string ();
         String $result = null;
         $result = this.removeItem (managerID, itemID, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // DLMS/CORBAInterface/listItemAvailability
       {
         String managerID = in.read_string ();
         String $result = null;
         $result = this.listItemAvailability (managerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // DLMS/CORBAInterface/borrowItem
       {
         String campusName = in.read_string ();
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String numberOfDays = in.read_string ();
         String $result = null;
         $result = this.borrowItem (campusName, userID, itemID, numberOfDays);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // DLMS/CORBAInterface/findItem
       {
         String userID = in.read_string ();
         String itemName = in.read_string ();
         String $result = null;
         $result = this.findItem (userID, itemName);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // DLMS/CORBAInterface/returnItem
       {
         String campusName = in.read_string ();
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String $result = null;
         $result = this.returnItem (campusName, userID, itemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // DLMS/CORBAInterface/waitInQueue
       {
         String campusName = in.read_string ();
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String $result = null;
         $result = this.waitInQueue (campusName, userID, itemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 7:  // DLMS/CORBAInterface/managerLogin
       {
         String adminID = in.read_string ();
         boolean $result = false;
         $result = this.managerLogin (adminID);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 8:  // DLMS/CORBAInterface/userLogin
       {
         String studentID = in.read_string ();
         boolean $result = false;
         $result = this.userLogin (studentID);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 9:  // DLMS/CORBAInterface/listBorrowedItem
       {
         String userID = in.read_string ();
         String $result = null;
         $result = this.listBorrowedItem (userID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 10:  // DLMS/CORBAInterface/exchangeItem
       {
         String studentID = in.read_string ();
         String newItemID = in.read_string ();
         String oldItemID = in.read_string ();
         String $result = null;
         $result = this.exchangeItem (studentID, newItemID, oldItemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:DLMS/CORBAInterface:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public CORBAInterface _this() 
  {
    return CORBAInterfaceHelper.narrow(
    super._this_object());
  }

  public CORBAInterface _this(org.omg.CORBA.ORB orb) 
  {
    return CORBAInterfaceHelper.narrow(
    super._this_object(orb));
  }


} // class CORBAInterfacePOA
