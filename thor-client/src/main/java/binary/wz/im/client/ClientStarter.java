package binary.wz.im.client;

import java.util.Scanner;

/**
 * @author binarywz
 * @date 2022/6/9 23:58
 * @description:
 */
public class ClientStarter {
    public static void main(String[] args) {
        System.out.println("please login");

        Scanner scan = new Scanner(System.in);

        String username = scan.nextLine();
        String pwd = scan.nextLine();

        ImClient imClient = new ImClient();
        imClient.start(username, pwd);

        System.out.println("\r\nlogin successfully (^_^)\r\n");

        imClient.printUserInfo();

        System.out.println("\r\nnow send msg to your friends\r\n");

        while (scan.hasNext()) {
            String userId = scan.nextLine();
            String text = scan.nextLine();
            imClient.send(userId, text);
        }
        scan.close();
    }
}
