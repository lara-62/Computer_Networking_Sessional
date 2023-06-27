package Server;

import Client.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

public class Client_info {
       private boolean isActive;
       private  int portNumber;
       private String userName;
       Vector<FileInfo> fileInfos;
       DataOutputStream sendMessage;
       DataInputStream  receiveMessage;
       Vector<String> UnreadMessages;
    public Client_info(boolean isActive,String userName,int portNumber)
    {
        this.isActive=isActive;
        this.userName=userName;
        this.portNumber=portNumber;
        fileInfos=new Vector<>();
        UnreadMessages=new Vector<>();
    }
    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }


    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
    public void addFile(FileInfo info)
    {
        fileInfos.add(info);
    }

    public void setFileInfos(Vector<FileInfo> fileInfos) {
        this.fileInfos = fileInfos;
    }

    public Vector<FileInfo> getFileInfos() {
        return fileInfos;
    }

    public DataOutputStream getSendMessage() {
        return sendMessage;
    }

    public DataInputStream getReceiveMessage() {
        return receiveMessage;
    }

    public void setReceiveMessage(DataInputStream receiveMessage) {
        this.receiveMessage = receiveMessage;
    }

    public void setSendMessage(DataOutputStream sendMessage) {
        this.sendMessage = sendMessage;
    }
    public void add_unread_message(String message)
    {
        UnreadMessages.add(message);

    }

    public void setUnreadMessages(Vector<String> unreadMessages) {
        UnreadMessages = unreadMessages;
    }

    public Vector<String> getUnreadMessages() {
        return UnreadMessages;
    }
    public void showUnreadMessages()
    {
        for (String message:UnreadMessages)
        {
            try {
                sendMessage.writeUTF(message);
            } catch (IOException e) {
                System.out.println("something went wrong in message sending");
            }
        }
        UnreadMessages=new Vector<>();

    }
}
