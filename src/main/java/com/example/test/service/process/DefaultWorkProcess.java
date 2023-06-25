package com.example.test.service.process;

import com.example.test.entity.WORK_TYPE;
import com.example.test.entity.WorkInfo;
import com.example.test.utils.DayUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 默认考勤信息处理过程
 */
@Component
@Primary
public class DefaultWorkProcess extends AbstractWorkProcess {

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
     * 目前考勤模板，时间列为第5列（即下标第4列）
     */
    @Override
    public Map<String, WorkInfo> process(Sheet sheet) {
        Map<String, WorkInfo> tempAns = new LinkedHashMap<>();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            String data = row.getCell(4).toString();
            String[] timeArray = data.split(" ");
            String day = timeArray[0];
            String time = timeArray[1];
            WorkInfo workInfo;
            if (!tempAns.containsKey(day)) {
                workInfo = new WorkInfo();
                workInfo.setDate(day);
            } else {
                workInfo = tempAns.get(day);
            }
            Arrays.stream(WORK_TYPE.values())
                    .skip(2)
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
                .parallel()
                .map(day -> day.substring(0, day.lastIndexOf("-")))
                .distinct()
                .map(day -> DayUtil.getWorkDay(day))
                .flatMap(workInfoMap -> workInfoMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));

        //判断非工作日考勤
        tempAns.keySet().stream().parallel().forEach(day -> {
            if (!standardWorks.containsKey(day)) {
                WorkInfo workInfo = tempAns.get(day);
                workInfo.setWork(WORK_TYPE.NON_WORK);
                workInfo.setHome(WORK_TYPE.NON_WORK);
                tempAns.put(day, workInfo);
            }
        });

        //整合考勤信息和标准工作日
        Map<String, WorkInfo> ans = Stream.of(standardWorks, tempAns)
                .parallel()
                .flatMap(work -> work.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, TreeMap::new));

        return ans;
    }
}
