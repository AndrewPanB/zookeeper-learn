package com.px;

import org.junit.Test;

public class ZookeeperTest {

    @Test
    public void test() throws Exception {
        ZookeeperLock zookeeperLock = new ZookeeperLock();
        zookeeperLock.lock("haha", 24 * 3600 * 1000);
        System.out.println("Get haha lock");
        Thread.sleep(Long.MAX_VALUE);

    }
}
