package mod.mh48.p2pNetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import mod.mh48.signaling.Utils;
import mod.mh48.signaling.client.ClientClient;
import mod.mh48.signaling.client.P2PConnection;

import java.util.Scanner;

public class nettyclienttestlol {
    public static void main(String[] args){
        System.out.println("id:");
        Scanner my_scan = new Scanner(System.in);
        String pid = my_scan.nextLine();
        try {
            P2PConnection p2pConnection = ClientClient.createWEBRTC(pid);
            LocalAddress address = p2pConnection.getAddressToConnect();
            Bootstrap b = new Bootstrap();
            System.out.println("1");
            b.group(new NioEventLoopGroup())
                    .channel(LocalChannel.class)
                    .handler(new ChannelInitializer<LocalChannel>() { // (4)
                        @Override
                        public void initChannel(LocalChannel ch) throws Exception {
                            ch.pipeline().addLast(new testnh());
                        }
                    });
            System.out.println("2");
            ChannelFuture f = b.connect(address).sync();
            System.out.println("3");
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Connected successful to ("+future.channel().remoteAddress()+") with ("+future.channel().localAddress()+")");
                ByteBuf buf = f.channel().alloc().buffer();
                Utils.writeString(buf,"test");
                f.channel().writeAndFlush(buf);
            });
            System.out.println("4");
            Channel channel = f.channel();
            f.channel().closeFuture().sync();

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
            Utils.writeString(buf,"lol"+s);
            ctx.channel().writeAndFlush(buf);
        }
    }
}
