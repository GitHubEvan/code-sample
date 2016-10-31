package com.evan.codesample;

import com.rabbitmq.client.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by hduser on 10/29/16.
 */
public class JavaRabbitmq {

    private final static String EXCHANGE_NAME = "topic_logs";
    private final static String[] LOG_LEVEL_ARR = {"dao.debug", "dao.info", "dao.error",
            "service.debug", "service.info",
            "service.error", "controller.debug",
            "controller.info", "controller.error"};

    public final static String RABBITMQ_CONFIG = "/rabbitmq-config.properties";
    public String defaultCharset = "UTF-8";

    public void producer() throws Exception {

        String host = "127.0.0.1";
        String port = "5267";
        String user = "guest";
        String pwd = "guest";

        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream(RABBITMQ_CONFIG);

        try {
            properties.load(in);
            host = properties.getProperty("mq.host").trim();
            port = properties.getProperty("mq.port").trim();
            user = properties.getProperty("mq.username").trim();
            pwd = properties.getProperty("mq.password").trim();
        } catch (IOException e) {
            e.printStackTrace();
        }


        ConnectionFactory cf = new ConnectionFactory();

        cf.setHost(host);
        cf.setPort(Integer.parseInt(port));
        cf.setUsername(user);
        cf.setPassword(pwd);

        //创建一个新的连接
        Connection connection = cf.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //指定一个转发器
        channel.exchangeDeclare(EXCHANGE_NAME, "topic" /*exchange类型为topic*/);

        String[] keys = {"debug", "info", "error"};
        for (String key : keys) {
            String routingKey = "*." + key;
            boolean durable = false;
            String queueName = "queue_" + key;
            channel.queueDeclare(queueName, durable, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
        }

        //发送消息
        for (int i = 0; i < 3; i++) {
            for (String severity : LOG_LEVEL_ARR) {
                String message = "Liang-MSG log : [" + severity + "]" + UUID.randomUUID().toString();
                //发布消息至转发器
                channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
            }
        }

        channel.close();
        connection.close();

    }

    public void consumer() throws Exception {

        String host = "127.0.0.1";
        String port = "5267";
        String user = "guest";
        String pwd = "guest";

        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream(RABBITMQ_CONFIG);

        try {
            properties.load(in);
            host = properties.getProperty("mq.host");
            port = properties.getProperty("mq.port");
            user = properties.getProperty("mq.username");
            pwd = properties.getProperty("mq.password");
        } catch (IOException e) {
            e.printStackTrace();
        }


        ConnectionFactory cf = new ConnectionFactory();
        cf.setHost(host);
        cf.setPort(Integer.parseInt(port));
        cf.setUsername(user);
        cf.setPassword(pwd);

        //创建一个新的连接
        Connection connection = cf.newConnection();
        //创建一个通道
        final Channel channel = connection.createChannel();

        String queueName = "queue_error";
        boolean durable = false;
        channel.queueDeclare(queueName, durable, false, false, null);

        int prefetchCount = 1;
        channel.basicQos(prefetchCount);

//        Consumer consumer = new DefaultConsumer(channel) {
//            @Override
//            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                super.handleDelivery(consumerTag, envelope, properties, body);
//
//                try {
//                    Thread.sleep(3 * 1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                String message = new String(body, defaultCharset);
//                System.out.print("[" + envelope.getExchange() + "," + envelope.getRoutingKey() + "," + envelope.getDeliveryTag() + "]");
//                channel.basicAck(envelope.getDeliveryTag(), false);
//            }
//        };
//
//        boolean autoAck = false;
//        channel.basicConsume(queueName, autoAck, consumer);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);

        int seconds = 0;
        while (seconds < 60) {

            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            System.out.println("[" + delivery.getEnvelope().getExchange() + "," + delivery.getEnvelope().getRoutingKey() + "," + delivery.getEnvelope().getDeliveryTag() + "]"
                    + ":" + new String(delivery.getBody(), defaultCharset));
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            Thread.sleep(5 * 1000);
            seconds++;
        }
    }
}
