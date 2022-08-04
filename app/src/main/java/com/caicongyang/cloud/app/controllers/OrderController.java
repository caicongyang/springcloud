package com.caicongyang.cloud.app.controllers;


import com.caicongyang.cloud.order.api.OrderFeignService;
import common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


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


    @Autowired
    private OrderFeignService orderFeignService;

    /**
     * 根据用户id查询订单信息
     *
     * @param userId
     * @return
     */
    @RequestMapping("/findOrderByUserId/{userId}")
    public R findOrderByUserId(@PathVariable("userId") Integer userId) {
        String url = "http://order/order/findOrderByUserId/" + userId;
        R result = restTemplate.getForObject(url, R.class);

        return result;
    }


    /**
     * 根据用户id查询订单信息
     *
     * @param userId
     * @return
     */
    @RequestMapping("/findOrderByUserId4Feign/{userId}")
    public R findOrderByUserId4Feign(@PathVariable("userId") Integer userId) {
        return orderFeignService.findOrderByUserId(userId);
    }


}
