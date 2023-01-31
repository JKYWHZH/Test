package com.example.test.selector;

import com.example.test.entity.Receiver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;

import java.util.List;

/**
 * 用户选择器
 * 包含条件：1. 包含用户列表中的姓名
 *         2. 是文件而不是文件夹
 */
public class UserSelector implements FileSelector {

    /**
     * 接收人信息
     */
    private static List<Receiver> receivers;

    public UserSelector(List<Receiver> receivers){
        this.receivers = receivers;
    }

    public List<Receiver> getReceivers() {
        return receivers;
    }

    @Override
    public boolean includeFile(FileSelectInfo fileInfo) throws FileSystemException {
        FileObject file = fileInfo.getFile();
        if (file.isFolder()) {
            return false;
        }
        String fileName = file.getName().toString();
        if (null != receivers && 0 != receivers.size()) {
            for (Receiver receiver : receivers) {
                if (fileName.contains(receiver.getName())) {
                    receiver.setFileObject(file);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * true 开启递归
     * @param fileInfo
     * @return
     */
    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo){
        return true;
    }
}
