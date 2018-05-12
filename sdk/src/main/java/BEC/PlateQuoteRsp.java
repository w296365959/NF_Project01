package BEC;

public final class PlateQuoteRsp extends com.dengtacj.thoth.Message implements java.lang.Cloneable, java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    public java.util.ArrayList<BEC.PlateQuoteDesc> vPlateQuoteDesc = null;

    public java.util.ArrayList<BEC.PlateQuoteDesc> getVPlateQuoteDesc()
    {
        return vPlateQuoteDesc;
    }

    public void  setVPlateQuoteDesc(java.util.ArrayList<BEC.PlateQuoteDesc> vPlateQuoteDesc)
    {
        this.vPlateQuoteDesc = vPlateQuoteDesc;
    }

    public PlateQuoteRsp()
    {
    }

    public PlateQuoteRsp(java.util.ArrayList<BEC.PlateQuoteDesc> vPlateQuoteDesc)
    {
        this.vPlateQuoteDesc = vPlateQuoteDesc;
    }

    public void write(com.dengtacj.thoth.BaseEncodeStream eos)
    {
        com.dengtacj.thoth.BaseEncodeStream ostream = new com.dengtacj.thoth.BaseEncodeStream(eos);
        ostream.setCharset(eos.getCharset());

        if (null != vPlateQuoteDesc) {
            ostream.writeList(0, vPlateQuoteDesc);
        }
    }

    static java.util.ArrayList<BEC.PlateQuoteDesc> VAR_TYPE_4_VPLATEQUOTEDESC = new java.util.ArrayList<BEC.PlateQuoteDesc>();
    static {
        VAR_TYPE_4_VPLATEQUOTEDESC.add(new BEC.PlateQuoteDesc());
    }


    public void read(com.dengtacj.thoth.BaseDecodeStream dos)
    {
        com.dengtacj.thoth.BaseDecodeStream istream = new com.dengtacj.thoth.BaseDecodeStream(dos);
        istream.setCharset(dos.getCharset());

        this.vPlateQuoteDesc = (java.util.ArrayList<BEC.PlateQuoteDesc>)istream.readList(0, false, VAR_TYPE_4_VPLATEQUOTEDESC);
    }

}

