package klijent;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

public class ClientThread extends Thread {

    private final Socket socket;
    private PublicKey publicKey;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
