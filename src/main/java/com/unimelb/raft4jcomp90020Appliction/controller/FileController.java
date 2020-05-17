package com.unimelb.raft4jcomp90020Appliction.controller;


import com.unimelb.raft4jcomp90020Appliction.domain.JsonData;
import com.unimelb.raft4jcomp90020Appliction.domain.StateType;
import com.unimelb.raft4jcomp90020Appliction.raft.Raft;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
@PropertySource("classpath:/application.properties")
public class FileController {
    @Value("${file.store.path}")
    private String fileStorePath;

    @Autowired
    private Raft raft;

    @PostMapping("/add")
    public JsonData uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request){
        String fileName = file.getOriginalFilename();
        fileName = fileName.trim();
        log.debug("the uploaded file name is [{}]", fileName);
        boolean result = raft.put(fileName, fileStorePath);
        if (!result){
            return JsonData.buildError(StateType.INTERNAL_SERVER_ERROR.getCode(),StateType.INTERNAL_SERVER_ERROR.value());
        }
        File dest = new File(fileStorePath + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("failed in storing the file to disk");
        }
        return JsonData.buildSuccess(StateType.CREATED.getCode(),StateType.CREATED.value());

    }

    @GetMapping("/search")
    public JsonData searchFileByName(String fileName){
        String result = raft.get(fileName);
        if (result.equals("")){
            return JsonData.buildError(StateType.NOT_FOUND.getCode(), StateType.NOT_FOUND.value());
        }else {
            return JsonData.buildSuccess(result);
        }
    }

    @DeleteMapping("/delete")
    public JsonData deleteFileByName(String fileName){
        boolean delete = raft.delete(fileName);
        if (!delete){
            //todo：返回代码待定
            return JsonData.buildError(StateType.NOT_FOUND.getCode(), StateType.NOT_FOUND.value());
        }
        return JsonData.buildSuccess();
    }



}
