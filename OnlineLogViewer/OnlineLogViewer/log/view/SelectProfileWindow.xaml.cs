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
using System.Windows.Shapes;

namespace OnlineLogViewer.log.view
{
    /// <summary>
    /// SelectProfileWindow.xaml 的交互逻辑
    /// </summary>
    public partial class SelectProfileWindow : Window
    {
        public SelectProfileWindow()
        {
            InitializeComponent();
        }

        private void SelectProfileComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            string text = ((sender as ComboBox).SelectedItem as ComboBoxItem).Content as string;
            LogConstants.PROFILE_NAME_CURRENT = text;
            LogConstants.initProfile();
        }

        private void Button_Click_Confirm(object sender, RoutedEventArgs e)
        {
            LogMessageQueue.getInstance();
            ConfigLoggerWindow.showWindow();
            this.Close();
        }
    }
}
