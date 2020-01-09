package com.example.springcloud.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author chezhenqi
 * @date 2019/12/24 星期二
 * @time 14:12
 * @description higerpoint
 */
@Configuration(value = "rabbitConfig")
public class RabbitConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    /**
     * 交换空间名称
     */
    public static final String EXCHANGE = "DLX";

    /**
     * @author chezhenqi
     * @date 14:42 2019/12/24
     * @description: 创建mq连接
     * @param:
     * @return:
     */
    @Bean(name = "connectionFactory")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    /**
     * @author chezhenqi
     * @date 16:36 2019/12/24
     * @description: 创建交换空间
     * @params: No such property: code for class: Script1
     * @return:
     */
    @Bean
    public DirectExchange exchange() { // 使用直连的模式
        return new DirectExchange(EXCHANGE, true, true);
    }

}
