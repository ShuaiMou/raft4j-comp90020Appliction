package com.unimelb.raft4jcomp90020Appliction.controller;


import com.unimelb.raft4jcomp90020Appliction.domain.JsonData;
import com.unimelb.raft4jcomp90020Appliction.domain.LogEntry;
import com.unimelb.raft4jcomp90020Appliction.domain.Response;
import com.unimelb.raft4jcomp90020Appliction.domain.StateType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/file")
@PropertySource("classpath:/application.properties")
public class FileController {


    @Value("${file.store.path}")
    private String fileStorePath;


    @Value("${raftLeaderIP}")
    public String raftLeaderIP;

    @Value("${raftLeaderPort}")
    public String raftLeaderPort;

    private Socket client;
    private ObjectOutputStream clientOut;
    private ObjectInputStream clientIn;


    ////与 raft 集群建立连接
    public Response put(LogEntry logEntry){
        Response response = null;
        if (client == null) {
            try {
                client = new Socket(raftLeaderIP, Integer.parseInt(raftLeaderPort));
                clientOut = new ObjectOutputStream(client.getOutputStream());
                clientIn = new ObjectInputStream(client.getInputStream());
                clientOut.writeObject(logEntry);
                clientOut.flush();
                response = (Response) clientIn.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (response != null && !response.getLeaderIP().equals(raftLeaderIP)) {
                    raftLeaderIP = response.getLeaderIP();
                    raftLeaderPort = Integer.toString(response.getLeaderPort());
                    try {
                        if (client != null) {
                            client.shutdownInput();
                            client.shutdownOutput();
                            client.close();
                            client = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            try {
                clientOut.writeObject(logEntry);
                clientOut.flush();
                response = (Response) clientIn.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    @PostMapping("/add")
    public JsonData uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request){
        String fileName = file.getOriginalFilename();
        System.out.println("文件名为"+fileName);

        List<String> parameters = new ArrayList<>();
        parameters.add(fileName);
        parameters.add(fileStorePath);
        LogEntry logEntry = LogEntry.newBuilder()
                .command("add")
                .parameters(parameters)
                .build();
        Response response = put(logEntry);
        if (!response.isSuccess()){
            return JsonData.buildError(StateType.INTERNAL_SERVER_ERROR.getCode(),StateType.INTERNAL_SERVER_ERROR.value());
        }

        //获取文件后缀
        File dest = new File(fileStorePath + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonData.buildSuccess(StateType.CREATED.getCode(),StateType.CREATED.value());

    }

}
