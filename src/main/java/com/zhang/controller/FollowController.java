package com.zhang.controller;


import com.zhang.dto.Result;
import com.zhang.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId,
                         @PathVariable("isFollow") Boolean isFollow) {

        return followService.follow(followUserId,isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result followOrNot(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }
    @GetMapping("/common/{id}")
    public Result followCommonUser(@PathVariable("id") Long id){
        return followService.followCommons(id);
    }

}
