package com.zyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyt.reggie.common.R;
import com.zyt.reggie.dto.DishDto;
import com.zyt.reggie.dto.OrdersDto;
import com.zyt.reggie.entity.Category;
import com.zyt.reggie.entity.Dish;
import com.zyt.reggie.entity.OrderDetail;
import com.zyt.reggie.entity.Orders;
import com.zyt.reggie.service.OrderDetailService;
import com.zyt.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
    /**
     * 后台查询订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> empPage(int page, int pageSize, String number,
                                      String beginTime,
                                      String endTime) {
        Page<OrdersDto> empPage = orderService.empPage(page, pageSize, number, beginTime, endTime);
        return R.success(empPage);
    }
    /**
     * 派送订单
     *
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return R.success("操作成功");
    }
    /**
     * 用户个人中心订单信息查看
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<OrdersDto> dtoPage = orderService.userPage(page, pageSize);
        return R.success(dtoPage);
    }
    /**
     * 再来一单
     * @param order
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders order) {
        orderService.again(order);
        return R.success("再来一单啊");
    }

}