package shop.tronlucky.trondapp.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {
    @Autowired
    JavaMailSender mailSender;

    public void sendMail(String subject, String msg){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cwangjie@qq.com");
        message.setTo("cwangjie@qq.com");
        message.setSubject(subject);
        message.setText(msg);
        mailSender.send(message);
    }
}