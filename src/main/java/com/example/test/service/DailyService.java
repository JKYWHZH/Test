package com.example.test.service;

import com.baidu.aip.ocr.AipOcr;
import com.example.test.utils.DayUtil;
import com.example.test.utils.GetFontUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j(topic = "考勤业务类")
public class DailyService {

    @Resource
    private AipOcr aipOcr;

    /**
     * 获取当前日报信息
     * @param file 图片文件
     * @return 解析后的日报文件
     */
    public String getCurrentDaily(MultipartFile file){
        //获取图片中文本内容
        List<String> content = GetFontUtil.getContent(aipOcr, file);
        //区分节假日
        List<String> daytData = DayUtil.getHoliday(content);
        String ans = "";
        for (int i = 0; i < daytData.size(); i++) {
            ans = ans.concat(daytData.get(i)).concat("\n");
        }
        return ans;
    }
}
