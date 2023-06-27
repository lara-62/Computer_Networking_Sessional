package Server;

import Client.Client;

import java.io.*;
import java.lang.ref.Cleaner;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import static java.lang.Math.min;

public class Server {



    private static Vector<Client_info> client_infos = null;
    private static int MAX_BUFFER_SIZE=10*1024*1024;
    private static int MIN_CHUNK_SIZE=0;
    private static int MAX_CHUNK_SIZE=10*10;
    private static int CUR_BUFFER_SIZE=0;
    private static int FileID=0;
    private static int requestID=0;
    private static HashMap<Integer,>s;
    private static Boolean Already_in_The_List(String name) {
        boolean in = false;
        for (Client_info clients : client_infos) {
            if (name.equalsIgnoreCase(clients.getUserName())) {
                in = true;
            }
        }
        return in;
    }

    static private Boolean isActive(String name) {
        boolean active = false;
        for (Client_info clients : client_infos) {
            if (name.equalsIgnoreCase(clients.getUserName()) && clients.isActive() == true) {
                active = true;
            }
        }
        return active;
    }

    private static Client_info getClient(String name) {
        for (Client_info clients : client_infos) {
            if (name.equalsIgnoreCase(clients.getUserName())) {
                return clients;
            }
        }
        return null;

    }

    private static void removeClient(String name) {
        for (Client_info clients : client_infos) {
            if (name.equalsIgnoreCase(clients.getUserName())) {
                client_infos.remove(clients);
                break;
            }
        }

    }



    private static String get_all_the_clients(String userName)
    {
        String info="(Clients with star are currently in online)\n";
        for (Client_info client:client_infos)
        {    if(!userName.equalsIgnoreCase(client.getUserName())) {
            if (client.isActive()) {
                info += "*";
            }
            info += " UserName:" + client.getUserName() + "\n";
        }
        }
        return info;
    }
    static int generatingRandomNumber(int min, int max)
    {
        Random random=new Random();

       return random.nextInt(max-min)+min;
    }
    static void UploadFile(DataOutputStream sendMessage,DataInputStream receiveMessage,Client_info Current_client,String filename,int filesize,String fileType,int CHUNK_SIZE,int FileID) throws IOException {
                File file=new File("src/Files/"+Current_client.getUserName()+"/"+fileType+"/"+filename);
       // System.out.println("you");
                FileOutputStream fileOutputStream=new FileOutputStream(file);
        //System.out.println("you");
        try {
            int bytes = -1;
            boolean success = true;
            byte[] buffer = new byte[CHUNK_SIZE];
            CUR_BUFFER_SIZE += CHUNK_SIZE;
            int totalChunk = 0;
           // System.out.println(filesize);
          //  System.out.println(CHUNK_SIZE);
            while (totalChunk < filesize) {
                //System.out.println("YES");
                success = (bytes = receiveMessage.read(buffer, 0, (int) min(filesize - totalChunk, buffer.length))) != -1;

                //System.out.println(bytes);
                if (!success) {
                    break;
                }
                fileOutputStream.write(buffer, 0, bytes);
                sendMessage.writeUTF("ACK");
                totalChunk += bytes;

            }
            //System.out.println("Kije hocche rasel vai");
            fileOutputStream.close();
            CUR_BUFFER_SIZE -= CHUNK_SIZE;
            String message = receiveMessage.readUTF();
            if (message.equalsIgnoreCase("ACK")) {
                if (totalChunk == filesize) {
                    sendMessage.writeUTF("File Uploaded SuccessFul");
                    FileInfo fileInfo=new FileInfo(filename,fileType);
                    fileInfo.setFileID(FileID);
                    Current_client.addFile(fileInfo);
                } else {
                    file.delete();
                    sendMessage.writeUTF("Failure to upload File.");
                }
            }
        }
        catch (IOException e)
        {
            file.delete();
        }

    }

    private static String LookUpFiles(Client_info Current_client,String query)
    {
        String message="";
        for (Client_info client_info:client_infos)
        {

            if(client_info.getUserName().equalsIgnoreCase(Current_client.getUserName()))
            {   if( query.equalsIgnoreCase("LMF")) {
                message += "All files of Client " + client_info.getUserName() + " (Yours) \n";
                for (FileInfo files : client_info.getFileInfos()) {
                    message += "FileName: " + files.getFilename() + "  FileID: " + files.getFileID() + " FileType: " + files.getFileType() + "\n";
                }
            }
            }
            else
            {   if(query.equalsIgnoreCase("LOF")) {
                message += "All files of Client " + client_info.getUserName() + "\n";
                for (FileInfo files : client_info.getFileInfos()) {
                    if (files.getFileType().equalsIgnoreCase("public"))
                        message += "FileName: " + files.getFilename() + "  FileID: " + files.getFileID() + " FileType: " + files.getFileType() + "\n";
                }
            }
            }
        }
        return message;
    }
    private static void SendFileToClient(DataOutputStream sendMessage,DataInputStream receiveMessage,int Fileid,Client_info info)
    {
            String filetype=null;
            String filename=null;
            Client_info fileofclient=null;
            boolean gotTheFile=false;
            for(Client_info client_info:client_infos)
            {
                for(FileInfo fileInfo:client_info.getFileInfos())
                {
                    if(Fileid==fileInfo.getFileID())
                    {
                        filetype=fileInfo.getFileType();
                        filename=fileInfo.getFilename();
                        fileofclient=client_info;
                        gotTheFile=true;
                        break;
                    }
                }
                if(gotTheFile)
                {
                    break;
                }
            }
            if(gotTheFile)
            {
                if(filetype.equalsIgnoreCase("public"))
                {
                    try {
                        sendMessage.writeUTF("Sending file");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(fileofclient==info)
                {
                    try {
                        sendMessage.writeUTF("Sending file");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    try {
                        sendMessage.writeUTF("Fail to send file");
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                int Chunksize=MAX_CHUNK_SIZE;

                int bytes = -1;
                boolean success = true;
                byte[] buffer = new byte[Chunksize];
                CUR_BUFFER_SIZE += Chunksize;

                int totalChunk = 0;
                File file=new File("src/Files/"+fileofclient.getUserName()+"/"+filetype+"/"+filename);
                long FileSize=file.length();
                try {
                    sendMessage.writeUTF(FileSize+" "+Chunksize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    FileInputStream fileInputStream=new FileInputStream(file);
                    while(fileInputStream.read(buffer,0,(int)min(buffer.length,FileSize))!=-1)
                    {
                        sendMessage.write(buffer,0,(int)min(buffer.length,FileSize));
                        sendMessage.flush();
                        if(FileSize>buffer.length)
                        {
                            FileSize-=buffer.length;
                        }

                    }
                    sendMessage.writeUTF("File download SuccessFull");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CUR_BUFFER_SIZE -= Chunksize;

            }
            else
            {
                try {
                    sendMessage.writeUTF("File with this id does not exists in server");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
   private  static  void SendMessage(Client_info currentClient,String file_description)
   {
       StringTokenizer stringTokenizer=new StringTokenizer(file_description," ");
       Vector<String>token=new Vector<>();
       while (stringTokenizer.hasMoreTokens())
       {
           token.add(stringTokenizer.nextToken());
       }
       String type=token.get(0);
       if(type.equalsIgnoreCase("FR"))
       {   requestID++;
           String s="Ping ping....\nNew File request From Client: "+currentClient.getUserName()+" with File Request Id: "+requestID+"\nRequested file description: ";
           for(int i=1;i<token.size();i++)
           {
               s+=token.get(i)+" ";
           }
           s+="\n";
           for(Client_info client_info:client_infos)
           {
               if(!client_info.getUserName().equalsIgnoreCase(currentClient.getUserName()))
               {     if(client_info.isActive()) {
                   try {
                       client_info.getSendMessage().writeUTF(s);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
               else
               {
                   client_info.add_unread_message(s);
               }
               }
           }
       }
       else if(type.equalsIgnoreCase("message"))
       {
            String to=token.get(1);
           String s="Ping ping....\nYou have a new message From Client "+currentClient.getUserName()+"\n"+currentClient.getUserName()+": ";
           for(int i=2;i<token.size();i++)
           {
               s+=token.get(i)+" ";
           }
           s+="\n";
           for(Client_info client_info:client_infos)
           {
               if(client_info.getUserName().equalsIgnoreCase(to) && !(client_info.getUserName().equalsIgnoreCase(currentClient.getUserName())))
               {  if(client_info.isActive()) {
                   try {
                       client_info.getSendMessage().writeUTF(s);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
               else
               {
                   client_info.add_unread_message(s);
               }
               }
           }
       }

   }
    public static void main(String[] args) throws IOException {


        ServerSocket serverSocket = new ServerSocket(6666);
        ServerSocket BroadCast=new ServerSocket(6667);//new added
        client_infos = new Vector<>();

        while (true) {
            System.out.println("\nServer is On...\n");
            Socket socket = serverSocket.accept();
            Socket broadcast=BroadCast.accept();//new added
            //Thread for individual clients
            Thread ClientThread = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            Client_info currentClient = null;
                            try {

                                DataOutputStream sendMessage = new DataOutputStream(socket.getOutputStream());
                                DataInputStream  receiveMessage = new DataInputStream(socket.getInputStream());
                                DataOutputStream SendBroadcast=new DataOutputStream(broadcast.getOutputStream());
                                DataInputStream ReceiveBroadcast=new DataInputStream(broadcast.getInputStream());
                                System.out.println("Client with port number : " + socket.getPort() + " is trying to connect");
                                sendMessage.writeUTF("Please give your User Name:");
                                String s = receiveMessage.readUTF();

                                if (Already_in_The_List(s)) {
                                    if (isActive(s)) {
                                        sendMessage.writeUTF("You are already logged in");
                                        System.out.println("Already Logged in .Connection Failed");
                                        socket.close();
                                    } else {
                                        sendMessage.writeUTF("Successfully logged in! Welcome to the portal:");
                                        Client_info prevAccount = getClient(s);
                                        Client_info client = new Client_info(true, s, socket.getPort());
                                        if (prevAccount != null) {
                                              client.setFileInfos(prevAccount.getFileInfos());
                                              client.setReceiveMessage(ReceiveBroadcast);
                                              client.setSendMessage(SendBroadcast);
                                              client.setUnreadMessages(prevAccount.getUnreadMessages());
                                        }
                                        removeClient(s);
                                        client_infos.add(client);
                                        System.out.println("Client with { port: " + socket.getPort() + ", ClientID: " + s + " } got connected to the portal");
                                        currentClient = getClient(s);
                                        if(currentClient.getUnreadMessages().size()>0)
                                        {
                                            currentClient.getSendMessage().writeUTF("Ping Ping ...\n You have some unread messages");
                                        }
                                    }
                                } else {
                                    sendMessage.writeUTF("Successfully Registered! Welcome to the portal:");
                                    Client_info client = new Client_info(true, s, socket.getPort());
                                    client.setReceiveMessage(ReceiveBroadcast);
                                    client.setSendMessage(SendBroadcast);
                                    client_infos.add(client);
                                    System.out.println("Client with { port: " + socket.getPort() + ", ClientID: " + s + " } got connected to the portal");
                                    //createfolder(s);
                                    new File("src/Files/"+s+"/public").mkdirs();
                                    new File("src/Files/"+s+"/private").mkdirs();
                                    currentClient = getClient(s);

                                }

                               while (true)
                               {
                                   s=receiveMessage.readUTF();
                                   //System.out.println(s);
                                   StringTokenizer stringTokenizer=new StringTokenizer(s," ");
                                   Vector<String>messages=new Vector<>();
                                   while (stringTokenizer.hasMoreTokens())
                                   {
                                       messages.add(stringTokenizer.nextToken());
                                   }
                                   if(messages.get(0).equalsIgnoreCase("SL"))
                                   {
                                       sendMessage.writeUTF(get_all_the_clients(currentClient.getUserName()));
                                   }
                                   else if(messages.get(0).equalsIgnoreCase("UF"))
                                   {
                                       String fileName=messages.get(1);
                                       int fileSize=Integer.parseInt(messages.get(2));

                                       String fileType=messages.get(3);
                                      // int CHUNK_SIZE=generatingRandomNumber(MIN_CHUNK_SIZE,MAX_CHUNK_SIZE);
                                       if(CUR_BUFFER_SIZE+fileSize>MAX_BUFFER_SIZE)
                                       {
                                           sendMessage.writeUTF("fail ");
                                       }
                                       else
                                       {
                                           int CHUNK_SIZE=generatingRandomNumber(MIN_CHUNK_SIZE,MAX_CHUNK_SIZE);
                                           FileID++;
                                           sendMessage.writeUTF("success "+CHUNK_SIZE+" "+FileID);
                                           UploadFile(sendMessage,receiveMessage,currentClient,fileName,fileSize,fileType,CHUNK_SIZE,FileID);
                                       }
                                   }
                                   else if((messages.get(0).equalsIgnoreCase("LMF"))||(messages.get(0).equalsIgnoreCase("LOF")))
                                   {
                                       sendMessage.writeUTF(LookUpFiles(currentClient,messages.get(0)));
                                   }
                                   else if(messages.get(0).equalsIgnoreCase("DF"))
                                   {
                                       SendFileToClient(sendMessage,receiveMessage,Integer.parseInt(messages.get(1)),currentClient);
                                   }
                                   else if(messages.get(0).equalsIgnoreCase("FR"))
                                   {

                                       SendMessage(currentClient,s);

                                   }
                                   else if(messages.get(0).equalsIgnoreCase("message"))
                                   {
                                      SendMessage(currentClient,s);
                                   }
                                   else if(messages.get(0).equalsIgnoreCase("show"))
                                   {
                                       if(currentClient.getUnreadMessages().size()>0)
                                       {
                                           currentClient.showUnreadMessages();
                                       }
                                       else
                                       {
                                           currentClient.getSendMessage().writeUTF("Ping ping...\n you have no unread messages");
                                       }
                                   }
                               }
                            } catch (IOException e) {

                                if(currentClient!=null)
                                {
                                    currentClient.setActive(false);
                                    System.out.println("Client with { port: " + socket.getPort() + ", ClientID: " + currentClient.getUserName() + " } logged out just now");
                                }
                                else
                                {
                                    System.out.println("Client with { port: " + socket.getPort() +  " } logged out just now");
                                }
                                try {
                                    socket.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }

                                return;
                            }


                        }
                    }
            );
            ClientThread.start();


        }

    }
}