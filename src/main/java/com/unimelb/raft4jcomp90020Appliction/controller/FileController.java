package com.unimelb.raft4jcomp90020Appliction.controller;


import com.unimelb.raft4jcomp90020Appliction.domain.JsonData;
import com.unimelb.raft4jcomp90020Appliction.domain.StateType;
import com.unimelb.raft4jcomp90020Appliction.raft.LogEntry;
import com.unimelb.raft4jcomp90020Appliction.raft.Raft;
import com.unimelb.raft4jcomp90020Appliction.raft.Response;
import com.unimelb.raft4jcomp90020Appliction.raft.StateMachine;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/file")
@PropertySource("classpath:/application.properties")
public class FileController {


    @Value("${file.store.path}")
    private String fileStorePath;

    @Autowired
    private Raft raft;

    @Autowired
    private StateMachine stateMachine;



    @PostMapping("/add")
    public JsonData uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request){
        String fileName = file.getOriginalFilename();
        System.out.println("文件名为"+fileName);

        List<String> parameters = new ArrayList<>();
        assert fileName != null;
        parameters.add(fileName);
        parameters.add(fileStorePath);
        LogEntry logEntry = LogEntry.newBuilder()
                .command("add")
                .parameters(parameters)
                .build();
        Response response = raft.put(logEntry);

        if (response == null || !response.isSuccess()){
            return JsonData.buildError(StateType.INTERNAL_SERVER_ERROR.getCode(),StateType.INTERNAL_SERVER_ERROR.value());
        }

        //将日志持久化
        stateMachine.apply(logEntry);

        File dest = new File(fileStorePath + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JsonData.buildSuccess(StateType.CREATED.getCode(),StateType.CREATED.value());

    }

}
