package com.zhang.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhang.entity.VoucherOrder;
import com.zhang.mapper.VoucherOrderMapper;
import com.zhang.service.IVoucherOrderService;
import org.springframework.stereotype.Service;


@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

}
