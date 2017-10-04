package iphonex;


import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import okhttp3.*;

import javax.net.ssl.*;

public class WebBuy {
    private HashMap<String,String> cookieMap;
    private String cellNum;
    private String cellNumEnc;
    private String verifyCode;
    private String cartCode;
    private String addressId;
    private String artifact;
    private OkHttpClient okHttpClient;
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private final String goodsUrl = "http://touch.10086.cn/goods/200_200_1045210_1040095.html";
    private final String loginTouchUrl = "https://login.10086.cn/html/login/touch.html?channelID=12012&backUrl=http%3A%2F%2Ftouch.10086.cn%2Fgoods%2F200_200_1045210_1040095.html%3FWT.ac%3DiphoneT_iphone8%3Fforcelogin%3D1";

    public WebBuy(String cellNum, String cellNumEnc){
        this.cellNum = cellNum;
        this.cellNumEnc = cellNumEnc;
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.sslSocketFactory(createSSLSocketFactory());
        mBuilder.hostnameVerifier(new WebBuy.TrustAllHostnameVerifier());
        okHttpClient = mBuilder.build();
        cookieMap = new HashMap<>();
        //自定义 PHPSESSID
        cookieMap.put("PHPSESSID", "nhjevoooaj1t5eb8jh337ihhr6");
        cookieMap.put("CmLocation", "200|200");
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
            sc.init(null, new TrustManager[]{new WebBuy.TrustAllManager()},
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
    public  boolean sendMsg(){
        String url="https://clientaccess.10086.cn/biz-orange/LN/uamrandcode/sendMsgLogin";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String data = "{\"ak\":\"F4AA34B89513F0D087CA0EF11A3277469DC74905\",\"cid\":\"ZQ7NGnFe+2Ob+ELjX6nA80oNw9raJFK96ckDGM/SJqdKa110jeool++QXR4R/VmoUbYy1yY6S0Tv7LQOgp8OxK/6BUQ0L7PEE0y+VwFEAMA=\",\"city\":\"0755\",\"ctid\":\"ZQ7NGnFe+2Ob+ELjX6nA80oNw9raJFK96ckDGM/SJqdKa110jeool++QXR4R/VmoUbYy1yY6S0Tv7LQOgp8OxK/6BUQ0L7PEE0y+VwFEAMA=\",\"cv\":\"4.0.0\",\"en\":\"0\",\"imei\":\"358811074939040\",\"nt\":\"3\",\"prov\":\"200\",\"reqBody\":{\"cellNum\":\"CELL_NUM\"},\"sb\":\"samsung\",\"sn\":\"SM-C7000\",\"sp\":\"1080x1920\",\"st\":\"1\",\"sv\":\"6.0.1\",\"t\":\"\",\"tel\":\"99999999999\",\"xc\":\"A0001\",\"xk\":\"8134206949ee8bbc89e534902056abc3b91c333ac8f5eb629e36cb0a3b37825736bf236e\"}";
        data = data.replace("CELL_NUM", this.cellNum);
        String xs = url + "_" + data + "_Leadeon/SecurityOrganization";
        xs = getMd5(xs);
        System.out.println("xs:" + xs);
        RequestBody body = RequestBody.create(JSON,data);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("xs", xs)

                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            System.out.println(result);
            if (result.contains("\"retDesc\":\"SUCCESS\"")){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    public void login(){
        System.out.print("输入验证码：");
        Scanner scanner = new Scanner(System.in);
        verifyCode = scanner.nextLine();
        String url = "https://clientaccess.10086.cn/biz-orange/LN/uamrandcodelogin/login";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String data = "{\"ak\":\"F4AA34B89513F0D087CA0EF11A3277469DC74905\",\"cid\":\"ZQ7NGnFe+2Ob+ELjX6nA80oNw9raJFK96ckDGM/SJqdKa110jeool++QXR4R/VmoUbYy1yY6S0Tv7LQOgp8OxK/6BUQ0L7PEE0y+VwFEAMA=\",\"city\":\"0755\",\"ctid\":\"ZQ7NGnFe+2Ob+ELjX6nA80oNw9raJFK96ckDGM/SJqdKa110jeool++QXR4R/VmoUbYy1yY6S0Tv7LQOgp8OxK/6BUQ0L7PEE0y+VwFEAMA=\",\"cv\":\"4.0.0\",\"en\":\"0\",\"imei\":\"358811074939040\",\"nt\":\"3\",\"prov\":\"200\",\"reqBody\":{\"cellNum\":\"CELL_NUM\",\"imei\":\"358811074939040\",\"sendSmsFlag\":\"1\",\"verifyCode\":\"VERIFY_CODE\"},\"sb\":\"samsung\",\"sn\":\"SM-C7000\",\"sp\":\"1080x1920\",\"st\":\"1\",\"sv\":\"6.0.1\",\"t\":\"\",\"tel\":\"99999999999\",\"xc\":\"A2061\",\"xk\":\"8134206949ee8bbc89e534902056abc3b91c333ac8f5eb629e36cb0a3b37825736bf236e\"}";
        data = data.replace("CELL_NUM", this.cellNumEnc).replace("VERIFY_CODE", this.verifyCode);
        System.out.println(data);
        String xs = url + "_" + data + "_Leadeon/SecurityOrganization";
        xs = getMd5(xs);
        System.out.println("xs:" + xs);
        RequestBody body = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("xs", xs)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            System.out.println(response.body().string());
            addRspCookie(response.headers("Set-Cookie"));
        } catch (IOException e) {
            e.printStackTrace();
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
                            .addHeader("Cookie", getReqCookie())
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

    public  static String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
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
        String url = goodsUrl + "?WT.ac=iphoneT_iphone8?forcelogin=1";
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
                .addHeader("Referer", goodsUrl)
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
                .addHeader("Referer", goodsUrl)
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
                System.out.println(cookie);
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
    public String  getBuyCookie(){
        String cookie = "PHPSESSID=" + cookieMap.get("PHPSESSID") + ";";
        cookie += "is_login=" + cookieMap.get("is_login") + ";";
        cookie += "CmLocation=" + cookieMap.get("CmLocation") + ";";
        cookie += "userinfokey=" + cookieMap.get("userinfokey") + ";";
        return cookie;
    }
    public void buy(){
        String url = "http://touch.10086.cn/ajax/buy/buy.json";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "sku%5B0%5D%5BModelId%5D=1040095&sku%5B0%5D%5BGoodsId%5D=1045210&sku%5B0%5D%5BNum%5D=1&sku%5B0%5D%5BChannel%5D=1&sku%5B0%5D%5BProvinceId%5D=200&sku%5B0%5D%5BCityId%5D=200";
//        String data = "sku%5B0%5D%5BModelId%5D=1040095&sku%5B0%5D%5BGoodsId%5D=1045210&sku%5B0%5D%5BNum%5D=1&sku%5B0%5D%5BItemFrom%5D=iphoneT_iphone8%3Fforcelogin&sku%5B0%5D%5BChannel%5D=1&sku%5B0%5D%5BProvinceId%5D=200&sku%5B0%5D%5BCityId%5D=200";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
//                .addHeader("Referer", goodsUrl + "?WT.ac=iphoneT_iphone8?forcelogin=1")
                .addHeader("Referer", goodsUrl)
//                .addHeader("Cookie", getReqCookie() + ";itemFrom=iphoneT_iphone8%3Fforcelogin")
                .addHeader("Cookie", getBuyCookie())
                .addHeader("User-Agent","Mozilla/5.0 (Linux; Android 6.0.1; SM-C7000 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36 leadeon/4.0.0")
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
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent","Mozilla/5.0 (Linux; Android 6.0.1; SM-C7000 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36 leadeon/4.0.0")
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
            String result = response.body().string();
            addressId = getValue(result, "address_id\" value=\"", "\"");
            System.out.println("addressId:" + addressId);
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
        //注意User-Agent要根据抓包的来设置，还用okhttp则出现系统繁忙
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", "http://touch.10086.cn/order/checkorder.php?cart_code=" + cartCode)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent","Mozilla/5.0 (Linux; Android 6.0.1; SM-C7000 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36 leadeon/4.0.0")
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
        WebBuy webBuy = new WebBuy("15013894358","ZOqzoAEe9U0KVnOUPK0IFfQRenD9kYL44VxyFPSionPEAMID4D60J9MENvkxRH0gRCWZKF+k22uukP4JTJlME7hm6cVMrgkg5Imow1KxWqrXqPUClS2RhpcnUTjEoSZFelmwTGe+o7kxmfewYdj/ofxtsXd+4EPFZUOGkew965g=");

        webBuy.getGoods();
        webBuy.getLoginTouch();
        webBuy.checkUidAvailable();
        webBuy.needVerifyCode();
        webBuy.chkNumberAction();
        webBuy.sendRandomCodeAction();
        webBuy.webLogin();
//        webBuy.sendMsg();
//        webBuy.login();
        webBuy.getArtifact();
        webBuy.getGoodsLogin();
        webBuy.getstock();
        webBuy.userinfo();
        webBuy.buy();
        webBuy.checkOrder();
        webBuy.submitOrder();
    }
}