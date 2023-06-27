package Client;

import Server.Client_info;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import static java.lang.Math.min;

public class Client {
    static private DataOutputStream SendMessage=null;
    static private DataInputStream ReceiveMessage=null;

    static Scanner scanner=null;


    //Functions

    private static void  SendFile(DataOutputStream sendMessage,DataInputStream receiveMessage,String FileName,String FileType) throws IOException {
        File file=new File("src/Client/"+FileName);
        long FileSize=file.length();
        //System.out.println(FileSize);
        sendMessage.writeUTF("UF "+FileName+" "+FileSize+" "+FileType);
        String s=null;
        while (true)
        {
            try {
                s=receiveMessage.readUTF();
               //System.out.println("YO"+s);
                break;
            }
           catch (IOException e)
           {
               System.out.println("not now");
               return;
           }
        }

        StringTokenizer stringTokenizer=new StringTokenizer(s," ");
        Vector<String>tokens=new Vector<>();
        int Chunksize;
        while (stringTokenizer.hasMoreTokens())
        {
            tokens.add(stringTokenizer.nextToken());
        }
        if(tokens.get(0).equalsIgnoreCase("fail"))
        {
            System.out.println("Server Buffer full");
            return;
        }
        else if(tokens.get(0).equalsIgnoreCase("success"))
        {

            Chunksize=Integer.parseInt(tokens.get(1));
            FileInputStream fileInputStream=new FileInputStream(file);
            byte [] buffer=new byte[Chunksize];
            while(fileInputStream.read(buffer,0,(int)min(buffer.length,FileSize))!=-1)
            {
                SendMessage.write(buffer,0,(int)min(buffer.length,FileSize));
                SendMessage.flush();
                if(FileSize>buffer.length)
                {
                    FileSize-=buffer.length;
                }
                s=ReceiveMessage.readUTF();
                System.out.println(s);
                if(!s.equalsIgnoreCase("ACK"))
                {
                    break;
                }
            }

            sendMessage.writeUTF("ACK");
            s=ReceiveMessage.readUTF();
            System.out.println(s);
        }
    }
    private  static  void DownloadFile(DataInputStream receiveMessage, DataOutputStream sendMessage, int FileId,String username)
    {
        try {
            String message=receiveMessage.readUTF();
             if(message.equalsIgnoreCase("Sending file"))
             {
                 message=receiveMessage.readUTF();
                 StringTokenizer stringTokenizer=new StringTokenizer(message," ");
                 Vector<String>messages=new Vector<>();
                 while (stringTokenizer.hasMoreTokens())
                 {
                     messages.add(stringTokenizer.nextToken());
                 }
                 long filesize=Long.parseLong(messages.get(0));
                 int Chunksize=Integer.parseInt(messages.get(0));
                 File file=new File("src/Client/Download/"+username+"_"+FileId+".txt");
                 FileOutputStream fileOutputStream=new FileOutputStream(file);
                 int bytes = -1;
                 boolean success = true;
                 byte[] buffer = new byte[Chunksize];
                 int totalChunk = 0;
                 while (totalChunk < filesize) {
                     success = (bytes = receiveMessage.read(buffer, 0, (int) min(filesize - totalChunk, buffer.length))) != -1;
                     if (!success) {
                         break;
                     }
                     fileOutputStream.write(buffer, 0, bytes);
                     totalChunk += bytes;
                 }
                 fileOutputStream.close();
                 message=receiveMessage.readUTF();
                 System.out.println(message);
             }
             else
             {
                 System.out.println(message);
                 return;
             }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws IOException {

        try {
            Socket socket=new Socket("localhost",6666);
            Socket Broadcast=new Socket("localhost",6667);
            ReceiveMessage=new DataInputStream(socket.getInputStream());
            SendMessage=new DataOutputStream(socket.getOutputStream());
            DataInputStream ReceiveBroadCastMessage=new DataInputStream(Broadcast.getInputStream());
            DataOutputStream SendBroadCastMessage=new DataOutputStream(Broadcast.getOutputStream());
            scanner=new Scanner(System.in);

               String message=ReceiveMessage.readUTF();
                System.out.println(message);
                message=scanner.nextLine();
                String userName=message;
                SendMessage.writeUTF(message);
                SendMessage.flush();
                message=ReceiveMessage.readUTF();
                System.out.println(message);
                if(message.equalsIgnoreCase("You are already logged in"))
                {
                    throw new IOException("Exit");
                }
                Thread Filethread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String query;
                        while (true)
                        {
                            System.out.println(userName+" What do you want to do?\n"+"\t# press 1 to see the list of other clients\n"+"\t# press 2 to upload you file\n"+"\t# press 3 to lookUp your own files\n"+"\t# press 4 to lookUp others public files\n"+"\t# press 5 to download Files\n"+"\t# press 6 to Request a File\n"+"\t# press 7 to upload a file acccording to any request\n"+"\t# press 8 to send Messages\n"+"\t# press 9 to see unread Messages");
                            query=scanner.nextLine();
                            if(query.equalsIgnoreCase("1"))
                            {

                                try {
                                    SendMessage.writeUTF("SL");
                                    SendMessage.flush();
                                    String message=ReceiveMessage.readUTF();
                                    System.out.println(message);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(query.equalsIgnoreCase("2"))
                            {
                                System.out.println("Give the file name:");
                                String fileName=scanner.nextLine();
                                System.out.println("Give the file type:");
                                String fileType=scanner.nextLine();

                                try {
                                    SendFile(SendMessage,ReceiveMessage,fileName,fileType);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(query.equalsIgnoreCase("3"))
                            {
                                try {
                                    SendMessage.writeUTF("LMF");
                                    String message=ReceiveMessage.readUTF();
                                    System.out.println(message);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(query.equalsIgnoreCase("4"))
                            {
                                try {
                                    SendMessage.writeUTF("LOF");
                                    String message=ReceiveMessage.readUTF();
                                    System.out.println(message);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(query.equalsIgnoreCase("5"))
                            {
                                System.out.println("Please give the file id you want to download:");
                                String fileid=scanner.nextLine();
                                try {
                                    SendMessage.writeUTF("DF "+fileid);
                                    DownloadFile(ReceiveMessage,SendMessage,Integer.parseInt(fileid),userName);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(query.equalsIgnoreCase("6"))
                            {
                                System.out.println("Please give the description of your requested file:");
                                String description=scanner.nextLine();
                                try {
                                   SendMessage.writeUTF("FR "+description);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(query.equalsIgnoreCase("8"))
                            {
                                System.out.println("Give the userName of the client you want to send the message:");
                                String userName=scanner.nextLine();
                                System.out.println("Give the message!");
                                String message=scanner.nextLine();
                                try {
                                    SendMessage.writeUTF("message "+userName+" "+message);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(query.equalsIgnoreCase("9"))
                            {

                                try {
                                    SendMessage.writeUTF("show");

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                });

          Filethread.start();
          Thread MessageThread=new Thread(new Runnable() {
              @Override
              public void run() {
                  while (true)
                  {

                      String s= null;
                      try {
                          s = ReceiveBroadCastMessage.readUTF();
                          System.out.println(s);
                      } catch (IOException e) {
                          e.printStackTrace();
                      }

                  }
              }
          });
          MessageThread.start();

        }
        catch (IOException e )
        {
            System.out.println("Connection Failed!");
            System.exit(1);
        }


    }
}
