
public enum EnumMsgModuleList {
    CPHY_RSSI_MEAS_CNF("RRC"),
    CPHY_SYNC_CNF("MAC"),
    CPHY_MIB_IND("PHY");

    private String strValue;

    private EnumMsgModuleList(String value) {
        this.strValue = value;
    }

    public String getValue() {
        return strValue;
    }
}
