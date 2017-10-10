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
import java.util.Scanner;

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
    private String codePath = "1.jpg";
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
        String phpsessid = String.valueOf(id) + Utils.getMd5(String.valueOf(System.currentTimeMillis()));
        phpsessid = phpsessid.substring(0, 26);
//        System.out.println("phpsessid:" + phpsessid);
//        cookieMap.put("PHPSESSID", phpsessid);
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
                        if(itemArr[0].length() == 2) {
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
                getCheckArtifact();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("loginPage: " + e.toString());
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
                getCaptcha();
            }
            @Override
            public void onFailure(Call call, IOException e) {
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
                verifyCaptcha();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("getCaptchazh failed");
            }
        });
    }
    
    public void verifyCaptcha() {
        String vcode = Dama2.getCode(codePath);
        System.out.println(vcode);
        if (!vcode.contains("error")){
            captcha = vcode;
        }

        System.out.println("input captcha:");
        Scanner scanner = new Scanner(System.in);
        this.captcha = scanner.nextLine();
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
                if(result.contains("resultCode\":\"0\"")) {
                    login();
                }else{
                    System.out.println("verify:" + result);
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("verifyCaptcha failed");
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
                artifact = Utils.getValue(result,"artifact\":\"", "\"");
                uid = Utils.getValue(result,"uid\":\"", "\"");
                System.out.println("artifact=" + artifact + ",uid=" + uid);
                getArtifact();
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    
    public void getArtifact() {
//        String url = "http://shop.10086.cn/sso/getartifact.php?&backUrl=" + backUrl;
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
                System.out.println("cookie1:" + getReqCookie());
//                getGoods();
                userinfo();
            }
            @Override
            public void onFailure(Call call, IOException e) {
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
                System.out.println("cookie1:" + getReqCookie());
                getLogin();
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }
    public void getGoods(){
        System.out.println("goods cookie:" + getReqCookie());

        Request request = new Request.Builder()
                .url(goodsUrl)
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
                String result = response.body().string();
                merchantId = Utils.getValue(result, "merchant_id:\"", "\"");
                System.out.println("merchantId:"+merchantId);
                getstock();
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
//                onBuyFail(cellNum + " " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                System.out.println("userinfo:" + result);
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
//                onBuyFail(cellNum + " " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                System.out.println("userinfo:" + result);
                buyBuy();
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
//                onBuyFail(cellNum + " " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                cartCode = Utils.getValue(result, "cart_code=", "\"");
                System.out.println("cart_code:" + cartCode);
                checkOrder();
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
                onBuyFail(cellNum + " " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                addressId = Utils.getValue(result, "address_id\" value=\"", "\"");
                System.out.println("addressId:" + addressId);
                submitOrder();
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
//                onBuyFail(cellNum + " " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                System.out.println("submit order: " + result );
                if (result.contains("topay")){
//                    onBuySuccess(cellNum);
                }else{
//                    onBuyFail(cellNum + "" + result);
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

    public static void main(String[] args) throws Exception {
        ReqManager reqManager = new ReqManager(new Mobile());
        String[] elementArr = {"280139726@qq.com", "godigmh123456"};
        InternetBuy internetBuy = new InternetBuy(reqManager,0,elementArr);
        internetBuy.ssocheck();

    }

}
