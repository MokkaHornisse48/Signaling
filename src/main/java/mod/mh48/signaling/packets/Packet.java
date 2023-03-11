package mod.mh48.signaling.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mod.mh48.signaling.NetworkHandler;
import mod.mh48.signaling.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class Packet {

    public static List<Packet> packets = new ArrayList<>();

    public static int protocolVersion = 1;


    public ChannelHandlerContext ctx;
    public NetworkHandler handler;
    static {
        registerPackets();//Am anfang damit alles an seiner Stelle ist
    }
    public static boolean registerPackets(){
        register(new PingPacket());//0
        register(new PongPacket());//1
        register(new LoginRequestPacket());//2
        register(new LoginSuccessPacket());//3
        register(new ErrorPacket());//4
        register(new GetPublicServersPacket());//5
        register(new ConnectPacket());//6
        register(new Candidate2CCPacket());//7
        register(new Candidate2CSPacket());//8
        register(new AnswerPacket());//9
        register(new FinishedPacket());//10
        System.out.println(packets);
        return true;
    }

    public static void register(Packet packet){
        packet.setId( packets.size());
        packets.add(packet);
    }

    public static void handlePacket(NetworkHandler pHandler, ChannelHandlerContext pctx, ByteBuf pbuf){
        int pid = pbuf.readInt();
        if(pid>=packets.size()){
            System.out.println("SUS:"+pctx.channel().localAddress());
            return;
        }
        Packet packet = packets.get(pid).decode(pbuf);
        packet.ctx = pctx;
        packet.handler = pHandler;
        System.out.println("Debug P:"+packet);
        if(Utils.isSide(packet.handler.getSide(),packet.getSide())) packet.handle();
        else System.out.println("Wrong side in received packet:"+packet);
    }

    abstract public Packet decode(ByteBuf pbuf) ;

    public void encode(ByteBuf pbuf) {
        pbuf.writeInt(this.getId());
    }

    abstract public void handle();

    public String toString(){
        return super.toString()+":(id="+this.getId()+",side="+this.getSide()+",handler="+this.handler+",ctx="+this.ctx+")";
    }

    abstract public void setId(int i);
    abstract public int getId();

    abstract public Side getSide();
}
