package com.example.test.entity;

import lombok.Data;

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
     * 详情 结果为false时出现
     */
    private String desc;
}
