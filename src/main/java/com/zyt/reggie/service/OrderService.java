package com.zyt.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyt.reggie.dto.OrdersDto;
import com.zyt.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    public Page<OrdersDto> empPage(int page, int pageSize, String number, String beginTime, String endTime);
    //订单信息查看
    public Page<OrdersDto> userPage(int page, int pageSize);
    //再来一单
    public void again(Orders order);
}
