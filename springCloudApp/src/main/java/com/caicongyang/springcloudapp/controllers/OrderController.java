package com.caicongyang.springcloudapp.controllers;


import com.caicongyang.springcloudapp.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * @author caicongyang
 * @email
 * @date 2021-01-28 15:46:19
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 根据用户id查询订单信息
     *
     * @param userId
     * @return
     */
    @RequestMapping("/findOrderByUserId/{userId}")
    public R findOrderByUserId(@PathVariable("userId") Integer userId) {
        String url = "http://order/order/findOrderByUserId/"+userId;
        R result = restTemplate.getForObject(url,R.class);

        return result;
    }


}
