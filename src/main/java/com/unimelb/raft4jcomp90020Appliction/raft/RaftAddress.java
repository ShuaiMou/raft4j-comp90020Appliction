package com.unimelb.raft4jcomp90020Appliction.raft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Date: 13/5/20 20:54
 * @Description:
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RaftAddress implements Serializable {
    private String ip;
    private int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaftAddress that = (RaftAddress) o;
        return port == that.port &&
                ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return "RaftAddress{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
