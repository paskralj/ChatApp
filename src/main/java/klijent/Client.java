package klijent;

import encryptdecrypt.CryptoUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    private static volatile boolean running = true;
    private static Socket socket;

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {

        try {
            socket = new Socket("localhost", 12345);
            System.out.println("Povezan na server.");

            // generiranje privatnog kljuca, a za public key cemo uzesti serverov
            KeyPairGenerator keyGen;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
            } catch (Exception e) {
                System.err.println("RSA algorithm not available. Ensure your environment supports RSA.");
                return;
            }
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey privateClientKey = keyPair.getPrivate();
            PublicKey publicClientKey = keyPair.getPublic();

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            byte[] publicEncodedKey = (byte[]) in.readObject();
            System.out.println("Primljen javni ključ od servera: " + Arrays.toString(publicEncodedKey));

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey serverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicEncodedKey));
            System.out.println("Primljen i rekonstruiran javni ključ servera." + Arrays.toString(serverPublicKey.toString().getBytes()));

            CryptoUtils cryptoUtils = new CryptoUtils(privateClientKey, serverPublicKey);

            new Thread(() -> handleIncomingMessages(in, cryptoUtils)).start();
            new Thread(() -> handleOutgoingMessages(out, cryptoUtils)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void handleIncomingMessages(ObjectInputStream in, CryptoUtils cryptoUtils) {
        try {
            while (running) {
                if (socket.isClosed()) {
                    System.out.println("Socket zatvoren. Prekidam čitanje.");
                    break;
                }
                try {
                    byte[] encryptedMessage = (byte[]) in.readObject();// Blokira dok ne stigne poruka
                    String decryptedMessage = cryptoUtils.decryptMessage(encryptedMessage);
                    System.out.println("Primljena poruka od servera: " + decryptedMessage);
                } catch (Exception e) {
                    System.out.println("Greška prilikom primanja poruke.");
                    e.printStackTrace();
                    running = false;
                }
            }
        } catch (Exception e) {
            System.out.println("Primanje poruka prekinuto.");
            e.printStackTrace();
        }
    }

    private static void handleOutgoingMessages(ObjectOutputStream out, CryptoUtils cryptoUtils) {
        try {
            Scanner scanner = new Scanner(System.in);
            while (running) {
                System.out.print("Unesite poruku za server: ");
                String message = scanner.nextLine();
                if (!running) break; // Dodatna provjera nakon unosa

                byte[] encryptedMessage = cryptoUtils.encryptMessage(message);
                out.writeObject(encryptedMessage);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Slanje poruka prekinuto.");
            e.printStackTrace();
        }
    }
}
