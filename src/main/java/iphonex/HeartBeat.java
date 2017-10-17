package iphonex;

import java.util.TimerTask;

public class HeartBeat extends TimerTask {

    private ReqManager reqManager;
    
    public HeartBeat(ReqManager reqManager) {
        this.reqManager = reqManager;
    }
    
    /**
     * 错误的goodsid用作心跳
     */
    @Override
    public void run() {
        for (int i = 0; i < reqManager.iphonexVec.size(); i++) {
            reqManager.iphonexVec.get(i).onHeartBeatBuy("1045219", "1040095");
        }
    }

}
