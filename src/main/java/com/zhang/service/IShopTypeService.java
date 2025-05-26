package com.zhang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.dto.Result;
import com.zhang.entity.ShopType;


public interface IShopTypeService extends IService<ShopType> {
    public Result queryTypeList();

}
