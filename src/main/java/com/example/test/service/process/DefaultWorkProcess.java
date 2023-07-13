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

/**
 * 默认考勤信息处理过程
 */
@Component
@Primary
@Slf4j
public class DefaultWorkProcess extends AbstractWorkProcess {

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
     * 目前考勤模板，时间列为第5列（即下标第4列），门禁系统读卡器为第7列（即下标第6列）
     */
    @Override
    public List<WorkInfo> process(Sheet sheet) {
        //默认不允许打卡
        allowClock.set(false);
        //临时结果
        Map<String, WorkInfo> tempAns = new LinkedHashMap<>();
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            String data = row.getCell(4).toString();
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
            //打卡位置
            String clockPosition = row.getCell(6).toString();
            //不参与判断打卡
            String[] nonIncluded = {"国际资源大厦", "万达"};
            if (Arrays.stream(nonIncluded).noneMatch(clockPosition::contains)) {
                workInfo.setProxyClock(judgeProxyClock(time, clockPosition));
            }
            Arrays.stream(WORK_TYPE.values())
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
                .peek(day -> {
                    WorkInfo workInfo = tempAns.get(day);
                    if (workInfo.getProxyClock()) {
                        log.info("姓名[{}]，日期[{}]，可能存在代打卡情况", sheet.getRow(1).getCell(1).toString(), day);
                    }
                })
                .map(day -> day.substring(0, day.lastIndexOf("-")))
                .distinct()
                .map(DayUtil::getWorkDay)
                .flatMap(workInfoMap -> workInfoMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));

        //判断非工作日考勤
        tempAns.keySet().stream().parallel().forEach(day -> {
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

        List<WorkInfo> collect = ans
                .values()
                .stream()
                .parallel()
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * 判断代打卡情况
     *
     * @param time          时分秒
     * @param clockPosition 打卡位置信息
     * @return 是否代打卡
     */
    private boolean judgeProxyClock(String time, String clockPosition) {
        //无打卡位置记录
        if (null == clockPosition || clockPosition.isEmpty()) {
            return false;
        }
        boolean ans = false;
        //打卡标识
        String clock = "汉军考勤机";
        //进入标识
        String in = "进";
        //出门标识
        String out = "出";
        if (clockPosition.contains(in)) {
            allowClock.set(true);
            return ans;
        }
        if (clockPosition.contains(out)) {
            allowClock.set(false);
            return ans;
        }
        Boolean isAllow = allowClock.get();
        //允许打卡 并且 正常打卡记录
        if (isAllow && clockPosition.contains(clock)) {
            ans = false;
        }
        //不允许打卡 但是 有正常打卡纪律
        if (!isAllow && clockPosition.contains(clock)) {
            //判断加班
            if (WORK_TYPE.OVER_WORK.judge(time)) {
                ans = false;
                allowClock.set(true);
            } else {
                ans = true;
            }
        }
        return ans;
    }

}
