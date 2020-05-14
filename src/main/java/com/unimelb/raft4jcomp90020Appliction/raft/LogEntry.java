package com.unimelb.raft4jcomp90020Appliction.raft;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Auther: Saul
 * @Date: 5/5/20 16:01
 * @Description:
 */
@Getter
@Setter
public class LogEntry implements Serializable{

    //收到时的任期号
    private long term;

    //一个用户状态机执行的指令
    private String command;
    private List<String> parameters;

    public LogEntry(){}

    public LogEntry(Builder builder){
        this.term = builder.term;
        this.command = builder.command;
        this.parameters = builder.parameters;
    }

    public static Builder newBuilder(){
        return new Builder();
    }

    public static final class Builder{
        //收到时的任期号
        private long term;

        //一个用户状态机执行的指令
        private String command;
        private List<String> parameters;

        private Builder(){}

        public Builder term(long term){
            this.term = term;
            return this;
        }

        public Builder command(String command){
            this.command = command;
            return this;
        }

        public Builder parameters(List<String> parameters){
            this.parameters = parameters;
            return this;
        }

        public LogEntry build(){
            return new LogEntry(this);
        }
    }


    @Override
    public String toString() {
        return "LogEntry{" +
                "term=" + term +
                ", command='" + command +
                ", parameters=" + parameters.get(0) + ":" + parameters.get(1)+
                '}';
    }

}
