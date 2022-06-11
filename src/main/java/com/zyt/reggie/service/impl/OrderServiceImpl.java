package com.zyt.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyt.reggie.common.BaseContext;
import com.zyt.reggie.common.CustomException;
import com.zyt.reggie.common.R;
import com.zyt.reggie.dto.OrdersDto;
import com.zyt.reggie.entity.*;
import com.zyt.reggie.mapper.OrderMapper;
import com.zyt.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        orders.setId(orderId);
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }

    /**
     * 后台查询订单明细
     *
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public Page<OrdersDto> empPage(int page, int pageSize, String number, String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageDto = new Page<>();

        //创建条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，根据number进行like模糊查询
        queryWrapper.like(number != null, Orders::getNumber, number);
        queryWrapper.gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime);
        queryWrapper.lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);
        //添加排序条件(根据更新时间降序排序)
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(pageInfo, queryWrapper);
        //将其除了records中的内存复制到pageDto中
        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> collect = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //对象拷贝
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //根据订单id查询订单详细信息
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());

            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetails);

            //根据userId查询用户姓名
            Long userID = item.getUserId();
            User user = userService.getById(userID);
            ordersDto.setUserName(user.getName());
            ordersDto.setPhone(user.getPhone());

            //获取地址信息
            Long addressBookId = item.getAddressBookId();
            AddressBook addressBook = addressBookService.getById(addressBookId);
            ordersDto.setAddress(addressBook.getDetail());
            ordersDto.setConsignee(addressBook.getConsignee());

            return ordersDto;
        }).collect(Collectors.toList());

        pageDto.setRecords(collect);
        return pageDto;
    }
    /**
     * 用户个人中心订单信息查看
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<OrdersDto> userPage(int page, int pageSize) {
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> dtoPage = new Page<>();
        //创建条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据更新时间降序排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        this.page(pageInfo, queryWrapper);

        //通过OrderId查寻对应的菜品/套餐
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        //对OrderDto进行需要的属性赋值
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //为orderDetails属性赋值
            //获取订单ID
            Long orderId = item.getId();
            //根据订单ID查询对应的订单明细
            wrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(wrapper);
            BeanUtils.copyProperties(item, ordersDto);
            //对ordersDto的OorderDetai属性进行赋值
            ordersDto.setOrderDetails(orderDetailList);

            return ordersDto;
        }).collect(Collectors.toList());

        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        dtoPage.setRecords(list);
        return dtoPage;
    }
    /**
     * 再来一单
     *
     * @param order
     */
    @Override
    public void again(Orders order) {
        //获取订单里订单号
        Orders orderId = this.getById(order.getId());
        String number = order.getNumber();

        //根据条件进行查询
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, number);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //根据查到的数据再次添加到购物车里
        List<ShoppingCart> list = orderDetailList.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setName(item.getName());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setDishId(item.getDishId());
            shoppingCart.setSetmealId(item.getSetmealId());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartService.saveBatch(list);
    }
}