package com.example.test.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.test.entity.API_TYPE;
import com.example.test.entity.WORK_TYPE;
import com.example.test.entity.WorkInfo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "节假日工具类")
public class DayUtil {

    private static final String KONG = "休息";

    /**
     * 在线假期请求
     */
    private static final String HOLIDAY_URL = "https://www.mxnzp.com/api/holiday/list/month/";

    /**
     * 缓存
     */
    private static Map<String, List<String>> HOLIDAY_CACHE = new ConcurrentHashMap<>();

    public static List<String> getHoliday(List<String> content){
        Calendar cal = Calendar.getInstance();
        //当前月总天数
        int totals = getCurrentMonthDay();
        //当前年
        int currentYear = cal.get(Calendar.YEAR);
        //当前月
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        List<String> ans = new ArrayList<>(totals);
        //当前年月的节假日（包含周末）
        List<String> holiday = get(currentYear, currentMonth, API_TYPE.HOLIDAY);
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

    /**
     * 获取工作日
     * @param date 年月日 （1997-05-06）
     * @return 工作日
     */
    public static Map<String, WorkInfo> getWorkDay(String date){
        String[] split = date.split("-");
        String url = HOLIDAY_URL.concat(split[0]).concat(split[1]);
        String ansUrl = url.concat(API_TYPE.WORKDAY.getURL());
        List<String> days = get(ansUrl);
        Map<String, WorkInfo> ans = new LinkedHashMap<>();
        for (String day : days) {
            WorkInfo workInfo = new WorkInfo();
            workInfo.setWork(WORK_TYPE.NULL);
            workInfo.setHome(WORK_TYPE.NULL);
            workInfo.setDate(day);
            ans.put(day, workInfo);
        }
        return ans;
    }

    private static Date string2Date(String day, String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            Date parse = simpleDateFormat.parse(day);
            return parse;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getCurrentMonthDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.roll(Calendar.DATE, -1);
        int maxDate = cal.get(Calendar.DATE);
        return maxDate;
    }

    private static List<String> get(int year, int month, API_TYPE apiType) {
        String url = HOLIDAY_URL.concat(String.valueOf(year));
        if (month >= 10){
            url = url.concat(String.valueOf(month));
        }else {
            url = url.concat("0").concat(String.valueOf(month));
        }

        String ansUrl = url.concat(apiType.getURL());
        return get(ansUrl);
    }

    /**
     * 通过url获取日历信息
     * @param url 请求路径
     * @return 日历信息
     */
    private static List<String> get(String url) {
        if (HOLIDAY_CACHE.containsKey(url)) {
            return HOLIDAY_CACHE.get(url);
        }
        Response response;
        OkHttpClient client = new OkHttpClient();
        List<String> day = new LinkedList<>();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            response = client.newCall(request).execute();
            Map map = JSONObject.parseObject(response.body().string(), Map.class);
            Object data = JSONArray.parseArray(map.get("data").toString()).get(0);
            JSONArray days = (JSONArray) ((JSONObject) data).get("days");
            days.forEach(e -> {
                String date = ((JSONObject) e).get("date").toString();
                day.add(date);
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        HOLIDAY_CACHE.put(url, day);
        return day;
    }

    /**
     * 判断时间是否属于区间内（左右为包含）
     * @param begin 开始时间
     * @param end   结束时间
     * @param time  待比较时间
     * @return 是否属于区间内
     */
    public static boolean judge(String begin, String end, String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date beginDate = null;
        Date endDate = null;
        Date timeDate = null;
        try {
            beginDate = simpleDateFormat.parse(begin);
            endDate = simpleDateFormat.parse(end);
            timeDate = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            return false;
        }
        //时间落在时间区间边界
        if (timeDate.getTime() == beginDate.getTime() || timeDate.getTime() == endDate.getTime()){
            return true;
        }
        //时间落在时间区间内
        if (timeDate.after(beginDate) && timeDate.before(endDate)){
            return true;
        }
        return false;
    }
}
