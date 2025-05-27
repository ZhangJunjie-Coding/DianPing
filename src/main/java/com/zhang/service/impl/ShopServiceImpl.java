package com.zhang.service.impl;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.dto.Result;
import com.zhang.entity.Shop;
import com.zhang.mapper.ShopMapper;
import com.zhang.service.IShopService;
import com.zhang.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 缓存穿透
//        Shop shop = queryWithPassThrough(id);

        // 互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);

        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    // 查询商铺缓存击穿方案解决
    public Shop queryWithMutex(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        Shop shop;
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 存在直接返回
            shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 判断命中的是否是空值
        if (shopJson != null) { // 或者 shopJson == ""
            return null;
        }
        // 4.实现缓存重建
        // 4.1 获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 判断是否获取成功
            if (!isLock) {
                // 4.3 失败,则休眠并重试
                Thread.sleep(50);
                // 这里也可以改为 while循环+重试次数机制
                return queryWithMutex(id);

            }
            // 再次检查缓存，防止缓存在等待锁期间被其他线程重建存在直接返回
            shopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(shopJson)) {
                shop = JSONUtil.toBean(shopJson, Shop.class);
                return shop;
            }
            if (shopJson != null) { // 或者 shopJson == ""
                return null;
            }

            // 4.4 成功,根据id  查询数据库
            shop = getById(id);
            // 模拟重建延时
            Thread.sleep(200);
            // 库中不存在
            if (shop == null) {
                // 将空值写入Redis ,解决缓存穿透问题
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 返回错误信息
                return null;
            }
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放互斥锁
            unLock(lockKey);
        }
        return shop;
    }

    // 查询商铺缓存穿透方案解决
    public Shop queryWithPassThrough(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从redis中查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 存在直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 判断命中的是否是空值
        if (shopJson != null) { // 或者 shopJson == ""
            return null;
        }
        // 不存在从数据库中查询
        Shop shop = getById(id);
        if (shop == null) {
            // 将空值写入Redis
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 返回错误信息
            return null;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);


        return shop;
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空!");
        }
        updateById(shop);
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

    private boolean tryLock(String key) {
        Boolean isLock = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(isLock);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
