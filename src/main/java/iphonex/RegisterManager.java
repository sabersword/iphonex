package iphonex;

import java.util.Random;

public class RegisterManager {
    private static int threadCount = 500;
    private static final int PASSWORD_LENGTH = 8;
    private static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final char[] DIGITAL = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    public static void main(String[] args) {
        RegisterMail registerThreads[] = new RegisterMail[threadCount];
        for (int i = 0; i < threadCount; i++) {
            registerThreads[i] = new RegisterMail();
            registerThreads[i].setName("Thread" + String.valueOf(i));
            registerThreads[i].setCaptchaPath("img/" + String.valueOf(i) + ".jpg");
            String password = randomPassword();
            registerThreads[i].setNewPasswordRepeat(password);
            registerThreads[i].setUserPassword(password);
            registerThreads[i].start();
        }

        for (int i = 0; i < threadCount; i++) {
            try {
                registerThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("全部结束");
    }

    public static String randomPassword() {
        StringBuffer password = new StringBuffer();
        Random random = new Random();
        int letterLength = random.nextInt(PASSWORD_LENGTH - 1) + 1;
        int digitLength = PASSWORD_LENGTH - letterLength;
        for (int i = 0; i < letterLength; i++) {
            password.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        for (int i = 0; i < digitLength; i++) {
            password.append(DIGITAL[random.nextInt(DIGITAL.length)]);
        }
        return password.toString();
    }

}
