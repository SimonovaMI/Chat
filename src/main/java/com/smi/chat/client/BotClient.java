package main.java.com.smi.chat.client;


import main.java.com.smi.chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    @Override
    protected String getUserName() {
        return "date_bot_" + (int)(Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.matches("[^:]+:[^:]+")) {
                boolean match = true;
                String name = message.substring(0, message.indexOf(":"));
                String command = message.substring(message.indexOf(":")+1).trim();
                SimpleDateFormat simpleDateFormat = null;
                Calendar calendar = new GregorianCalendar();
                Date date = calendar.getTime();
                switch (command){
                    case "дата": { 
                            simpleDateFormat = new SimpleDateFormat("d.MM.YYYY");
                            break;
                        }
                    case "день": {
                        simpleDateFormat = new SimpleDateFormat("d");
                        break;
                    }
                    case "месяц":{
                        simpleDateFormat = new SimpleDateFormat("MMMM");
                        break;
                        
                    }
                    case "год":{
                        simpleDateFormat = new SimpleDateFormat("YYYY");
                        break;
                    }
                    case "время":{
                        simpleDateFormat = new SimpleDateFormat("H:mm:ss");
                        break;
                    }
                    case "час":{
                        simpleDateFormat = new SimpleDateFormat("H");
                        break;
                    }
                    case "минуты":{
                        simpleDateFormat = new SimpleDateFormat("m");
                        break;
                    }
                    case "секунды":{
                        simpleDateFormat = new SimpleDateFormat("s");
                        break;
                    }
                    default:{
                        match = false;
                    }
                        
                }
                if (match){
                    BotClient.this.sendTextMessage("Информация для " + name + ": " + simpleDateFormat.format(date));
                }
            }
            
            
        }
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
