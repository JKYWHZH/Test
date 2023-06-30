package com.example.test.entity;

import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 考勤实体类
 */
@Data
public class WorkInfo {
    /**
     * 考勤人员姓名
     */
    private String name;

    /**
     * 考勤时间
     */
    private String date;

    /**
     * 考勤结果
     */
    private Boolean ans;

    /**
     * 考勤表地址
     */
    private String path;

    /**
     * 上班情况
     * 默认值为 无打卡记录
     */
    private WORK_TYPE work = WORK_TYPE.NULL;

    /**
     * 下班情况
     * 默认值为 无打卡记录
     */
    private WORK_TYPE home = WORK_TYPE.NULL;

    /**
     * 打卡位置队列
     */
    private List<String> clockPosition = Collections.synchronizedList(new LinkedList<>());

    /**
     * 代打卡情况
     */
    private Boolean proxyClock = false;

    public void setWork(WORK_TYPE work) {
        if (work.getLevel().getLevel() <= this.work.getLevel().getLevel()) {
            this.work = work;
        }
    }

    public void setHome(WORK_TYPE home) {
        if (home.getLevel().getLevel() <= this.home.getLevel().getLevel()) {
            this.home = home;
        }
    }

    public Boolean getAns() {
        Integer level = WORK_TYPE.WORK_TYPE_LEVEL.NO_NEED_DEAL.getLevel();
        if (work.getLevel().getLevel() < level && home.getLevel().getLevel() < level) {
            return true;
        }
        return false;
    }

    public void setClockPosition(String clockPosition) {
        //不计入打卡
        String[] nonIncluded = {"国际资源大厦", "万达"};
        if (Arrays.stream(nonIncluded).noneMatch(clockPosition::contains)) {
            this.clockPosition.add(clockPosition);
        }
    }
}
