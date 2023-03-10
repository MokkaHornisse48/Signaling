package mod.mh48.signaling.server;

import mod.mh48.signaling.packets.Packet;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting signaling server!");
        Packet.packets.isEmpty();
        new Server().run();

    }
}