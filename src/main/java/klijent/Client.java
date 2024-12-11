package klijent;

import encryptdecrypt.CryptoUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    private static volatile boolean running = true;

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException {

        try {
            Socket socket = new Socket("localhost", 12345);
            System.out.println("Povezan na server.");

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            byte[] publicEncodedKey = (byte[]) in.readObject();
            System.out.println("Primljen javni ključ od servera: " + Arrays.toString(publicEncodedKey));

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey serverPublicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicEncodedKey));
            System.out.println("Primljen i rekonstruiran javni ključ servera.");

            new Thread(() -> handleIncomingMessages(in)).start();
            new Thread(() -> handleOutgoingMessages(out, serverPublicKey)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void handleIncomingMessages(ObjectInputStream in) {
        try {
            while (running) {
                if (in.available() > 0) {
                    byte[] encryptedMessage = (byte[]) in.readObject();
                    String decryptedMessage = CryptoUtils.decryptMessage(encryptedMessage);
                    System.out.println("Primljena poruka: " + decryptedMessage);
                } else System.out.println("Nema podataka za primiti.");
            }
        } catch (Exception e) {
            System.out.println("Primanje poruka prekinuto.");
            e.printStackTrace();
        }
    }

    private static void handleOutgoingMessages(ObjectOutputStream out, PublicKey serverPublicKey) {
        try {
            Scanner scanner = new Scanner(System.in);
            while (running) {
                System.out.print("Unesite poruku za server: ");
                String message = scanner.nextLine();
                if (!running) break; // Dodatna provjera nakon unosa
                byte[] encryptedMessage = CryptoUtils.encryptMessage(message);
                out.writeObject(encryptedMessage);
            }
        } catch (Exception e) {
            System.out.println("Slanje poruka prekinuto.");
            e.printStackTrace();
        }
    }
}
