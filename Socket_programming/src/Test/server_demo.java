package Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Math.min;

public class server_demo {
    static DataInputStream dataInputStream=null;
    static DataOutputStream dataOutputStream=null;
    static FileInputStream fileInputStream=null;
    static FileOutputStream fileOutputStream=null;
    static int fileNumber=0;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket=new ServerSocket(6666);

        while (true)
        {
            System.out.println("Wating for client");
            Socket socket=serverSocket.accept();
            dataOutputStream=new DataOutputStream(socket.getOutputStream());
            dataInputStream=new DataInputStream(socket.getInputStream());
            System.out.println("localPort: "+socket.getLocalPort()+"\nRemotePort :"+socket.getPort());
            dataOutputStream.writeUTF("Hello client");
            dataOutputStream.flush();
            String s=dataInputStream.readUTF();

            if(s.equalsIgnoreCase("sendingfiles"))
            {
                System.out.println("dhuklam");
                File file=new File("src/Server/"+fileNumber+".txt");
                fileOutputStream=new FileOutputStream(file);
                fileNumber++;
                long length=dataInputStream.readLong();
                System.out.println(length);
                int ChunkSize=dataInputStream.read();
                System.out.println(ChunkSize);
                int  bytes=-1;
                byte[] buffer=new byte[ChunkSize];
                while (length>0)
                {  bytes=dataInputStream.read(buffer,0,(int)min(length,buffer.length));
                    System.out.println(bytes);

                   fileOutputStream.write(buffer,0,bytes);

                   if(length>ChunkSize)
                   {
                       length-=ChunkSize;
                   }
                   else
                   {
                       length=0;
                   }
                    System.out.println("bleh");
                }
                fileOutputStream.close();
                dataOutputStream.writeUTF("Downloaded your file");
                dataOutputStream.flush();
            }

        }
    }
}
