package iphonex;

import java.util.ArrayList;
import java.util.HashMap;

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
                if(!buyState){//有一个购买成功则正常开启全部线程
                    buyState = true;
                }
            } else {//购买失败, 暴力购买
                String code = Utils.getValue(result, "\"code\":", ",").trim();
                if(buyState && code.equals("9000")){//若该skuId无货，设置无货标记。
                    String currentSkuId = this.iphonexVec.get(id).skuId;
                    hasStock.put(currentSkuId, false);
                }
                System.out.println("购买失败id=" + id);
                if (! buyState) {
                    this.iphonexVec.get(id).onBuy(goodsId, skuId);
                    return;
                }
                curBuyFinishNum ++;
            }
            mobile.updateTableState(id, state);
        }

        ArrayList<String> hasStockList = new ArrayList<>();
        for (HashMap.Entry<String, Boolean> entry : hasStock.entrySet()) {
            if (entry.getValue()){
                hasStockList.add(entry.getKey());
            }
        }
        int hasStockLen = hasStockList.size();
        if(hasStockLen == 0)return;

        int diff = maxParaBuyReqNum - (curBuyReqNum - curBuyFinishNum);
        while(curBuyReqNum < iphonexVec.size() && diff > 0){
            this.iphonexVec.get(curBuyReqNum).onBuy(goodsId, skuId);
            //均匀分布购买的skuId
            int index = curBuyReqNum % hasStockLen;
            if (!buyState){//试探阶段，用默认的skuId
                index = 0;
            }
            this.iphonexVec.get(curBuyReqNum).onBuy(goodsId, hasStockList.get(index));
            curBuyReqNum ++;
            diff --;
        }

    }

}
