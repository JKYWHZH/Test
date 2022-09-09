package com.example.test.utils;

import org.apache.poi.ss.usermodel.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DayUtil {

    private static String kong = "休息";

    public static List<String> get(List<String> content){
        Calendar cal = Calendar.getInstance();
        int totals = getCurrentMonthDay();
        int size = content.size();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        String tag = String.valueOf(currentYear).concat("/");
        if(currentMonth < 10){
            tag = tag.concat("0").concat(String.valueOf(currentMonth));
        }else{
            tag = tag.concat(String.valueOf(currentMonth));
        }
        List<String> ans = new ArrayList<>(totals);
        //偏移量
        int day = 0;
        for (int i = 0; i < totals; i++) {
            int currentDay = i + 1;
            Date date;
            if(currentDay < 10){
                date = DateUtil.parseYYYYMMDDDate(tag.concat("/").concat("0").concat(String.valueOf(currentDay)));
            }else{
                date = DateUtil.parseYYYYMMDDDate(tag.concat("/").concat(String.valueOf(currentDay)));
            }
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if(dayOfWeek != 1 && dayOfWeek != 7){
                if (day >= size){
                    ans.add("");
                }else{
                    ans.add(content.get(day));
                }
                day++;
            }else{
                ans.add(kong);
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

}
