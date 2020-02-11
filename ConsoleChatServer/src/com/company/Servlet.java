package com.company;

import com.company.interfaces.Observer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Servlet extends Thread implements Observer {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    public String nickName;
    public static final String MESSAGE_DELIMITER = " - ";

    public Servlet(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("---Welcome to the chat!---");
            validateNickName();
            writer.println("----------------");
            DataBase.getInstance().registerObserver(this);
            printLastTenMessages();
            printAllCommands();
            while (!socket.isClosed()) {

                String message = reader.readLine();

                if (message.equalsIgnoreCase("!exit")) {
                    break;
                } else if (message.equalsIgnoreCase("!members")) {
                    printAllChatMembers();
                    continue;
                } else if (message.equalsIgnoreCase("!help")) {
                    printAllCommands();
                    continue;
                }

                new Thread(() -> DataBase.getInstance().
                        addMessageToList(nickName + MESSAGE_DELIMITER + message)).start();
            }

            terminateClient();

        } catch (IOException e) {
//            System.out.println("I/O error " + e.getMessage());
            terminateClient();
        }
    }

    public void terminateClient() {
        DataBase.getInstance().removeObserver(this);
        DataBase.getInstance().removeNickName(nickName);
        System.out.println("Client " + nickName + " disconnected");
        new Thread(() -> DataBase.getInstance().
                addMessageToList(nickName + " disconnected")).start();
    }

    @Override
    public void update(String message) {
        String[] split = message.split(MESSAGE_DELIMITER);
        if (!split[0].equals(nickName)) {
            new Thread(() -> writer.println(message)).start();
        }
    }

    private void validateNickName() throws IOException {
        writer.println("----------------");
        writer.println("Please enter your name below: ");
        while (!socket.isClosed()) {
            String name = reader.readLine();
            if (name.length() < 1) {
                writer.println("Your name is to short");
            } else if (name.length() > 12) {
                writer.println("Your name is too long!");
            } else {
                if (DataBase.getInstance().addNickname(name)) {
                    writer.println("All set " + name + ", your name is unique!");
                    nickName = name;
                    new Thread(() -> DataBase.getInstance().
                            addMessageToList(name + MESSAGE_DELIMITER + " entered the chat!")).start();
                    return;
                } else {
                    writer.println("Sorry, this name is taken. Try other one:");
                }
            }
        }
    }

    public void printLastTenMessages() {
        if (DataBase.getInstance().getCurrentMessagesCount() == 0) {
            writer.println("---No Recent Messages Found---");
            return;
        }
        writer.println("<<<Last Messages>>>");
        for (String message : DataBase.getInstance().getTenLastMessages()) {
            writer.println(message);
        }
        writer.println("<<<Last Messages>>>");
    }

    public void printAllChatMembers() {
        writer.println("----------------");
        writer.println("These people are currently in the chat:");
        StringBuilder sb = new StringBuilder();
        for (String name : DataBase.getInstance().getNicknames()) {
            sb.append(name).append(" | ");
        }
        writer.println(sb.toString());
        writer.println("----------------");
    }

    public void printAllCommands() {
        writer.println("----------------");
        writer.println("All available commands are:");
        writer.println("!help !members !exit");
        writer.println("----------------");
    }
}
