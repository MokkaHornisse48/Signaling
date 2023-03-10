package mod.mh48.signaling.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import mod.mh48.signaling.ConnectorInfo;
import mod.mh48.signaling.Instance;
import mod.mh48.signaling.NetworkHandler;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.packets.GetPublicServersPacket;
import mod.mh48.signaling.packets.Packet;
import mod.mh48.signaling.packets.Side;

import javax.swing.text.Utilities;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Client extends ChannelInboundHandlerAdapter implements Instance {

    public Channel channel;
    public EventLoopGroup workerGroup;

    public Queue<Packet> packetQueue = new ConcurrentLinkedQueue();

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
        String host = "127.0.0.1";
        int port = 1234;
        workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            Instance i = this;
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NetworkHandler(Side.CLIENT,i));
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.connect(host, port).sync();
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Connected successful to ("+future.channel().remoteAddress()+") with ("+future.channel().localAddress()+")");

                this.onConnected(future.channel());
            });
            channel = f.channel();

        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void onConnected(Channel channel);
}
