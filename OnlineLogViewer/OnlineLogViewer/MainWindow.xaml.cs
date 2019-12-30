using Newtonsoft.Json;
using OnlineLogViewer.log;
using OnlineLogViewer.log.view;
using OnlineLogViewer.mq;
using System;
using System.Collections.Generic;
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
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace OnlineLogViewer
{
    /// <summary>
    /// MainWindow.xaml 的交互逻辑
    /// </summary>
    public partial class MainWindow : Window
    {
        private static MainWindow instance = null;
        public MainWindow()
        {
            InitializeComponent();
            instance = this;
        }

        private void Button_Click_RESET_ALL(object sender, RoutedEventArgs e)
        {

        }

        private void Button_Click_PROBE(object sender, RoutedEventArgs e)
        {

        }

        private void Button_Click_(object sender, RoutedEventArgs e)
        {

        }

        private void Button_Click_SHOW_CONFIG_LOGGER(object sender, RoutedEventArgs e)
        {
            SelectProfileWindow instance = new SelectProfileWindow();
            instance.Show();
        }

        private void Window_Closed(object sender, EventArgs e)
        {
            instance = null;
        }

        public static void AppendMessageStatic(string mesasge)
        {
            if (instance != null)
            {
                instance.AppendMessage(mesasge);
            }

        }
        public void AppendMessage(string mesasge)
        {
            LogOutputTextBlock.Dispatcher.BeginInvoke(new Action(() =>
            {
                LogOutputTextBlock.Text += mesasge + Environment.NewLine;              
            }));
        }

        private void Button_Click_ClearLog(object sender, RoutedEventArgs e)
        {
            LogOutputTextBlock.Text = "";
        }
    }
}
