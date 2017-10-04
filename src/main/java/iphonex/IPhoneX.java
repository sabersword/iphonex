package iphonex;

import okhttp3.*;

import java.io.IOException;

public class IPhoneX {
    private String cellNum;
    private String cellNumEnc;
    private ReqManager reqManager;
    private final int id;
    private OkHttpClient okHttpClient;
    public String getCellNum(){
        return cellNum;}
    public String getCellNumEnc(){
        return cellNumEnc;
    }

    public IPhoneX(ReqManager reqManager, int id, String cellNum, String cellNumEnc){
        this.reqManager = reqManager;
        this.id = id;
        this.cellNum = cellNum;
        this.cellNumEnc = cellNumEnc;
        okHttpClient = new OkHttpClient();
    }

    public void sendMsg(){
        Request request = new Request.Builder()
                .url("https://www.baidu.com")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {

            public void onResponse(Call call, Response response) throws IOException {
                reqManager.sendMsg(id, "", "发送验证码成功");
            }

            public void onFailure(Call call, IOException e) {
                reqManager.login(id, e.toString(), "发送验证码失败");

            }
        });
    }
    public void login(){
        Request request = new Request.Builder()
                .url("https://www.baidu.com")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {

            public void onResponse(Call call, Response response) throws IOException {
                reqManager.login(id, "","登录成功");
            }

            public void onFailure(Call call, IOException e) {
                reqManager.login(id, e.toString(), "登录失败");

            }
        });
    }
    public void buy(){
        Request request = new Request.Builder()
                .url("https://www.baidu.com")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {

            public void onResponse(Call call, Response response) throws IOException {
                reqManager.buy(id, "", "购买成功");
            }

            public void onFailure(Call call, IOException e) {
                reqManager.buy(id, e.toString(), "购买失败");
            }
        });
    }


}
