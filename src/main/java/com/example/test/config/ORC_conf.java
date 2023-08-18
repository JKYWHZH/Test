package com.example.test.config;

import com.baidu.aip.ocr.AipOcr;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ORC_conf {

    private static String APP_ID = "272380751";

    private static String API_KEY = "Gs2GqSts44IUAfW1Ct3OSERU1";

    private static String SECRET_KEY = "ktA2vEkWuNsZ1uEgWxkh2cTEIlTb6Edj1";

    @Bean
    public AipOcr aipOcr(){
        return new AipOcr(APP_ID, API_KEY, SECRET_KEY);
    }
}
