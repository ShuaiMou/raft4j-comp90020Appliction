package com.unimelb.raft4jcomp90020Appliction.testFileControllerTCP;

import com.unimelb.raft4jcomp90020Appliction.raft.LogEntry;
import com.unimelb.raft4jcomp90020Appliction.raft.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Date: 12/5/20 16:37
 * @Description:
 */
public class RaftLeader {
    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8011);
            System.out.println("raft node started successfully");
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(new UserThread(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class UserThread implements Runnable {
        private Socket socket;

        public UserThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            Socket client = null;
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                LogEntry entry = (LogEntry) in.readObject();
                System.out.println(entry);

                entry.setTerm(11111);
                out.writeObject(new Response("127.0.0.1", 8021, true));
                out.flush();
            } catch (IOException | ClassNotFoundException e) {
               e.printStackTrace();
            } finally {
                try {
                    if (client != null){
                        client.shutdownInput();
                        client.shutdownOutput();
                        client.close();
                    }
                    socket.shutdownInput();
                    socket.shutdownOutput();
                    socket.close();
                } catch (Exception e2) {
                    System.out.println("failed in closing socket io stream");
                }
            }

        }

    }

}
