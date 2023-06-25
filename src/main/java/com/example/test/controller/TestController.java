package com.example.test.controller;

import com.example.test.entity.Receiver;
import com.example.test.service.DailyService;
import com.example.test.service.ExcelService;
import com.example.test.service.MailService;
import com.example.test.service.WorkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

@Slf4j(topic = "上传接口")
@RestController
public class TestController {

    @Resource
    private MailService mailService;

    @Resource
    private WorkService workService;

    @Resource
    private DailyService dailyService;

    @Resource
    private ExcelService excelService;

    /**
     * 支持文件类型
     */
    @Value("${mail.type}")
    private String type;

    @RequestMapping(method = RequestMethod.POST, value = "upload")
    public String start(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        long size = file.getSize();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        boolean contains = type.contains(fileType);
        if (!contains) {
            log.info("[{}]文件类型为[{}]，不予操作", fileName, fileType);
            return "不支持此文件类型";
        }
        //解析考勤发邮件
        if (fileType.equals("zip")) {
            List<Receiver> receivers;
            try {
                //获取用户压缩文件信息
                receivers = workService.getZipFileInfo(file);
            } catch (Exception e) {
                log.error("解析压缩包报错：[{}]", e);
                return "解析考勤失败";
            }
            //发送邮件
            mailService.send(receivers);
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

    @RequestMapping(method = RequestMethod.POST, value = "uploads")
    public void upload(HttpServletResponse response, @RequestParam("file") MultipartFile file) throws Exception {
        List<Receiver> receivers = workService.getZipFileInfo(file);
        response.setHeader("content-disposition", "attachment;filename*=utf-8''" + URLEncoder.encode("考勤统计.xlsx", "UTF-8"));
        Workbook export = excelService.export(receivers);
        ServletOutputStream outputStream = response.getOutputStream();
        export.write(outputStream);
        outputStream.close();
        export.close();
    }
}