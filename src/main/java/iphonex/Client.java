package iphonex;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;


/**
 * 
* @ClassName: Client 
* @Description: 不管怎么样,提交一下总是好的,对吧....
* @author god
* @date 2018年2月6日 下午8:09:00
 */
public enum Client{
    INSTANCE;
    private OkHttpClient client;
    private Client(){
        client = new OkHttpClient().newBuilder().hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();
    }
    public OkHttpClient getInstance() {
        return client;
    }
}
