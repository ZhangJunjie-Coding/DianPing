package com.zhang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.dto.Result;
import com.zhang.entity.Shop;

public interface IShopService extends IService<Shop> {

    public Result queryById(Long id);

    Result update(Shop shop);
}
