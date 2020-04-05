package com.px;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class ZookeeperTest {

    @Test
    public void test() throws Exception {
        ZookeeperLock zookeeperLock = new ZookeeperLock();
        zookeeperLock.lock("haha", 24 * 3600 * 1000);
        System.out.println("Get haha lock");
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void multiThreadTest() throws IOException, InterruptedException {
        ZookeeperLock zookeeperLock = new ZookeeperLock();
        File file = new File("C:/GitHub/test.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                MyLock lock = null;
                try {
                    lock = zookeeperLock.lock(file.getPath(), 10000);
                    String firstLine = Files.lines(file.toPath()).findFirst().orElse("0");
                    int count = Integer.parseInt(firstLine);
                    count++;
                    Files.write(file.toPath(), String.valueOf(count).getBytes(StandardCharsets.UTF_8));
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    zookeeperLock.unlock(lock);
                }
            });

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            String firstLine = Files.lines(file.toPath()).findFirst().orElse("0");
            System.out.println(firstLine);
        }
    }
}
