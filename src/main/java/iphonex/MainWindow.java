package iphonex;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.fromObject(sendMsgVO).toString());
                Builder builder = new Request.Builder().url("https://clientaccess.10086.cn/biz-orange/LN/uamrandcode/sendMsgLogin").post(body);
                builder.addHeader("xs", "8e25a666b86982ea194d4b6415a7d42c");
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
                loginReqBody.setCellNum("Y+kFfUs00E2xNuRFnYKaq7nGWuy1FGlqK+L8xt2Cul9L3uKMm3kfDi0EbROFlykL4J6nPOMi+qfWqjmnedz7OgU0cFz4/A73/PraX5t5g4zOPe7moPD85dFerXfEHLW1HeW3EFOOjFDL2tu1d0ORY1dvUiLdlJc3/sxzq2HL3lc=");
                loginReqBody.setVerifyCode(yzm.getText().trim());
                sendMsgVO.setReqBody(loginReqBody);
                String string = JSONObject.fromObject(sendMsgVO).toString();
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSONObject.fromObject(sendMsgVO).toString());
                Builder builder = new Request.Builder().url("https://clientaccess.10086.cn/biz-orange/LN/uamrandcodelogin/login").post(body);
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
