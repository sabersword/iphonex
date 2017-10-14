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

	public ReqManager(Mobile mobile){
		this.mobile = mobile;
		iphonexVec = new Vector<>();
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
		curBuyFinishNum = 0;
		curBuyReqNum = 0;
		curBuySucNum = 0;
		buy(-1, "", "", goodsId, skuId);
	}

	public synchronized void buy(int id, String result, String state, String goodsId, String skuId){
		if(id >= 0){
			if (state.contains("成功")){
				curBuySucNum ++;
				mobile.updateBuyState(curBuySucNum, curBuyReqNum);
				if (!result.equals("")){//为空为重复提交，不写入
					mobile.addResult(result);
				}
			}else {
				mobile.addLog("buy:" + result);
			}
			mobile.updateTableState(id, state);
			curBuyFinishNum ++;
		}
		int diff = maxParaBuyReqNum - (curBuyReqNum - curBuyFinishNum);
		while(curBuyReqNum < iphonexVec.size() && diff > 0){
			this.iphonexVec.get(curBuyReqNum).onBuy(goodsId, skuId);
			curBuyReqNum ++;
			diff --;
		}

	}

}
