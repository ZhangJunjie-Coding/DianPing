package com.zhang.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.dto.Result;
import com.zhang.entity.ShopType;
import com.zhang.mapper.ShopTypeMapper;
import com.zhang.service.IShopTypeService;
import com.zhang.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;


@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String shot_type_list = stringRedisTemplate.opsForValue().get(RedisConstants.SHOP_TYPE_KEY);
        if(StrUtil.isNotBlank(shot_type_list)){
            JSONArray objects = JSONUtil.parseArray(shot_type_list);
            List<ShopType> shopTypes = JSONUtil.toList(objects, ShopType.class);
            return Result.ok(shopTypes);
        }

        List<ShopType> typeList = query().orderByAsc("sort").list();
        if(typeList == null){
            return Result.fail("查询商户类型列表失败！");
        }
        stringRedisTemplate.opsForValue().set(RedisConstants.SHOP_TYPE_KEY, JSONUtil.toJsonStr(typeList));

        return Result.ok(typeList);
    }
}
