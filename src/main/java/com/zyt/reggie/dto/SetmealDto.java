package com.zyt.reggie.dto;
import com.zyt.reggie.entity.Setmeal;
import com.zyt.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
