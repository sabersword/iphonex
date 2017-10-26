package iphonex;

public class ViolentReqManager extends ReqManager {

    public ViolentReqManager(Mobile mobile) {
        super(mobile);
    }
    
    @Override
    public synchronized void buy(int id, String result, String state){
        if(id >= 0) {
            if (state.contains("成功")) {
                curBuySucNum ++;
                mobile.updateBuyState(curBuySucNum, curBuyReqNum);
                mobile.addResult(result);
                curBuyFinishNum ++;
            } else {//购买失败, 暴力购买
                System.out.println("购买失败id=" + id);
                this.iphonexVec.get(id).onBuy(goodsId, skuId);
            }
            mobile.updateTableState(id, state);
        }

        int diff = maxParaBuyReqNum - (curBuyReqNum - curBuyFinishNum);
        while(curBuyReqNum < iphonexVec.size() && diff > 0){
            this.iphonexVec.get(curBuyReqNum).onBuy(goodsId, skuId);
            curBuyReqNum ++;
            diff --;
        }

    }

}
