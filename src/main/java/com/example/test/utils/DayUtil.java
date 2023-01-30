package com.example.test.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.test.entity.API_TYPE;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "节假日工具类")
public class DayUtil {

    private static final String KONG = "休息";

    /**
     * 在线假期请求
     */
    private static final String HOLIDAY_URL = "https://www.mxnzp.com/api/holiday/list/month/";

    /**
     * 假期缓存
     */
    private static Map<String, List<String>> HOLIDAY_CACHE = new ConcurrentHashMap<>();

    public static List<String> get(List<String> content){
        Calendar cal = Calendar.getInstance();
        //当前月总天数
        int totals = getCurrentMonthDay();
        //当前年
        int currentYear = cal.get(Calendar.YEAR);
        //当前月
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        List<String> ans = new ArrayList<>(totals);
        //当前年月的节假日（包含周末）
        List<String> holiday = getHoliday(currentYear, currentMonth);
        //偏移量
        int day_offset = 0;
        for (int i = 1; i <= totals; i++) {
            String currentDay = String.valueOf(currentYear).concat(currentMonth < 10 ? "-0".concat(String.valueOf(currentMonth)) : "-".concat(String.valueOf(currentMonth)));
            if(i < 10){
                currentDay = currentDay.concat("-0").concat(String.valueOf(i));
            }else{
                currentDay = currentDay.concat("-").concat(String.valueOf(i));
            }
            if (holiday.contains(currentDay)){
                ans.add(KONG);
            }else{
                try {
                    String s = content.get(day_offset);
                    ans.add(s);
                    day_offset++;
                }catch (Exception e){
                    ans.add("");
                }
            }
        }
        return ans;
    }

    private static int getCurrentMonthDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.roll(Calendar.DATE, -1);
        int maxDate = cal.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取节假日
     * @param year   年
     * @param month  月
     * @return  节假日信息
     */
    private static List<String> getHoliday(int year, int month) {
        String url = HOLIDAY_URL.concat(String.valueOf(year));
        if (month >= 10){
            url = url.concat(String.valueOf(month));
        }else {
            url = url.concat("0").concat(String.valueOf(month));
        }
        if (HOLIDAY_CACHE.containsKey(url)) {
            return HOLIDAY_CACHE.get(url);
        }
        String ans_url = url.concat(API_TYPE.HOLIDAY.getURL());
        Response response;
        OkHttpClient client = new OkHttpClient();
        List<String> holiday = new ArrayList<>();
        Request request = new Request.Builder()
                .url(ans_url)
                .get()
                .build();
        try {
            response = client.newCall(request).execute();
            Map map = JSONObject.parseObject(response.body().string(), Map.class);
            Object data = ((JSONArray) map.get("data")).get(0);
            JSONArray days = (JSONArray) ((JSONObject) data).get("days");
            days.stream().forEach(e -> {
                String date = ((JSONObject) e).get("date").toString();
                holiday.add(date);
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return holiday;
    }
}
