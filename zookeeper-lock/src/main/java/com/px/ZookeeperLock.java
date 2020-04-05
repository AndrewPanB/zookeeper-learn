package com.px;

import java.util.List;
import java.util.stream.Collectors;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

public class ZookeeperLock {
    //连接zk服务器

    //create temprapory sequece node
    private ZkClient zkClient;

    public ZookeeperLock() {
        zkClient = new ZkClient("192.168.1.14:2181", 30000, 20000);
        System.out.println("px exist in zk: "+ zkClient.exists("/px"));
    }

    //get lock
    public MyLock lock(String lockId, long timeout) throws Exception {
        MyLock lockNode = createLockNode(lockId);
        MyLock myLock = tryActiveLock(lockNode);
        if (!myLock.isActive()) {
            try {
                synchronized (lockNode) {
                    lockNode.wait(timeout);
                }
            } catch (Exception ex) {
                throw ex;
            }
        }
        return myLock;
    }

    //active lock
    public MyLock tryActiveLock(MyLock lockNode) {
        //check whether we get lock
        List<String> list = zkClient.getChildren("/px")
            .stream()
            .sorted()
            .map(p -> "/px/" + p)
            .collect(Collectors.toList());
        String firstPath = list.get(0);
        if (firstPath.equals(lockNode.getPath())) {
            lockNode.setActive(true);
        } else {
            //add the listener of last node
            String lastPath = list.get(list.indexOf(lockNode.getPath())-1);
            zkClient.subscribeDataChanges(lastPath, new IZkDataListener() {
                @Override
                public void handleDataChange(String dataPath, Object o) throws Exception {

                }

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    System.out.println("Node delete: " + dataPath);
                    MyLock myLock = tryActiveLock(lockNode);
                    synchronized (lockNode){
                        if(lockNode.isActive()){
                            lockNode.notify();
                        }
                    }
                    zkClient.unsubscribeDataChanges(lastPath, this);
                }
            });
        }


        return lockNode;
    }

    //unlock
    public void unlock(MyLock lock) {
    }

    //crete temparory node
    public MyLock createLockNode(String lockId) {
        String path = zkClient.createEphemeralSequential("/px/" + lockId, "w");
        MyLock myLock = new MyLock();
        myLock.setPath(path);
        myLock.setLockId(lockId);
        myLock.setActive(false);
        return myLock;
    }

    public static void main(String[] args) {


        System.out.println("Hello");
        MyLock myLock = new MyLock();
        myLock.setActive(true);
        myLock.setLockId("lockId-1");
        myLock.setPath("path-1");

        System.out.println(myLock);
    }
}
