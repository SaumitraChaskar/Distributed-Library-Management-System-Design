package DLMS;

/**
* DLMS/CORBAInterfaceHolder.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��DLMS.idl
* 2019��3��12�� ���ڶ� ����05ʱ10��58�� EDT
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
