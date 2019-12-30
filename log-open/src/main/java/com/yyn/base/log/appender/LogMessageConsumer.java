/**
 * 
 */
package com.yyn.base.log.appender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author dandan
 *
 */
@Component
public class LogMessageConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(LogMessageConsumer.class);
	@Autowired
	private LogManager logManager;
	@Autowired
	private Queue logCommandQueue;

	@RabbitListener(queues = { "#{logCommandQueue.name}" }, concurrency = "1")
	public void receiveLogCommandQueue(String message) {
//		logger.debug("receive msg: {}", message);
		try {
			logManager.processLogCommandMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
