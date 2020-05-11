package com.unimelb.raft4jcomp90020Appliction.domain;

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
    private boolean success;

    @Override
    public String toString() {
        return "Response{" +
                "leaderIP='" + leaderIP + '\'' +
                ", leaderPort=" + leaderPort +
                ", success=" + success +
                '}';
    }
}
