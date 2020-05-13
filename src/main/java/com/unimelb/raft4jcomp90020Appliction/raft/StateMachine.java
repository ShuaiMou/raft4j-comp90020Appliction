package com.unimelb.raft4jcomp90020Appliction.raft;

import java.io.Serializable;

/**
 * @Auther: Saul
 * @Date: 13/5/20 20:20
 * @Description:
 */
public interface StateMachine extends Serializable {

    void apply(LogEntry logEntry);
}
