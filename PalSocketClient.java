
import java.awt.Rectangle;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


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

        MyJFrame.writeLog("CLIENT close inputStream and outputStream");
        inputStream.close();
        outputStream.close();

        MyJFrame.writeLog("CLIENT close socket.");
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
    DefaultTableModel defTableModel;

    byte [] byteRcv = new byte[SOCKET_DATA_LEN_RECEIVED];
    InputStream isReader=null;
    InputStreamReader isr=null;
    BufferedReader br=null;
    OutputStream os=null;
    PrintWriter pw=null;

    HashTableMsg    hashTableMsg;

    public static final int PACKET_HEAD_LENGTH = 4;//MSG LENGTH PART.
    public volatile byte[] bytes = new byte[0];

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
        this.defTableModel      = myJFrame.defTableModel;

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
            int intRet;
            //int allByteNum = 0;

            // SOCKET->getInputStream->InputStreamReader->BufferedReader->readLine.
            // get the source of the socket inputStream.
            isReader = socket.getInputStream();


            //while(isReader.available() != 0)


            //while(true)
            while(EnumSocketState.SOCKET_STATE_CONNECTED == MyJFrame.enumSocketState)
            {
                /*
                oneTimeByteNum
                    = isReader.read(byteRcv, 0, (SOCKET_DATA_LEN_RECEIVED));
                System.out.println("MSG coming from server, oneTimeByteNum = "+oneTimeByteNum);
                MyJFrame.writeLog("MSG coming from server, oneTimeByteNum = "+oneTimeByteNum);*/
                // MSG BODY withoud header
                intRet = rcvOneMsg(isReader);
                if(0 == intRet)
                {   //analyze the MSG HEADER, and print it in TEXT AREA.
                    analyseMsgHeader();
                }
            }

            MyJFrame.writeLog("MyJFrame.enumSocketState IS DISCONNECTED.  "
                                + MyJFrame.enumSocketState);
        }
        catch (IOException e)
        {
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


    /*
    * The packet received may contain several messages, such as 1 MSG, 2 MSG,
    * or even 0.5 MSG, 1.5 MSG.
    */
    public void analysePacket()
    {

    }
    public void analyseMsgHeader()
    {
        int ret;
        Boolean boolRet;
        String  strDirection;
        String  strMsgMoudleFound;
        String  strMsgNameFound;

        if(MyJFrame.PAL_MSG_MAX_NUM < this.intTatalReceivedMsgNum)
        {
            System.out.println("ERROR. intTatalReceivedMsgNum == "+this.intTatalReceivedMsgNum);
            return;
        }

        ret = analyseMsgContentOrMsgId();
        boolRet = hashTableMsg.findMsg(intMsgId);

        if(DATA_TYPE_MSG_SN == ret)
        {

            if(true == boolRet)
            {

                if(hashTableMsg.strMsgDirectionFound.equals("FROM_PAL"))
                {
                    strDirection = new String("----->>");
                }
                else
                {
                    strDirection = new String("<<-----");
                }
                strMsgMoudleFound = hashTableMsg.strMsgMoudleFound;
                strMsgNameFound   = hashTableMsg.strMsgNameFound;
            }
            else
            {
                strDirection = new String("-------");
                strMsgMoudleFound = "UNKNOWN";
                strMsgNameFound   = "UNKNOWN";
            }


            Object[] ObjOneRow=
                {intMsgSn, 0, "PAL", strDirection, strMsgMoudleFound, strMsgNameFound};

            defTableModel.addRow(ObjOneRow);


            /*
            //"MSG_SN","PAL_STATE","PAL","DIRECTION","OTHER_MODULE","MSG_TYPE","CARD_ID"};//column name
            this.jTablePalAllMsg.setValueAt(intMsgSn,   this.intTatalReceivedMsgNum, 0);
            this.jTablePalAllMsg.setValueAt("PAL",      this.intTatalReceivedMsgNum, 2);

            if(true == boolRet)
            {

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
            }
            else
            {
                this.jTablePalAllMsg.setValueAt("?-----?",
                                                this.intTatalReceivedMsgNum, 3);

                this.jTablePalAllMsg.setValueAt("UNKNOWN",
                                                this.intTatalReceivedMsgNum, 4);

                this.jTablePalAllMsg.setValueAt("UNKNOWN",
                                                this.intTatalReceivedMsgNum, 5);
            }

            this.intTatalReceivedMsgNum++;
            */

            /*
            int rowCount = jTablePalAllMsg.getRowCount();
            jTablePalAllMsg.getSelectionModel().setSelectionInterval(rowCount-1, rowCount-1);
            Rectangle rect = jTablePalAllMsg.getCellRect(rowCount-1, 0, true);
            jTablePalAllMsg.scrollRectToVisible(rect);
            */

        }
        else
        {
            if(true == boolRet)
            {
                System.out.println(hashTableMsg.strMsgNameFound);
                ta.append("PS \t<<--- \tPHY: \t"+hashTableMsg.strMsgNameFound+"\r\n");
            }
            else
            {
                System.out.println("UNKNOWN MSG. ");
                ta.append("PS \t<<--- \tPHY: \tUNKNOWN MSG\r\n");
            }

        }
    }



    //merge two byte into one, and return the merged byte.
    public byte[] mergebyte(byte[] byteA, byte[] byteB, int byteBBegin, int byteBEnd)
    {
        byte[] add = new byte[byteA.length + byteBEnd - byteBBegin];
        int i = 0;

        for (i = 0; i < byteA.length; i++)
        {
            add[i] = byteA[i];
        }

        for (int k = byteBBegin; k < byteBEnd; k++, i++)
        {
            add[i] = byteB[k];
        }

        return add;
    }

    public int rcvOneMsg(InputStream is)
    {
        int intRet = 1;
        int count =0;
        bytes = new byte[0];
        byte[] bodyTotal = new byte[0];;

        InputStream reader = is;

        //try
        //{
            //while(reader.available() != 0)
            //while (true)
            while(EnumSocketState.SOCKET_STATE_CONNECTED == MyJFrame.enumSocketState)
            {
                try
                {

                    //the initial bytes.length is 0
                    //After this if, the header length is read.
                    if (bytes.length < PACKET_HEAD_LENGTH)
                    {
                        // the bytes number of the unread part of the header
                        byte[] headLeftPart = new byte[PACKET_HEAD_LENGTH - bytes.length];

                        MyJFrame.writeLog("rcvOneMsg()  before: int couter = reader.read(headLeftPart);");
                        while(reader.available() == 0)
                        {
                        	//STATY HERE. UNTIL COMING DATA.
                        }
                        //counter is the length of the data read actually .
                        int couter = reader.read(headLeftPart);
                        MyJFrame.writeLog("rcvOneMsg() header couter = "+couter);

                        if (couter < 0)
                        {
                            continue;
                        }

                        bytes = mergebyte(bytes, headLeftPart, 0, couter);
                        if (couter < PACKET_HEAD_LENGTH)
                        {
                            continue;
                        }
                    }

                    //
                    //byte[] temp = new byte[0];
                    //temp = mergebyte(temp, bytes, 0, PACKET_HEAD_LENGTH);
                    //String templength = new String(temp);
                    //int bodylength = Integer.parseInt(templength);// PACKET TOTAL LENGTH

                    int bodylength  = byte2Int(bytes);
                    MyJFrame.writeLog("rcvOneMsg() msg bodylength = "+bodylength);


                    if (bytes.length - PACKET_HEAD_LENGTH < bodylength)
                    {// less than a packet length.

                        // the bytes number of the unread part of the packet
                        byte[] bodyPart = new byte[bodylength + PACKET_HEAD_LENGTH - bytes.length];

                        MyJFrame.writeLog("rcvOneMsg()  before: int couterReadThisTime = reader.read(bodyPart);");
                        while(reader.available() == 0)
                        {
                        	//STATY HERE. UNTIL COMING DATA.
                        }
                        //read body length
                        int couterReadThisTime = reader.read(bodyPart);
                        MyJFrame.writeLog("rcvOneMsg() msg couterReadThisTime = "+couterReadThisTime);
                        if (couterReadThisTime < 0)
                        {
                            continue;
                        }

                        //merge header and body together.
                        bytes = mergebyte(bytes, bodyPart, 0, couterReadThisTime);
                        if (couterReadThisTime < bodyPart.length)
                        {
                            continue;
                        }
                    }

                    bodyTotal = new byte[0];
                    bodyTotal = mergebyte(bodyTotal, bytes, PACKET_HEAD_LENGTH, bytes.length);
                    count++;
                    //byteRcv = bytes;

                    //System.out.println(" receive msg: count = " + count+" content: "new String(bodyTotal));
                    System.out.println(" receive msg: bytes.length = " + bytes.length);
                    MyJFrame.writeLog("rcvOneMsg() msg bytes.length = "+bytes.length);
                    byteRcv = bodyTotal;
                    intRet = 0;
                    break;
                }

                catch (Exception e)
                {
                    MyJFrame.writeLog("rcvOneMsg()  Exception e. ;");

                    e.printStackTrace();
                    MyJFrame.enumSocketState = EnumSocketState.SOCKET_STATE_DISCONNECT;
                    intRet = 1;
                }
            }
        /*
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        */
        return intRet;
    }


}

/*
 * packet=packetHead+content
 * read length of the MSG first, then the content of the MSG,
 * If the length of the received data is less than length of the MSG, read.
 */

