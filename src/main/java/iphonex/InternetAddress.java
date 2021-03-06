package iphonex;

import okhttp3.*;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
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

public class InternetAddress extends IPhoneX{

    private OkHttpClient client;
    private OkHttpClient okHttpClient;
    private HashMap<String, String> cookieMap;
    private String uid;
    private String codePath;
    private String captcha;
    private String artifact;
    private String addressInfo;
    private final String userAgent = "Mozilla/5.0 (Linux; Android 6.0.1; SM-C7000 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36 leadeon/4.0.0";
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();



    public InternetAddress(ReqManager reqManager, int id, String[] elementArr){
        super(reqManager, id, elementArr);
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.sslSocketFactory(createSSLSocketFactory());
        mBuilder.hostnameVerifier(new InternetAddress.TrustAllHostnameVerifier());
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
                        System.out.println("item:"+ item);
                        String[] itemArr = item.split("=");
                        if(itemArr.length == 2) {
                            System.out.println("item:"+ item);
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
        addressInfo = "AddressInfo," + cellNum + "," + cellNumEnc + "," +
                recName + "," + recPhoneNo + "," +
                provinceCode + "," + cityCode + "," +
                regionCode + "," + street;
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
            sc.init(null, new TrustManager[]{new InternetAddress.TrustAllManager()},
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
    public void onBuy(String a, String b){
        getAddress();
    }
    public void ssocheck(){
        String url = "https://login.10086.cn/SSOCheck.action?channelID=12002&backUrl=http://shop.10086.cn/i/?f=home_isLogin";
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
                System.out.println("loginPage: " + e.toString());
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }
    public void getCheckArtifact() {
        String url = "http://shop.10086.cn/sso/getartifact.php?artifact=-1&backUrl=http%3A%2F%2Fshop.10086.cn%2Fi%2F%3Ff%3Dhome_isLogin";
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
    public void getCaptcha() {
        System.out.println("getCaptcha...");
        String url = "https://login.10086.cn/captchazh.htm?type=12&timestamp=" + String.valueOf(System.currentTimeMillis());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=home")
                .addHeader("Cookie", getReqCookie())
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                BufferedImage bi = ImageIO.read(response.body().byteStream());
                ImageIO.write(bi, "png", new File(codePath));
                response.close();
                int width = bi.getWidth();
                int height = bi.getHeight();
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
        System.out.println("dama...");
        Call call = client.newCall(LZDama.getDamaRequest(codePath));
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                response.close();
                System.out.println("dama: " + result);
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
        System.out.println("verifyCaptcha...");
        Request request = new Request.Builder()
                .url("https://login.10086.cn/verifyCaptcha?inputCode=" + captcha)
                .addHeader("Cookie", getReqCookie())
                .addHeader("Referer", "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=home")
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                addRspCookie(response.headers("Set-Cookie"));
                String result = response.body().string();
                response.close();

                if(result.contains("resultCode\":\"0\"")) {
                    System.out.println("verify success");
                    login();
                }else{
                    System.out.println("verify:" + result);
                    onLoginFail(cellNum + ":" + result);
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());

            }
        });
    }
    public void getLogin() {
        Request request = new Request.Builder()
                .url("https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=home")
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
    
    public void login(){
        System.out.println("login...");
        String account = "";
        String password = "";
        try {
            account = URLEncoder.encode(cellNum, "utf-8");
            password = URLEncoder.encode(cellNumEnc, "utf-8");
        }catch (UnsupportedEncodingException e){

        }
        Request request = new Request.Builder()
                .url("https://login.10086.cn/login.htm?accountType=02&account=" + account +"&password=" +password+"&pwdType=03&smsPwd=&backUrl=http%3A%2F%2Fshop.10086.cn%2Fi%2F%3Ff%3Dhome&rememberMe=0&channelID=12002&protocol=https%3A&timestamp=" + String.valueOf(System.currentTimeMillis()) + "&inputCode=" + this.captcha)
                .addHeader("Cookie", getReqCookie())
                .addHeader("Referer", "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/?f=home")
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
        System.out.println("getArtifact...");

        String url = "http://shop.10086.cn/i/v1/auth/getArtifact?backUrl=http%3A%2F%2Fshop.10086.cn%2Fi%2F%3Ff%3Dhome&artifact=" + artifact;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",userAgent)
                .build();
        Call call= okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
                welcome();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());
            }
        });
    }
    public void welcome() {
        System.out.println("welcome...");
        String url = "http://shop.10086.cn/i/?welcome=" + String.valueOf(System.currentTimeMillis());
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
                System.out.println("welcome:" + getReqCookie());
                logininfo();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                onLoginFail(cellNum + ":" + e.toString());

            }
        });
    }
    public void logininfo(){
        String url = "http://shop.10086.cn/i/v1/auth/loginfo?_=" + String.valueOf(System.currentTimeMillis());
        System.out.println("logininfocookie:" + getReqCookie());
        Request request = new Request.Builder()
                .url(url)
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
                String result = response.body().string();
                addRspCookie(response.headers("Set-Cookie"));
                response.close();
                if(result.contains("\"loginValue\"")){
                    onLoginSuccess(cellNum);
                }else{
                    onLoginFail(cellNum + ":" + result);
                }

            }
        });
    }

    public void getAddress(){
        String url = "http://shop.10086.cn/i/v1/cust/recaddr/"+cellNum+"?_=" + String.valueOf(System.currentTimeMillis());
        System.out.println("addresscookie:" + getReqCookie());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", getReqCookie())
                .addHeader("Referer", "http://shop.10086.cn/i/?f=home&welcome=1507903488631")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onBuyFail(cellNum + ":" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("address:" + response.body().string());
                response.close();
                addAddress();
            }
        });
    }
    public void addAddress(){
        String url = "http://shop.10086.cn/i/v1/cust/recaddr/" + cellNum;
        String data = "{\"recName\":\""+recName+"\",\"recPhoneNo\":\""+recPhoneNo+"\",\"zipCode\":\"\",\"recTel\":\"\",\"provinceCode\":\""+provinceCode+"\",\"regionCode\":\""+regionCode+"\",\"cityCode\":\""+cityCode+"\",\"street\":\""+street+"\",\"isDefault\":1,\"oneStepBuy\":0}";
        System.out.println("data:" + data);
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", getReqCookie())
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onBuyFail(e.toString() + addressInfo);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                System.out.println("add:" + result);
                response.close();
                if(result.contains("添加常用收货地址成功")) {
                    System.out.println("add success");
                    onBuySuccess(addressInfo);
                }else{
                    onBuyFail(result + addressInfo);
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
        String[] elementArr = {"2275174122@qq.com", "huzhenghui119"};
        InternetAddress internetBuy = new InternetAddress(reqManager,0,elementArr);
        internetBuy.ssocheck();

    }

}
