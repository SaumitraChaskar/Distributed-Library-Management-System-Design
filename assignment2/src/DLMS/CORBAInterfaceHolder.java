package DLMS;

/**
* DLMS/CORBAInterfaceHolder.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从DLMS.idl
* 2019年3月12日 星期二 上午05时10分58秒 EDT
*/

public final class CORBAInterfaceHolder implements org.omg.CORBA.portable.Streamable
{
  public DLMS.CORBAInterface value = null;

  public CORBAInterfaceHolder ()
  {
  }

  public CORBAInterfaceHolder (DLMS.CORBAInterface initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = DLMS.CORBAInterfaceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    DLMS.CORBAInterfaceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return DLMS.CORBAInterfaceHelper.type ();
  }

}
