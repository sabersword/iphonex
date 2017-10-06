package iphonex;


import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class AppBuy {
    private HashMap<String,String> cookieMap;
    private int id;
    private String cellNum;
    private String cellNumEnc;
    private String verifyCode;
    private String cartCode;
    private String addressId;
    private String TransactionID;
    private String uid;
    private String goodsId = "1045210";
    private String merchantId;
    private String skuId = "1040095";
    private String targetChannelID;
    private final String cid = "e4149e28ae8c44679120daf9f625ace7";
    private final String ctid = "ZQ7NGnFe+2Ob+ELjX6nA80oNw9raJFK96ckDGM/SJqdKa110jeool++QXR4R/VmoUbYy1yY6S0Tv7LQOgp8OxK/6BUQ0L7PEE0y+VwFEAMA=";
    private final String ak = "F4AA34B89513F0D087CA0EF11A3277469DC74905";
    private final String imei = "358811074939040";
    private final String xk = "8134206949ee8bbc89e534902056abc3b91c333ac8f5eb629e36cb0a3b37825736bf236e";
    private final String xc = "A0001";
    private final String userAgent = "Mozilla/5.0 (Linux; Android 6.0.1; SM-C7000 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36 leadeon/4.0.0";

    private OkHttpClient okHttpClient;
    private final String goodsUrl = "http://touch.10086.cn/goods/200_200_"+goodsId+"_"+skuId+".html";

    public AppBuy(String cellNum, String cellNumEnc){
        this.cellNum = cellNum;
        this.cellNumEnc = cellNumEnc;
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.sslSocketFactory(createSSLSocketFactory());
        mBuilder.hostnameVerifier(new AppBuy.TrustAllHostnameVerifier());
        okHttpClient = mBuilder.build();
        cookieMap = new HashMap<>();
        String phpsessid = String.valueOf(id) + getMd5(String.valueOf(System.currentTimeMillis()));
        phpsessid = phpsessid.substring(0, 26);
        System.out.println("phpsessid:" + phpsessid);
        cookieMap.put("PHPSESSID", phpsessid);
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
            sc.init(null, new TrustManager[]{new AppBuy.TrustAllManager()},
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
        String data = "{\"ak\":\""+ ak +"\",\"cid\":\"" + ctid +"\",\"city\":\"0755\",\"ctid\":\"" + ctid +"\",\"cv\":\"4.0.0\",\"en\":\"0\",\"imei\":\""+ imei +"\",\"nt\":\"3\",\"prov\":\"200\",\"reqBody\":{\"cellNum\":\"" + cellNum +"\"},\"sb\":\"samsung\",\"sn\":\"SM-C7000\",\"sp\":\"1080x1920\",\"st\":\"1\",\"sv\":\"6.0.1\",\"t\":\"\",\"tel\":\"99999999999\",\"xc\":\""+ xc +"\",\"xk\":\""+ xk +"\"}";
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
        String data = "{\"ak\":\""+ ak +"\",\"cid\":\""+ cid +"\",\"city\":\"0755\",\"ctid\":\""+ ctid +"\",\"cv\":\"4.0.0\",\"en\":\"0\",\"imei\":\""+ imei +"\",\"nt\":\"3\",\"prov\":\"200\",\"reqBody\":{\"cellNum\":\""+ cellNumEnc +"\",\"imei\":\""+ imei +"\",\"sendSmsFlag\":\"1\",\"verifyCode\":\""+ verifyCode +"\"},\"sb\":\"samsung\",\"sn\":\"SM-C7000\",\"sp\":\"1080x1920\",\"st\":\"1\",\"sv\":\"6.0.1\",\"t\":\"\",\"tel\":\"99999999999\",\"xc\":\""+ xc +"\",\"xk\":\""+ xk +"\"}";
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
            String result = response.body().string();
            System.out.println(result);
            addRspCookie(response.headers("Set-Cookie"));
            uid = cookieMap.get("UID");
            System.out.println("uid:" + uid);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getsaleAdver(){
        String url = "https://app.10086.cn/biz-orange/DN/homeSale/getsaleAdver";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "{\"cid\":\"" + cid +"\",\"t\":\"" + getReqCookie() +"\",\"sn\":\"SM-C7000\",\"cv\":\"4.0.0\",\"st\":\"1\",\"sv\":\"6.0.1\",\"sp\":\"1080x1920\",\"xk\":\""+ xk +"\",\"xc\":\""+ xc +"\",\"imei\":\"" + imei + "\",\"nt\":\"3\",\"sb\":\"samsung\",\"prov\":\"200\",\"city\":\"0755\",\"tel\":\"" + cellNum +"\",\"reqBody\":{\"provinceCode\":\"200\",\"cityCode\":\"0755\",\"adverType\":1}}";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .addHeader("Cookie", getReqCookie())
                            .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            String result = response.body().string();
            System.out.println("homeSale:" + result);
            TransactionID = getValue(result, "TransactionID=", "\"");
            targetChannelID = getValue(result, "targetChannelID=", "&");
            System.out.println("TransactionID:" + TransactionID);
            System.out.println("targetChannelID:" + targetChannelID);
        }catch (IOException e){

        }
    }
    public void getAppSSo(){
        String url = "https://login.10086.cn/AppSSO.action?targetChannelID="+targetChannelID+"&targetUrl=" +goodsUrl + "&TransactionID=" + TransactionID +"&telNo=" + "" + "&provinceCode=200&cityCode=0755&clientVer=4.0.0&devType=1&UID=" + uid + "&clientId=" +ctid+"&scnType=1080x1920&timestamp=" + String.valueOf(System.currentTimeMillis());
        Request request = new Request.Builder()
                            .url(url)
                            .addHeader("Cookie", getReqCookie())
                            .addHeader("User-Agent", userAgent)
                            .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            addRspCookie(response.headers("Set-Cookie"));
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

    public void getGoods(){
        Request request = new Request.Builder()
                .url(goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call = okHttpClient.newCall(request);
        try{
            Response response = call.execute();
            String result = response.body().string();
            merchantId = getValue(result, "merchant_id:\"", "\"");
            System.out.println("merchantId:"+merchantId);
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
        String url = "http://touch.10086.cn/ajax/detail/getstock.json?goods_id="+goodsId+"&merchant_id="+merchantId+"&sale_type=1&sku_id="+skuId;
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
                System.out.println(cookie.trim());
                String[] arr = cookie.split("=");
                if (arr.length == 2) {
                    cookieMap.put(arr[0].trim(), arr[1].trim());
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

    public void buy(){
        String url = "http://touch.10086.cn/ajax/buy/buy.json";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "sku%5B0%5D%5BModelId%5D="+skuId+"&sku%5B0%5D%5BGoodsId%5D="+goodsId+"&sku%5B0%5D%5BNum%5D=1&sku%5B0%5D%5BChannel%5D=1&sku%5B0%5D%5BProvinceId%5D=200&sku%5B0%5D%5BCityId%5D=200";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", goodsUrl)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
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
                .addHeader("User-Agent",userAgent)
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
                .addHeader("User-Agent",userAgent)
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
        AppBuy appBuy = new AppBuy("15013894358","ZOqzoAEe9U0KVnOUPK0IFfQRenD9kYL44VxyFPSionPEAMID4D60J9MENvkxRH0gRCWZKF+k22uukP4JTJlME7hm6cVMrgkg5Imow1KxWqrXqPUClS2RhpcnUTjEoSZFelmwTGe+o7kxmfewYdj/ofxtsXd+4EPFZUOGkew965g=");

        appBuy.sendMsg();
        appBuy.login();
        appBuy.getsaleAdver();
        appBuy.getAppSSo();
        appBuy.getGoods();
        appBuy.getstock();
        appBuy.userinfo();
        appBuy.buy();
        appBuy.checkOrder();
        appBuy.submitOrder();
    }
}