import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.*;
/**
* @FILENAME: MyJFrame
* @BRIEF: This is the Front Page of the PS STUB.
* @auther: LUFENG.HAN
* @date:
* @version: 1.0
 * @param <clientFile>
*/
public class MyJFrame  extends WindowAdapter
                        implements ActionListener{

    //declare one window variable, which will be created in the method,"go",
    //and the method, "go", will be called in main().
    JFrame      fFrontWin;
    JButton     bSend, bStop;
    JTextField  tf1, tfRcv;
    JTextArea   taSend, taRcv;
    JScrollPane jspSend, jspRcv;
    Container   contentPane;
    JRadioButton jRadioButtonPowerSweep;
    JRadioButton jRadioButtonMibReq;
    JRadioButton jRadioButtonSib1Req;
    JTable       jTablePalAllMsg;
    String       msgFileName = null;
    static final int PAL_MSG_TABLE_COLUMN_NUM= 7;
    static final int PAL_MSG_MAX_NUM= 1024;

    Object[] columnNames = new Object[]{
            "MSG_SN","PALMS","PAL","DIRECTION","OTHER_MOD","MSG_TYPE","CARD_ID"};//column name
    Object[][] rowData = new Object[PAL_MSG_MAX_NUM][PAL_MSG_TABLE_COLUMN_NUM];//ROW NUMBER, COLUMN NUMBER


    public final static Color colorBlack = new Color(0,0,0);
    public final static Color colorBule = new Color(0,0,255);
    public final static Color colorLightSlateGray = new Color(119,136,153);
    public final static Color colorLightSeaGreen = new Color(32,178,170);
    public final static Color colorLightYellow = new Color(255,255,224);

    //static PalSocketClient palSocketClient;
    //static PalSocketClient palSocketClient;// = new PalSocketClient();
    PalSocketClient palSocketClient;// = new PalSocketClient();
    FileTransferClient clientFile;

    static String strLogFileLocation;
    static String strLogFileName = "LOG_PAL_SIMU.txt";
    static File fileLog;
    static FileWriter filewriterLog;
    static BufferedWriter bufferWriterLog;

    /*******************************************************************************
    * ENTRY point.
    *******************************************************************************/
    public static void main(String[] args) throws Exception
    {
        // TODO Auto-generated method stub
        MyJFrame myJFrame = new MyJFrame();

        //initiate all parameters of the class MyJFrame
        myJFrame.init();


        strLogFileLocation = System.getProperty("user.dir") +"\\"+ strLogFileName;
        fileLog = new File(strLogFileLocation);
        filewriterLog = new FileWriter(fileLog,false);
        bufferWriterLog = new BufferedWriter(filewriterLog);


        // CONNEC THE SOCKET.
        try
        {
            myJFrame.palSocketClient.connect();
        }
        catch(Exception ec)
        {
            ec.printStackTrace();
        }



        //*!read from the file, "msgx", and send its content through socket
        try
        {   // OPEN CASE FILE
            myJFrame.clientFile
                = new FileTransferClient(myJFrame.palSocketClient.getSocket(),
                                         myJFrame.taRcv);
        } catch (Exception ef)
        {
            ef.printStackTrace();
        }


        //*!
        ClientSocketReceive clientSocketReceive =new ClientSocketReceive(myJFrame);
                /*
            new ClientSocketReceive(myJFrame.palSocketClient.getSocket(),
                                    myJFrame.taRcv,
                                    myJFrame.jTablePalAllMsg);*/

        new Thread(clientSocketReceive).start();
    }


    /*******************************************************************************
    * @brief: used to initiate all parameters of the class MyJFrame.
    *******************************************************************************/
    public void init()
    {
        //creat the objects used by its class.
        fFrontWin   = new JFrame("PAL SIMULATER");
        palSocketClient = new PalSocketClient();

        // prepare layout.
        contentPane = fFrontWin.getContentPane();

        //------------------------------------------------------------
        //text areas used for sending and receiving.
        taSend = new JTextArea();
        taRcv = new JTextArea();
        jTablePalAllMsg = new JTable(rowData,columnNames);




        //to used scroll, these two text area are put into two JScrollPane seperately:
        //jspSend, and jspRcv
        jspSend = new JScrollPane(jTablePalAllMsg,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);


        jspRcv = new JScrollPane(taRcv,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //------------------------------------------------------------
        //The two JScrollPanels are put into one JPanel, jpUp.
        //Additional, two labels are put into this JPanel too.
        JPanel jpUp = new JPanel();



        JPanel jpUpSend = new JPanel();
        JPanel jpUpSendRadioButton = new JPanel();
        JPanel jpUpSendTextArea = new JPanel();
        JPanel jpUpRecv = new JPanel();

        //jpUp use one BoxLayout as layout.
        //All things in this jp2(ie BoxLayout) are set as vertical direction.
        jpUp.setLayout(new BoxLayout(jpUp, BoxLayout.Y_AXIS));
        jpUp.add(jpUpSend);//FOR send
        jpUp.add(jpUpRecv);//FOR recv


        jpUpSend.setLayout(new BoxLayout(jpUpSend, BoxLayout.X_AXIS));
        jpUpSend.add(jpUpSendRadioButton);//FOR JRadioButton
        //jpUpSend.add(jpUpSendTextArea);//FOR textarea
        jpUpSend.add(jspSend);//FOR textarea



        //jspSend.setBounds(200, 100, 550, 650);
        jspSend.setViewportView(jTablePalAllMsg);
        //jTablePalAllMsg.setRowHeight(35);

        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(JLabel.CENTER);
        //jTablePalAllMsg.setGridColor(arg0);
        jTablePalAllMsg.setShowGrid(false);
        jTablePalAllMsg.setDefaultRenderer(Object.class,r);
        //"MSG_SN","PAL_STATE","PAL","DIRECTION","OTHER_MODULE","MSG_TYPE","CARD_ID"};//column name

        jTablePalAllMsg.getColumnModel().getColumn(0).setPreferredWidth(40);
        jTablePalAllMsg.getColumnModel().getColumn(1).setPreferredWidth(2);
        jTablePalAllMsg.getColumnModel().getColumn(2).setPreferredWidth(10);
        jTablePalAllMsg.getColumnModel().getColumn(3).setPreferredWidth(30);
        jTablePalAllMsg.getColumnModel().getColumn(4).setPreferredWidth(10);
        jTablePalAllMsg.getColumnModel().getColumn(5).setPreferredWidth(200);
        jTablePalAllMsg.getColumnModel().getColumn(6).setPreferredWidth(2);

        // LEFT ALIGNMENT
        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(SwingConstants.LEFT);

        TableColumn column = jTablePalAllMsg.getColumnModel().getColumn(5);
        column.setCellRenderer(render);


        //setOneRowBackgroundColor(jTablePalAllMsg, 0, colorLightSeaGreen);
        //setOneRowBackgroundColor(jTablePalAllMsg, 1, colorLightYellow);
        //add(jspSend);




        jpUpSendRadioButton.setLayout(new BoxLayout(jpUpSendRadioButton, BoxLayout.Y_AXIS));
        jRadioButtonPowerSweep = new JRadioButton("PS_PHY_POWER_SWEEP_REQ");
        jRadioButtonPowerSweep.addActionListener(this);
        jRadioButtonPowerSweep.setActionCommand("PS_PHY_POWER_SWEEP_REQ");
        jpUpSendRadioButton.add(jRadioButtonPowerSweep);//ADD A JRadioButton


        jRadioButtonMibReq = new JRadioButton("CPHY_BCCH_CONFIG_REQ_MIB");
        jRadioButtonMibReq.addActionListener(this);
        jRadioButtonMibReq.setActionCommand("CPHY_BCCH_CONFIG_REQ_MIB");
        jpUpSendRadioButton.add(jRadioButtonMibReq);//ADD A JRadioButton



        jRadioButtonSib1Req = new JRadioButton("CPHY_BCCH_CONFIG_REQ_SIB1");
        jRadioButtonSib1Req.addActionListener(this);
        jRadioButtonSib1Req.setActionCommand("CPHY_BCCH_CONFIG_REQ_SIB1");
        jpUpSendRadioButton.add(jRadioButtonSib1Req);//ADD A JRadioButton




        ButtonGroup btGroupMsgSent = new ButtonGroup();
        btGroupMsgSent.add(jRadioButtonPowerSweep);
        btGroupMsgSent.add(jRadioButtonMibReq);
        btGroupMsgSent.add(jRadioButtonSib1Req);

        Border etched = BorderFactory.createEtchedBorder();
        Border border = BorderFactory.createTitledBorder(etched, "SELECT MSG ");
        jpUpSendRadioButton.setBorder(border);



        //jpUpSendTextArea.setLayout(new BoxLayout(jpUpSendTextArea, BoxLayout.Y_AXIS));
        //jpUpSendTextArea.add(new JLabel("MSG being sent:"));//ADD A LABEL
        //jpUpSendTextArea.add(jspSend);//ADD A Java Scroll Pane for sendding

        jpUpRecv.setLayout(new BoxLayout(jpUpRecv, BoxLayout.Y_AXIS));
        jpUpRecv.add(new JLabel("RRC-PAL MSG FLOW:"));//ADD A LABEL
        jpUpRecv.add(jspRcv);//ADD A Java Scroll Pane for receiving



        //jpUp is set to the center of contentPane
        contentPane.add(jpUp, "Center");


        //------------------------------------------------------------
        //*! Add one Button SEND
        bSend = new JButton("SEND");

        //THE BUTTON, SEND, will be listened by "this", the obj of the class MyJFrame .
        //When this button is pushed or released, event is triggered, and
        //the this method, actionPerformed, is called.
        bSend.addActionListener(this);

        // give this button, SEND, a name, which will indicate which button creat the event
        // received by the OBJ.
        bSend.setActionCommand("SEND");


        //------------------------------------------------------------
        /*! Add one Button STOP*/
        bStop = new JButton("STOP");
        bStop.addActionListener(this);
        bStop.setActionCommand("STOP");


        //------------------------------------------------------------
        JPanel jpDown;
        jpDown = new JPanel();
        jpDown.add(bSend);
        jpDown.add(bStop);

        //jp1 is set to the bottom of contentPane
        contentPane.add(jpDown, "South");


        //------------------------------------------------------------
        //This window object, fFrontWin, begin to listen the windows event
        //of "this", such as "closing window". To used this event, the
        //overwrite function is created in the following: "windowClosing".
        fFrontWin.addWindowListener(this);

        /*! show Frame */
        fFrontWin.setSize(1000, 650);
        fFrontWin.setVisible(true);



    }


    /*******************************************************************************
    *******************************************************************************/
    public void buttonSendAct(ActionEvent e)
    {



        if((null != clientFile))
        {
            if(null == msgFileName)
            {
                taSend.append("ERROR. PLEASE SELECT THE MSG TO BE SENT. \r\n");
            }
            else
            {
                clientFile.fileTransferClientSetFilename(msgFileName);

                try
                {   //*! read from the file, and send its content through socket
                    clientFile.sendFile(msgFileName); //
                }
                catch (Exception ef)
                {
                    ef.printStackTrace();
                }

                //taSend.append("SENDING...\r\n");

                if(null != palSocketClient.outputStream)
                {
                    try
                    {
                        palSocketClient.sendPacket("hello, PAL SIMULATER.");
                    }
                    catch(Exception es)
                    {
                        es.printStackTrace();
                    }
                }
                else
                {
                    //taSend.append("SOCKET ERR.\r\n");
                }
            }

        }
    }

    /*******************************************************************************
    *******************************************************************************/
    public void buttonStopAct(ActionEvent e)
    {
        if((null != clientFile))
        {
            try
            {
                palSocketClient.sendPacket("quit");
                palSocketClient.closeSocket();
                clientFile.closeFile();
            }
            catch(Exception es)
            {
                es.printStackTrace();
            }

            fFrontWin.setVisible(false);
            fFrontWin.dispose();
            System.exit(0);
        }
    }

    /*******************************************************************************
    *******************************************************************************/
    public void buttonPowerSweepReqAct(ActionEvent e)
    {
        msgFileName = new String("PS_PHY_POWER_SWEEP_REQ");
    }


    /*******************************************************************************
    *******************************************************************************/
    public void buttonMibReqAct(ActionEvent e)
    {
        msgFileName = new String("CPHY_BCCH_CONFIG_REQ_MIB");
    }


    /*******************************************************************************
    *******************************************************************************/
    public void buttonSib1ReqAct(ActionEvent e)
    {
        msgFileName = new String("CPHY_BCCH_CONFIG_REQ_SIB1");
    }


    /*******************************************************************************
    *******************************************************************************/
    @Override
    public void actionPerformed(ActionEvent e){
        // TODO Auto-generated method stub

        if("SEND" == e.getActionCommand())
        {   //*! If BUTTON-SEND is tapped.
            buttonSendAct(e);
        }
        else if(("STOP" == e.getActionCommand()) )
        {   //*! If BUTTON-STOP is tapped.
            buttonStopAct(e);
        }
        else if(("PS_PHY_POWER_SWEEP_REQ" == e.getActionCommand()) )
        {   //*! If BUTTON-STOP is tapped.
            buttonPowerSweepReqAct(e);
        }
        else if(("CPHY_BCCH_CONFIG_REQ_MIB" == e.getActionCommand()) )
        {   //*! If BUTTON-STOP is tapped.
            buttonMibReqAct(e);
        }
        else if(("CPHY_BCCH_CONFIG_REQ_SIB1" == e.getActionCommand()) )
        {   //*! If BUTTON-STOP is tapped.
            buttonMibReqAct(e);
        }
        else
        {
            System.out.println("UNKNOWN EVENT.");
        }

    }

    /*******************************************************************************
    *******************************************************************************/
    @Override
    public void windowClosing(WindowEvent arg0) {
        System.out.println("windowClosing");
        // TODO Auto-generated method stub

        // set the window invisible
        arg0.getWindow().setVisible(false);

        // delete this window.
        arg0.getWindow().dispose();
        //arg0.getSource();
        //((Window) arg0.getComponent()).dispose();
        closeLogFile();
        System.exit(0);
    }

    public static void writeLog(String content)
    {
        System.out.println("writeLog()");
        try
        {
            if(fileLog.exists())
            {
                bufferWriterLog.write(content + "\r\n");
                bufferWriterLog.flush();
                System.out.println("writeLog() write : "+content);
            }
            else
            {
                System.out.println("writeLog()  fileLog not exist.");            	
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void closeLogFile()
    {
        try
        {
            if(fileLog.exists())
            {
                bufferWriterLog.close();
                filewriterLog.close();
                System.out.println("test1 done!");
            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
    }

}
