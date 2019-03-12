package DLMS;


/**
* DLMS/CORBAInterfaceHelper.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从DLMS.idl
* 2019年3月12日 星期二 上午05时10分58秒 EDT
*/

abstract public class CORBAInterfaceHelper
{
  private static String  _id = "IDL:DLMS/CORBAInterface:1.0";

  public static void insert (org.omg.CORBA.Any a, DLMS.CORBAInterface that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static DLMS.CORBAInterface extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (DLMS.CORBAInterfaceHelper.id (), "CORBAInterface");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static DLMS.CORBAInterface read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_CORBAInterfaceStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, DLMS.CORBAInterface value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static DLMS.CORBAInterface narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DLMS.CORBAInterface)
      return (DLMS.CORBAInterface)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      DLMS._CORBAInterfaceStub stub = new DLMS._CORBAInterfaceStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static DLMS.CORBAInterface unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof DLMS.CORBAInterface)
      return (DLMS.CORBAInterface)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      DLMS._CORBAInterfaceStub stub = new DLMS._CORBAInterfaceStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
