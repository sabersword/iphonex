package iphonex;

import okhttp3.*;

import java.io.*;

public class LZDama {
    public static final String damaUrl = "http://v1-http-api.jsdama.com/api.php?mod=php&act=upload";
    public static final String userName = "confiself";
    public static final String userPsw = "HuZhengHui0671";
    public static final String yzmMinLen = "4";
    public static final String yzmMaxLen = "6";
    public static final String yzmTypeMark = "1014";
    public static final String zzToolToken = "b1f13e6202c4a54e5ba29857b3e4a864";
    public static void main(String[] args) {
        LZDama lzDama = new LZDama();
        lzDama.dama("1.jpg");
    }
    public static Request getDamaRequest(String filePath){
        File file = new File(filePath);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_name", LZDama.userName)
                .addFormDataPart("user_pw", LZDama.userPsw)
                .addFormDataPart("yzm_minlen", LZDama.yzmMinLen)
                .addFormDataPart("yzm_maxlen", LZDama.yzmMaxLen)
                .addFormDataPart("yzmtype_mark", LZDama.yzmTypeMark)
                .addFormDataPart("zztool_token", LZDama.zzToolToken)
                .addFormDataPart("upload", filePath, fileBody)
                .build();
        Request request = new Request.Builder()
                .url(LZDama.damaUrl)
                .post(requestBody)
                .addHeader("User-Agent", "Java/1.8.0_144")
                .build();
        return request;
    }
    public void dama(String filePath){
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(LZDama.getDamaRequest(filePath));
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.body().string());
            }
        });
    }
}
