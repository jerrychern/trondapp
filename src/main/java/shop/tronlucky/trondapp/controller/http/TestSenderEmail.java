package shop.tronlucky.trondapp.controller.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/mail")
public class TestSenderEmail {
    @Autowired
    JavaMailSender mailSender;

    @GetMapping("/send")
    public void send(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cwangjie@qq.com");
        message.setTo("cwangjie@gmail.com");
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");
        mailSender.send(message);
    }
}
