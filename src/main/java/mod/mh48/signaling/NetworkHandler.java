package mod.mh48.signaling;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import mod.mh48.signaling.packets.Side;
import mod.mh48.signaling.packets.Packet;

public class NetworkHandler extends ChannelInboundHandlerAdapter {

    public Side side;
    public Instance instance;

    public NetworkHandler(Side pSide,Instance pInstance){
        side = pSide;
        instance = pInstance;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgo) { // (2)
        //System.out.println( msgo);
        ByteBuf msg = ((ByteBuf) msgo);
        // Discard the received data silently.
        //System.out.println(Utils.readString(msg));
        Packet.handlePacket(this,ctx,msg);
        msg.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    public Side getSide() {
        return this.side;
    }

    public Instance getInstance(){
        return instance;
    }

    public String toString(){
        return super.toString()+":(side="+this.getSide()+")";
    }
}
