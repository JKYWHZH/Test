package com.example.test.utils;

import com.sun.mail.util.MailSSLSocketFactory;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Slf4j(topic = "邮箱工具类")
public class MailUtil {

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 邮箱登陆密码（非qq邮箱密码）
     */
    private String password;

    /**
     * 邮箱session
     */
    private Session session;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Session getSession() {
        return session == null ? session() : session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 检查是否可用
     * @param mailUtil mail对象
     * @return 可用结果
     */
    public boolean check(MailUtil mailUtil){
        return mailUtil.email != null && mailUtil.password != null;
    }

    /**
     * 获取连接
     * @return 连接对象
     */
    public Transport connect(){
        boolean check = check(this);
        if (!check){
            log.error("请设置正确邮件信息");
            return null;
        }
        //连接对象
        Transport transport = null;

        if(session == null){
            session = session();
        }
        try {
            transport = session.getTransport();
        } catch (NoSuchProviderException e) {
            log.error("获取连接对象时报错，可能原因[{}]", e.getCause().getMessage());
        }

        //连接服务器
        try {
            transport.connect("smtp.qq.com", email, password);
        } catch (MessagingException e) {
            log.error("连接邮件服务器时报错，可能原因[{}]", e.getCause().getMessage());
        }
        return transport;
    }

    /**
     * 获取session对象
     * @return session对象
     */
    public Session session(){
        boolean check = check(this);
        if (!check){
            log.error("请设置正确邮件信息");
            return null;
        }
        Properties properties = new Properties();

        properties.setProperty("mail.host","smtp.qq.com");

        properties.setProperty("mail.transport.protocol","smtp");

        properties.setProperty("mail.smtp.auth","true");

        properties.put("mail.smtp.ssl.enable", "true");
        //QQ存在一个特性设置SSL加密
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        sf.setTrustAllHosts(true);

        properties.put("mail.smtp.ssl.socketFactory", sf);
        //创建一个session对象
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
        //开启debug模式
        session.setDebug(true);
        return session;
    }
}
