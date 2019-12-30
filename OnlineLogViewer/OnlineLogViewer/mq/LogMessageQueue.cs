using Newtonsoft.Json;
using OnlineLogViewer.log;
using OnlineLogViewer.log.view;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;

namespace OnlineLogViewer.mq
{
    public class LogMessageQueue
    {
        private static LogMessageQueue instance = null;
        public static LogMessageQueue getInstance()
        {
            if (instance == null)
            {
                instance = new LogMessageQueue();
                instance.init();
            }
            return instance;
        }
        private ConnectionFactory factory = null;
        private IConnection conn = null;
        private IModel channel = null;
        
        private EventingBasicConsumer consumer = null;
        private string consumerTag = null;
        private string logOutputConsumerTag = null;
        

        public ObservableCollection<Logger> LoggerList = new ObservableCollection<Logger>();
        public ObservableCollection<Logger> ModifiedLoggerList = new ObservableCollection<Logger>();

        public ObservableCollection<string> ServicesList = new ObservableCollection<string>();
        private LogMessageQueue()
        {
            //init();
        }

        private void init()
        {
            factory = new ConnectionFactory();
            factory.AutomaticRecoveryEnabled = true;
            factory.NetworkRecoveryInterval = TimeSpan.FromSeconds(10);
            // "guest"/"guest" by default, limited to localhost connections
            factory.UserName = "userName";// 用户名
            factory.Password = "password";// 密码
            factory.VirtualHost = "dms";// 替换为自己的应用名
            factory.HostName = "xxx.xxx.com";// 主机名
            conn = factory.CreateConnection();
            channel = conn.CreateModel();
            
            string queueName = "OnlineLogViewer" + ".dms." + LogConstants.PROFILE_NAME_CURRENT + ".log.command_response." + Guid.NewGuid().ToString();
            channel.ExchangeDeclare(LogConstants.EXCHANGE_NAME_COMMAND, ExchangeType.Fanout, false, true);
            channel.ExchangeDeclare(LogConstants.EXCHANGE_NAME_COMMAND_RESPONSE, ExchangeType.Fanout, false, true);
            channel.QueueDeclare(queueName, false, true, true, null);
            channel.QueueBind(queueName, LogConstants.EXCHANGE_NAME_COMMAND_RESPONSE, "OnlineLogViewer", null);
            
            // logoutput
            string queueName2 = "OnlineLogViewer" + ".dms." + LogConstants.PROFILE_NAME_CURRENT + ".log.output." + Guid.NewGuid().ToString();
            channel.ExchangeDeclare(LogConstants.EXCHANGE_NAME_OUTPUT, ExchangeType.Fanout, false, true);
            channel.QueueDeclare(queueName2, false, true, true, null);
            channel.QueueBind(queueName2, LogConstants.EXCHANGE_NAME_OUTPUT, "OnlineLogViewer", null);

            consumer = new EventingBasicConsumer(channel);
            consumer.Received += (ch, ea) =>
            {
                var body = ea.Body;
                processResponse(body);
                //channel.BasicAck(ea.DeliveryTag, false);
            };            
            
            consumerTag = channel.BasicConsume(queueName, true, consumer);
            logOutputConsumerTag = channel.BasicConsume(queueName2, true, consumer);
        }

        public void deinit()
        {
            channel?.Close();
            conn?.Close();
            channel = null;
            conn = null;
        }

        public void sendCommand(LogCommand logCommand)
        {
            string message = JsonConvert.SerializeObject(logCommand);
            sendMessage(message);
        }
        public void sendMessage(string message)
        {
            if (channel == null)
            {
                return;
            }
            byte[] messageBodyBytes = System.Text.Encoding.UTF8.GetBytes(message);
            IBasicProperties props = channel.CreateBasicProperties();
            props.ContentType = "application/json";
            props.DeliveryMode = 1;

            channel?.BasicPublish(LogConstants.EXCHANGE_NAME_COMMAND, "", props, messageBodyBytes);
        }

        private void processResponse(byte[] data)
        {
            if (data == null)
            {
                return;
            }
            string message = Encoding.UTF8.GetString(data, 0, data.Length);
            LogCommandResponse logCommandResponse = JsonConvert.DeserializeObject<LogCommandResponse>(message);
            switch (logCommandResponse.command)
            {
                case LogCommand.COMMAND_PROBE_RESPONSE:
                    {
                        string contextName = logCommandResponse.contextName;                        
                        Application.Current.Dispatcher.BeginInvoke(new Action(() =>
                        {
                            if (!ServicesList.Contains(contextName))
                            {
                                ServicesList.Add(contextName);
                            }
                            logCommandResponse.probeLoggers?.ForEach(p =>
                            {
                                //if (!p.loggerName.StartsWith("com.yyn"))
                                //{
                                //    return;
                                //}
                                p.contextName = contextName;
                                {
                                    Logger existLogger = LoggerList.FirstOrDefault((loggerInList) =>
                                    {
                                        if (String.Equals(p.contextName, loggerInList.contextName) && String.Equals(p.loggerName, loggerInList.loggerName))
                                        {
                                            return true;
                                        }
                                        return false;
                                    });
                                    if (existLogger == null)
                                    {
                                        p.setLoggerLevel = p.loggerLevel;
                                        LoggerList.Add(p);
                                    }
                                    else
                                    {
                                        if (existLogger.loggerLevel != p.loggerLevel || existLogger.setLoggerLevel != p.setLoggerLevel)
                                        {
                                            existLogger.loggerLevel = p.loggerLevel;
                                            existLogger.setLoggerLevel = p.setLoggerLevel;
                                            ConfigLoggerWindow.refreshList();
                                        }                                       
                                    }
                                    if (p.configured)
                                    {
                                        addToModifiedProbeLoggerList(p);
                                    }
                                }
                            });

                        }), null);
                    }
                    break;
                case LogCommand.COMMAND_PROBE_SERVICE_RESPONSE:
                    {
                        ProbeServiceResponse probeServiceResponse = JsonConvert.DeserializeObject<ProbeServiceResponse>(message);
                        string contextName = probeServiceResponse.contextName;
                        if (!String.IsNullOrEmpty(probeServiceResponse.contextName))
                        {
                            
                            Application.Current.Dispatcher.BeginInvoke(new Action(() =>
                            {
                                if (!ServicesList.Contains(contextName))
                                {
                                    ServicesList.Add(contextName);
                                }
                            }), null);
                        }
                    }
                    break;
                case LogCommand.COMMAND_LOG_OUTPUT:
                    {
                        MainWindow.AppendMessageStatic(logCommandResponse.mesasge);
                    }
                    break;
            }
        }

        public void addToModifiedProbeLoggerList(Logger p)
        {
            Logger existLogger = ModifiedLoggerList.FirstOrDefault((loggerInList) =>
            {
                if (String.Equals(p.contextName, loggerInList.contextName) && String.Equals(p.loggerName, loggerInList.loggerName))
                {
                    return true;
                }
                return false;
            });
            if (existLogger == null)
            {
                ModifiedLoggerList.Add(p);
            }
            else
            {
                if (existLogger.loggerLevel != p.loggerLevel || existLogger.setLoggerLevel != p.setLoggerLevel)
                {
                    existLogger.loggerLevel = p.loggerLevel;
                    existLogger.setLoggerLevel = p.setLoggerLevel;
                    ConfigLoggerWindow.refreshModifiedLoggerListCollectionView();
                }
            }
        }

        public void clearLoggerList()
        {
            LoggerList.Clear();
        }
        public void clearModifiedProbeLoggerList()
        {
            ModifiedLoggerList.Clear();
        }
        public void removeModifiedLogger(Logger p)
        {
            Logger existLogger = ModifiedLoggerList.FirstOrDefault((loggerInList) =>
            {
                if (String.Equals(p.contextName, loggerInList.contextName) && String.Equals(p.loggerName, loggerInList.loggerName))
                {
                    return true;
                }
                return false;
            });
            if (existLogger == null)
            {
                return;
            }
            else
            {
                ModifiedLoggerList.Remove(existLogger);
            }
        }
    }


}
