// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 3.2.1.6 by WSRD Tencent.
// Generated from `NewsInfo.jce'
// **********************************************************************

package BEC;

public final class E_NEMS_GET_SOURCE implements java.io.Serializable
{
    private static E_NEMS_GET_SOURCE[] __values = new E_NEMS_GET_SOURCE[2];
    private int __value;
    private String __T = new String();

    public static final int _E_STOCK_MARKET_GET = 1;
    public static final E_NEMS_GET_SOURCE E_STOCK_MARKET_GET = new E_NEMS_GET_SOURCE(0,_E_STOCK_MARKET_GET,"E_STOCK_MARKET_GET");
    public static final int _E_STOCK_NEWSLIST_GET = 2;
    public static final E_NEMS_GET_SOURCE E_STOCK_NEWSLIST_GET = new E_NEMS_GET_SOURCE(1,_E_STOCK_NEWSLIST_GET,"E_STOCK_NEWSLIST_GET");

    public static E_NEMS_GET_SOURCE convert(int val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].value() == val)
            {
                return __values[__i];
            }
        }
        assert false;
        return null;
    }

    public static E_NEMS_GET_SOURCE convert(String val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].toString().equals(val))
            {
                return __values[__i];
            }
        }
        assert false;
        return null;
    }

    public int value()
    {
        return __value;
    }

    public String toString()
    {
        return __T;
    }

    private E_NEMS_GET_SOURCE(int index, int val, String s)
    {
        __T = s;
        __value = val;
        __values[index] = this;
    }

}
