/**
 * 
 */
package com.yyn.base.mq;

import java.util.concurrent.Executor;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author dandan Rabbit MQ配置类，安装Rabbit请参考官网，安装完成后，将主机名，用户名密码改为自己对应的
 *
 */
@Configuration
public class MQConfig {
	// 正式环境配置
	@Profile(value = { "production" })
	@Bean
	public ConnectionFactory connectionFactory_production(Executor threadPoolTaskExecutor) {
		CachingConnectionFactory factory = new CachingConnectionFactory("xxx.xxx.com");// 主机名
		factory.setPort(5672);
		factory.setVirtualHost("dms");// 应用/项目名
		factory.setUsername("username");// 用户名
		factory.setPassword("password");// 密码
		return factory;
	}

	// 测试/预发环境配置
	@Profile(value = { "test" })
	@Bean
	public ConnectionFactory connectionFactory_test(Executor threadPoolTaskExecutor) {
		CachingConnectionFactory factory = new CachingConnectionFactory("xxx.xxx.com");
		factory.setPort(5672);
		factory.setVirtualHost("dms");
		factory.setUsername("username");
		factory.setPassword("password");
		return factory;
	}

	// 开发环境配置
	@Profile(value = { "dev" })
	@Bean
	public ConnectionFactory connectionFactory_dev(Executor threadPoolTaskExecutor) {
		CachingConnectionFactory factory = new CachingConnectionFactory("xxx.xxx.com");
		factory.setPort(5672);
		factory.setVirtualHost("dms");
		factory.setUsername("username");
		factory.setPassword("password");
		return factory;
	}

	@Bean
	public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		return factory;
	}

	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		return rabbitAdmin;
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
