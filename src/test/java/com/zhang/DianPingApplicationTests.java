package com.zhang;

import com.zhang.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class DianPingApplicationTests {
    @Resource
    private ShopServiceImpl shopService;

    @Test
    public void testSaveSHop() throws InterruptedException {
        shopService.saveShop2Redis(1l, 10l);

    }
}
