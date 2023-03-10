package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;

public class ErrorPacket extends Packet{
    public static int id;

    public int code;//0 = name to long //1 = id not found

    public int cause;//Packet id

    public ErrorPacket(){}

    public ErrorPacket(int pcause,int pcode){
        this.code = pcode;
        this.cause = pcause;
    }

    @Override
    public Packet decode(ByteBuf pbuf) {
        int pCause = pbuf.readInt();
        int pCode = pbuf.readInt();
        return new ErrorPacket(pCause,pCode);
    }

    @Override
    public void encode(ByteBuf pbuf) {
        super.encode(pbuf);
        pbuf.writeInt(cause);
        pbuf.writeInt(code);
    }

    @Override
    public void handle() {
        System.out.println("Error in "+Packet.packets.get(cause)+" with Errorcode:"+code);
    }

    @Override
    public void setId(int i) {
        this.id = i;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}
