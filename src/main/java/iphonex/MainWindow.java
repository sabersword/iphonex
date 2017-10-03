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
//                long time = System.currentTimeMillis();
//                String newstr = null;
//                try {
//                    char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
//                    MessageDigest md5 = MessageDigest.getInstance("MD5");
//                    //加密后的字符串
//                    byte[] md = md5.digest(String.valueOf(time).getBytes("utf-8"));
//                    int j = md.length;
//                    char str[] = new char[j * 2];
//                    int k = 0;
//                    for (int i = 0; i < j; i++) {
//                        byte byte0 = md[i];
//                        str[k++] = hexDigits[byte0 >>> 4 & 0xf];
//                        str[k++] = hexDigits[byte0 & 0xf];
//                    }
//                    newstr = new String(str);
//                    newstr = newstr.toLowerCase();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
                
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
                loginReqBody.setCellNum("WcLzYP249H2xpJ8SZpWLiV7uJR3GA53R90xf56y1p3dy4dtoVMe/Qv5EMVrZryF4HhZss20Yls7AY7g+66PdOH8oCtTC2dE45EiAKsHRFlllPlegYtMq5PsEEIxn5VIoHREeH3rUCEYDyp29wDZ2HgA3dATr6YRDosYz6cETioE=");
                loginReqBody.setVerifyCode(yzm.getText().trim());
                sendMsgVO.setReqBody(loginReqBody);
                String string = JSONObject.fromObject(sendMsgVO).toString();
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.fromObject(sendMsgVO).toString());
                Builder builder = new Request.Builder().url("https://clientaccess.10086.cn/biz-orange/LN/uamrandcodelogin/login").post(body);
                String xs = MD5Util.MD5("https://clientaccess.10086.cn/biz-orange/LN/uamrandcodelogin/login" + JSONObject.fromObject(loginReqBody) + "_Leadeon/SecurityOrganization");
                builder.addHeader("xs", "a6cf74de15455d86955766d53201efcc");
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
    }
}
