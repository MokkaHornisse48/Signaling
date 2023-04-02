package mod.mh48.signaling.test;

import mod.mh48.signaling.client.ClientServer;

public class MainCS {
    public static void main(String[] args) {
        System.out.println("Starting signaling ClientServer!");
        new ClientServer("test",true,"127.0.0.1").run();

    }
}