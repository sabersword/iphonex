package iphonex;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

public class InternetBuy {

    private OkHttpClient client;
    private HashMap<String, String> cookieMap;
    private String codePath = "e:/img/1.jpg";
    private String account;
    private String password;
    private String captcha;
    private String artifact;
    private String backUrl = "https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/";
    private String phpSessid;
    private String sku = "1040095";
    private String goodsId = "1045210";
    private String uid;
    
    public InternetBuy(String account, String password) {
        Builder builder = new OkHttpClient.Builder();
        builder.cookieJar(new CookieJar() {
            private List<Cookie> cookies = new ArrayList<Cookie>();
            
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                for (Cookie cookie : cookies) {
                    this.cookies.add(cookie);
                }
            }
            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
//                okhttp3.Cookie.Builder builder = new Cookie.Builder();
//                Cookie phpSessidCookie = builder.name("PHPSESSID").value(phpSessid).domain("shop.10086.cn").build();
//                cookies.add(phpSessidCookie);
                return cookies;
            }
        });
        client = builder.build();
        phpSessid = Utils.getMd5(String.valueOf(System.currentTimeMillis())).substring(0, 26);
        this.account = account;
        this.password = password;
//        this.cookieMap = new HashMap<String, String>();
//        cookieMap.put("PHPSESSID", phpSessid);
        
    }

    public void getLogin() {
        Request request = new Request.Builder()
                .url("https://login.10086.cn/login.html?channelID=12003&backUrl=http://shop.10086.cn/i/")
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.headers("set-cookie"));
                String str = response.body().string();
                System.out.println(str);
                getCaptcha();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("getLogin failed");
            }
        });
    }
    
    public void getCaptcha() {
        Request request = new Request.Builder()
                .url("https://login.10086.cn/captchazh.htm?type=12")
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.headers("set-cookie"));
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
        System.out.println("input captcha:");
        Scanner scanner = new Scanner(System.in);
        this.captcha = scanner.nextLine();
        Request request = new Request.Builder()
                .url("https://login.10086.cn/verifyCaptcha?inputCode=" + captcha)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.headers("set-cookie"));
                String str = response.body().string();
                System.out.println(str);
                login();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("verifyCaptcha failed");
            }
        });
    }
    
    public void login() {
        Request request = new Request.Builder()
                .url("https://login.10086.cn/login.htm?accountType=02&account=280139726%40qq.com&password=godigmh123456&pwdType=0321&smsPwd=&backUrl=http%3A%2F%2Fshop.10086.cn%2Fi%2F&rememberMe=0&channelID=12003&protocol=https%3A&timestamp=" + String.valueOf(System.currentTimeMillis()) + "&inputCode=" + this.captcha)
                .addHeader("Referer", backUrl)
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.headers("set-cookie"));
                String str = response.body().string();
                System.out.println(str);
//                addRspCookie(response.headers("Set-Cookie"));
                artifact = Utils.getValue(str,"artifact\":\"", "\"");
                uid = Utils.getValue(str,"uid\":\"", "\"");
                System.out.println("artifact=" + artifact + ",uid=" + uid);
                getArtifact();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("login failed");
            }
        });
    }
    
    public void getArtifact() {
        Request request = new Request.Builder()
                .url("http://shop.10086.cn/i/v1/auth/getArtifact?backUrl=http%3A%2F%2Fshop.10086.cn%2Fi%2F&artifact=" + artifact)
//                .addHeader("Cookie", "PHPSESSID=" + phpSessid)
//                .addHeader("Cookie", getReqCookie())
                .addHeader("User-Agent",
                        "Mozilla/5.0 (Linux; Android 5.1; OPPO A37m Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.121 Mobile Safari/537.36 leadeon/4.1.0")
                .addHeader("Cookie", "CmLocation=100|100;itemFrom=res_search_042_100_2aaf56e0811c42b9ba1902a74472cf46_1045210")
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("code=" + response.code());
                System.out.println("-----getArtifact----");
                System.out.println(response.headers("set-cookie"));
                buy();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("getArtifact failed");
            }
        });
    }
    
    public void buy() {
        FormBody formBody = new FormBody.Builder()
                .add("sku[0][ItemFrom]", "res_search_042_100_6cff38aa78a64031ad03db3993ff8103_" + goodsId)
                .add("sku[0][GoodsType]", "70000")
                .add("sku[0][ModelId]", sku)
                .add("sku[0][GoodsId]", goodsId)
                .add("sku[0][Num]", "1")
                .add("sku[0][Channel]", "1")
                .add("sku[0][ProvinceId]", "100")
                .add("sku[0][CityId]", "100")
                .build();
        okhttp3.Request.Builder builder = new Request.Builder()
//                .addHeader("Cookie", "PHPSESSID=" + phpSessid)
//                .addHeader("Cookie", getReqCookie())
                .addHeader("Cookie", "CmLocation=100|100")
                .addHeader("Referer", "http://shop.10086.cn/goods/100_100_1045210_1040095.html?WT.ac=res_search_042_100_6cff38aa78a64031ad03db3993ff8103_1045210")
                .addHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/7.0)")
                .url("http://shop.10086.cn/ajax/buy/buy.json").post(formBody);
        Request request = builder.build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.headers("set-cookie"));
                String str = response.body().string();
                System.out.println(str);
                getLoginfo();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("buy failed");
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
    
    public void getPhpSessid() {
        Request request = new Request.Builder()
                .url("http://shop.10086.cn/service/excanvas.min.js")
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.headers("set-cookie"));
                getLoginfo();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("getPhpSessid failed");
            }
        });
    }
    
    public void getLoginfo() {
        Request request = new Request.Builder()
                .url("http://shop.10086.cn/i/v1/auth/loginfo?_=" + String.valueOf(System.currentTimeMillis()))
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("-----getArtifact----");
                System.out.println(response.headers("set-cookie"));
                System.out.println(response.body().string());
                getLgToken();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("getLoginfo failed");
            }
        });
    }
    
    public void getLgToken() {
        Request request = new Request.Builder()
                .url("https://login.10086.cn/genqr.htm")
                .build();
        Call call= client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.headers("set-cookie"));
                getCaptcha();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("getLgToken failed");
            }
        });
    }
    
    public static void main(String[] args) throws Exception {
        InternetBuy buy = new InternetBuy("280139726@qq.com", "godigmh123456");
        buy.getPhpSessid();
        
    }

}
