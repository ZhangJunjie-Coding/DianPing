package com.zhang;

import com.zhang.entity.Shop;
import com.zhang.service.impl.ShopServiceImpl;
import com.zhang.utils.CacheClient;
import com.zhang.utils.RedisConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest

public class DianPingApplicationTests {
    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Test
    public void testSaveSHop() throws InterruptedException {
        Shop shop = shopService.getById(1l);
        cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY + 1, shop, 10L, TimeUnit.SECONDS);
    }
}
