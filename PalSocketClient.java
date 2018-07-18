
import java.awt.Rectangle;
import java.io.*;
import java.net.Socket;
import javax.swing.*;


/**
*****************************************************************************
 * @
*******************************************************************************/
public class PalSocketClient {

    //
    String  host = "127.0.0.1";
    int     port = 8888;
    Socket  socket;
    OutputStream outputStream;
    InputStream inputStream;

    public void connect() throws Exception
    {
        //
        System.out.println("client send socket req: IP:" + host+ " PORT: " + port);
        socket = new Socket(host, port);

        //
        System.out.println("CLIENT SOCKET is CONNECTED. outputStream and inPutStream are READY.");
        outputStream = socket.getOutputStream();
        inputStream  = socket.getInputStream();
   }

    /**
    *****************************************************************************
     * @throws Exception
    *******************************************************************************/
    public Socket getSocket()
    {
        return this.socket;
    }


    /**
    *****************************************************************************
     * @throws Exception
    *******************************************************************************/
    void sendPacket(String message) throws Exception
    {
        //socket.getOutputStream().write(message.getBytes("UTF-8"));
        outputStream.write(message.getBytes("UTF-8"));
        outputStream.flush();
        System.out.println("sendPacket(), message.length() = "+message.length());
    }

    /**
    *****************************************************************************
     * @throws Exception
    *******************************************************************************/
    void closeSocket() throws Exception
    {
        //
        //socket.shutdownOutput();

        System.out.println("CLIENT close inputStream and outputStream");
        inputStream.close();
        outputStream.close();

        System.out.println("CLIENT close socket.");
        socket.close();
   }

}


/**
*****************************************************************************
* @brief: This is a thread class.
          Then one object of this class should be newed first.
          Then one object of the class Thread should be NEWED in the way as following.
          new Thread(clientSocketReceive).start();
          And when the method start() of the OBJ of the class Thread is called,
          the override method, run(), of the class implements Runnable is called.
          And a new thread is created.
* @throws Exception
*******************************************************************************/
class ClientSocketReceive implements Runnable
{
    MyJFrame myJFrameMainPage;
    static final int SOCKET_DATA_LEN_RECEIVED = 2048;
    final static int DATA_TYPE_MSG_SN = 0;
    final static int DATA_TYPE_MSG_CONTENT = 1;


    int intMsgSn = 0;
    int intMsgId = 0;
    int intTatalReceivedMsgNum = 0;

    private Socket socket;
    JTextField tf;
    JTextArea  ta;
    JTable jTablePalAllMsg;

    byte [] byteRcv = new byte[SOCKET_DATA_LEN_RECEIVED];
    InputStream is=null;
    InputStreamReader isr=null;
    BufferedReader br=null;
    OutputStream os=null;
    PrintWriter pw=null;

    HashTableMsg    hashTableMsg;


    /**
    *****************************************************************************
     * @throws Exception
    *******************************************************************************/
    /*
    public ClientSocketReceive( Socket socket,
                                        JTextArea ta,
                                        JTable jTablePalAllMsg) */
    public ClientSocketReceive( MyJFrame myJFrame)
    {
        this.myJFrameMainPage   = myJFrame;
        this.socket             = myJFrame.palSocketClient.getSocket();
        this.ta                 = myJFrame.taRcv;
        this.jTablePalAllMsg    = myJFrame.jTablePalAllMsg;

        hashTableMsg    = new HashTableMsg();
    }

    /**
    *****************************************************************************
    * @brief: To implements Runnable, this override function is necessary.
              This function is used to receive packets from socket.
    * @throws Exception
    * @Override
    *****************************************************************************/
    public void run()
    {
        try
        {
            int oneTimeByteNum = 0;
            //int allByteNum = 0;

            // SOCKET->getInputStream->InputStreamReader->BufferedReader->readLine.
            // get the source of the socket inputStream.
            is = socket.getInputStream();
            //br = new BufferedReader(is);

            ///*
            //receive one MSG sent from server.
            /*
            while(oneTimeByteNum >=0 )
            {
                allByteNum += oneTimeByteNum;
                oneTimeByteNum
                    = is.read(byteRcv, allByteNum, (SOCKET_DATA_LEN_RECEIVED - allByteNum));
                System.out.println("MSG coming from server, oneTimeByteNum = "+oneTimeByteNum);
            }
            */

            while(true)
            {
                oneTimeByteNum
                    = is.read(byteRcv, 0, (SOCKET_DATA_LEN_RECEIVED));
                System.out.println("MSG coming from server, oneTimeByteNum = "+oneTimeByteNum);
                MyJFrame.writeLog("MSG coming from server, oneTimeByteNum = "+oneTimeByteNum);
                //analyze the MSG HEADER, and print it in TEXT AREA.
                analyseMsgHeader();
            }

            //*/

            /** Because the MSG sent from server is not character, but byte array, the
            //    client should read byte from socket but not character.

            // read from the socket inputStream into one InputStreamReader, isr.
            // Class InputStreamReader provides a bridge from byte stream to character stream.
            isr = new InputStreamReader(is);

            // put the data from InputStreamReader into BufferedReader
            // Class BufferedReader provides character reading methods with buffer.
            br = new BufferedReader(isr);

            String info=null;

            while((info=br.readLine())!=null){//read one line from the data coming from socket.
                System.out.println("MSG coming from server: "+info);

                // print the coming MSG into the TEXT AREA.
                ta.append(info);
                ta.append("\r\n");
            }
            */
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int byte2Int(byte[] bs)
    {
        return ((bs[0]&0xFF)         |
                ((bs[1] & 0xFF)<<8)  |
                ((bs[2] & 0xFF)<<16) |
                ((bs[3] & 0xFF)<<24));
    }

    public int analyseMsgContentOrMsgId()
    {
        /*The first 8 byte are MSG HEADER:
         * -------------------------------------------------------------------
         * bit1                     bit16   |   bit17                   bit32
         * -------------------------------------------------------------------
         *              CARD_ID             |       0X5555
         * -------------------------------------------------------------------
         *                              MSG ID
         * -------------------------------------------------------------------
         *                              MSG CONTENT
         *
         *
         * -------------------------------------------------------------------
         * */
         /*
         * IF CARD ID == 0xFFFF,  MSG CONTENT is MSG SN;
         * IF CARD ID == 0 OR 1,  MSG CONTENT is MSG CONTENT;
         */
        byte byteMsgId[] = new byte[4];
        byte byteMsgSn[] = new byte[4];

        byteMsgId[0] = byteRcv[4];
        byteMsgId[1] = byteRcv[5];
        byteMsgId[2] = byteRcv[6];
        byteMsgId[3] = byteRcv[7];

        byteMsgSn[0] = byteRcv[8];
        byteMsgSn[1] = byteRcv[9];
        byteMsgSn[2] = byteRcv[10];
        byteMsgSn[3] = byteRcv[11];

        intMsgId = byte2Int(byteMsgId);
        intMsgSn = byte2Int(byteMsgSn);

        System.out.println("intMsgId = "+intMsgId+" intMsgSn = "+intMsgSn);
        MyJFrame.writeLog("intMsgId = "+intMsgId+" intMsgSn = "+intMsgSn);

        System.out.println("byteRcv[0] = "+byteRcv[0]+"byteRcv[1] = "+byteRcv[1]);
        System.out.println("byteRcv[2] = "+byteRcv[2]+"byteRcv[3] = "+byteRcv[3]);
        if(0x0F == byteRcv[0] && 0x0F == byteRcv[1])
        {
            return DATA_TYPE_MSG_SN;// MSG SN
        }
        else
        {
            return DATA_TYPE_MSG_CONTENT;// MSG SN
        }

    }


    public void analyseMsgHeader()
    {
        int ret;
        if(MyJFrame.PAL_MSG_MAX_NUM < this.intTatalReceivedMsgNum)
        {
            System.out.println("ERROR. intTatalReceivedMsgNum == "+this.intTatalReceivedMsgNum);
            return;
        }

        ret = analyseMsgContentOrMsgId();
        if(DATA_TYPE_MSG_SN == ret)
        {
            Boolean boolRet;

            boolRet = hashTableMsg.findMsg(intMsgId);
            if(true == boolRet)
            {
               //"MSG_SN","PAL_STATE","PAL","DIRECTION","OTHER_MODULE","MSG_TYPE","CARD_ID"};//column name
                this.jTablePalAllMsg.setValueAt(intMsgSn,   this.intTatalReceivedMsgNum, 0);
                this.jTablePalAllMsg.setValueAt("PAL",      this.intTatalReceivedMsgNum, 2);

                if(hashTableMsg.strMsgDirectionFound.equals("FROM_PAL"))
                {
                    this.jTablePalAllMsg.setValueAt("----->>",
                                                this.intTatalReceivedMsgNum, 3);
                }
                else
                {
                    this.jTablePalAllMsg.setValueAt("<<-----",
                                                this.intTatalReceivedMsgNum, 3);
                }
                this.jTablePalAllMsg.setValueAt(hashTableMsg.strMsgMoudleFound,
                                                this.intTatalReceivedMsgNum, 4);

                this.jTablePalAllMsg.setValueAt(hashTableMsg.strMsgNameFound,
                                                this.intTatalReceivedMsgNum, 5);

                this.intTatalReceivedMsgNum++;
            }
            else
            {
               //"MSG_SN","PAL_STATE","PAL","DIRECTION","OTHER_MODULE","MSG_TYPE","CARD_ID"};//column name
                this.jTablePalAllMsg.setValueAt(intMsgSn,   this.intTatalReceivedMsgNum, 0);
                this.jTablePalAllMsg.setValueAt("PAL",      this.intTatalReceivedMsgNum, 2);


                this.jTablePalAllMsg.setValueAt("?-----?",
                                                this.intTatalReceivedMsgNum, 3);

                this.jTablePalAllMsg.setValueAt("UNKNOWN",
                                                this.intTatalReceivedMsgNum, 4);

                this.jTablePalAllMsg.setValueAt("UNKNOWN",
                                                this.intTatalReceivedMsgNum, 5);

                this.intTatalReceivedMsgNum++;
            }

            int rowCount = jTablePalAllMsg.getRowCount();
            jTablePalAllMsg.getSelectionModel().setSelectionInterval(rowCount-1, rowCount-1);
            Rectangle rect = jTablePalAllMsg.getCellRect(rowCount-1, 0, true);
            jTablePalAllMsg.scrollRectToVisible(rect);

        }
        else
        {
            if(0x70 == byteRcv[4])
            {
                System.out.println("RRC_PAL_POWER_SWEEP_REQ ");
                ta.append("PS \t<<--- \tPHY: \tRRC_PAL_POWER_SWEEP_REQ\r\n");
            }
            else
            {
                System.out.println("UNKNOWN MSG. ");
                ta.append("PS \t<<--- \tPHY: \tUNKNOWN MSG\r\n");
            }

        }
    }
}
