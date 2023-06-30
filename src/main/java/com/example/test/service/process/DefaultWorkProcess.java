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
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class DefaultWorkProcess extends AbstractWorkProcess {

    private ThreadLocal<Changer> changer = new ThreadLocal<>();

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
    public Map<String, WorkInfo> process(Sheet sheet) {
        changer.set(Changer.on);
        //临时结果
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
            //打卡位置
            String clockPosition = row.getCell(6).toString();
            workInfo.setClockPosition(clockPosition);
            tempAns.put(day, workInfo);
        }
        //标准工作日
        Map<String, WorkInfo> standardWorks = tempAns
                .keySet()
                .stream()
                //判断代打卡情况
                .peek(day -> {
                    WorkInfo workInfo = tempAns.get(day);
                    boolean proxyClock = judgeProxyClock(workInfo.getClockPosition());
                    if (proxyClock){
                        log.info("姓名[{}]，日期[{}]，可能存在代打卡情况", sheet.getRow(1).getCell(1).toString(), day);
                    }
                    workInfo.setProxyClock(proxyClock);
                    tempAns.put(day, workInfo);
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
        return ans;
    }

    /**
     * 判断代打卡情况
     *
     * @param queue 打卡位置队列
     * @return 是否代打卡
     */
    private boolean judgeProxyClock(List<String> queue) {
        //无打卡位置记录
        if (null == queue || queue.isEmpty()) {
            return false;
        }
        boolean ans = false;
        //打卡标识
        String clock = "汉军考勤机";
        for (String oneClock : queue) {
            if (oneClock.contains(changer.get().getInfo())) {
                changer.set(changer.get().getNext());
                continue;
            }
            if (changer.get() != DefaultWorkProcess.Changer.on && oneClock.contains(clock)) {
                continue;
            }
            ans = true;
        }
        return ans;
    }


    /**
     * 内部枚举开关
     */
    public enum Changer {
        on {
            @Override
            public String getInfo() {
                return "进";
            }

            @Override
            public Changer getNext() {
                return off;
            }
        },
        off {
            @Override
            public String getInfo() {
                return "出";
            }

            @Override
            public Changer getNext() {
                return on;
            }
        };

        /**
         * 表示信息
         *
         * @return 信息
         */
        abstract public String getInfo();

        /**
         * 获取下次开关
         *
         * @return 下次开关
         */
        abstract public Changer getNext();
    }
}
