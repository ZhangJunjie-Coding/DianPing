package com.zhang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.dto.LoginFormDTO;
import com.zhang.dto.Result;
import com.zhang.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result userSign();

    Result signCount();
}
