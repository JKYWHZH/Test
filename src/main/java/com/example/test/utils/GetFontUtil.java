package com.example.test.utils;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetFontUtil{

    private static String RESULT = "words_result";

    private static String WORDS = "words";

    public static List<String> getContent (AipOcr aipOcr, MultipartFile file) {
        HashMap<String,String> options = new HashMap<>(4);
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true"); // 检测图片朝上
        options.put("detect_language", "true");  // 检测语言,默认是不检查
        options.put("probability", "false");   //是否返回识别结果中每一行的置信度

        byte[] bytes = new byte[0];
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = aipOcr.basicAccurateGeneral(bytes, options);
        JSONArray words_result = (JSONArray)jsonObject.get(RESULT);
        List<String> ans = new ArrayList<>();
        words_result.toList().stream().forEach(object -> {
            Map map = com.alibaba.fastjson.JSONObject.parseObject(com.alibaba.fastjson.JSONObject.toJSONString(object), Map.class);
            ans.add(map.get(WORDS).toString());
        });
        return  ans;
    }
}

