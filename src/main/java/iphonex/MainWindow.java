package iphonex;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.MessageDigest;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import net.sf.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainWindow {

    private JFrame frame;
    private JTextField yzm;
    private OkHttpClient client = new OkHttpClient();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainWindow() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 725, 470);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        
        yzm = new JTextField();
        yzm.setBounds(53, 47, 66, 21);
        frame.getContentPane().add(yzm);
        yzm.setColumns(10);
        
        JButton btnNewButton = new JButton("验证码");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
//                Request.Builder requestBuilder = new Request.Builder().url("http://www.baidu.com");
//                requestBuilder.method("GET",null);
//                Request request = requestBuilder.build();
                SendMsgVO sendMsgVO = new SendMsgVO();
                MsgReqBody msgReqBody = new MsgReqBody();
                msgReqBody.setCellNum("13710842223");
                sendMsgVO.setReqBody(msgReqBody);
                String string = JSONObject.fromObject(sendMsgVO).toString();
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.fromObject(sendMsgVO).toString());
                Builder builder = new Request.Builder().url("https://clientaccess.10086.cn/biz-orange/LN/uamrandcode/sendMsgLogin").post(body);
                
                builder.addHeader("xs", "1c6781282933d1ef7fde7e4b85ca0d28");    //020
//                builder.addHeader("xs", "2a161b81b6150cc0308d8723430eb959");     //0754
                Request request = builder.build();
               
                Call call= client.newCall(request);
                call.enqueue(new Callback() {
                    
                    public void onResponse(Call call, Response response) throws IOException {
                        String str = response.body().string();
                        System.out.println(str);
                    }
                    
                    public void onFailure(Call call, IOException e) {
                        System.out.println("获取验证码失败");
                        
                    }
                });
                
            }
        });
        btnNewButton.setBounds(145, 46, 93, 23);
        frame.getContentPane().add(btnNewButton);
        
        JButton btnNewButton_1 = new JButton("登陆");
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                SendMsgVO sendMsgVO = new SendMsgVO();
                LoginReqBody loginReqBody = new LoginReqBody();
                loginReqBody.setCellNum("Ow3QF5HW55JgFe9RkVIx76dKXxh/OP5jJAlo9OBluaMbVk1CZX8ehaBTaudIc9cWR3f+6PZcVJV0p7mnyF9lE/fPtGT+GqpIleLwTMTE7/SAdtpnpvDRhC/i5OI8FaLLB+JEph7hb4ejHFM01B3FOSwCFwfdTqQanEKtMGBu45o=");
                loginReqBody.setVerifyCode(yzm.getText().trim());
                sendMsgVO.setReqBody(loginReqBody);
                String string = JSONObject.fromObject(sendMsgVO).toString();
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.fromObject(sendMsgVO).toString());
                Builder builder = new Request.Builder().url("https://clientaccess.10086.cn/biz-orange/LN/uamrandcodelogin/login").post(body);
                String xs = MD5Util.MD5("https://clientaccess.10086.cn/biz-orange/LN/uamrandcodelogin/login_" + JSONObject.fromObject(sendMsgVO) + "_Leadeon/SecurityOrganization");
                builder.addHeader("xs", xs);
                Request request = builder.build();
               
                Call call= client.newCall(request);
                call.enqueue(new Callback() {
                    
                    public void onResponse(Call call, Response response) throws IOException {
                        String str = response.body().string();
                        System.out.println(str);
                        
                    }
                    
                    public void onFailure(Call call, IOException e) {
                        System.out.println("登陆失败");
                        
                    }
                });
            }
        });
        btnNewButton_1.setBounds(273, 46, 93, 23);
        frame.getContentPane().add(btnNewButton_1);
        
        JButton buyButton = new JButton("购买");
        buyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sku = "1040095";
                String goodsId = "1045210";
//                String json = "sku[0][ModelId]=" + sku + "&sku[0][GoodsId]=" + goodsId + "&sku[0][Num]=1&sku[0][Channel]=1&sku[0][ProvinceId]=200&sku[0][CityId]=200";
//                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                FormBody formBody = new FormBody.Builder()
                        .add("sku[0][ModelId]", sku)
                        .add("sku[0][GoodsId]", goodsId)
                        .add("sku[0][Num]", "1")
                        .add("sku[0][Channel]", "1")
                        .add("sku[0][ProvinceId]", "200")
                        .add("sku[0][CityId]", "200")
                        .build();
                Builder builder = new Request.Builder()
                        .addHeader("cookie", "is_login=true;userinfokey=%7b%22loginType%22%3a%2201%22%2c%22provinceName%22%3a%22200%22%2c%22pwdType%22%3a%2202%22%7d;CmLocation=200|200;PHPSESSID=q8pl44pc3er70qgquvnbi5p5f6")
                        .addHeader("Referer", "http://touch.10086.cn/goods/200_200_1045210_1040095.html")
                        .addHeader("Origin", "http://touch.10086.cn")
                        .addHeader("X-Requested-With", "com.greenpoint.android.mc10086.activity")
                        .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.1; OPPO A37m Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.121 Mobile Safari/537.36 leadeon/4.1.0")
                        .url("http://touch.10086.cn/ajax/buy/buy.json")
                        .post(formBody);
                Request request = builder.build();
               
                Call call= client.newCall(request);
                call.enqueue(new Callback() {
                    
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str = response.body().string();
                        System.out.println(str);
                    }
                    
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.println("购买");
                    }
                });
                
                
            }
        });
        buyButton.setBounds(375, 46, 93, 23);
        frame.getContentPane().add(buyButton);
    }
}
