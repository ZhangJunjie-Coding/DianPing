package com.zhang;

import com.zhang.entity.Shop;
import com.zhang.service.impl.ShopServiceImpl;
import com.zhang.utils.CacheClient;
import com.zhang.utils.RedisConstants;
import com.zhang.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.lang.reflect.Executable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest

public class DianPingApplicationTests {
    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    public void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        StopWatch stopWatch = new StopWatch();
        Runnable task = () -> {
            for(int i = 0;i<100;i++){
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        stopWatch.start();
        for(int i = 0;i<300;i++){
            es.submit(task);
        }
        latch.await();
        stopWatch.stop();
        System.out.println("运行时间: "+stopWatch.getTotalTimeSeconds());
    }

    @Test
    public void testSaveSHop() throws InterruptedException {
        Shop shop = shopService.getById(1l);
        cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY + 1, shop, 10L, TimeUnit.SECONDS);
    }
}
