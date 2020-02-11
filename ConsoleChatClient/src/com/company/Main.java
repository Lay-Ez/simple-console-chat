package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static Socket socket;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int portNumber;
        String ip;

        IpValidator ipValidator = new IpValidator();

        while (true) {
            System.out.println("Enter IP in the format XXX.XXX.XXX.XXX or enter 'local' to find the server on your computer");
            ip = scanner.nextLine();
            if (ipValidator.validateIpFormat(ip)) {
                System.out.println("IP address is in correct format");
                break;
            } else if (ip.contains("local")) {
                ip = "localhost";
                break;
            } else {
                System.out.println("Wrong IP format");
            }
        }

        System.out.println("Enter a port number");
        while (true) {
            if (scanner.hasNextInt()) {
                portNumber = scanner.nextInt();
                break;
            } else {
                System.out.println("Please enter number of the port you're trying to connect to");
                scanner.next();
            }
        }

        System.out.println("Trying to connect to the server with port number " + portNumber);

        try {
            socket = new Socket(ip, portNumber);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            new Thread(new ReadingIn(socket)).start();

            while (!socket.isClosed()) {
                String message = scanner.nextLine();
                if (message.length() < 1) {
                    continue;
                }
                printWriter.println(message);

                if (message.equalsIgnoreCase("!exit")) {
                    break;
                }
            }
            scanner.close();
            socket.close();

        } catch (IOException e) {
            System.out.println("Client I/O error " + e.getMessage());
            System.out.println("Check your IP address and Port number and try again");
        }
    }
}

class ReadingIn extends Thread {
    private Socket socket;

    public ReadingIn(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
            while (!socket.isInputShutdown()) {
                String message = reader.readLine();
                System.out.println(message);
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server...");
        }
    }
}

class IpValidator{
    private Pattern pattern;
    private Matcher matcher;

    private static final String IP_ADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public IpValidator() {
        pattern = Pattern.compile(IP_ADDRESS_PATTERN);
    }

    public boolean validateIpFormat(String ip) {
        matcher = pattern.matcher(ip);
        return matcher.matches();
    }
}