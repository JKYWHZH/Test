package com.example.test.config;

import com.example.test.entity.Receiver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Receiver_conf {

    @Value("${mail.receivers}")
    private String receiver;

    @Bean
    public List<Receiver> getReceivers(){
        List<Receiver> receivers = new ArrayList<>();
        if (receiver.contains(",")) {
            String[] split = receiver.split(",");
            for (String tmp : split) {
                String[] tmpSplit = tmp.split(":");
                Receiver receiver = Receiver.builder()
                        .name(tmpSplit[0].intern().trim())
                        .mailAddress(tmpSplit[1].intern().trim())
                        .build();
                receivers.add(receiver);
            }
        }else{
            String[] tmpSplit = receiver.split(":");
            Receiver receiver = Receiver.builder().name(tmpSplit[0].intern().trim())
                    .mailAddress(tmpSplit[1].intern().trim())
                    .build();
            receivers.add(receiver);
        }
        return receivers;
    }
}
