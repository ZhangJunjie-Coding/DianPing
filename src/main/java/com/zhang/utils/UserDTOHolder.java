package com.zhang.utils;

import com.zhang.dto.UserDTO;
import com.zhang.entity.User;

public class UserDTOHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }


    public static UserDTO getUser(){
        return tl.get();
    }
    public static void removeUser(){
        tl.remove();
    }
}
