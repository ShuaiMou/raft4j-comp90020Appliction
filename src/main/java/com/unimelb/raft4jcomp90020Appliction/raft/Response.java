package com.unimelb.raft4jcomp90020Appliction.raft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Response implements Serializable {
    private String leaderIP;
    private int leaderPort;
    private String path;
    private boolean success;

    @Override
    public String toString() {
        return "Response{" +
                "leaderIP='" + leaderIP + '\'' +
                ", leaderPort=" + leaderPort +
                ", path='" + path + '\'' +
                ", success=" + success +
                '}';
    }
}
