package iphonex;

public class ViolentInternetBuy extends InternetBuy {

    private static final int VIOLENT_SLEEP = 2000;
    
    public ViolentInternetBuy(ReqManager reqManager, int id, String[] elementArr) {
        super(reqManager, id, elementArr);
    }
    
    @Override
    public void onBuyFail(String msg){
        super.onBuyFail(msg);
        try {
            Thread.sleep(VIOLENT_SLEEP + (long)(Math.random() * 1000));    //暴力购买间隔,随机数为了各个手机号购买时间错开,放在这里不会影响原有逻辑
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
