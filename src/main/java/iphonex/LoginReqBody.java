package iphonex;

public class LoginReqBody extends MsgReqBody {

    private String imei = "864803032732671";
    private String sendSmsFlag = "1";
    private String verifyCode = "";
    
    public String getImei() {
        return imei;
    }
    public void setImei(String imei) {
        this.imei = imei;
    }
    public String getSendSmsFlag() {
        return sendSmsFlag;
    }
    public void setSendSmsFlag(String sendSmsFlag) {
        this.sendSmsFlag = sendSmsFlag;
    }
    public String getVerifyCode() {
        return verifyCode;
    }
    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
    
}


