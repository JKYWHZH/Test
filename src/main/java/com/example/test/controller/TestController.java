package com.example.test.controller;

import com.example.test.entity.WorkInfo;
import com.example.test.service.DailyService;
import com.example.test.service.MailService;
import com.example.test.service.WorkService;
import com.example.test.utils.ExeclUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j(topic = "上传接口")
@RestController
public class TestController {

    @Resource
    private MailService mailService;

    @Resource
    private WorkService workService;

    @Resource
    private DailyService dailyService;

    @Value("${mail.zipPath}")
    private String zipPath;

    @Value("${mail.type}")
    private String type;

    @RequestMapping(method=RequestMethod.POST, value = "upload")
    public String start(@RequestParam("file") MultipartFile file) throws IOException{
        String fileName = file.getOriginalFilename();
        long size = file.getSize();
        int i = fileName.lastIndexOf(".");
        String fileType = fileName.substring(i + 1, fileName.length());
        boolean contains = type.contains(fileType);
        if (!contains){
            log.info("[{}]文件类型为[{}]，不予操作", fileName, fileType);
            return "不支持此文件类型";
        }
        //解析考勤发邮件
        if (fileType.equals("zip")){
            //临时保存地址
            String tmpFolder = UUID.randomUUID().toString().concat("/");
            try{
                List<String> filePathList = ExeclUtil.batchadd(file, zipPath, tmpFolder);
                Map<String, List<WorkInfo>> work = workService.getWork(filePathList);
                mailService.send(work);
            }catch (Exception e){
                log.info("发送邮件失败[{}]", e);
                return "解析考勤失败";
            }finally {
                String[] cmd = new String[] { "/bin/sh", "-c", "rm -rf ".concat(zipPath).concat(tmpFolder)};
                log.info("执行删除命令[{}]", Arrays.deepToString(cmd));
                Runtime.getRuntime().exec(cmd);
            }
            return "考勤解析成功，请查看邮件";
        }else{//解析日报
            if (size > 10485760){
                log.info("图片名为[{}]大小为[{}]，图片过大不予操作", fileName, size);
                return "图片过大，请重新上传";
            }
            String currentDaily = dailyService.getCurrentDaily(file);
            return "日报内容已解析并复制[]" + currentDaily;
        }
    }
}
