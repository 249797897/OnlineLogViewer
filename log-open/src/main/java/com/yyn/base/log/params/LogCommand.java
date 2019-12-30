/**
 * 
 */
package com.yyn.base.log.params;

import java.util.List;

/**
 * @author dandan
 *	日志相关的命令
 */
public class LogCommand {

	public static class LogCommandResponse {
		private String command;

		public LogCommandResponse(String command) {
			super();
			this.command = command;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}
	}

	public static class ProbeResponse extends LogCommandResponse {

		public static class ProbeLogger {
			private String loggerName;
			private String loggerLevel;
			private boolean configured;

			public String getLoggerName() {
				return loggerName;
			}

			public void setLoggerName(String loggerName) {
				this.loggerName = loggerName;
			}

			public String getLoggerLevel() {
				return loggerLevel;
			}

			public void setLoggerLevel(String loggerLevel) {
				this.loggerLevel = loggerLevel;
			}

			public boolean isConfigured() {
				return configured;
			}

			public void setConfigured(boolean configured) {
				this.configured = configured;
			}

		}

		private String contextName;
		private List<ProbeLogger> probeLoggers;

		public ProbeResponse(String contextName, List<ProbeLogger> probeLogger) {
			super(COMMAND_PROBE_RESPONSE);
			this.contextName = contextName;
			this.probeLoggers = probeLogger;
		}

		public String getContextName() {
			return contextName;
		}

		public void setContextName(String contextName) {
			this.contextName = contextName;
		}

		public List<ProbeLogger> getProbeLoggers() {
			return probeLoggers;
		}

		public void setProbeLoggers(List<ProbeLogger> probeLoggers) {
			this.probeLoggers = probeLoggers;
		}

	}

	public static class LogOutput extends LogCommandResponse {
		private String contextName;
		private String mesasge;

		public String getContextName() {
			return contextName;
		}

		public void setContextName(String contextName) {
			this.contextName = contextName;
		}

		public String getMesasge() {
			return mesasge;
		}

		public void setMesasge(String mesasge) {
			this.mesasge = mesasge;
		}

		public LogOutput(String contextName, String mesasge) {
			super(COMMAND_LOG_OUTPUT);
			this.contextName = contextName;
			this.mesasge = mesasge;
		}

	}

	public static class ProbeServiceResponse extends LogCommandResponse {
		private String contextName;

		public ProbeServiceResponse(String contextName) {
			super(COMMAND_PROBE_SERVICE_RESPONSE);
			this.contextName = contextName;
		}

		public String getContextName() {
			return contextName;
		}

		public void setContextName(String contextName) {
			this.contextName = contextName;
		}
	}

	public static class LoggerConfig {
		private String contextName;
		private String loggerName;
		private String loggerLevel;

		public String getContextName() {
			return contextName;
		}

		public void setContextName(String contextName) {
			this.contextName = contextName;
		}

		public String getLoggerName() {
			return loggerName;
		}

		public void setLoggerName(String loggerName) {
			this.loggerName = loggerName;
		}

		public String getLoggerLevel() {
			return loggerLevel;
		}

		public void setLoggerLevel(String loggerLevel) {
			this.loggerLevel = loggerLevel;
		}
	}

	public final static String COMMAND_PROBE = "PROBE";
	public final static String COMMAND_PROBE2 = "PROBE2";
	public final static String COMMAND_PROBE_RESPONSE = "PROBE_RESPONSE";
	public final static String COMMAND_SET_LOGGER_LEVEL = "CONFIG_LOGGER";
	public final static String COMMAND_RESET_LOGGER_LEVEL = "RESET_LOGGER_CONFIG";
	public final static String COMMAND_RESET_ALL_LOGGER = "RESET_ALL_LOGGER_CONFIG";

	public final static String COMMAND_LOG_OUTPUT = "LOG_OUTPUT";

	public final static String COMMAND_PROBE_SERVICE = "PROBE_SERVICE";
	public final static String COMMAND_PROBE_SERVICE_RESPONSE = "PROBE_SERVICE_RESPONSE";

	private String command;
	private String contextName;
	private String loggerName;
	private List<LoggerConfig> probeLoggers;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public List<LoggerConfig> getProbeLoggers() {
		return probeLoggers;
	}

	public void setProbeLoggers(List<LoggerConfig> probeLoggers) {
		this.probeLoggers = probeLoggers;
	}

}
