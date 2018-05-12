package BEC;

public final class GetPriviDescListRsp extends com.dengtacj.thoth.Message implements Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    public java.util.ArrayList<BEC.AccuPointPriviDesc> vItem = null;

    public java.util.ArrayList<BEC.AccuPointPriviDesc> getVItem()
    {
        return vItem;
    }

    public void  setVItem(java.util.ArrayList<BEC.AccuPointPriviDesc> vItem)
    {
        this.vItem = vItem;
    }

    public GetPriviDescListRsp()
    {
    }

    public GetPriviDescListRsp(java.util.ArrayList<BEC.AccuPointPriviDesc> vItem)
    {
        this.vItem = vItem;
    }

    public void write(com.dengtacj.thoth.BaseEncodeStream eos)
    {
        com.dengtacj.thoth.BaseEncodeStream ostream = new com.dengtacj.thoth.BaseEncodeStream(eos);
        ostream.setCharset(eos.getCharset());

        if (null != vItem) {
            ostream.writeList(0, vItem);
        }
    }

    static java.util.ArrayList<BEC.AccuPointPriviDesc> VAR_TYPE_4_VITEM = new java.util.ArrayList<BEC.AccuPointPriviDesc>();
    static {
        VAR_TYPE_4_VITEM.add(new BEC.AccuPointPriviDesc());
    }


    public void read(com.dengtacj.thoth.BaseDecodeStream dos)
    {
        com.dengtacj.thoth.BaseDecodeStream istream = new com.dengtacj.thoth.BaseDecodeStream(dos);
        istream.setCharset(dos.getCharset());

        this.vItem = (java.util.ArrayList<BEC.AccuPointPriviDesc>)istream.readList(0, false, VAR_TYPE_4_VITEM);
    }

}

