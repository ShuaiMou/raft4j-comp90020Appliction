package com.unimelb.raft4jcomp90020Appliction.raft;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @Date: 13/5/20 20:37
 * @Description:
 */
@Component
@Slf4j
@PropertySource("classpath:/application.properties")
public class Raft {
    private Gson gson = new Gson();

    @Autowired
    private StateMachine stateMachine;

    private String raftLeaderIP;

    private int raftLeaderPort;

    @Value("${raft.cluster.addresses}")
    private String raftClusterNodes;

    private Socket client;

    private List<RaftAddress> addresses;


    //与 raft 集群建立连接
    public boolean put(String fileName, String fileStorePath){
        List<String> parameters = new ArrayList<>();
        assert fileName != null;
        parameters.add(fileName);
        parameters.add(fileStorePath);
        LogEntry logEntry = LogEntry.newBuilder()
                .command("add")
                .parameters(parameters)
                .build();
        Response response = sendRequest(logEntry);
        if (response == null || !response.isSuccess()){
            return false;
        }else {
            //将日志持久化
            stateMachine.apply(logEntry);
            return true;
        }
    }

    public String get(String fileName){
        List<String> parameters = new ArrayList<>();
        parameters.add(fileName);
        LogEntry logEntry = LogEntry.newBuilder()
                .command("search")
                .parameters(parameters)
                .build();
        Response response = sendRequest(logEntry);
        if (response == null || !response.isSuccess()){
            return "";
        }

        return response.getPath();

    }

    public boolean delete(String fileName){
        List<String> parameters = new ArrayList<>();
        parameters.add(fileName);
        LogEntry logEntry = LogEntry.newBuilder()
                .command("delete")
                .parameters(parameters)
                .build();
        Response response = sendRequest(logEntry);
        if (response == null || !response.isSuccess()){
            return false;
        }
        stateMachine.apply(logEntry);
        return true;
    }

    private Response sendRequest(LogEntry logEntry){
        Response response = null;
        if (!buildConnection()){
            return response;
        }
        try {
            System.out.println(raftLeaderIP + ": "+ raftLeaderPort);
            ObjectOutputStream clientOut = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream clientIn = new ObjectInputStream(client.getInputStream());
            clientOut.writeObject(gson.toJson(logEntry));
            clientOut.flush();
            String responseString = (String) clientIn.readObject();
            if (responseString != null) {
                response = gson.fromJson(responseString, Response.class);
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage());
        } finally {
            if (response != null && (!response.getLeaderIP().equals(raftLeaderIP) || response.getLeaderPort()!=raftLeaderPort)) {
                log.info("修正 raftleader 地址" + raftLeaderIP +": "+ raftLeaderPort);
                raftLeaderIP = response.getLeaderIP();
                raftLeaderPort = response.getLeaderPort();
                log.info("修改后地址为：" + raftLeaderIP + ": "+ raftLeaderPort);
                try {
                    if (client != null) {
                        client.shutdownInput();
                        client.shutdownOutput();
                        client.close();
                        client = null;
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return response;
    }


    private boolean buildConnection(){
        //第一次调用加载集群节点 ip:port
        if (addresses == null){
            initialRaftClusterAddress();
        }
        //将 leader 地址放到 addresses 第一位
        RaftAddress leader = new RaftAddress(raftLeaderIP,raftLeaderPort);
        addresses.remove(leader);
        addresses.add(0,leader);

        boolean success = false;

        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        long timeout = 1000L;

        while (!success && endTime - startTime < timeout){
            for (RaftAddress address : addresses){
                endTime = System.currentTimeMillis();
                try {
                    client = new Socket(address.getIp(), address.getPort());
                    log.info("successfully connecting raft node [{}]", address);
                    success = true;
                    break;
                } catch (IOException e) {
                    log.error("build connect with raft node [{}]",address);
                }
            }
        }
        return success;
    }

    private void initialRaftClusterAddress(){
        addresses = new ArrayList<>();
        String[] nodes = raftClusterNodes.split(",");
        for (String node : nodes){
            String[] split = node.split(":");
            addresses.add(new RaftAddress(split[0].trim(), Integer.parseInt(split[1])));
            log.info("loading raft node address [{}]", node);
        }
        if (addresses.size() > 0){
            raftLeaderIP = addresses.get(0).getIp();
            raftLeaderPort = addresses.get(0).getPort();
        }
        log.info("complete in loading initial raft node, totally {} nodes", addresses.size());
    }

}
