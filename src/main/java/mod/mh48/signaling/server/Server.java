package mod.mh48.signaling.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import mod.mh48.signaling.ConnectorInfo;
import mod.mh48.signaling.Instance;
import mod.mh48.signaling.LogUtils;
import mod.mh48.signaling.NetworkHandler;
import mod.mh48.signaling.packets.Side;

import java.util.*;

public class Server extends ChannelInboundHandlerAdapter implements Instance {

    public HashMap<String,Connector> connectors = new HashMap();

    public HashMap<String,Channel> clientClients = new HashMap();

    public List<ConnectorInfo> getConnectorInfos(){//todo Admin access
        List<ConnectorInfo> list = new ArrayList<ConnectorInfo>(connectors.size());
        for (Map.Entry<String, Connector> set : connectors.entrySet()) {
            list.add(new ConnectorInfo(set.getKey(),set.getValue().name,set.getValue().isPublic));
        }
        return list;
    }
    public List<ConnectorInfo> getPublicConnectorInfos(){
        ArrayList<ConnectorInfo> list = new ArrayList(connectors.size());
        for (Map.Entry<String, Connector> set : connectors.entrySet()) {
            if(set.getValue().isPublic) {
                list.add(new ConnectorInfo(set.getKey(), set.getValue().name, set.getValue().isPublic));
            }
        }
        list.trimToSize();
        return list;
    }

    public void updateClients(){
        for (Map.Entry<String, Connector> set : connectors.entrySet()) {
            if(!set.getValue().channel.isActive()){
                connectors.remove(set.getKey());
                LogUtils.info("Connector "+set.getKey()+" disconnected!");
            }
        }
        for (Map.Entry<String, Channel> set : clientClients.entrySet()) {
            if(!set.getValue().isActive()){
                connectors.remove(set.getKey());
                LogUtils.info("Client "+set.getKey()+" disconnected!");
            }
        }
    }

    public String newId(HashMap<String,?> map) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 6;
        Random random = new Random();
        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        if(map.containsKey(generatedString)){
            return newId(map);
        }else {
            return generatedString;
        }
    }

    public void run(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            Instance i = this;
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NetworkHandler(Side.SERVER,i));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(27776).sync(); // (7)
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Bind successful to"+future.channel().localAddress());
            });

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.

            f.channel().closeFuture().sync();

        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }



}
