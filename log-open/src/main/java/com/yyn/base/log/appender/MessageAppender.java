/**
 * 
 */
package com.yyn.base.log.appender;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * @author dandan
 * 用于将日志信息发送到MQ
 *
 */
public class MessageAppender extends AppenderBase<ILoggingEvent> {

	private LogManager logManager;
	private PatternLayoutEncoder encoder;

	@Override
	public void start() {
		if (this.encoder == null) {
			addError("No encoder set for the appender named [" + name + "].");
			return;
		}
		super.start();
	}

	public void append(ILoggingEvent event) {
		byte[] data = this.encoder.encode(event);
		if (logManager != null) {
			logManager.sendLogMessage(data);
		}
	}

	public LogManager getLogManager() {
		return logManager;
	}

	public void setLogManager(LogManager logManager) {
		this.logManager = logManager;
	}

	public PatternLayoutEncoder getEncoder() {
		return encoder;
	}

	public void setEncoder(PatternLayoutEncoder encoder) {
		this.encoder = encoder;
	}

}
