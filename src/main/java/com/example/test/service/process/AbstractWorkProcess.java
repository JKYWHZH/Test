package com.example.test.service.process;

import com.example.test.entity.WorkInfo;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;

/**
 * 抽象考勤处理过程
 */
public abstract class AbstractWorkProcess {

    /**
     * 处理一页数据
     *
     * @param sheet 一页数据
     * @return 考勤信息实体
     */
    abstract public List<WorkInfo> process(Sheet sheet);
}
