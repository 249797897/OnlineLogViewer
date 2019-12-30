/**
 * 
 */
package com.yyn.base.log.appender;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dandan
 * MQ相关的配置，请将dms替换为自己对应的应用名
 *
 */
@Configuration
public class LogConfig {

	public static String EXCHANGE_NAME_COMMAND = "dms.fanout.Unknown.log.command";
	public static String EXCHANGE_NAME_COMMAND_RESPONSE = "dms.fanout.Unknown.log.command_response";

	public static String EXCHANGE_NAME_OUTPUT = "dms.fanout.Unknown.log.output";

	@Value("${spring.profiles.active:Unknown}")
	public void setProfile(String profile) {
		EXCHANGE_NAME_COMMAND = "dms.fanout." + profile + ".log.command";
		EXCHANGE_NAME_COMMAND_RESPONSE = "dms.fanout." + profile + ".log.command_response";

		EXCHANGE_NAME_OUTPUT = "dms.fanout." + profile + ".log.output";
	}

	@Bean
	public Queue logCommandQueue() {
		Queue queue = new AnonymousQueue();
		return queue;
	}

	@Bean
	public Exchange logCommandResponseExchange() {
		return ExchangeBuilder.fanoutExchange(EXCHANGE_NAME_COMMAND_RESPONSE).durable(false).autoDelete().build();
	}

	@Bean
	public Declarables logCommandExchange() {
		FanoutExchange exchange = (FanoutExchange) ExchangeBuilder.fanoutExchange(EXCHANGE_NAME_COMMAND).durable(false)
				.autoDelete().build();
		return new Declarables(exchange, BindingBuilder.bind(logCommandQueue()).to(exchange));

	}

	@Bean
	public Exchange logOutputExchange() {
		return ExchangeBuilder.fanoutExchange(EXCHANGE_NAME_OUTPUT).durable(false).autoDelete().build();

	}
}
