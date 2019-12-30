/**
 * 
 */
package com.yyn.base.log.appender;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * @author dandan
 * 日志状态类，记录每个logger的当前状态（输出级别，appender等）
 *
 */
public class LogStatus {
	private LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

	public static class LoggerStatus {
		private LoggerContext loggerContext;
		private ch.qos.logback.classic.Logger logbackLogger;
		private List<Appender<ILoggingEvent>> originalAppenderList;
		private Level originalLevel;
		private String currentLoggerLevel;
		private boolean originalAdditive = false;

		public LoggerStatus(Logger logbackLogger) {
			super();
			this.logbackLogger = logbackLogger;
			init();
		}

		private void init() {
			originalAppenderList = new LinkedList<Appender<ILoggingEvent>>();
			Iterator<Appender<ILoggingEvent>> it = logbackLogger.iteratorForAppenders();
			while (it.hasNext()) {
				Appender<ILoggingEvent> appender = it.next();
				originalAppenderList.add(appender);
				logbackLogger.detachAppender(appender);
			}
			originalLevel = logbackLogger.getLevel();
			originalAdditive = logbackLogger.isAdditive();
			logbackLogger.setAdditive(false);
		}

		public void setMessageAppender(Appender<ILoggingEvent> messageAppender) {
			if (messageAppender != null) {
				logbackLogger.addAppender(messageAppender);
			}
		}

		public void setLevel(String level) {
			Level newLevel = Level.toLevel(level, Level.ALL);
			if (newLevel == Level.ALL) {
				return;
			}
			logbackLogger.setLevel(newLevel);
		}

		private void detachAllAppenders() {
			Iterator<Appender<ILoggingEvent>> it = logbackLogger.iteratorForAppenders();
			while (it.hasNext()) {
				Appender<ILoggingEvent> appender = it.next();
				logbackLogger.detachAppender(appender);
			}
		}

		public void reset() {
			logbackLogger.setLevel(originalLevel);
			logbackLogger.setAdditive(originalAdditive);
			detachAllAppenders();
			if (!CollectionUtils.isEmpty(originalAppenderList)) {
				for (Appender<ILoggingEvent> appender : originalAppenderList) {
					logbackLogger.addAppender(appender);
				}
			}
		}

		public ch.qos.logback.classic.Logger getLogbackLogger() {
			return logbackLogger;
		}

	}
}
