package main.java.com.smi.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        try {
            for (Map.Entry<String,Connection> connection: connectionMap.entrySet()) {
                connection.getValue().send(message);
            }
        } catch (IOException e) {
            System.out.println("Ваше сообщение не было отправлено.");
        }
    }
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                MessageType receiveType = message.getType();

                if (receiveType.equals(MessageType.USER_NAME)) {
                    String name = message.getData();
                    if (!name.isEmpty() && name != null && !connectionMap.containsKey(name)) {
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return name;
                    }
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> connectionEntry : connectionMap.entrySet()
            ) {
                if (!connectionEntry.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, connectionEntry.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    Message formatedMessage = new Message(MessageType.TEXT, String.format("%s: %s", userName, message.getData()));
                    sendBroadcastMessage(formatedMessage);
                } else {
                    ConsoleHelper.writeMessage("Что-то пошло не так.. Тип сообщения не TEXT.");
                }
            }
        }

        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)) {
                String name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");


            }


        }

    }
    public static void main(String[] args) throws IOException {
        int port =  ConsoleHelper.readInt();
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущен.");
        try {
            while (true){

               new Handler(serverSocket.accept()).start();
           }
           }catch (Exception e){
               System.out.println(e.getMessage());
               serverSocket.close();
           }




    }
}
