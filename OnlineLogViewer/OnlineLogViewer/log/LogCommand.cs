using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OnlineLogViewer.log
{
    public class LogConstants
    {
        public const string PROFILE_NAME_DEV = "dev";
        public const string PROFILE_NAME_TEST = "test";
        public const string PROFILE_NAME_PRODUCT = "production";
        public static string PROFILE_NAME_CURRENT = PROFILE_NAME_PRODUCT;

        public static string EXCHANGE_NAME_COMMAND = "dms.fanout." + PROFILE_NAME_CURRENT + ".log.command";
        public static string EXCHANGE_NAME_COMMAND_RESPONSE = "dms.fanout." + PROFILE_NAME_CURRENT + ".log.command_response";

        public static string EXCHANGE_NAME_OUTPUT = "dms.fanout." + PROFILE_NAME_CURRENT + ".log.output";

        public static void initProfile()
        {
            EXCHANGE_NAME_COMMAND = "dms.fanout." + PROFILE_NAME_CURRENT + ".log.command";
            EXCHANGE_NAME_COMMAND_RESPONSE = "dms.fanout." + PROFILE_NAME_CURRENT + ".log.command_response";
            EXCHANGE_NAME_OUTPUT = "dms.fanout." + PROFILE_NAME_CURRENT + ".log.output";
        }
    }
    public class Logger
    {
        public String contextName { get; set; }
        public string loggerName { get; set; }
        public string loggerLevel { get; set; }
        public string setLoggerLevel { get; set; }
        public bool configured { get; set; }
    }

    public class LogCommandResponse
    {        
        public string command { get; set; }
        public string contextName { get; set; }
        public string mesasge;
        public List<Logger> probeLoggers { get; set; }
    }
    public class ProbeServiceResponse
    {
        public string command { get; set; }
        public string contextName { get; set; }        
    }
    public class LogCommand
    {
        public const string COMMAND_PROBE = "PROBE";
        public const string COMMAND_PROBE2 = "PROBE2";
        public const string COMMAND_PROBE_RESPONSE = "PROBE_RESPONSE";
        public const string COMMAND_CONFIG_LOGGER = "CONFIG_LOGGER";
        public const string COMMAND_RESET_LOGGER_CONFIG = "RESET_LOGGER_CONFIG";
        public const string COMMAND_RESET_ALL_LOGGER_CONFIG = "RESET_ALL_LOGGER_CONFIG";

        public const string COMMAND_LOG_OUTPUT = "LOG_OUTPUT";

        public const string COMMAND_PROBE_SERVICE = "PROBE_SERVICE";
        public const string COMMAND_PROBE_SERVICE_RESPONSE = "PROBE_SERVICE_RESPONSE";

        public string command { get; set; }
        public String contextName { get; set; }
        public LogCommand(string command)
        {
            this.command = command;
        }
    }

    public class ProbeLoggerCommand : LogCommand
    {
        public ProbeLoggerCommand() : base(COMMAND_PROBE)
        {

        }        
    }
    public class ProbeLoggerCommand2 : LogCommand
    {
        public String loggerName { get; set; }
        public ProbeLoggerCommand2() : base(COMMAND_PROBE2)
        {

        }
    }
    public class ConfigLoggerCommand : LogCommand
    {
        public List<Logger> probeLoggers { get; set; }
        
        public ConfigLoggerCommand() : base(COMMAND_CONFIG_LOGGER)
        {

        }
    }
    public class ResetLoggerConfigCommand : LogCommand
    {
        public List<Logger> probeLoggers { get; set; }

        public ResetLoggerConfigCommand() : base(COMMAND_RESET_LOGGER_CONFIG)
        {

        }
    }

    public class ResetAllLoggerConfigCommand : LogCommand
    {
        public ResetAllLoggerConfigCommand() : base(COMMAND_RESET_ALL_LOGGER_CONFIG)
        {

        }
    }

    public class ProbeServiceCommand : LogCommand
    {
        public ProbeServiceCommand() : base(COMMAND_PROBE_SERVICE)
        {

        }
    }
}
