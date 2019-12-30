using Newtonsoft.Json;
using OnlineLogViewer.mq;
using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace OnlineLogViewer.log.view
{
    /// <summary>
    /// ConfigLogger.xaml 的交互逻辑
    /// </summary>
    public partial class ConfigLoggerWindow : Window
    {
        private static ConfigLoggerWindow instance = null;
        public static void showWindow()
        {
            if (instance != null)
            {
                return;
            }
            instance = new ConfigLoggerWindow();
            instance.Show();
        }

        public static void refreshList()
        {
            if (instance != null)
            {
                instance.allLoggerListCollectionView.Refresh();                
            }
        }

        public static void refreshModifiedLoggerListCollectionView()
        {
            if (instance != null)
            {                
                instance.modifiedLoggerListCollectionView.Refresh();
            }
        }

        public class LoggerSorter : IComparer
        {
            public int Compare(object l1, object l2)
            {
                Logger probeLogger1 = l1 as Logger;
                Logger probeLogger2 = l2 as Logger;
                if (String.Equals(probeLogger1.contextName, probeLogger2.contextName))
                {
                    return probeLogger1.loggerName.CompareTo(probeLogger2.loggerName);
                }
                else
                {
                    return probeLogger1.contextName.CompareTo(probeLogger2.contextName);
                }

            }
        }


        private ListCollectionView allLoggerListCollectionView = null;
        private ListCollectionView modifiedLoggerListCollectionView = null; 
        private string filterServiceName = null;
        private string filterLoggerName = null;
        private DateTime lastProbeService = new DateTime(1970, 1, 1, 0, 0, 0,
                                                          DateTimeKind.Local);
        private DateTime lastProbeTime = new DateTime(1970, 1, 1, 0, 0, 0,
                                                          DateTimeKind.Local);
        public ConfigLoggerWindow()
        {
            InitializeComponent();
            AllServiceComboBox.ItemsSource = LogMessageQueue.getInstance().ServicesList;

            allLoggerListCollectionView = (ListCollectionView)CollectionViewSource.GetDefaultView(LogMessageQueue.getInstance().LoggerList);
            allLoggerListCollectionView.CustomSort = new LoggerSorter();
            allLoggerListCollectionView.Filter = (logger) =>
            {
                Logger probeLogger = logger as Logger;
                if (!String.IsNullOrWhiteSpace(filterServiceName))
                {
                    if (!String.Equals(filterServiceName, probeLogger.contextName))
                    {
                        return false;
                    }
                }
                if (!String.IsNullOrWhiteSpace(filterLoggerName))
                {
                    if (String.IsNullOrWhiteSpace(probeLogger.loggerName))
                    {
                        return false;
                    }
                    if (probeLogger.loggerName.IndexOf(filterLoggerName) == -1)
                    {
                        return false;
                    }
                }
                return true;
            };
            allLoggerListView.ItemsSource = allLoggerListCollectionView;
            //
            modifiedLoggerListCollectionView = (ListCollectionView)CollectionViewSource.GetDefaultView(LogMessageQueue.getInstance().ModifiedLoggerList);
            modifiedLoggerListCollectionView.CustomSort = new LoggerSorter();
            modifiedLoggerListView.ItemsSource = modifiedLoggerListCollectionView;

            probeService();
        }

        private void Window_Closed(object sender, EventArgs e)
        {
            instance = null;
        }

        private void AllLoggerListView_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (allLoggerListView.SelectedIndex >= 0)
            {

            }
        }

        private void SetLoggerButton_Click(object sender, RoutedEventArgs e)
        {
            Logger item = ((Button)sender).DataContext as Logger;
            if (item == null)
            {
                return;
            }
            LogMessageQueue.getInstance().addToModifiedProbeLoggerList(item);
            ConfigLoggerCommand logCommand = new ConfigLoggerCommand();
            logCommand.contextName = item.contextName;

            Logger probeLogger = new Logger();
            probeLogger.loggerLevel = item.setLoggerLevel;
            probeLogger.loggerName = item.loggerName;
            logCommand.probeLoggers = new List<Logger>() { probeLogger };
            LogMessageQueue.getInstance().sendCommand(logCommand);
        }

        private void ResetLoggerButton_Click(object sender, RoutedEventArgs e)
        {
            Logger item = ((Button)sender).DataContext as Logger;
            if (item == null)
            {
                return;
            }

            ResetLoggerConfigCommand logCommand = new ResetLoggerConfigCommand();
            logCommand.contextName = item.contextName;

            Logger probeLogger = new Logger();            
            probeLogger.loggerName = item.loggerName;
            logCommand.probeLoggers = new List<Logger>() { probeLogger };
            LogMessageQueue.getInstance().sendCommand(logCommand);

            LogMessageQueue.getInstance().removeModifiedLogger(item);
        }

        private void FilterLoggerButton_Click(object sender, RoutedEventArgs e)
        {
            filterServiceName = AllServiceComboBox.Text;
            filterLoggerName = FilterLoggerNameTextBox.Text;
            allLoggerListCollectionView.Refresh();
        }

        private void Button_Click_PROBE(object sender, RoutedEventArgs e)
        {
            probe();
        }
        private void probe()
        {
            DateTime now = DateTime.Now;
            if (lastProbeTime.AddSeconds(10) >= now)
            {
                MessageBox.Show("正在探测中，请稍后...", "", MessageBoxButton.OK);
                return;
            }
            string contextName = this.AllServiceComboBox.Text;
            if (String.IsNullOrEmpty(contextName))
            {
                MessageBox.Show("必须选择服务!", "", MessageBoxButton.OK);
                return;
            }
            LogMessageQueue.getInstance().clearLoggerList();
            LogMessageQueue.getInstance().clearModifiedProbeLoggerList();
            
            lastProbeTime = now;
            ProbeLoggerCommand2 probeLogCommand = new ProbeLoggerCommand2();
            probeLogCommand.contextName = contextName;
            probeLogCommand.loggerName = this.FilterLoggerNameTextBox.Text;
            LogMessageQueue.getInstance().sendCommand(probeLogCommand);
        }
        private void probeService()
        {
            DateTime now = DateTime.Now;
            if (lastProbeService.AddSeconds(10) >= now)
            {
                MessageBox.Show("正在探测中，请稍后...", "", MessageBoxButton.OK);
                return;
            }
            lastProbeService = now;
            ProbeServiceCommand probeLogCommand = new ProbeServiceCommand();            
            LogMessageQueue.getInstance().sendCommand(probeLogCommand);
        }

        private void Button_Click_RESET_ALL(object sender, RoutedEventArgs e)
        {
            ResetAllLoggerConfigCommand logCommand = new ResetAllLoggerConfigCommand();
            LogMessageQueue.getInstance().sendCommand(logCommand);
            LogMessageQueue.getInstance().ModifiedLoggerList.Clear();
        }
    }
}
