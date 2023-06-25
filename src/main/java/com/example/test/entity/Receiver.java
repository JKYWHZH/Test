package com.example.test.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.vfs2.FileObject;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Random;

/**
 * 邮件接收人信息
 */
@Getter
@Setter
@Builder
public class Receiver implements Cloneable{

    /**
     * 接收人姓名
     */
    private String name;

    /**
     * 接收人邮箱地址
     */
    private String mailAddress;

    /**
     * 接收人所属的压缩文件信息
     */
    private FileObject fileObject;

    /**
     * 工作sheet页
     */
    private Sheet sheet;

    /**
     * 接收人的考勤信息
     */
    private List<WorkInfo> workInfos;

    /**
     * 5分钟内下班次数
     */
    private long worryCount;

    public long getWorryCount() {
        return workInfos
                .stream()
                .parallel()
                .filter(workInfo -> workInfo.getHome().getLevel().equals(WORK_TYPE.WORK_TYPE_LEVEL.WORRY_HOME))
                .count();
    }

    @Override
    public Receiver clone() {
        return Receiver.builder()
                .name(this.name)
                .mailAddress(this.mailAddress)
                .build();
    }
}
