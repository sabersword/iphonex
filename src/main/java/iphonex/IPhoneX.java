package iphonex;


public class IPhoneX {
    protected ReqManager reqManager;
    public final int id;

    public String cellNum;
    public String cellNumEnc;
    public String provinceCode;
    public String cityCode;
    public String regionCode;
    public String street;
    public String recName;
    public String recPhoneNo;

    public String goodsId;
    public String skuId;

    public IPhoneX(ReqManager reqManager, int id, String[] elementArr){
        this.reqManager = reqManager;
        this.id = id;
        if (elementArr.length >= 2) {
            this.cellNum = elementArr[0];
            this.cellNumEnc = elementArr[1];
        }
        if (elementArr.length == 8){
            this.provinceCode = elementArr[2];
            this.cityCode = elementArr[3];
            this.regionCode = elementArr[4];
            this.recName = elementArr[5];
            this.recPhoneNo = elementArr[6];
            this.street = elementArr[7];
        }
    }
    public  void onSendMsg(){
    }
    public void onLogin(){
    }
    public void onBuy(String goodsId, String skuId){
    }
    public void onHeartBeatBuy(String goodsId, String skuId) {
    }
    public void onGetStock(String goodsId,String skuId){
    }
    public void onSendMsgFail(String msg){
        reqManager.sendMsg(id, msg, "发送验证码失败");
    }
    public void onSendMsgSuccess(String msg){
        reqManager.sendMsg(id, msg, "发送验证码成功");
    }
    public void onLoginFail(String msg){
        reqManager.login(id, msg, "登录失败");
    }
    public void onLoginSuccess(String msg){
        reqManager.login(id, msg, "登录成功");
    }
    public void onBuyFail(String msg){
        reqManager.buy(id, msg, "购买失败");
    }
    public void onBuySuccess(String msg){
        reqManager.buy(id, msg, "购买成功");
    }
    public String getCellNum(){
        return cellNum;
    }
    public String getCellNumEnc(){
        return cellNum;
    }

}
