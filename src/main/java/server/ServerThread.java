package server;

import encryptdecrypt.CryptoUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class ServerThread extends Thread {

    private final Socket socket;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private volatile boolean running = true;

    public ServerThread(Socket socket, PublicKey publicKey, PrivateKey privateKey) {
        this.socket = socket;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.write(publicKey.getEncoded());
            System.out.println("Server je poslao javni kljuc.");

            Thread outgoingThread = new Thread(() -> handleOutgoingRequests(out));
            Thread incomingThread = new Thread(() -> handleIncomingRequests(in));

            incomingThread.start();
            outgoingThread.start();

            incomingThread.join();
            outgoingThread.join();

            System.out.println("Oba threada ugašena, zatvaram socket.");
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingRequests(ObjectInputStream in) {
        try {
            while (running) {
                if (socket.isClosed()) {
                    System.out.println("Socket je zatvoren. Prekidam primanje poruka.");
                    break;
                }
                byte[] encryptedMessage = (byte[]) in.readObject();
                String decryptedMessage = CryptoUtils.decryptMessage(encryptedMessage);
                System.out.println("Primljena poruka: " + decryptedMessage);

                if (decryptedMessage.equalsIgnoreCase("quit")) {
                    System.out.println("Primljena naredba za gašenje.");
                    running = false; // Postavlja se signal za prekid
                }
            }
        } catch (Exception e) {
            System.out.println("Primanje poruka prekinuto!");
            e.printStackTrace();
        }
    }

    private void handleOutgoingRequests(ObjectOutputStream out) {
        Scanner scanner = new Scanner(System.in);
        try {
            while (running) {
                System.out.print("Unesite poruku za klijenta: ");
                String message = scanner.nextLine();
                if (!running) break; // Dodatna provjera nakon unosa

                byte[] encryptedMessage = CryptoUtils.encryptMessage(message);
                out.writeObject(encryptedMessage);
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("Slanje poruka prekinuto.");
            e.printStackTrace();
        }
    }
}
