package iphonex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.Vector;

public class ReqManager {
	public Mobile mobile;
	public Vector<IPhoneX> iphonexVec;

	private volatile int curSendMsgReqNum; //已请求数目
	private volatile int curSendMsgFinishNum;  //已完成数目
	private volatile int curSendMsgSucNum; //已成功数目
	private volatile int maxParaSendMsgReqNum; //最大并发请求

	private volatile int curLoginReqNum;
	private volatile int curLoginFinishNum;
	private volatile int curLoginSucNum;
	private volatile int maxParaLoginReqNum;

	private volatile int curBuyReqNum;
	private volatile int curBuyFinishNum;
	private volatile int curBuySucNum;
	private volatile int maxParaBuyReqNum;

	private volatile String goodsId;
	private volatile String skuId;

	private volatile boolean buyState = false;
	private volatile HashMap<String, Boolean> hasStock;
	private static final int heartBeatPeriod = 300000;

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
		new Timer().schedule(new HeartBeat(this), heartBeatPeriod, heartBeatPeriod);
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
	public synchronized void startBuy(int maxParaReqNum, String goodsId, String skuId){
		this.maxParaBuyReqNum = maxParaReqNum;
		setGoodsId(goodsId);
		setSkuId(skuId);
		buy(-1, "", "");
	}

	public synchronized void buy(int id, String result, String state){
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
					if (!code.equals("2") && ! result.contains("login")){	// 只要不提示未登录，则选一个有货的skuId重复购买。（提前确保地址有效，对于超时等异常重复购买）
						String currentSkuId = "";
						for (HashMap.Entry<String, Boolean> entry : hasStock.entrySet()) {
							if (entry.getValue()){
								currentSkuId = entry.getKey();
								break;
							}
						}
						String currentStatus = "商品已无货";
						if(!currentSkuId.equals("")){
							this.iphonexVec.get(id).onBuy(goodsId, currentSkuId);
							currentStatus = "重新购买...";
						}
						mobile.updateTableState(id, currentStatus);
						return;
					}
				}


				//试探阶段只用一个账号，且当监测当已经登录有效时，重复尝试购买，否则用下一个帐号
				if ((! buyState) && result.contains("BUY ERROR")) {
					tmpBuyReqNum = 1;
					if (! code.equals("2")) {//登录有效重复购买，否则会启动下一个账号
						this.iphonexVec.get(id).onBuy(goodsId, skuId);
						mobile.updateTableState(id, "重新购买...");
						try {
							Thread.sleep(500);//0.5s
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
