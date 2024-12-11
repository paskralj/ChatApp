package server;

import encryptdecrypt.CryptoUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Scanner;

public class ServerThread extends Thread {

    private final Socket socket;
    private final CryptoUtils cryptoUtils;
    private volatile boolean running = true;
    private ObjectInputStream in;
    private PublicKey publicServerKey;

    public ServerThread(Socket socket, CryptoUtils cryptoUtils, ObjectInputStream in, PublicKey publicServerKey) {
        this.socket = socket;
        this.cryptoUtils = cryptoUtils;
        this.in = in;
        this.publicServerKey = publicServerKey;
    }

    @Override
    public void run() {
        PublicKey publicKey = cryptoUtils.getPublicKey();
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(publicServerKey.getEncoded());
            out.flush();
            System.out.println("Server je poslao javni kljuc." + Arrays.toString(publicKey.toString().getBytes()));

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
                try {
                    byte[] encryptedMessage = (byte[]) in.readObject(); // Blokira dok poruka ne stigne
                    String decryptedMessage = cryptoUtils.decryptMessage(encryptedMessage);
                    System.out.println("Primljena poruka: " + decryptedMessage);

                    if (decryptedMessage.equalsIgnoreCase("quit")) {
                        System.out.println("Primljena naredba za gašenje.");
                        running = false; // Signal za prekid
                    }
                } catch (Exception e) {
                    System.out.println("Greška prilikom čitanja poruke.");
                    e.printStackTrace();
                    running = false;
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
