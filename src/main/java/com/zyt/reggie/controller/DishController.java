package com.zyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyt.reggie.common.R;
import com.zyt.reggie.entity.Dish;
import com.zyt.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("dish")
public class DishController {
    @Autowired
    private DishService dishService;
    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name ={}",page,pageSize);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件过滤器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort);
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName,name);
        //执行查询
        dishService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
}
