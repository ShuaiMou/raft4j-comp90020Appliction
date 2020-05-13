package com.unimelb.raft4jcomp90020Appliction.raft;

import lombok.extern.slf4j.Slf4j;
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

    private String raftLeaderIP;

    private int raftLeaderPort;

    @Value("${raft.cluster.addresses}")
    private String raftClusterNodes;

    private Socket client;

    private List<RaftAddress> addresses;


    //与 raft 集群建立连接
    public Response put(LogEntry logEntry){
        Response response = null;
        buildConnection();
        try {
            System.out.println(raftLeaderIP + ": "+ raftLeaderPort);
            ObjectOutputStream clientOut = new ObjectOutputStream(client.getOutputStream());
            ObjectInputStream clientIn = new ObjectInputStream(client.getInputStream());
            clientOut.writeObject(logEntry);
            clientOut.flush();
            response = (Response) clientIn.readObject();
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

        //Todo: 后期可以增加超时，如果全部地址无法建立连接可以进一步处理
        while (!success){
            for (RaftAddress address : addresses){
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
        return true;
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
