package com.example.test.service.process;

import com.example.test.entity.WORK_TYPE;
import com.example.test.entity.WorkInfo;
import com.example.test.utils.DayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.test.entity.WORK_TYPE.values;

/**
 * 默认考勤信息处理过程
 */
@Component
@Primary
@Slf4j
public class NoNameWorkProcess extends AbstractWorkProcess {

    //是否允许打卡
    private ThreadLocal<Boolean> allowClock = new ThreadLocal<>();

    /**
     * 处理需求（左右为包含关系）
     * 00：00 ~ 06：44 打卡 为 上班未打卡
     * 06：45 ~ 08：45 打卡 为 正常上班
     * 08：46 ~ 09：45 打卡 为 上班迟到
     * 09：46 ~ 13：45 打卡 为 上班未打卡
     * 13：46 ~ 16：44 打卡 为 下班未打卡
     * 16：45 ~ 17：44 打卡 为 下班早退
     * 17：45 ~ 23：59 打卡 为 正常下班
     * 以上时间区间解释为，当且仅当一人一天打一次卡出现的记录
     */

    /**
     * 目前考勤模板，时间列为第3列（即下标第2列）
     */
    final int arrange = 2;

    @Override
    public List<WorkInfo> process(Sheet sheet) {
        //临时结果
        Map<String, WorkInfo> tempAns = new LinkedHashMap<>();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            String data = row.getCell(arrange).toString();
            String[] timeArray = data.split(" ");
            //年月日
            String day = timeArray[0];
            //时分秒
            String time = timeArray[1];
            WorkInfo workInfo;
            //新一天数据
            if (!tempAns.containsKey(day)) {
                workInfo = new WorkInfo();
                workInfo.setDate(day);
            } else { //同一天数据
                workInfo = tempAns.get(day);
            }

            Arrays.stream(values())
                    .skip(3)
                    .parallel()
                    .forEach(workType -> {
                        if (workType.judge(time)) {
                            if (workType.getInfo().contains("上班")) {
                                workInfo.setWork(workType);
                            }
                            if (workType.getInfo().contains("下班")) {
                                workInfo.setHome(workType);
                            }
                        }
                    });

            tempAns.put(day, workInfo);
        }

        //标准工作日
        Map<String, WorkInfo> standardWorks = tempAns
                .keySet()
                .stream()
                .map(day -> day.substring(0, day.lastIndexOf("-")))
                .distinct()
                .map(DayUtil::getWorkDay)
                .flatMap(workInfoMap -> workInfoMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));

        //判断非工作日考勤
        tempAns
                .keySet()
                .stream()
                .parallel()
                .forEach(day -> {
                    if (!standardWorks.containsKey(day)) {
                        WorkInfo workInfo = tempAns.get(day);
                        workInfo.setWork(WORK_TYPE.NON_WORK);
                        workInfo.setHome(WORK_TYPE.NON_WORK);
                        //设置代打卡情况
                        workInfo.setProxyClock(false);
                        tempAns.put(day, workInfo);
                    }
                });

        //整合考勤信息和标准工作日
        Map<String, WorkInfo> ans = Stream.of(standardWorks, tempAns)
                .parallel()
                .flatMap(work -> work.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, TreeMap::new));

        return ans
                .values()
                .stream()
                .parallel()
                .collect(Collectors.toList());
    }
}
