package com.px;

import java.util.List;
import java.util.stream.Collectors;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

public class ZookeeperLock {

    private final ZkClient zkClient;

    //connect zk server
    public ZookeeperLock() {
        zkClient = new ZkClient("192.168.1.14:2181", 30000, 20000);
    }

    //get lock
    public MyLock lock(String lockId, long timeout) throws InterruptedException {
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
        if(lock.isActive()){
            zkClient.delete(lock.getPath());
        }
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
}
