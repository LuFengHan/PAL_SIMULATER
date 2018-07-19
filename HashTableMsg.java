import java.util.*;



public class HashTableMsg {

    String strMsgNameFound = "NULL";
    String strMsgMoudleFound = "NULL";
    String strMsgDirectionFound = "NULL";


    final static String strDirectionToPal = "TO_PAL";
    final static String strDirectionFromPal = "FROM_PAL";

    final static int intMasMsgNum = 256;

    static Hashtable<EnumMsgList, String> hashtableMsgName = new Hashtable<EnumMsgList, String>(intMasMsgNum);
    static Hashtable<EnumMsgModuleList, Object> hashtableMsgModule = new Hashtable<EnumMsgModuleList, Object>(intMasMsgNum);
    static Hashtable<EnumMsgDirectionList, Object> hashtableMsgDirection = new Hashtable<EnumMsgDirectionList, Object>(intMasMsgNum); //key is msgId(int)

    public HashTableMsg()
    {
        for ( EnumMsgList enumOneMsg : EnumMsgList.values()) {
            addMsgIdName(enumOneMsg);
        }

        for ( EnumMsgModuleList enumOneMsg : EnumMsgModuleList.values()) {
            addMsgIdModule(enumOneMsg, enumOneMsg.getValue());
        }

        for ( EnumMsgDirectionList enumOneMsg : EnumMsgDirectionList.values()) {
            addMsgIdDirection(enumOneMsg, enumOneMsg.getValue());
        }
    }

    public void addMsgIdName(EnumMsgList enuMsgId)
    {
        this.hashtableMsgName.put(enuMsgId, enuMsgId.toString());
    }

    public void addMsgIdModule(EnumMsgModuleList enuMsgId, Object moduleName)
    {
        this.hashtableMsgModule.put(enuMsgId, moduleName);
    }

    public void addMsgIdDirection(EnumMsgDirectionList enuMsgId, Object direction)
    {
        this.hashtableMsgDirection.put(enuMsgId, direction);
    }


    public Boolean findMsg(int intMsgId)
    {
        Boolean boolFindOrNot = false;

        if( EnumMsgModuleList.values().length <= intMsgId)
        {
            MyJFrame.writeLog("ERR. findMsg() intMsgId = "+intMsgId);
            System.out.println("ERR. findMsg() intMsgId = "+intMsgId);
            return boolFindOrNot;
        }

        EnumMsgModuleList       enMsgIdOfModule = EnumMsgModuleList.values()[intMsgId];
        EnumMsgDirectionList    enMsgIdOfDirect = EnumMsgDirectionList.values()[intMsgId];

        System.out.println("intMsgId = "+intMsgId);
        MyJFrame.writeLog("intMsgId = "+intMsgId);

        strMsgNameFound     =  enMsgIdOfModule.toString();
        strMsgMoudleFound   = (hashtableMsgModule.get(enMsgIdOfModule)).toString();
        strMsgDirectionFound= (hashtableMsgDirection.get(enMsgIdOfDirect)).toString();

        System.out.println("strMsgNameFound = "+strMsgNameFound+" strMsgMoudleFound"+strMsgMoudleFound);
        System.out.println("strMsgDirectionFound = "+strMsgDirectionFound);

        MyJFrame.writeLog("strMsgNameFound = "+strMsgNameFound+" strMsgMoudleFound"+strMsgMoudleFound);
        MyJFrame.writeLog("strMsgDirectionFound = "+strMsgDirectionFound);

        if( (null == strMsgNameFound) ||
            (null == strMsgMoudleFound) ||
            (null == strMsgDirectionFound))
        {
            boolFindOrNot = false;
        }
        else
        {
            boolFindOrNot = true;
        }

        return boolFindOrNot;
    }
}
