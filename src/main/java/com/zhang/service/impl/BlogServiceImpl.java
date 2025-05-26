package com.zhang.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.entity.Blog;
import com.zhang.mapper.BlogMapper;
import com.zhang.service.IBlogService;
import org.springframework.stereotype.Service;


@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
