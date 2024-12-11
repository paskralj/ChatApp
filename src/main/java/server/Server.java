package server;

import encryptdecrypt.CryptoUtils;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

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
            PublicKey publicServerKey = keyPair.getPublic();
            PrivateKey privateServerKey = keyPair.getPrivate();


            // server socket port 12345
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server pokrenut na portu 12345. Čekam klijente...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Klijent povezan.");

                // primanje public klijent key-a
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                byte[] clientPublicKeyBytes = (byte[]) in.readObject(); // prima javni ključ klijenta
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey clientPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(clientPublicKeyBytes));
                System.out.println("Primljen javni ključ klijenta: " + Arrays.toString(clientPublicKey.toString().getBytes()));

                CryptoUtils cryptoUtils = new CryptoUtils(privateServerKey, clientPublicKey);
                // ode mi treba klijentov public key
                new Thread(new ServerThread(socket, cryptoUtils, in, publicServerKey)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
