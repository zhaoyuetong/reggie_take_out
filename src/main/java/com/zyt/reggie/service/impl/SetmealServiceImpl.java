package com.zyt.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyt.reggie.dto.SetmealDto;
import com.zyt.reggie.entity.DishFlavor;
import com.zyt.reggie.entity.Setmeal;
import com.zyt.reggie.entity.SetmealDish;
import com.zyt.reggie.mapper.SetmealMapper;
import com.zyt.reggie.service.SetmealDishService;
import com.zyt.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl  extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时保存套餐与菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long setmealDtoId = setmealDto.getId();
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDtoId);
            return item;
        }).collect(Collectors.toList());
        //保存套餐菜品关系表
        setmealDishService.saveBatch(setmealDishes);
    }
    /**
     * 根据id回显套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //从setmeal表查询
        Setmeal setmeal = this.getById(this.getById(id));
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        //查询当前套餐查对应的菜品信息，从setmeal_dish表查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    /**
     * 保存套餐菜品信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新dish表基本信息
        this.updateById(setmealDto);

        //清理当前菜品对应口味数据---setmeal_dish表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的口味数据---setmeal_dish表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }
}
