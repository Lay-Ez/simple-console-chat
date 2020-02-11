package com.company;

import com.company.interfaces.Observable;
import com.company.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DataBase implements Observable {

    private List<String> messages = new ArrayList<>();
    private final ReentrantLock lockMessages = new ReentrantLock();

    private List<Observer> observers = new ArrayList<>();
    private final ReentrantLock lockObservers = new ReentrantLock();

    private List<String> nicknames = new ArrayList<>();
    private final ReentrantLock lockNicknames = new ReentrantLock();

    private static DataBase instance;

    private DataBase() {
    }

    static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }

        return instance;
    }

    public void addMessageToList(String message) {
        lockMessages.lock();
        try {
            messages.add(message);
            notifyObservers(message);
        } finally {
            lockMessages.unlock();
        }

    }

    public boolean addNickname(String nickname) {
        lockNicknames.lock();
        try {
            if (nicknames.contains(nickname)) {
                return false;
            }
            nicknames.add(nickname);
            return true;
        } finally {
            lockNicknames.unlock();
        }

    }

    public List<String> getTenLastMessages() {
        lockMessages.lock();
        try {
            if (messages.size() <= 10) {
                List<String> tenMessages = new ArrayList<>(messages);
                return tenMessages;
            } else {
                List<String> tenMessages = new ArrayList<>();
                for (int i = messages.size() - 10; i < messages.size(); i++) {
                    tenMessages.add(messages.get(i));
                }
                return tenMessages;
            }
        } finally {
            lockMessages.unlock();
        }
    }

    public void removeNickName(String nickname) {
        lockNicknames.lock();
        try {
            this.nicknames.remove(nickname);
        } finally {
            lockNicknames.unlock();
        }
    }

    public List<String> getNicknames() {
        lockNicknames.lock();
        try {
            return new ArrayList<>(nicknames);
        } finally {
            lockNicknames.unlock();
        }
    }

    public int getCurrentClientsCount() {
        return observers.size();
    }

    public int getCurrentMessagesCount() {
        return messages.size();
    }

    @Override
    public void registerObserver(Observer observer) {
        lockObservers.lock();
        try {
            if (!observers.contains(observer)) {
                observers.add(observer);
            } else {
                System.out.println("Observer already registered");
            }
        } finally {
            lockObservers.unlock();
        }

    }

    @Override
    public void removeObserver(Observer observer) {
        lockObservers.lock();
        try {
            if (observers.contains(observer)) {
                observers.remove(observer);
            } else {
                System.out.println("Trying to remove unregistered observer");
            }
        } finally {
            lockObservers.unlock();
        }
    }

    @Override
    public void notifyObservers(String lastMessage) {
        lockObservers.lock();
        try {
            observers.forEach(observer -> observer.update(lastMessage));
        } finally {
            lockObservers.unlock();
        }
    }
}