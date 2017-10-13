package iphonex;

public class RegisterManager {
    private static int threadCount = 400;
    private static String password = "abcd1234";
    
    public static void main(String[] args) {
        RegisterMail registerThreads[] = new RegisterMail[threadCount];
        for (int i = 0; i < threadCount; i++) {
            registerThreads[i] = new RegisterMail();
            registerThreads[i].setName("Thread" + String.valueOf(i));
            registerThreads[i].setCaptchaPath(String.valueOf(i) + ".jpg");
            registerThreads[i].setNewPasswordRepeat(password);
            registerThreads[i].setUserPassword(password);
            registerThreads[i].start();
        }
        
        for(int i = 0; i < threadCount; i++) {
            try {
                registerThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("全部结束");
    }

}
