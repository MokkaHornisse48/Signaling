package mod.mh48.signaling.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Random;

public class Connector {

    public Channel channel;
    public String name;
    public boolean isPublic;

    public Connector(Channel pChannel,String pName,boolean pIsPublic){
        this.channel = pChannel;
        this.name = pName;
        this.isPublic = pIsPublic;
    }
}
