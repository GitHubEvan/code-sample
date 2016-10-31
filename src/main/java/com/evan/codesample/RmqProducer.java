package com.evan.codesample;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;

/**
 * Created by hduser on 10/29/16.
 */
public class RmqProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送信息
     *
     * @param msg
     */
    public void sendMessage(RabbitMessage msg) {
        try {
            System.out.println(rabbitTemplate.getConnectionFactory().getHost());
            System.out.println(rabbitTemplate.getConnectionFactory().getPort());
            //发送信息
            rabbitTemplate.convertAndSend(msg.getExchange(), msg.getRouteKey(), msg);

        } catch (Exception e) {
        }
    }
}
