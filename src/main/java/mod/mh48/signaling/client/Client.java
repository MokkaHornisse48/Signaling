package mod.mh48.signaling.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import mod.mh48.signaling.*;
import mod.mh48.signaling.packets.*;

import javax.swing.text.Utilities;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Client extends ChannelInboundHandlerAdapter implements Instance {

    public Channel channel;
    public EventLoopGroup workerGroup;

    public Queue<Packet> packetQueue = new ConcurrentLinkedQueue();

    public String status;

    public boolean failed = false;

    String host = "127.0.0.1";

    public Client(String pHost){
        host = pHost;
        status = "Initialized";
    }

    public void sendAllPackets(){
        Packet p = packetQueue.poll();
        while (p!=null){
            Utils.sendPacket(p,channel);
            p = packetQueue.poll();
        }
    }

    public void addPacket(Packet packet){
        packetQueue.add(packet);
    }

    public void connect(){
        status = "Connecting";
        int port = 27776;
        workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NetworkHandler(Side.CLIENT,Client.this));
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.connect(host, port).sync();
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                status = "Pinging";
                Utils.sendPacket(new PingPacket(),future.channel());
            });
            channel = f.channel();

        }catch (InterruptedException e) {
            failed(e.getMessage());
        }
    }

    public void failed(String s){
        LogUtils.fatal(s);
        failed = true;
        status = s;
    }

    public String getStatus(){
        return status;
    }
    public void preOnConnected(Channel channel){
        status = "Connected";
        this.onConnected(channel);
    }

    public abstract void onConnected(Channel channel);

    public abstract void onError(Channel channel, ErrorPacket error);
}
