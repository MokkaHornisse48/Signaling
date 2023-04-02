package mod.mh48.signaling;

import io.netty.buffer.ByteBuf;

public class ConnectorInfo {
    public String id;
    public String name;

    public ConnectorInfo(String pId,String pName){
        this.id = pId;
        this.name = pName;
    }

    public String toString(){
        return super.toString()+":(id="+id+",name="+name+")";
    }
}
