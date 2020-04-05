package com.px;

import java.io.UnsupportedEncodingException;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;

public class Zookeeper {
    private final ZkClient zkClient;
    private String homePath;


    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    //connect zk server
    public Zookeeper() {
        zkClient = new ZkClient("192.168.1.14:2181", 30000, 20000);
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Zookeeper zookeeper = new Zookeeper();
        zookeeper.setHomePath("/px/");
        String childPath = "first";
        zookeeper.getZkClient().delete(zookeeper.getHomePath() + childPath);
        zookeeper.getZkClient().create(zookeeper.getHomePath() + childPath, "hello", Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(zookeeper.getZkClient().getAcl(zookeeper.getHomePath() + childPath).getKey());
    }

}
