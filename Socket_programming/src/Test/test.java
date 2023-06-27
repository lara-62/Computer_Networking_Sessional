package Test;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import static java.lang.Math.min;

public class test {
    static DataOutputStream dataOutputStream=null;
    static DataInputStream dataInputStream=null;
    public static void main(String[] args) throws IOException {

        File read=new File("src/Test/demo.txt");
        FileInputStream fileInputStream=new FileInputStream(read);
        long length=read.length();
        System.out.println(length);
        File write=new File("src/Test/result.txt");
        FileOutputStream fileOutputStream=new FileOutputStream(write);

        byte [] buffer=new byte[10];


   try {
       Socket socket=new Socket("localhost", 6666);
       System.out.println("LocalPort: "+socket.getLocalPort()+"\nRemotePort: "+socket.getPort());
       dataInputStream=new DataInputStream(socket.getInputStream());
       dataOutputStream=new DataOutputStream(socket.getOutputStream());
       while (true) {
           String s = dataInputStream.readUTF();
           System.out.println(s);
           dataOutputStream.writeUTF("sendingfiles");
           dataOutputStream.flush();
           dataOutputStream.writeLong(length);
           dataOutputStream.flush();
           dataOutputStream.write(10);
           dataOutputStream.flush();
           while(fileInputStream.read(buffer,0,(int)min(buffer.length,length))!=-1)
           {
               dataOutputStream.write(buffer,0,(int)min(buffer.length,length));
               dataOutputStream.flush();
               if(length>buffer.length)
               {
                   length-=buffer.length;
               }
           }
           fileInputStream.close();
            s=dataInputStream.readUTF();
           System.out.println(s);
       }
   }
   catch (SocketException e)
   {
       System.out.println("Connection failed!");
   }

    }
}
