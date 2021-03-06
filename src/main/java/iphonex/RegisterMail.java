package iphonex;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
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

public class RegisterMail extends Thread {

    private OkHttpClient client;
    private String captchaPath = "default.jpg";
    private String captcha;
    private String loginName;
    private String newPasswordRepeat;
    private String userPassword;
    private String mail;
    private String mid;
    private String verifyKey;
    private static final String suffix = "@chacuo.net";
    private static final int waitMaxCount = 30;
    private int waitCount = 0;
    private static FileWriter logWriter, resultWriter;
    private static String lineSeparator;

    public String getCaptchaPath() {
        return captchaPath;
    }

    public void setCaptchaPath(String captchaPath) {
        this.captchaPath = captchaPath;
    }

    public String getNewPasswordRepeat() {
        return newPasswordRepeat;
    }

    public void setNewPasswordRepeat(String newPasswordRepeat) {
        this.newPasswordRepeat = newPasswordRepeat;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

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
    
    static {
        try {
            logWriter = new FileWriter("registerLog.txt", true);
            resultWriter = new FileWriter("registerResult.txt", true);
            lineSeparator = System.getProperty("line.separator");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeLog(String log) {
        String threadName = Thread.currentThread().getName();
        try {
            logWriter.write(threadName + ":" + log + lineSeparator);
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getNewMail() {
        String url = "http://24mail.chacuo.net/";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/7.0)")
                .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            response.close();
            mail = Utils.getValue(result, "type=\"text\" value=\"", "\"");
            if (mail.isEmpty()) {
                writeLog("mail为空,获取失败");
                getNewMail();
            }
            loginName = mail + suffix;
        } catch (IOException e) {
            writeLog(e.getMessage());
            getNewMail();
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
            response.close();
        } catch (IOException e) {
            writeLog(e.getMessage());
            getCaptcha();
        }
    }

    public String dama() {
        Call call = client.newCall(LZDama.getDamaRequest(captchaPath));

        try {
            Response response = call.execute();
            String result = response.body().string();
            response.close();
            System.out.println("dama: " + result);
            if (result.contains("\"result\":true")) {
                this.captcha = Utils.getValue(result, "val\":\"", "\"");
                System.out.println("captcha:" + captcha);
            } else {
                return dama();
            }
        } catch (Exception e) {
            writeLog(e.getMessage());
            return dama();
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
            response.close();
            System.out.println("registerSubmit:" + result);
            if (result.contains("注册成功")) {
                writeLog(loginName + "注册成功");
            }
            else {
                writeLog(loginName + "注册失败");
                getCaptcha();
                dama();
                registerSubmit();
            }
        } catch (IOException e) {
            writeLog(e.getMessage());
            registerSubmit();
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
            response.close();
            System.out.println(result);
        } catch (IOException e) {
            writeLog(e.getMessage());
            setMail();
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
            response.close();
            System.out.println(Thread.currentThread().getName() + ":" + result);
            mid = Utils.getValue(result, "MID\":", ",");
            if (mid.isEmpty()) {
                if (waitCount++ > waitMaxCount) {
                    writeLog(loginName + "超时没有收到邮件");
                    System.out.println(loginName + "超时没有收到邮件");
                    return;
                }
                Thread.sleep(30000);
                System.out.println(loginName + "没有收到激活邮件,重新获取");
                writeLog(loginName + "没有收到激活邮件,重新获取");
                getActiveMail();
            }
            System.out.println("mid:" + mid);
        } catch (Exception e) {
            writeLog(e.getMessage());
            getActiveMail();
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
            response.close();
            System.out.println(result);
            verifyKey = Utils.getValue(result, "verifyKey=", "\\");
            System.out.println("verifyKey:" + verifyKey);
        } catch (IOException e) {
            writeLog(e.getMessage());
            getActiveUrl();
        }
    }
    
    public void active() {
        String url = "https://login.10086.cn/activeMailbox.action?verifyKey=" + verifyKey;
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            response.close();
            if (result.contains("激活成功")) {
                System.out.println("成功激活" + loginName);
                writeLog(loginName + "激活成功");
                resultWriter.write(loginName + "," + userPassword + lineSeparator);
                resultWriter.flush();
            }
            else {
                System.out.println("激活失败" + loginName);
                writeLog(loginName + "激活失败");
                writeLog("result:" + result);
            }
        } catch (IOException e) {
            writeLog(e.getMessage());
            active();
        }
    }
    
    
    @Override
    public void run() {
        getNewMail();
        getCaptcha();
        dama();
        registerSubmit();
        getActiveMail();
        getActiveUrl();
        active();
    }

    public static void main(String[] args) {
        RegisterMail registerMail = new RegisterMail();
        registerMail.getNewMail();
        registerMail.getCaptcha();
        registerMail.dama();
        registerMail.registerSubmit();
        registerMail.setMail();
        registerMail.getActiveMail();
        registerMail.getActiveUrl();
        registerMail.active();
    }


}
