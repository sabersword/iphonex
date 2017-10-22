package iphonex;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.net.ssl.*;

import okhttp3.*;

public class InternetBuy extends IPhoneX{

    private OkHttpClient client;
    private OkHttpClient okHttpClient;
    private HashMap<String, String> cookieMap;
    private String cartCode;
    private String addressId;
    private String TransactionID;
    private String uid;
    private String merchantId;
    private String targetChannelID;
    private String codePath;
    private String captcha;
    private String artifact;
    private final String backUrl = "http%3A%2F%2Fshop.10086.cn%2Fmall_200_200.html%3Fforcelogin%3D1";
    private final String refererUrl = "https://login.10086.cn/html/login/login.html?channelID=12002&backUrl=" + backUrl;
    private final String userAgent = "Mozilla/5.0 (Linux; Android 6.0.1; SM-C7000 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36 leadeon/4.0.0";
    private String skuId = "1040095";
    private String goodsId = "1045210";
    private final String goodsUrl = "http://shop.10086.cn/goods/200_200_"+ goodsId+"_"+skuId+".html";
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();



    public InternetBuy(ReqManager reqManager, int id, String[] elementArr){
        super(reqManager, id, elementArr);
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.sslSocketFactory(createSSLSocketFactory());
        mBuilder.hostnameVerifier(new InternetBuy.TrustAllHostnameVerifier());
        client = mBuilder.build();
        cookieMap = new HashMap<>();
        mBuilder.cookieJar(new CookieJar() {

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
                for(Cookie cookie:cookies){
                    String str = cookie.toString();
                    String[] arr = str.split(";");
                    System.out.println("str:" + str);

                    for (String item: arr) {
                        String[] itemArr = item.split("=");
                        if(itemArr.length == 2) {
                            cookieMap.put(itemArr[0], itemArr[1]);
                        }
                    }
                }
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });
        okHttpClient = mBuilder.build();
        codePath = "img/" + String.valueOf(id) + ".jpg";
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }

    /**
     * 默认信任所有的证书
     * TODO 最好加上证书认证，主流App都有自己的证书
     *
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory() {

        SSLSocketFactory sSLSocketFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new InternetBuy.TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return sSLSocketFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    @Override
    public void onLogin(){
        ssocheck();
    }
    @Override
    public void onBuy(String goodsId, String skuId){
        this.goodsId = goodsId;
        this.skuId = skuId;
        buyBuy();
    }
    public void ssocheck(){
        String url = "https://login.10086.cn/SSOCheck.action?channelID=12002&backUrl=" + backUrl;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                response.close();
                getCheckArtifact();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }
    public void getLogin() {
        Request request = new Request.Builder()
                .url(refererUrl)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
                getCaptcha();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }
    
    public void getCaptcha() {
        String url = "https://login.10086.cn/captchazh.htm?type=12&timestamp=" + String.valueOf(System.currentTimeMillis());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", refererUrl)
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                BufferedImage bi = ImageIO.read(response.body().byteStream());
                ImageIO.write(bi, "png", new File(codePath));
                int width = bi.getWidth();
                int height = bi.getHeight();
                response.close();
                if (width == 200 && height == 50) {
                    if(Mobile.damaPlatForm == 0) {
                        dama();
                    }else {
                        yunDama();
                    }
                }else{
                    getCaptcha();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }
    public void dama() {
        Call call = client.newCall(LZDama.getDamaRequest(codePath));
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                System.out.println("dama: " + result);
                response.close();
                if (result.contains("\"result\":true")) {
                    captcha = Utils.getValue(result, "val\":\"", "\"");
                    System.out.println("captcha:" + captcha);
                    verifyCaptcha();
                } else {
                    onLoginFail(cellNum + ":" + result);
                }
            }
        });
    }
    public void yunDama() {
        Call call = client.newCall(YUNDama.getDamaRequest(codePath));
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                System.out.println("yunDama: " + result);
                response.close();
                if (result.contains("\"ret\":0")) {
                    String cid = Utils.getValue(result, "cid\":", ",");
                    System.out.println("cid:" + cid);
                    try {
                        Thread.sleep(3000);
                        getYunDamaResult(cid, 10);  //需要等待一段时间才有结果,最长等待时间30s
                    }catch (Exception e){
                        onLoginFail(cellNum + ":" + result);
                    }
                } else {
                    onLoginFail(cellNum + ":" + result);
                }
            }
        });
    }
    public void getYunDamaResult(final String cid, final int leftTryCount) {
        Request request = new Request.Builder()
                .url("http://api.yundama.com/api.php?method=result&cid=" + cid)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                response.close();
                captcha = Utils.getValue(result, "\"text\":\"", "\"");
                System.out.println("captcha: " + captcha);
                if(!captcha.equals("")) {
                    verifyCaptcha();
                }else{
                    if(result.contains("-3002") && leftTryCount > 1){
                        try {
                            Thread.sleep(3000);
                        }catch (Exception e){

                        }
                        getYunDamaResult(cid, leftTryCount - 1);
                    }else {
                        onLoginFail(cellNum + ":" + result);
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());

            }
        });
    }

    public void verifyCaptcha() {
        Request request = new Request.Builder()
                .url("https://login.10086.cn/verifyCaptcha?inputCode=" + captcha)
                .addHeader("Cookie", getReqCookie())
                .addHeader("Referer", refererUrl)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                response.close();
                if(result.contains("resultCode\":\"0\"")) {
                    login();
                }else{
                    onLoginFail(cellNum + ":" + result);
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());

            }
        });
    }
    
    public void login() {
        String account = "";
        String password = "";
        try {
            account = URLEncoder.encode(cellNum, "utf-8");
            password = URLEncoder.encode(cellNumEnc, "utf-8");
        }catch (UnsupportedEncodingException e){

        }
        Request request = new Request.Builder()
                .url("https://login.10086.cn/login.htm?accountType=02&account=" + account +"&password=" +password+"&pwdType=03&smsPwd=&backUrl="+backUrl+"&rememberMe=0&channelID=12002&protocol=https%3A&timestamp=" + String.valueOf(System.currentTimeMillis()) + "&inputCode=" + this.captcha)
                .addHeader("Cookie", getReqCookie())
                .addHeader("Referer", refererUrl)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                response.close();
                artifact = Utils.getValue(result,"artifact\":\"", "\"");
                uid = Utils.getValue(result,"uid\":\"", "\"");
                System.out.println("artifact=" + artifact + ",uid=" + uid);
                if (! artifact.equals("")){
                    getArtifact();
                }else{
                    onLoginFail(cellNum + ":" + result);
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }
    
    public void getArtifact() {
        String url = "http://search.10086.cn/shop/acceptArtifact?backUrl=http%3A%2F%2Fsearch.10086.cn%2Fshop%2Flist%3Fkey%3DiPhone%2B8%26cityId%3D200%26provinceId%3D200%26nh%3D1&artifact=" + artifact;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
                .build();
        Call call= okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                response.close();
                userinfo();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }
    public void getCheckArtifact() {
        String url = "http://shop.10086.cn/sso/getartifact.php?artifact=-1&backUrl=" + backUrl;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                response.close();
                getLogin();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }

    public void getstock(){
        String url = "http://shop.10086.cn/ajax/detail/getstock.json?goods_id="+goodsId+"&merchant_id="+merchantId+"&sale_type=1&sku_id="+skuId;
        System.out.println("cookie:" + getReqCookie());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                onBuyFail(cellNum + " " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                response.close();
                System.out.println("getstock:" + result);
                cookieStore.get("shop.10086.cn");
                userinfo();
            }
        });
    }
    public void userinfo(){
        String url = "http://shop.10086.cn/ajax/user/userinfo.json?province_id=200&city_id=200";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                System.out.println("userinfo:" + result);
                response.close();
                sosocheck2();
            }
        });
    }
    private void sosocheck2(){
        String url = "https://login.10086.cn/SSOCheck.action?channelID=12002&backUrl=http%3A%2F%2Fshop.10086.cn%2Fsso%2Fminilogincallback.php";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                System.out.println("ssocheck2:" + result);
                response.close();
                if (result.contains(cellNum)){
                    onLoginSuccess(cellNum);
                }else{
                    onLoginSuccess(cellNum + ":" + "ssocheck2 error");
                }
            }
        });


    }
    public void buyBuy(){
        String url = "http://shop.10086.cn/ajax/buy/buy.json";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "sku%5B0%5D%5BModelId%5D="+skuId+"&sku%5B0%5D%5BGoodsId%5D="+goodsId+"&sku%5B0%5D%5BNum%5D=1&sku%5B0%5D%5BGoodsType%5D=70000&sku%5B0%5D%5BChannel%5D=1&sku%5B0%5D%5BProvinceId%5D=200&sku%5B0%5D%5BCityId%5D=200";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onBuyFail(cellNum + "BUY ERROR:" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                cartCode = Utils.getValue(result, "cart_code=", "\"");
//                System.out.println("cart_code:" + cartCode);
                response.close();
                if (! cartCode.equals("")){
                    checkOrder();
                }else{
                    onBuyFail(cellNum + "BUY ERROR:" + result);
                }
            }
        });
    }
    
    /**
     * 
    * @Title: heartBeatBuy 
    * @Description: 发送购买心跳防止session过期,与buybuy()区分,避免cartcode影响
    * @param    参数
    * @return void    返回类型 
    * @throws
     */
    @Override
    public void onHeartBeatBuy(String goodsId, String skuId){
        String url = "http://shop.10086.cn/ajax/buy/buy.json";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "sku%5B0%5D%5BModelId%5D="+skuId+"&sku%5B0%5D%5BGoodsId%5D="+goodsId+"&sku%5B0%5D%5BNum%5D=1&sku%5B0%5D%5BGoodsType%5D=70000&sku%5B0%5D%5BChannel%5D=1&sku%5B0%5D%5BProvinceId%5D=200&sku%5B0%5D%5BCityId%5D=200";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                System.out.println(result);
                response.close();
            }
        });
    }
    
    public void checkOrder(){
        String url = "http://shop.10086.cn/order/checkorder.php?";
        url += "cart_code=" + cartCode;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onBuyFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                addressId = Utils.getValue(result, "address_id\" value=\"", "\"");
                System.out.println("addressId:" + addressId);
                response.close();
                if(! addressId.equals("")) {
                    submitOrder();
                }else{
                    onBuyFail(cellNum + ":" + "address is null");
                }
            }
        });
    }
    public void submitOrder(){
        String url = "http://shop.10086.cn/ajax/submitorder/addorder.json";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "address_id=" + addressId;
        data += "&invoice_id=personal&pay_type=1&coupon_type=&eticket_number_mall=";
        data += "&cart_code=" + cartCode;
        data += "&ticket_no=";
        data += "&cart_code=" + cartCode;
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", "http://shop.10086.cn/order/checkorder.php?cart_code=" + cartCode)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onBuyFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                System.out.println("submit order: " + result );
                response.close();
                if (result.contains("topay")){
                    onBuySuccess(cellNum + "," + cellNumEnc);
                }else{
                    onBuyFail(cellNum + "" + result);
                }
            }
        });
    }
    public void addRspCookie(List<String> cookies){
        if(cookies.equals(null)){
            return;
        }
        for(String cookieLine: cookies){
            String[] cookieArr = cookieLine.split(";");
            for(String cookie: cookieArr){
//                System.out.println(cookie);
                String[] arr = cookie.split("=");
                if (arr.length == 2) {
                    cookieMap.put(arr[0], arr[1]);
                }
            }
        }
    }
    
    public String getReqCookie(){
        String cookie = "";
        for (String key: cookieMap.keySet()){
            cookie += key + "=" + cookieMap.get(key) + ";";
        }
        return cookie;

    }

    public static void main(String[] args) throws Exception {
        ReqManager reqManager = new ReqManager(new Mobile());
        String[] elementArr = {"avefdo04921@chacuo.net", "abcd1234"};
        InternetBuy internetBuy = new InternetBuy(reqManager,0,elementArr);
        internetBuy.onLogin();
        internetBuy.onBuy("1045210", "1040095");
    }

}
