package mod.mh48.signaling.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.ClientServer;

public class nettyservertestlol {
    public static void main(String[] args){
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            ClientServer clientServer = new ClientServer("cool",true);
            b.group(new NioEventLoopGroup(),new NioEventLoopGroup())
                    .channel(LocalServerChannel.class) // (3)
                    .childHandler(new ChannelInitializer<LocalChannel>() { // (4)
                        @Override
                        public void initChannel(LocalChannel ch) throws Exception {
                            ch.pipeline().addLast(new testnh());
                        }
                    });

            // Bind and start to accept incoming connections.
            LocalAddress address = new LocalAddress("P2PS");
            clientServer.localAddress = address;
            ChannelFuture f = b.bind(address).sync(); // (7)
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Bind successful to "+future.channel().localAddress());
                new Thread(clientServer).start();
            });

            f.channel().closeFuture().sync();
            System.out.println("done");
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static class testnh extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msgo) { // (2)
            //System.out.println( msgo);
            ByteBuf msg = ((ByteBuf) msgo);
            // Discard the received data silently.
            String s = Utils.readString(msg);
            System.out.println(s);
            msg.release();
            ByteBuf buf = ctx.alloc().buffer();
            Utils.writeString(buf,"yay"+s);
            ctx.channel().writeAndFlush(buf);
        }
    }
}
