package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class Main {

    static boolean runServer = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a port number:");
        int portNumber;
        while (true) {
            if (scanner.hasNextInt()) {
                portNumber = scanner.nextInt();
                break;
            } else {
                System.out.println("Please enter number of the port");
                scanner.next();
            }
        }
        System.out.println("Starting the server with port number " + portNumber);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

            CommandListener commandListener = new CommandListener();
            new Thread(commandListener).start();

            while (runServer) {
                Socket socket = serverSocket.accept();
                Servlet servlet = new Servlet(socket);
                System.out.println("Client connected");

                new Thread(servlet).start();
            }

        } catch (IOException e) {
            System.out.println("Error in the main method " + e.getMessage());
        }
    }
}

class CommandListener extends Thread {

    private Scanner scanner;

    public CommandListener() {
        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (true) {
            String command = scanner.nextLine();
            if (command.equals("shutdown")) {
                System.out.println("Stopping the server");
                System.exit(0);
            }
        }
    }
}
