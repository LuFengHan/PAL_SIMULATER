
//0: from PAL;  1: to PAL
public enum EnumMsgDirectionList {
    CPHY_RSSI_MEAS_CNF("FROM_PAL"),
    CPHY_SYNC_CNF("TO_PAL"),
    CPHY_MIB_IND("FROM_PAL");


    private String valueDirection;

    //
    private EnumMsgDirectionList(String value) {
      //0: from PAL;  1: to PAL
      this.valueDirection = value;
    }

    public String getValue() {
      return valueDirection;
    }
}
