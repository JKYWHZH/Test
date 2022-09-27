package com.example.test.utils;

import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DayUtil {

    private static final String KONG = "休息";

    private static final String HOLIDAY_URL = "http://timor.tech/api/holiday/year";

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
     * 获取节假日（不包含周末）
     * @param year   年
     * @param month  月
     * @return  节假日信息
     */
    private static Map getJjr(int year, int month) {
        String url = HOLIDAY_URL.concat("/").concat(String.valueOf(year));
        if (month >= 10){
            url = url.concat("-").concat(String.valueOf(month));
        }else {
            url = url.concat("-0").concat(String.valueOf(month));
        }
        OkHttpClient client = new OkHttpClient();
        Response response;
        //解密数据
        String rsa = null;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            response = client.newCall(request).execute();
            rsa = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map map = JSONObject.parseObject(rsa, Map.class);
        Map<String, Map<String, Object>> holiday = (Map<String, Map<String, Object>>) map.get("holiday");
        return holiday;
    }

    /**
     * 获取节假日
     * @param year   年
     * @param month  月
     * @return  返回假期集合
     */
    private static List<String> getHoliday(int year, int month){
        List<String> ans = new ArrayList<>();
        SimpleDateFormat simdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar(year, month - 1, 1);
        Calendar endCalendar = new GregorianCalendar(year, month - 1, 1);
        endCalendar.add(Calendar.MONTH, 1);
        while (true) {
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
            if (weekday == 1 || weekday == 7) {
                ans.add(simdf.format(calendar.getTime()));
            }
            calendar.add(Calendar.DATE, 1);
            if (calendar.getTimeInMillis() >= endCalendar.getTimeInMillis()) {
                break;
            }
        }
        Map<String, Map<String, Object>> holiday = getJjr(year, month);
        Set<String> strings = holiday.keySet();
        for (String str : strings) {
            Map<String, Object> stringObjectMap = holiday.get(str);
            Integer wage = (Integer) stringObjectMap.get("wage");
            String date = (String) stringObjectMap.get("date");
            //筛选掉补班
            if (wage.equals(1)) {
                ans.remove(date);
            } else {
                if (!ans.contains(date)) {
                    ans.add(date);
                }
            }
        }
        Collections.sort(ans);
        return ans;
    }

}
