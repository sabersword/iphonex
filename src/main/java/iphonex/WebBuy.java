package iphonex;


import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.*;
import org.apache.commons.lang.StringUtils;

import javax.net.ssl.*;

public class WebBuy {
    private String cookie;
    private HashMap<String,String> cookieMap;
    private String cellNum;
    private String cellNumEnc;
    private String verifyCode;
    private String cartCode;
    private String addressId;
    private String artifact;
    private OkHttpClient okHttpClient;
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private final String goodsUrl = "http://touch.10086.cn/goods/200_200_1045210_1040095.html?WT.ac=iphoneT_iphone8";
    private final String loginTouchUrl = "https://login.10086.cn/html/login/touch.html?channelID=12012&backUrl=http%3A%2F%2Ftouch.10086.cn%2Fgoods%2F200_200_1045210_1040095.html%3FWT.ac%3DiphoneT_iphone8%3Fforcelogin%3D1";

    public WebBuy(String cellNum) {
        this.cellNum = cellNum;
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.sslSocketFactory(createSSLSocketFactory());
        mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
//        mBuilder.cookieJar(new CookieJar() {
//            @Override
//            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
//                if (httpUrl.url().toString().contains("login.htm?accountType=0")) {
//                    cookieStore.put(httpUrl.host(), list);
//                }
//            }
//
//            @Override
//            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
//                List<Cookie> cookies = cookieStore.get("login.10086.cn");  //httpUrl.host()
//                // 注意登录和购买不是同一个host
////                List<Cookie> loginCookies = cookieStore.get("login.10086.cn");
////                if (cookies == null){cookies = new ArrayList<Cookie>(); }
////                if(loginCookies != null && !cookies.equals(loginCookies)) {
////                    cookies.addAll(loginCookies);
////                }
//                return cookies != null ? cookies : new ArrayList<Cookie>();
//            }
//        });
        okHttpClient = mBuilder.build();
        cookieMap = new HashMap<>();
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
            sc.init(null, new TrustManager[]{new TrustAllManager()},
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
    public void getGoods(){
        Request request = new Request.Builder().url(goodsUrl).build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
        }catch (IOException e){

        }
    }
    public void getLoginTouch(){
        Request request = new Request.Builder()
                            .url(loginTouchUrl)
                            .addHeader("Referer", goodsUrl)
                            .addHeader("Cookie", "CmLocation=200|200")
                            .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            String result = response.body().string();
            System.out.println("");
        }catch (IOException e){

        }
    }
    public void checkUidAvailable(){
        String url = "https://login.10086.cn/checkUidAvailable.action";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", loginTouchUrl)
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            String result = response.body().string();
            System.out.println("checkUidAvailable:" + result);
        }catch (IOException e){

        }
    }
    public void needVerifyCode(){
        String url = "https://login.10086.cn/needVerifyCode.htm?";
        url += "account=" + cellNum;
        url += "&pwdType=02";
        url += "&timestamp=" + String.valueOf(System.currentTimeMillis());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", loginTouchUrl)
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
        }catch (IOException e){

        }
    }
    public void chkNumberAction(){
        String url = "https://login.10086.cn/chkNumberAction.action";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "userName=" + cellNum;
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", loginTouchUrl)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            String result = response.body().string();
            System.out.println("chkNumberAction:" + result);
        }catch (IOException e){

        }
    }
    public void sendRandomCodeAction(){
        String url = "https://login.10086.cn/sendRandomCodeAction.action";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "userName=" + cellNum + "&type=01&channelID=12012";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", loginTouchUrl)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            String result = response.body().string();// 0
            System.out.println("sendRandomCodeAction:" + result);
        }catch (IOException e){

        }
    }
    public void webLogin(){
        String url = "https://login.10086.cn/login.htm?";

        url += "accountType=01&pwdType=02";
        url += "&account=" + cellNum;
        System.out.print("输入验证码：");
        Scanner scanner = new Scanner(System.in);
        String password = scanner.nextLine();
        url += "&password=" + password;
        url += "&inputCode=";
        url += "&backUrl=" + loginTouchUrl;
        url += "&rememberMe=0&channelID=12012&protocol=https%3A";
        url += "&timestamp=" + String.valueOf(System.currentTimeMillis());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", loginTouchUrl)
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            String result = response.body().string();//artifact
            artifact = getValue(result,"artifact\":\"", "\"");
            System.out.println("webLogin:" + result);

        }catch (IOException e){

        }
    }
    public void getArtifact(){
        String url = "http://touch.10086.cn/sso/getartifact.php?backurl=" + loginTouchUrl;
        url += "&artifact=" + artifact;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            String result = response.body().string();
            addRspCookie(response.headers("Set-Cookie"));

        }catch (IOException e){

        }
    }
    public void getGoodsLogin(){
        String url = goodsUrl + "?forcelogin=1";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            //获取address_id
            String result = response.body().string();
            addressId = getValue(result, "address_id\" value=\"", "\"");
            System.out.println("addressId:" + addressId);

        }catch (IOException e){

        }
    }
    private String getValue(String tarStr, String startStr, String stopStr){
        if (!tarStr.contains(startStr)) return "";
        tarStr = tarStr.substring(tarStr.indexOf(startStr) + startStr.length());
        if (!tarStr.contains(stopStr)) return "";
        return tarStr.substring(0, tarStr.indexOf(stopStr));
    }
    public void getstock(){
        String url = "http://touch.10086.cn/ajax/detail/getstock.json?goods_id=1045210&merchant_id=1000049&sale_type=1&sku_id=1040095";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl + "?forcelogin=1")
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            String result = response.body().string(); //stock
            System.out.println("getstock:" + result);
        }catch (IOException e){

        }
    }
    public void userinfo(){
        String url = "http://touch.10086.cn/ajax/user/userinfo.json?province_id=200&city_id=200";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl + "?forcelogin=1")
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            String result = response.body().string();
            System.out.println("userinfo:" + result);
        }catch (IOException e){

        }
    }
    public void addRspCookie(List<String> cookies){
        if(cookies.equals(null)){
            return;
        }
        for(String cookieLine: cookies){
            String[] cookieArr = cookieLine.split(";");
            for(String cookie: cookieArr){
                cookieMap.put(cookie, "");
            }
        }

    }
    public String getReqCookie(){
        if(cookieMap.size() == 0) return "";
        return StringUtils.join(cookieMap.keySet().toArray(), ";");

    }
    public void webBuy(){
        String url = "http://touch.10086.cn/ajax/buy/buy.json";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "sku%5B0%5D%5BModelId%5D=1040095&sku%5B0%5D%5BGoodsId%5D=1045210&sku%5B0%5D%5BNum%5D=1&sku%5B0%5D%5BItemFrom%5D=iphoneT_iphone8%3Fforcelogin&sku%5B0%5D%5BChannel%5D=1&sku%5B0%5D%5BProvinceId%5D=200&sku%5B0%5D%5BCityId%5D=200";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl + "?forcelogin=1")
                .addHeader("Cookie", getReqCookie())
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            String result = response.body().string();
            cartCode = getValue(result, "cart_code=", "\"");
            System.out.println("cart_code:" + cartCode);
        }catch (IOException e){

        }
    }
    public void checkOrder(){
        String url = "http://touch.10086.cn/order/checkorder.php?";
        url += "cart_code=" + cartCode;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl + "?forcelogin=1")
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            System.out.println("checkorder: " + response.body().string());
        }catch (IOException e){

        }
    }
    public void submitOrder(){
        String url = "http://touch.10086.cn/ajax/submitorder/addorder.json";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "address_id=" + addressId;
        data += "&invoice_id=personal&pay_type=1&coupon_type=&eticket_number_mall=";
        data += "&cart_code=" + cartCode;
        data += "&ticket_no=";
        data += "&cart_code=" + cartCode;
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", "http://touch.10086.cn/order/checkorder.php?cart_code=" + cartCode)
                .addHeader("Cookie", getReqCookie())
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            String result = response.body().string();
            System.out.println("submit order: " + result );
        }catch (IOException e){

        }
    }

    public static void main(String args[]) {

        WebBuy webBuy = new WebBuy("15013894358");
        webBuy.getGoods();
        webBuy.getLoginTouch();
        webBuy.checkUidAvailable();
        webBuy.needVerifyCode();
        webBuy.chkNumberAction();
        webBuy.sendRandomCodeAction();
        webBuy.webLogin();
        webBuy.getArtifact();
        webBuy.getGoodsLogin();
        webBuy.getstock();
        webBuy.userinfo();
        webBuy.webBuy();
        webBuy.checkOrder();
        webBuy.submitOrder();
//        String result = "<input type=\"hidden\" name=\"address_id\" value=\"1213213\" />";
//        String re = "address_id\" value=\"";
//        System.out.println(webBuy.getValue(result, re, "\""));
    }
}