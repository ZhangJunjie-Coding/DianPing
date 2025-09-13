package com.zhang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.zhang.dto.UserDTO;
import com.zhang.entity.Shop;
import com.zhang.entity.User;
import com.zhang.service.impl.ShopServiceImpl;
import com.zhang.service.impl.UserServiceImpl;
import com.zhang.utils.CacheClient;
import com.zhang.utils.RedisConstants;
import com.zhang.utils.RedisIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.*;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zhang.utils.RedisConstants.LOGIN_USER_KEY;
import static com.zhang.utils.RedisConstants.LOGIN_USER_TTL;

@SpringBootTest
@Slf4j
public class DianPingApplicationTests {
    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private RedisIdWorker redisIdWorker;


    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Resource
    private UserServiceImpl userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void saveToken() throws IOException {
        File file = new File("./token.txt");

        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file);

        List<User> list = userService.list();
        for (User user : list) {
            String token = UUID.randomUUID().toString(true);
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                    CopyOptions
                            .create()
                            .setIgnoreNullValue(true)
                            .setFieldValueEditor((filedName, filedValue) -> filedValue.toString()));
            fileWriter.write(token + "\n");
            // 存储
            String tokenKey = LOGIN_USER_KEY + token;
            stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
            // 设置token有效期
            stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        }
    }

    @Test
    public void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        StopWatch stopWatch = new StopWatch();
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        stopWatch.start();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        stopWatch.stop();
        System.out.println("运行时间: " + stopWatch.getTotalTimeSeconds());
    }

    @Test
    public void testSaveSHop() throws InterruptedException {
//        Shop shop = shopService.getById(1l);
        List<Shop> list = shopService.list();
        for (Shop shop : list) {

            cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY + shop.getId(), shop, 10L, TimeUnit.SECONDS);
        }
    }

    @Resource
    private RedissonClient redissonClient;

//    @Resource
//    private RedissonClient redissonClient2;
//
//    @Resource
//    private RedissonClient redissonClient3;

    private RLock lock1 = null;
    private RLock lock2 = null;
    private RLock lock3 = null;
    private RLock lock = null;

    //    @BeforeEach
    void setup() {
        lock1 = redissonClient.getLock("order");
//        lock2 = redissonClient1.getLock("order");
//        lock3 = redissonClient2.getLock("order");
        // 创建联锁 multiLock
        lock = redissonClient.getMultiLock(lock1, lock2, lock3);
    }

    @Test
    public void testRedisson() throws InterruptedException {
        // 获取锁（可重入），指定锁的名称
        RLock lock = redissonClient.getLock("anyLock");
        // 尝试获取锁，参数分别是：获取所得最大等待时间（期间会重试），锁自动释放时间，事件单位
        boolean isLock = lock.tryLock(1, 1, TimeUnit.SECONDS);
        // 判断锁是否获取成功
        if (isLock) {
            try {
                System.out.println("执行业务");
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
    }

    @Test
    public void method1() throws InterruptedException {
        boolean isLock = lock.tryLock(1l, TimeUnit.SECONDS);
        if (!isLock) {
            log.error("获取锁失败....1");
            return;
        }
        try {
            log.info("获取锁成功....1");
            method2();
            log.info("开始执行业务....1");
        } finally {
            log.warn("准备释放锁.......1");
            lock.unlock();
        }
    }

    void method2() throws InterruptedException {
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("获取锁失败....2");
            return;
        }
        try {
            log.info("获取锁成功....2");
            log.info("开始执行业务....2");
        } finally {

            log.warn("准备释放锁.......2");
            lock.unlock();
        }
    }

    @Test
    void test03() {
        int i = 0;
        System.out.println(i++ + ++i);
    }

    @Test
    void loadShopData() {
        // 1.查询店铺信息
        List<Shop> list = shopService.list();
        // 2.将店铺分组，按照typeId分组，typeId一致的放到一个集合中
        Map<Long, List<Shop>> map = list.stream().collect(Collectors.groupingBy(shop -> shop.getTypeId()));
        // 3.分批写入Redis
        for (Map.Entry<Long, List<Shop>> entry : map.entrySet()) {
            // 获取类型id
            Long typeId = entry.getKey();
            String key = "shop:geo:" + typeId;
            // 获取同类型的店铺集合
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            // 写入redis GEOADD key 经度 维度 member
            for (Shop shop : value) {
                // 一个一个的添加
//                stringRedisTemplate.opsForGeo().add(key, new Point(shop.getX(), shop.getY()), shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())
                ));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        }
    }

    @Test
    public void testHyperLogLog() {
        String[] users = new String[1000];
        int j = 0;
        for (int i = 0; i < 1000000; i++) {

            j = i % 1000;
            users[j] = "user_" + i;
            if (j  == 999) {
                stringRedisTemplate.opsForHyperLogLog().add("hl1", users);
            }
        }
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hl1");
        System.out.println("size= " + size);
    }

    @AfterEach
    public void tearDown() {
        es.shutdown();
    }

    @Test
    public void testHyperLogLog02() {
        try {
            String key = "hl1";

            // 先清除可能存在的旧数据
            stringRedisTemplate.delete(key);

            int batchSize = 1000;
            for (int i = 0; i < 1000000; i += batchSize) {
                String[] users = new String[batchSize];
                for (int j = 0; j < batchSize && (i + j) < 1000000; j++) {
                    users[j] = "user_" + (i + j);
                }
                stringRedisTemplate.opsForHyperLogLog().add(key, users);

                // 添加短暂延迟，避免资源过度消耗
                if (i % 10000 == 0) {
                    Thread.sleep(10);
                }
            }

            Long size = stringRedisTemplate.opsForHyperLogLog().size(key);
            System.out.println("HyperLogLog estimated size: " + size);

        } catch (Exception e) {
            log.error("HyperLogLog test failed", e);
        }
    }
}
