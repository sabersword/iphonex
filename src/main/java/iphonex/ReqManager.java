package iphonex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.Vector;

public class ReqManager {
	public Mobile mobile;
	public Vector<IPhoneX> iphonexVec;

	protected volatile int curSendMsgReqNum; //已请求数目
	protected volatile int curSendMsgFinishNum;  //已完成数目
	protected volatile int curSendMsgSucNum; //已成功数目
	protected volatile int maxParaSendMsgReqNum; //最大并发请求

	protected volatile int curLoginReqNum;
	protected volatile int curLoginFinishNum;
	protected volatile int curLoginSucNum;
	protected volatile int maxParaLoginReqNum;

	protected volatile int curBuyReqNum;
	protected volatile int curBuyFinishNum;
	protected volatile int curBuySucNum;
	protected volatile int maxParaBuyReqNum;

	protected volatile String goodsId;
	protected volatile String skuId;

	protected volatile boolean buyState = false;
	protected volatile HashMap<String, Boolean> hasStock;
	public static final int HEART_BEAT_PERIOD = 600000;
	public static final int HEART_BEAT_INTERVAL = 100;

	public ReqManager(Mobile mobile){
		this.mobile = mobile;
		iphonexVec = new Vector<>();
	}
	public synchronized void setGoodsId(String goodsId){
		this.goodsId = goodsId;
	}
	public synchronized void setSkuId(String skuId){
		hasStock = new HashMap<>();
		this.skuId = skuId;
		//动态设置其它skuid
		String prefix = skuId.substring(0, skuId.length() - 3);
		int last3Num = Integer.valueOf(skuId.substring(skuId.length() - 3));
		for (int i = 0; i< 4; i++){
			int cur3Num = last3Num + i;
			String last3Str = String.valueOf(cur3Num);
			while(last3Str.length() < 3){
				last3Str = "0" + last3Str;
			}
			hasStock.put(prefix + last3Str, true);
		}
	}
	public synchronized void startSendMsg(int maxParaReqNum){
		this.maxParaSendMsgReqNum = maxParaReqNum;
		sendMsg(-1, "", "");
	}
	public synchronized void sendMsg(int id, String result, String state){
		if(id >= 0){
			if (state.contains("成功")){
				curSendMsgSucNum ++;
				mobile.updateSendMsgState(curSendMsgSucNum, curSendMsgReqNum);
			}else {
				mobile.addLog(result);
			}
			mobile.updateTableState(id, state);
			curSendMsgFinishNum ++;
		}
		int diff = maxParaSendMsgReqNum - (curSendMsgReqNum - curSendMsgFinishNum);
		while(curSendMsgReqNum < iphonexVec.size() && diff>0){
			this.iphonexVec.get(curSendMsgReqNum).onSendMsg();
			curSendMsgReqNum ++;
			diff--;
		}

	}
	public synchronized void startLogin(int maxParaReqNum){
		this.maxParaLoginReqNum = maxParaReqNum;
		curLoginFinishNum = 0;
		curLoginReqNum = 0;
		curLoginSucNum = 0;
		login(-1, "", "");
		new Timer().schedule(new HeartBeat(this), HEART_BEAT_PERIOD, HEART_BEAT_PERIOD);
	}
	public synchronized void login(int id, String result, String state){
		if(id >= 0){
			if (state.contains("成功")){
				curLoginSucNum ++;
				mobile.updateLoginState(curLoginSucNum, curLoginReqNum);
			}else {
				if(result.contains("SocketTimeoutException") || result.contains("\"resultCode\":\"1\"")){
					//重新登录
					this.iphonexVec.get(id).onLogin();
					mobile.updateTableState(id, "重新登录...");
					return;
				}else {
					mobile.addLog("login:" + result);
				}
			}
			mobile.updateTableState(id, state);
			curLoginFinishNum ++;
		}
		int diff = maxParaLoginReqNum - (curLoginReqNum - curLoginFinishNum);
		while(curLoginReqNum < iphonexVec.size() && diff > 0){
			this.iphonexVec.get(curLoginReqNum).onLogin();
			curLoginReqNum ++;
			diff --;
		}

	}
	public synchronized void startBuy(int maxParaReqNum, String goodsId, String skuId, int mode){
		this.maxParaBuyReqNum = maxParaReqNum;
		setGoodsId(goodsId);
		setSkuId(skuId);
		String result = "";
		if (mode == 2){
		    result = "STOCK_ZERO";
        }
		buy(-1, result, "");
	}


	public synchronized void buy(int id, String result, String state){
	    if (result.equals("STOCK_ZERO")){
	        try {
                Thread.sleep(200);
            }catch (Exception e){

            }
            this.iphonexVec.get(0).onGetStock(goodsId, skuId);
            return;
        }
        if (result.equals("STOCK_ACTIVE")){
            id = -1;  //开始探测
        }

		int tmpBuyReqNum = maxParaBuyReqNum;
		if(id >= 0){
			if (state.contains("成功")){
				curBuySucNum ++;
				mobile.updateBuyState(curBuySucNum, curBuyReqNum);
				mobile.addResult(result);
				if(!buyState){//有一个购买成功则正常开启全部线程
					buyState = true;
				}
			}else {//购买失败
				String code = Utils.getValue(result, "\"code\":", ",").trim();
				if (buyState){//已经进入购买状态
					if(code.equals("9000")){//若该skuId无货，设置无货标记。
						String currentSkuId = this.iphonexVec.get(id).skuId;
						hasStock.put(currentSkuId, false);
					}
				}

				//试探阶段只用一个账号，且当监测当已经登录有效时，重复尝试购买，否则用下一个帐号
				if ((! buyState) && result.contains("BUY ERROR")) {
					tmpBuyReqNum = 1;
					if (! code.equals("2") && !result.contains("login")) {//登录有效重复购买，否则会启动下一个账号
						this.iphonexVec.get(id).onBuy(goodsId, skuId);
						mobile.updateTableState(id, "重新购买...");
						try {
							Thread.sleep(200);//0.5s
						}catch (Exception e){

						}
						return;
					}
				}

				mobile.addLog("buy:" + result);
			}
			mobile.updateTableState(id, state);
			curBuyFinishNum ++;
		}

		if (id == -1){//刚点击购买，用一个试探线程
			tmpBuyReqNum = 1;
		}
		int diff = tmpBuyReqNum - (curBuyReqNum - curBuyFinishNum);
		ArrayList<String> hasStockList = new ArrayList<>();
		for (HashMap.Entry<String, Boolean> entry : hasStock.entrySet()) {
			if (entry.getValue()){
				hasStockList.add(entry.getKey());
			}
		}
		int hasStockLen = hasStockList.size();
		if(hasStockLen == 0)return;
		while(curBuyReqNum < iphonexVec.size() && diff > 0){
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
