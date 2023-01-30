package com.example.test.controller;

import com.example.test.entity.Receiver;
import com.example.test.service.DailyService;
import com.example.test.service.MailService;
import com.example.test.service.WorkService;
import com.example.test.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
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

    @RequestMapping(method = RequestMethod.POST, value = "upload")
    public String start(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        long size = file.getSize();
        int i = fileName.lastIndexOf(".");
        String fileType = fileName.substring(i + 1, fileName.length());
        boolean contains = type.contains(fileType);
        if (!contains) {
            log.info("[{}]文件类型为[{}]，不予操作", fileName, fileType);
            return "不支持此文件类型";
        }
        //解析考勤发邮件
        if (fileType.equals("zip")) {
            try {
                //获取用户压缩文件信息
                List<Receiver> receivers = workService.getZipFileInfo(file);
                //发送邮件
                mailService.send(receivers);
            } catch (Exception e) {
                log.info("发送邮件失败[{}]", e);
                return "解析考勤失败";
            }
            return "考勤解析成功，请查看邮件";
        } else {//解析日报
            if (size > 10485760) {
                log.info("图片名为[{}]大小为[{}]，图片过大不予操作", fileName, size);
                return "图片过大，请重新上传";
            }
            String currentDaily = dailyService.getCurrentDaily(file);
            return "日报内容已解析[]" + currentDaily;
        }
    }

    /**
     * 校验接口
     * @param id 文件唯一标识
     * @return 文件唯一标识。若标识存在则续传，否则为新上传
     */
    @RequestMapping(method = RequestMethod.POST, value = "check")
    public String check(String id) {
        if (null == id || "".equals(id)){
            id = UUID.randomUUID().toString();
            File tempFile = new File(zipPath.concat(id));
            tempFile.mkdirs();
            return id;
        }
        File tempFile = new File(zipPath.concat(id));
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        return id;
    }

    /**
     * 分片上传接口
     * @param id      文件唯一标识
     * @param file    第skip块文件
     * @param skip    文件块数
     * @return  操作结果
     */
    @RequestMapping(method = RequestMethod.POST, value = "chunkUpload")
    public String chunkUpload(String id, MultipartFile file, String skip){
        File file1 = new File(zipPath.concat(id).concat("\\").concat(skip));
        try {
            file.transferTo(file1);
            log.info("[{}],[{}] is success", skip, file.getName());
            return "success";
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            log.info("[{}],[{}] is failed", skip, file.getName());
            return "fail";
        }
    }

    /**
     * 合并文件
     * @param id        文件id
     * @param fileName  文件名称
     * @return 操作结果
     */
    @RequestMapping(method = RequestMethod.POST, value = "merge")
    public String merge(String id, String fileName){
        FileUtil.merge(zipPath.concat(id), zipPath.concat(id), fileName);
        return null;
    }

}