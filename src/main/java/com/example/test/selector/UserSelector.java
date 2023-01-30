package com.example.test.selector;

import com.example.test.entity.Receiver;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;

import java.util.List;

/**
 * 用户选择器
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
    public boolean includeFile(FileSelectInfo fileInfo){
        FileObject file = fileInfo.getFile();
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
