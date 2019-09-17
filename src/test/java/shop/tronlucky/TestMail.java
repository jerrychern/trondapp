package shop.tronlucky;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.junit4.SpringRunner;
import shop.tronlucky.trondapp.TronDApp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TronDApp.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestMail {
    @Test
    public void sendSimpleMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cwangjie@qq.com");
        message.setTo("cwangjie@gmail.com");
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");
    }
}
