package com.example.test.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.vfs2.FileObject;

import java.util.List;

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
     * 接收人的考勤信息
     */
    private List<WorkInfo> workInfos;

    @Override
    public Receiver clone() {
        return Receiver.builder()
                .name(this.name)
                .mailAddress(this.mailAddress)
                .build();
    }
}
