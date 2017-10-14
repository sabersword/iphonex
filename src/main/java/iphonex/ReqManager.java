package iphonex;

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

	public ReqManager(Mobile mobile){
		this.mobile = mobile;
		iphonexVec = new Vector<>();
	}
	public synchronized void setGoodsId(String goodsId){
		this.goodsId = goodsId;
	}
	public synchronized void setSkuId(String skuId){
		this.skuId = skuId;
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
				buyState = true; //有一个购买成功则正常开启全部线程
			}else {
				//在爬虫购买环节异常时，只开一个线程
				//进一步分析当已经登录有效时，重复尝试购买
				if ((! buyState) && result.contains("BUY ERROR")) {
					tmpBuyReqNum = 1;
					String code = Utils.getValue(result, "\"code\":", ",").trim();
					if (! code.equals("2")) {//登录有效
						this.iphonexVec.get(id).onBuy(goodsId, skuId);
						mobile.updateTableState(id, "重新购买...");
						try {
							Thread.sleep(5000);
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
		//用一个试探线程
		if (id == -1){
			tmpBuyReqNum = 1;
		}
		int diff = tmpBuyReqNum - (curBuyReqNum - curBuyFinishNum);
		while(curBuyReqNum < iphonexVec.size() && diff > 0){
			this.iphonexVec.get(curBuyReqNum).onBuy(goodsId, skuId);
			curBuyReqNum ++;
			diff --;
		}

	}

}
