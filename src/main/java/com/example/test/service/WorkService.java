package com.example.test.service;

import com.example.test.entity.WorkInfo;
import com.example.test.utils.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "考勤业务类")
public class WorkService {

    @Resource
    private FileService fileService;

    /**
     * 邮件接收人
     */
    @Value("${mail.receivers}")
    private String receiver;

    public Map<String, List<WorkInfo>> getWork(List<String> paths) throws IOException {
        MailUtil mailUtil = new MailUtil();
        Map<String, String> receivers = mailUtil.getReceivers(receiver);
        List<String> collect = receivers.keySet().stream().collect(Collectors.toList());
        Map<String, WorkInfo> path = fileService.getPath(collect, paths);
        Map<String, List<WorkInfo>> workInfos = fileService.getWorkInfos(path);
        return workInfos;
    }
}
