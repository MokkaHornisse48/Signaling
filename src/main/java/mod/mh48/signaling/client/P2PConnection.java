package mod.mh48.signaling.client;

import dev.onvoid.webrtc.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class P2PConnection {

    public RTCPeerConnection peerConnection;

    public RTCDataChannel localDataChannel;
    public RTCDataChannel remoteDataChannel;

    public LocalChannel localChannel;

    public LocalAddress localAddress;

    public Queue<RTCDataChannelBuffer> sendQueue = new ConcurrentLinkedQueue();
    public Queue<byte[]> rcvQueue = new ConcurrentLinkedQueue();

    public P2PConnection(Consumer<RTCIceCandidate> onIce){
        PeerConnectionFactory factory = new PeerConnectionFactory();
        RTCIceServer iceServer = new RTCIceServer();
        iceServer.urls.add("stun:stun.l.google.com:19302");
        iceServer.urls.add("stun:stun1.l.google.com:19302");

        RTCConfiguration config = new RTCConfiguration();
        config.iceServers.add(iceServer);

        Random rn = new Random();

        peerConnection = factory.createPeerConnection(config, new PeerConnectionObserver() {
            @Override
            public void onSignalingChange(RTCSignalingState state) {
                System.out.println("Signaling:"+state);
            }

            @Override
            public void onConnectionChange(RTCPeerConnectionState state) {
                System.out.println("ConnectionChange:"+state);
                if(state==RTCPeerConnectionState.DISCONNECTED){
                    if(localChannel != null){
                        try {
                            localChannel.disconnect().sync();
                            localChannel = null;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);//todo error handling
                        }
                    }
                }
            }

            @Override
            public void onIceCandidate(RTCIceCandidate candidate) {
                onIce.accept(candidate);
            }

            @Override
            public void onDataChannel(RTCDataChannel pDataChannel) {
                remoteDataChannel = pDataChannel;
                System.out.println(remoteDataChannel.getLabel());
                remoteDataChannel.registerObserver(new RTCDataChannelObserver() {
                    @Override
                    public void onBufferedAmountChange(long previousAmount) {
                    }

                    @Override
                    public void onStateChange() {
                        System.out.println(remoteDataChannel.getState());
                        if(remoteDataChannel.getState()==RTCDataChannelState.OPEN){
                            RTCDataChannelBuffer p = sendQueue.poll();
                            while (p!=null){
                                try {
                                    localDataChannel.send(p);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);//todo error handling
                                }
                                p = sendQueue.poll();
                            }
                            if(localAddress != null){
                                try {
                                    Bootstrap b = new Bootstrap();
                                    b.group(new NioEventLoopGroup())
                                            .channel(LocalChannel.class)
                                            .handler(new ChannelInitializer<LocalChannel>() { // (4)
                                                @Override
                                                public void initChannel(LocalChannel ch) throws Exception {
                                                    ch.pipeline().addLast(new p2preader());
                                                }
                                            });
                                    ChannelFuture f = b.connect(localAddress).sync();
                                    f.addListener((ChannelFutureListener) future -> {
                                        if (!future.isSuccess()) {
                                            future.channel().pipeline().fireExceptionCaught(future.cause());
                                        }
                                        System.out.println("Connected successful to ("+future.channel().remoteAddress()+") with ("+future.channel().localAddress()+")");

                                        byte[] p2 = rcvQueue.poll();
                                        while (p2!=null){
                                            ByteBuf buf = localChannel.alloc().buffer();
                                            buf.writeBytes(p2);
                                            localChannel.writeAndFlush(buf);
                                            p2 = rcvQueue.poll();
                                        }
                                    });
                                    localChannel = (LocalChannel) f.channel();

                                }catch (InterruptedException e) {
                                    throw new RuntimeException(e);//todo error handling
                                }
                            }
                        }
                        if(remoteDataChannel.getState()==RTCDataChannelState.CLOSED){
                            if(localChannel != null){
                                try {
                                    localChannel.disconnect().sync();
                                    localChannel = null;
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);//todo error handling
                                }
                            }
                        }
                        /*ByteBuffer data = ByteBuffer.wrap("lol".getBytes());
                        try {
                            localDataChannel.send(new RTCDataChannelBuffer(data,true));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }*/
                    }

                    @Override
                    public void onMessage(RTCDataChannelBuffer buffer) {
                        byte[] arrayBuffer = new byte[buffer.data.remaining()];
                        buffer.data.get(arrayBuffer);
                        System.out.println("rcvd");
                        if(localChannel != null){
                            ByteBuf buf = localChannel.alloc().buffer();
                            buf.writeBytes(arrayBuffer);
                            localChannel.writeAndFlush(buf);
                        }else {
                            rcvQueue.add(arrayBuffer);
                        }
                        /*
                        ByteBuffer data = ByteBuffer.wrap(("Saft"+rn.nextInt()).getBytes());
                        try {
                            localDataChannel.send(new RTCDataChannelBuffer(data,true));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }*/
                    }
                });
            }
        });

        RTCDataChannelInit dataChannelOptions = new RTCDataChannelInit();
        //dataChannelOptions.priority = RTCPriorityType.HIGH;
        localDataChannel = peerConnection.createDataChannel("data", dataChannelOptions);
    }

    public LocalAddress getAddressToConnect(){
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(new NioEventLoopGroup(),new NioEventLoopGroup())
                    .channel(LocalServerChannel.class) // (3)
                    .childHandler(new ChannelInitializer<LocalChannel>() { // (4)
                        @Override
                        public void initChannel(LocalChannel ch) throws Exception {
                            ch.pipeline().addLast(new p2preader());
                            localChannel = ch;
                        }
                    });
            LocalAddress address = new LocalAddress("P2PC");
            ChannelFuture f = b.bind(address).sync(); // (7)
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
                System.out.println("Bind successful to "+future.channel().localAddress());
                byte[] p = rcvQueue.poll();
                while (p!=null){
                    ByteBuf buf = localChannel.alloc().buffer();
                    buf.writeBytes(p);
                    localChannel.writeAndFlush(buf);
                    p = rcvQueue.poll();
                }

            });

            //f.channel().closeFuture().sync();
            return address;

        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public TmpAsyncStore<String> makeOffer(){
        TmpAsyncStore<String> store = new TmpAsyncStore();
        peerConnection.createOffer(new RTCOfferOptions(), new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription description) {
                store.finish(description.sdp);
                peerConnection.setLocalDescription(description, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {

                    }
                    @Override
                    public void onFailure(String error) {
                        System.out.println(error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                System.out.println(error);
            }
        });
        return store;
    }

    public TmpAsyncStore<String> makeAnswer(String offer){
        TmpAsyncStore<String> store = new TmpAsyncStore();
        peerConnection.setRemoteDescription(new RTCSessionDescription(RTCSdpType.OFFER, offer), new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String error) {
                System.out.println(error);
            }
        });
        peerConnection.createAnswer(new RTCAnswerOptions(), new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription description) {
                store.finish(description.sdp);
                peerConnection.setLocalDescription(description, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(String error) {
                        System.out.println(error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                System.out.println(error);
            }
        });
        return store;
    }

    public void setAnswer(String answer){
        peerConnection.setRemoteDescription(new RTCSessionDescription(RTCSdpType.ANSWER, answer), new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(String error) {
                System.out.println(error);
            }
        });
    }

    public void addCandidate(RTCIceCandidate candidate){
        peerConnection.addIceCandidate(candidate);
    }

    public boolean isUsable(){
        if(remoteDataChannel!=null){
            return remoteDataChannel.getState()==RTCDataChannelState.OPEN;
        }
        return false;
    }

    public class p2preader extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msgo) throws Exception { // (2)
            System.out.println( msgo);
            ByteBuf msg = ((ByteBuf) msgo);
            byte[] msgb = new byte[msg.readableBytes()];
            msg.readBytes(msgb);
            //System.out.println(Arrays.toString(msgb));
            if(isUsable()){
                localDataChannel.send(new RTCDataChannelBuffer(ByteBuffer.wrap(msgb),true));
            }else {
                sendQueue.add(new RTCDataChannelBuffer(ByteBuffer.wrap(msgb),true));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            peerConnection.close();
        }
    }
}
