package com.example.test.service;

import com.example.test.entity.Receiver;
import com.example.test.entity.WorkInfo;
import com.example.test.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
@Slf4j(topic = "邮箱业务类")
public class MailService {

    /**
     * 邮箱密钥
     */
    private static String code = "pacuynxtstasbdhj";

    /**
     * 邮件发送人
     */
    private static String sender = "541213978@qq.com";

    /**
     * 发件人名称
     */
    private static String name = "考勤机器人";

    /**
     * 线程池
     */
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 50, 4, TimeUnit.SECONDS, new ArrayBlockingQueue(10), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 邮件发送
     * @return 发送是否成功
     */
    public void send(List<Receiver> data){
        //创建邮箱工具类
        MailUtil mailUtil = new MailUtil();
        //设置邮箱账号
        mailUtil.setEmail(sender);
        //设置邮箱code
        mailUtil.setPassword(code);
        //获取邮箱session
        Session session = mailUtil.getSession();
        //获取邮箱连接
        Transport connect = mailUtil.connect();
        //异步发送邮件
        CompletableFuture[] completableFutures = IntStream
                .rangeClosed(0, data.size() - 1)
                .mapToObj(i -> data.get(i))
                .parallel()
                .map(receiver -> CompletableFuture.runAsync(() -> {
                    //创建邮件对象
                    MimeMessage mimeMessage = new MimeMessage(session);
                    try {
                        //收件人地址
                        String emailAddress = receiver.getMailAddress();
                        //收件人姓名
                        String name = receiver.getName();
                        //待发送数据
                        List<WorkInfo> workInfos = receiver.getWorkInfos();
                        //邮件发送人
                        mimeMessage.setFrom(new InternetAddress(sender, name));
                        //邮件接收人
                        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
                        //邮件标题
                        mimeMessage.setSubject("考勤统计");
                        //邮件内容
                        mimeMessage.setContent(html(name, null == workInfos || 0 == workInfos.size() ? null : workInfos), "text/html;charset=UTF-8");
                        //发送邮件
                        connect.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                        log.info("用户[{}]的考勤信息发送成功", name);
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        log.error("发送邮件至[{}]时，发生报错，可能原因为[{}]", name, e.getCause().getMessage());
                    }
                }, threadPool))
                .toArray(size -> new CompletableFuture[size]);
        //回归主线程
        CompletableFuture.allOf(completableFutures).join();
        try {
            //关闭连接
            connect.close();
        } catch (MessagingException e) {
            log.error("关闭邮件连接发生报错，可能原因为[{}]", e);
        }
    }

    /**
     * 发送邮件模板
     * @param userName 用户名
     * @param data     考勤数据
     * @return 邮件模板
     */
    private String html(String userName, List<WorkInfo> data){
        String tmpTitle = "<html><head></head><body><h2>你好，"+userName+"! </h2>";
        StringBuilder content = new StringBuilder(tmpTitle);
        if (data == null) {
            content.append("<h3>暂无待统计数据!</h3>");
            content.append("</body></html>");
            return content.toString();
        }
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\"><th>序号</th><th>姓名</th><th>日期</th><th>合规</th><th>结果</th></tr>");
        String notRule = "";
        String tmpAns = "";
        for (int i = 0; i < data.size(); i++) {
            boolean ans = data.get(i).getAns();
            if (ans){
                content.append("<tr>");
                tmpAns = "<td>" + "是" + "</td>";
            }else{
                content.append("<tr style=\"color:red\">");
                tmpAns = "<td>" + "否" + "</td>";
                if (notRule == ""){
                    notRule = notRule.concat(data.get(i).getDate());
                }else{
                    notRule = notRule.concat("，").concat(data.get(i).getDate());
                }
            }
            content.append("<td>" + i + "</td>"); //序号列
            content.append("<td>" + userName + "</td>"); //姓名列
            content.append("<td>" + data.get(i).getDate() + "</td>"); //日期列
            content.append(tmpAns);                                   //合规列
            content.append("<td>" + data.get(i).getDesc() + "</td>"); //合规列
            content.append("</tr>");
        }
        content.append("</table>");
        content.append(String.format("<h3>结果：不合规天为： %s，共 %s 天。 </h3>", notRule.equals("") ? "无" : notRule, notRule.contains("，") ? notRule.split("，").length : notRule.equals("") ? 0 : 1 ));
        content.append("</body></html>");
        return content.toString();
    }

    /**
     * 按邮件模板发送
     * @param htmlPath 邮件模板地址
     * @return 邮件内容
     */
    public String html(String htmlPath) {
        //获取文件路径
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(htmlPath);
        BufferedReader fileReader = null;
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            fileReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = fileReader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            log.error("发送邮件读取模板失败，错误堆栈信息：{}", e.getMessage());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //使用动态参数替换html模板中的占位符参数
        return buffer.toString();
    }
}
