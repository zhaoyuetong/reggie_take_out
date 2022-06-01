package com.zyt.reggie.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyt.reggie.dto.SetmealDto;
import com.zyt.reggie.entity.Setmeal;
public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时保存套餐与菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);
    /**
     * 根据id回显套餐信息
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);

    /**
     * 修改菜品
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
}
