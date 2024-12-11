package server;

import encryptdecrypt.CryptoUtils;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Server {

    public static void main(String[] args) {
        try {
            // generate public and private keys
            KeyPairGenerator keyGen;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
            } catch (Exception e) {
                System.err.println("RSA algorithm not available. Ensure your environment supports RSA.");
                return;
            }
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            CryptoUtils.setPrivateKey(privateKey);
            CryptoUtils.setPublicKey(publicKey);

            // server socket port 12345
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server pokrenut na portu 12345. Čekam klijente...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Klijent povezan.");
                new Thread(new ServerThread(socket, publicKey, privateKey)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}