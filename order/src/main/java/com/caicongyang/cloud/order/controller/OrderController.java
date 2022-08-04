package com.caicongyang.cloud.order.controller;

import com.caicongyang.cloud.order.dto.Order;
import common.R;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    /**
     * 根据用户id查询订单信息
     *
     * @param userId
     * @return
     */
    @RequestMapping("/findOrderByUserId/{userId}")
    public R findOrderByUserId(@PathVariable("userId") Integer userId) {
        Order order1 = new Order();
        order1.setUserId(String.valueOf(userId));
        order1.setAmount(new BigDecimal(100));
        order1.setOrderNo("NO01");


        Order order2 = new Order();
        order2.setUserId(String.valueOf(userId));
        order2.setAmount(new BigDecimal(100));
        order2.setOrderNo("NO02");

        List<Order> list = new ArrayList<>();
        list.add(order1);
        list.add(order2);

        return R.ok().put("orders", list);
    }


}
