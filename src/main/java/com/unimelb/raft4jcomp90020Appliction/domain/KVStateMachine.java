package com.unimelb.raft4jcomp90020Appliction.domain;

import com.unimelb.raft4jcomp90020Appliction.raft.LogEntry;
import com.unimelb.raft4jcomp90020Appliction.raft.StateMachine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Date: 13/5/20 20:25
 * @Description: 先将日志写到 wal 中，再写入 redis 持久化
 */
@Component("stateMachine")
public class KVStateMachine implements StateMachine {
    private ConcurrentHashMap<String, String> map;
    private List<LogEntry> wal;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public KVStateMachine(){
        map = new ConcurrentHashMap<>(16);
        wal = new CopyOnWriteArrayList<>();
    }

    @Override
    public void apply(LogEntry logEntry){
        wal.add(logEntry);
        String key = logEntry.getParameters().get(0);
        String value = logEntry.getParameters().get(1);
        map.put(key,value);
        redisTemplate.opsForValue().set(key,value);
    }
}
