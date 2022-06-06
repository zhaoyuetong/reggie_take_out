package com.zyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyt.reggie.common.R;
import com.zyt.reggie.dto.SetmealDto;
import com.zyt.reggie.entity.*;
import com.zyt.reggie.service.SetmealDishService;
import com.zyt.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
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
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName,name);
        queryWrapper.orderByAsc(Setmeal::getId);
        //执行查询
        setmealService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 保存套餐，套餐菜品关系
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息{}",setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("保存成功");
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }
    /**
     * 修改菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("修改菜品：{}",setmealDto.toString());

        setmealService.updateWithDish(setmealDto);

        return R.success("修改菜品成功");
    }

    /**
     * 删除---批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String ids){
        log.info("套餐编号:{}",ids);
        String[] split = ids.split(",");
        for (String id:split) {
            Setmeal setmeal = new Setmeal();
            long setmealid = Long.parseLong(id);
            setmeal.setId(setmealid);
            setmealService.removeById(setmeal);
            //清理当前菜品对应口味数据---SetmealDish表的delete操作
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(SetmealDish::getSetmealId,setmealid);
            setmealDishService.remove(queryWrapper);
        }
        return R.success("删除成功");
    }
    /**
     * 修改菜品售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable String status, String ids){
        log.info("售卖状态:{}，套餐编号:{}",status,ids);
        String[] split = ids.split(",");
        for (String id:split) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(Long.parseLong(id));
            setmeal.setStatus(Integer.parseInt(status));
            setmealService.updateById(setmeal);
        }
        return R.success("售卖状态修改成功");
    }
    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

}