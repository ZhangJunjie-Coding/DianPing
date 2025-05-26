package com.zhang.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zhang.dto.Result;
import com.zhang.entity.Voucher;


public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
