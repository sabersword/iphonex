package iphonex;

import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class YUNDama {
    public static final String damaUrl = "http://api.yundama.com/api.php";
    public static final String userName = "confiself";
    public static final String userPsw = "huzhenghui119";
    public static final String codeType = "5006";
    public static final String appid = "4128";
    public static final String appkey = "39c48804502ca5b9167f78a7a3d9935d";
    public static final String timeout = "30";
    public static void main(String[] args) {
        YUNDama yunDama = new YUNDama();
        yunDama.dama("1.jpg");
    }
    public static Request getDamaRequest(String filePath){
        File file = new File(filePath);
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", YUNDama.userName)
                .addFormDataPart("password", YUNDama.userPsw)
                .addFormDataPart("codetype", YUNDama.codeType)
                .addFormDataPart("appid", YUNDama.appid)
                .addFormDataPart("appkey", YUNDama.appkey)
                .addFormDataPart("timeout", YUNDama.timeout)
                .addFormDataPart("file", filePath, fileBody)
                .addFormDataPart("method", "upload")
                .build();
        Request request = new Request.Builder()
                .url(YUNDama.damaUrl)
                .post(requestBody)
                .addHeader("User-Agent", "Java/1.8.0_144")
                .build();
        return request;
    }

    public void dama(String filePath){
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(YUNDama.getDamaRequest(filePath));
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
