package com.zhang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.dto.Result;
import com.zhang.entity.Follow;


public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long id);
}
