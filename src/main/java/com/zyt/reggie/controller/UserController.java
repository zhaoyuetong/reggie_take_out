package com.zyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zyt.reggie.common.R;
import com.zyt.reggie.entity.User;
import com.zyt.reggie.service.UserService;
import com.zyt.reggie.utils.SMSUtils;
import com.zyt.reggie.utils.ValidateCodeUtils;
import com.zyt.reggie.utils.sendsmsutf8;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            //生成4位随机验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("测试验证码code:{}",code);
            //使用阿里云接口发送短信
/*
            SMSUtils.sendMessage("菩提阁","SMS_243160642",phone,code);
*/
            //使用互亿无线发送短信
            //sendsmsutf8.sendMessage(phone,code);
            session.setAttribute(phone,code);
            return R.success("短信发送成功");
        }
        return R.error("短信发送失败");
    }
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        //从前台获取手机号与验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        //从session中获取验证码
        Object codeInsession = session.getAttribute(phone);
        //验证码比对成功
        if (codeInsession!=null&& codeInsession.equals(code)){
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(lambdaQueryWrapper);
            //新用户
            if (user==null){
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("登录失败");
    }

}
