package main.java.com.smi.chat.client;



import main.java.com.smi.chat.Connection;
import main.java.com.smi.chat.ConsoleHelper;
import main.java.com.smi.chat.Message;
import main.java.com.smi.chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;

    private volatile boolean clientConnected = false;
    protected String getServerAddress(){
        ConsoleHelper.writeMessage("Введите адрес сервера.");
        return ConsoleHelper.readString();
    }
    protected int getServerPort(){
       ConsoleHelper.writeMessage("Введите номер порта.");
       return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        ConsoleHelper.writeMessage("Введите имя пользователя.");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        SocketThread socketThread = new SocketThread();
        return socketThread;
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT,text));
        }catch (IOException e){
            ConsoleHelper.writeMessage("Произошла ошибка при отправке сообщения.");
            //clientConnected = false;
        }

    }
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {

                this.wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Прерывание. Выход из программы.");

        }


        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено.\n" +
                    "Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

        while (clientConnected) {
            String message = ConsoleHelper.readString();
            if (!message.equals("exit")) {
                if (shouldSendTextFromConsole())
                    sendTextMessage(message);
            } else {
                break;
            }
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " присоединился к чату.");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " покинул чат.");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true) {


                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else {
                    if (message.getType() == MessageType.NAME_ACCEPTED) {
                        notifyConnectionStatusChanged(true);
                        break;

                    } else throw new IOException("Unexpected MessageType");
                }
            }

        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else {
                    if (message.getType() == MessageType.USER_ADDED){
                        informAboutAddingNewUser(message.getData());
                    }else {
                        if (message.getType() == MessageType.USER_ADDED){
                            informAboutAddingNewUser(message.getData());
                        }else {
                            if (message.getType() == MessageType.USER_REMOVED){
                                informAboutDeletingNewUser(message.getData());
                            }else throw new IOException("Unexpected MessageType");
                        }
                    }
                }

            }
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(getServerAddress(),getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }

        }
    }





    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
