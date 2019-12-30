/**
 * 
 */
package com.yyn.base.log.appender;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.yyn.base.json.YYNJsonUtils;
import com.yyn.base.log.params.LogCommand;
import com.yyn.base.log.params.LogCommand.LoggerConfig;
import com.yyn.base.log.params.LogCommand.ProbeResponse.ProbeLogger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * @author dandan
 *
 */
@Component
public class LogManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogManager.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
	private String contextName = loggerContext.getName();
	private Appender<ILoggingEvent> messageAppender;
	private Map<String, LogStatus.LoggerStatus> loggerMap = new HashMap<String, LogStatus.LoggerStatus>();

	@PostConstruct
	public void init() {
//		ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger("messageAppenderLogger");
//		if (logbackLogger != null) {
//			messageAppender = logbackLogger.getAppender("MESSAGEAPPENDER");
//		}
		createMessageAppender();
	}

	private void createMessageAppender() {
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setPattern("%d{HH:mm:ss.SSS} %-5level [%thread][%logger{0}] %m%n");
		encoder.setContext(loggerContext);
		encoder.start();
		MessageAppender appender = new MessageAppender();
		appender.setEncoder(encoder);
		appender.setContext(loggerContext);
		appender.setLogManager(this);
		appender.start();

		this.messageAppender = appender;
	}

	public void sendLogMessage(byte[] data) {
		try {
			String message = new String(data, "UTF-8");
			LogCommand.LogOutput logCommand = new LogCommand.LogOutput(loggerContext.getName(), message);
			rabbitTemplate.convertAndSend(LogConfig.EXCHANGE_NAME_OUTPUT, null, logCommand);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (AmqpException e) {
			e.printStackTrace();
		}
	}

	public void processLogCommandMessage(String message) {
		LogCommand command = YYNJsonUtils.stringToBean(YYNJsonUtils.MAPPER_LOOSE_DESERIALIZATION, message,
				LogCommand.class);
		processLogConfigCommand(command);
	}

	private void processLogConfigCommand(LogCommand command) {
		switch (command.getCommand()) {
		case LogCommand.COMMAND_PROBE:
		case LogCommand.COMMAND_PROBE2: {
			process_COMMAND_PROBE(command);
		}
			break;
		case LogCommand.COMMAND_SET_LOGGER_LEVEL: {
			process_COMMAND_SET_LOGGER_LEVEL(command);
		}
			break;
		case LogCommand.COMMAND_RESET_LOGGER_LEVEL: {
			process_COMMAND_RESET_LOGGER_LEVEL(command);
		}
			break;
		case LogCommand.COMMAND_RESET_ALL_LOGGER: {
			process_COMMAND_RESET_ALL_LOGGER(command);
		}
			break;
		case LogCommand.COMMAND_PROBE_SERVICE: {
			process_COMMAND_PROBE_SERVICE(command);
		}
			break;
		}
	}

	private void process_COMMAND_PROBE(LogCommand command) {
		if (!StringUtils.equals(contextName, command.getContextName())) {
			return;
		}
		List<ch.qos.logback.classic.Logger> list = loggerContext.getLoggerList();
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		String loggerNameParam = command.getLoggerName();

		// 过滤
		list = list.stream().filter(l -> {
			String thisLoggerName = l.getName();
			if (isLoggerConfigured(thisLoggerName)) {
				return true;
			}
			if (StringUtils.isNotBlank(loggerNameParam) && !thisLoggerName.contains(loggerNameParam)) {
				return false;
			}
			return true;
		}).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		int packageSize = 100;
		for (int i = 0; i < list.size(); i += packageSize) {
			int to = i + packageSize;
			if (to > list.size()) {
				to = list.size();
			}
			List<ch.qos.logback.classic.Logger> subList = list.subList(i, to);
			sendProbeResponse(subList);
		}
	}

	private boolean isLoggerConfigured(String loggerName) {
		return loggerMap.containsKey(loggerName);
	}

	private void sendProbeResponse(List<ch.qos.logback.classic.Logger> subList) {
		List<LogCommand.ProbeResponse.ProbeLogger> probeLoggerList = new ArrayList<LogCommand.ProbeResponse.ProbeLogger>(
				subList.size());
		for (ch.qos.logback.classic.Logger logbackLogger : subList) {
			LogCommand.ProbeResponse.ProbeLogger probeLogger = new ProbeLogger();
			probeLogger.setLoggerLevel(logbackLogger.getEffectiveLevel().toString());
			probeLogger.setLoggerName(logbackLogger.getName());
			if (isLoggerConfigured(logbackLogger.getName())) {
				probeLogger.setConfigured(true);
			}
			probeLoggerList.add(probeLogger);
		}
		LogCommand.ProbeResponse probeResponse = new LogCommand.ProbeResponse(loggerContext.getName(), probeLoggerList);
		rabbitTemplate.convertAndSend(LogConfig.EXCHANGE_NAME_COMMAND_RESPONSE, null, probeResponse);
	}

	private void process_COMMAND_SET_LOGGER_LEVEL(LogCommand command) {
		List<LoggerConfig> loggerConfigs = command.getProbeLoggers();
		if (!StringUtils.equals(loggerContext.getName(), command.getContextName())) {
			return;
		}
		if (CollectionUtils.isEmpty(loggerConfigs)) {
			return;
		}
		for (LoggerConfig loggerConfig : loggerConfigs) {
			String loggerName = loggerConfig.getLoggerName();
			if (StringUtils.isBlank(loggerName)) {
				continue;
			}
			ch.qos.logback.classic.Logger logbackLogger = loggerContext.exists(loggerName);
			if (logbackLogger == null) {
				continue;
			}

			configLogger(loggerConfig, logbackLogger);
		}
	}

	private void configLogger(LoggerConfig loggerConfig, ch.qos.logback.classic.Logger logbackLogger) {
		String loggerName = loggerConfig.getLoggerName();
		LogStatus.LoggerStatus loggerStatus = loggerMap.get(loggerName);
		if (loggerStatus == null) {
			loggerStatus = new LogStatus.LoggerStatus(logbackLogger);
			loggerStatus.setMessageAppender(messageAppender);
			loggerMap.put(loggerName, loggerStatus);
		}
		loggerStatus.setLevel(loggerConfig.getLoggerLevel());

		// send response
		sendProbeResponse(Collections.singletonList(logbackLogger));
	}

	public void sendProbeResponse(ch.qos.logback.classic.Logger logbackLogger) {
		sendProbeResponse(Collections.singletonList(logbackLogger));
	}

	/**
	 * 重置日志设置
	 * 
	 * @param command
	 */
	private void process_COMMAND_RESET_LOGGER_LEVEL(LogCommand command) {
		List<LoggerConfig> loggerConfigs = command.getProbeLoggers();
		if (!StringUtils.equals(loggerContext.getName(), command.getContextName())) {
			return;
		}
		if (CollectionUtils.isEmpty(loggerConfigs)) {
			return;
		}
		for (LoggerConfig loggerConfig : loggerConfigs) {
			String loggerName = loggerConfig.getLoggerName();
			if (StringUtils.isBlank(loggerName)) {
				continue;
			}
			ch.qos.logback.classic.Logger logbackLogger = loggerContext.exists(loggerName);
			if (logbackLogger == null) {
				continue;
			}

			resetLoggerConfig(loggerConfig, logbackLogger);
		}
	}

	private void resetLoggerConfig(LoggerConfig loggerConfig, ch.qos.logback.classic.Logger logbackLogger) {
		String loggerName = loggerConfig.getLoggerName();
		LogStatus.LoggerStatus loggerStatus = loggerMap.get(loggerName);
		if (loggerStatus == null) {
			return;
		}
		loggerStatus.reset();
		sendProbeResponse(loggerStatus.getLogbackLogger());
		loggerMap.remove(loggerName);
	}

	/**
	 * 重置所有日志设置
	 * 
	 * @param command
	 */
	private void process_COMMAND_RESET_ALL_LOGGER(LogCommand command) {
		resetAllLoggerConfig();
	}

	private void resetAllLoggerConfig() {

		for (LogStatus.LoggerStatus loggerStatus : loggerMap.values()) {
			loggerStatus.reset();
			sendProbeResponse(loggerStatus.getLogbackLogger());
		}
		loggerMap.clear();

	}

	/**
	 * 探测服务
	 * 
	 * @param command
	 */
	private void process_COMMAND_PROBE_SERVICE(LogCommand command) {
		LogCommand.ProbeServiceResponse probeResponse = new LogCommand.ProbeServiceResponse(loggerContext.getName());
		rabbitTemplate.convertAndSend(LogConfig.EXCHANGE_NAME_COMMAND_RESPONSE, null, probeResponse);
	}
}
