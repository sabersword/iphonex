package iphonex;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiSystem;

import net.sf.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterMail {

    private OkHttpClient client;
    private String captchaPath = "1.jpg";
    private String captcha;
    private String loginName;
    private String newPasswordRepeat = "abcd1234";
    private String userPassword = "abcd1234";
    private String mail;
    private String mid;
    private String verifyKey;
    private static final String suffix = "@chacuo.net";

    public RegisterMail() {
        OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
        mBuilder.cookieJar(new CookieJar() {
            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });
        client = mBuilder.build();
    }

    public void getNewMail() {
        String url = "http://24mail.chacuo.net/";
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
//            System.out.println("getNewMail:" + result);
            mail = Utils.getValue(result, "type=\"text\" value=\"", "\"");
//            mail = "fndkba74850";
            loginName = mail + suffix;
        } catch (IOException e) {

        }
    }

    public void getCaptcha() {
        String url = "https://login.10086.cn/captchazh.htm?type=05";
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            BufferedImage bi = ImageIO.read(response.body().byteStream());
            ImageIO.write(bi, "png", new File(captchaPath));
        } catch (IOException e) {

        }
    }

    public String dama() {
        Call call = client.newCall(LZDama.getDamaRequest(captchaPath));

        try {
            Response response = call.execute();
            String result = response.body().string();
            System.out.println("dama: " + result);
            if (result.contains("\"result\":true")) {
                this.captcha = Utils.getValue(result, "val\":\"", "\"");
                System.out.println("captcha:" + captcha);
            } else {
                return dama();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return captcha;
    }

    public void registerSubmit() {
        String url = "https://login.10086.cn/registersubmit.htm";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "channelId=";
        data += "&inputCode=" + captcha;
        data += "&loginName=" + loginName;
        data += "&newPasswordRepeat=" + newPasswordRepeat;
        data += "&timestamp=" + String.valueOf(System.currentTimeMillis());
        data += "&userPassword=" + userPassword;
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", "https://login.10086.cn/html/register/register.html")
                .addHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/7.0)")
                .post(requestBody).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            System.out.println("registerSubmit:" + result);
        } catch (IOException e) {

        }
    }
    
    public void setMail() {
        String url = "http://24mail.chacuo.net/";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "data=" + mail;
        data += "&type=" + "set";
        data += "&arg=" + "d=chacuo.net_f=";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            System.out.println(result);
        } catch (IOException e) {

        }
    }
    
    public void getActiveMail() {
        String url = "http://24mail.chacuo.net/";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "data=" + mail;
        data += "&type=" + "refresh";
        data += "&arg=" + "";
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            System.out.println(result);
            mid = Utils.getValue(result, "MID\":", ",");
            if (mid.isEmpty()) {
                Thread.sleep(5000);
                System.out.println("没有收到激活邮件,重新获取");
                getActiveMail();
            }
            System.out.println("mid:" + mid);
        } catch (Exception e) {

        }
    }
    
    public void getActiveUrl() {
        String url = "http://24mail.chacuo.net/";
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
        String data = "data=" + mail;
        data += "&type=" + "mailinfo";
        data += "&arg=" + "f=" + mid;
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", "http://24mail.chacuo.net/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0")
                .addHeader("Host", "24mail.chacuo.net")
                .post(requestBody).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            System.out.println(result);
            verifyKey = Utils.getValue(result, "verifyKey=", "\\");
            System.out.println("verifyKey:" + verifyKey);
        } catch (IOException e) {

        }
    }
    
    public void active() {
        String url = "https://login.10086.cn/activeMailbox.action?verifyKey=" + verifyKey;
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            if (result.contains("激活成功"))
                System.out.println("成功激活" + loginName);
            else
                System.out.println("未能激活" + loginName);
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        RegisterMail registerMail = new RegisterMail();
        registerMail.getNewMail();
        registerMail.getCaptcha();
        registerMail.dama();
        registerMail.registerSubmit();
//        registerMail.setMail();
        registerMail.getActiveMail();
        registerMail.getActiveUrl();
        registerMail.active();
    }


}
