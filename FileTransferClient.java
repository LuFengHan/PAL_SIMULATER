import java.io.*;
import java.net.Socket;

import javax.swing.JTextArea;

/**
 * @FILENAME: FileTransferClient
 * @brief: used to send a MSG from the socket.
 * @auther: LUFENG.HAN
 * @date:
 * @version: 1.0
 */
public class FileTransferClient /*extends Socket */{

    //private static final String SERVER_IP = "127.0.0.1";
    //private static final int SERVER_PORT = 5555;


    private Socket clientSocket;
    private FileInputStream fis;
    private DataOutputStream dos;
    JTextArea taLog;

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

    static final int FILE_HEAD_LOCATION = 0;
    static final int BYTE_LEN = 2048;
    static final int SOCKET_MSG_HEADER_LEN = 8;
    byte[] bytes = new byte[BYTE_LEN]; // used to buffer the bytes sent from the socket.
    int length = 0;
    long progress = 0;
    int fileReadTimes = 0;
    int fileReadLen = 0;
    int byteNumShouldBeSent = 0;
    String msgFileLocation;
    String msgFileName;
    String strPreviousFileName;

    //File file = new File("d:\\PS_PHY_POWER_SWEEP_REQ");
    File file;

    /**
    *****************************************************************************
    *******************************************************************************/
    public FileTransferClient(Socket clientSocket, JTextArea taLog)
    {
        this.clientSocket = clientSocket;
        this.taLog = taLog;
        this.strPreviousFileName = new String("NO_FILE");
        try
        {
            //write from dos into socket.
            this.dos = new DataOutputStream(clientSocket.getOutputStream());
            if(null != this.dos)
            {
                System.out.println("FileTransferClient, dos is NOT null.");
            }
            else
            {
                System.out.println("FileTransferClient, dos is null.");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
    *****************************************************************************
    *******************************************************************************/
    public void fileTransferClientSetFilename(String msgFileName)
    {
        this.msgFileName = msgFileName;
        //this.msgFileLocation = "d:\\" + msgFileName;
        this.msgFileLocation = System.getProperty("user.dir") +"\\"+ msgFileName;
        System.out.println("SEND MSG FILE: \t" + this.msgFileLocation);


        File file = new File(this.msgFileLocation);

        if(file.exists())
        {
            // next, will read from file into fis.
            try
            {
                fis = new FileInputStream(file);
            }
            catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
    *****************************************************************************
    *******************************************************************************/
    /*
    public void readFileIntoBuf()
    {
        try
        {
            while((length = fis.read(bytes, 0, bytes.length)) != -1)
            {  }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    */
    /**
    *****************************************************************************
     * @throws Exception
    *******************************************************************************/
    public void sendFile(String msgFileName) throws Exception
    {


        try
        {
            //File file = new File("d:\\PAL_MSG_POWER_SEEP_REQ_1");
            //if(file.exists())
            if(null != dos)
            {
                System.out.println("======== START SENDING FILE. ========");

                /*
                If the first time to read file, you should read from the file into the buffer.*/
                //if(0 == fileReadTimes)
                if(!strPreviousFileName.equals(msgFileName))
                {
                	strPreviousFileName = msgFileName;
                    prepareMsgHeader(msgFileName);
                    changeAsicCodeFileToValueBytes();
                    fileReadTimes = 1;
                }

                //write into DataOutputStream, dos, which will transfer the data into socket.
                dos.write(bytes, FILE_HEAD_LOCATION, fileReadLen);
                dos.flush();

                taLog.append("PS \t--->> \tPHY:\t"+msgFileName+"\r\n");
                //taLog.append(msgFileName+"\r\n");

                System.out.println("PS-->PHY: \tPAL_MSG_POWER_SEEP_REQ");
                System.out.println("======== MSG SEND OVER.========");
            }
            else
            {
                taLog.append("SOCKET ERR. Pleas check it first.\r\n");
                System.out.println("ERROR. null == dos");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
    *****************************************************************************
     * @throws Exception
    *******************************************************************************/
    public void closeFile() throws Exception {

        System.out.println("CLOSE FILE.");
        if(fis != null)
            fis.close();

        if(dos != null)
            dos.close();
    }

    /**
    *****************************************************************************
     * @
    *******************************************************************************/
    public void prepareMsgHeader(String msgFileName)
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
         * -------------------------------------------------------------------
         * */
        bytes[0] = 0;//CARD ID = 0
        bytes[1] = 0;//CARD ID = 0
        bytes[2] = 0X55;//isInDDRFlag = 0X5555
        bytes[3] = 0X55;//isInDDRFlag = 0X5555
        if(msgFileName.equals(new String("PS_PHY_POWER_SWEEP_REQ")))
        {
            bytes[4] = 0x70;//0x00000070 is POWER_SWEEP_REQ
            bytes[5] = 0x00;
            bytes[6] = 0x00;
            bytes[7] = 0x00;
        }
        else if(msgFileName.equals("CPHY_BCCH_CONFIG_REQ_MIB"))
        {
            bytes[4] = 0x13;//0x00000013 is CPHY_BCCH_CONFIG_REQ
            bytes[5] = 0x00;
            bytes[6] = 0x00;
            bytes[7] = 0x00;        	
        }
        else
        {
        	System.out.println("ERR FILE NAME.");
        }

    }

    /**
    *****************************************************************************
     * @throws Exception
    *******************************************************************************/
    public void changeAsicCodeFileToValueBytes()
    {
        int  xUtf8OneByte = 0;
        int  xUtf8OneByteValue = 0;
        int  xOneMsgByte = 0;
        int  xValidflag = 0; //

        fileReadLen = SOCKET_MSG_HEADER_LEN;
        try
        {
            //*!read one byte from the MSG file.
            while((xUtf8OneByte = fis.read()) != -1)
            {
                //*! Compute a continuous NUMBER from its ASCI format.
                //   For example: if one MSG BYTE is 0x30, because the file is saved as
                //   ASCII code, the file content is : 51 and 48.
                //   So, when reading from the MSG FILE, this FUN convert the continuous
                //   NUMBER BYTE into one NUMBER.
                if(xUtf8OneByte >= 0x30 && xUtf8OneByte <= 0x39)
                {
                    xUtf8OneByteValue = xUtf8OneByte - 0x30;
                    xOneMsgByte = xOneMsgByte *16 + xUtf8OneByteValue;
                    xValidflag = 1;
                }
                else if(xUtf8OneByte >= 65 && xUtf8OneByte <= 70)
                {
                    xUtf8OneByteValue = xUtf8OneByte - 55;
                    xOneMsgByte = xOneMsgByte *16 + xUtf8OneByteValue;
                    xValidflag = 1;
                }
                else if(xUtf8OneByte >= 97 && xUtf8OneByte <= 102)
                {
                    xUtf8OneByteValue = xUtf8OneByte - 87;
                    xOneMsgByte = xOneMsgByte *16 + xUtf8OneByteValue;
                    xValidflag = 1;
                }
                else
                {
                    if(1 == xValidflag)
                    {
                        // When confronting a none-number byte, this part change the proceeding
                        // continuous BYTES into one number.
                        bytes[fileReadLen] = (byte)xOneMsgByte;
                        fileReadLen += 1;
                        if(BYTE_LEN <= fileReadLen)
                        {   //ERROR.
                            System.out.println("ERROR. FILE IS BIGGER THAN 2048 bytes.");
                            break;
                        }
                        xOneMsgByte = 0;
                        xValidflag = 0;
                    }
                }
            }

            //
            if(1 == xValidflag)
            {
                //
                bytes[fileReadLen] = (byte)xOneMsgByte;
                fileReadLen += 1;
                if(BYTE_LEN <= fileReadLen)
                {   //
                    System.out.println("ERROR. FILE IS BIGGER THAN 2048 bytes.");
                }
                xOneMsgByte = 0;
                xValidflag = 0;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }
}
