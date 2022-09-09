package com.example.test.service;

import com.example.test.entity.WorkInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.test.utils.ExeclUtil.readExcel;

@Service
@Slf4j(topic = "文件业务类")
public class FileService {

    /**
     * 获取地址
     * @param userNames
     * @return
     * @throws IOException
     */
    public static Map<String, WorkInfo> getPath(List<String> userNames, List<String> paths) throws IOException {

        Map<String, WorkInfo> ans = new LinkedHashMap<>(userNames.size());
        //寻找匹配人文件
        userNames.forEach(userName -> {
            CompletableFuture<Void> tmpFuture = CompletableFuture.runAsync(() -> {
                for (String path : paths) {
                    if (path.contains(userName)) {
                        WorkInfo workInfo = new WorkInfo();
                        workInfo.setName(userName);
                        workInfo.setPath(path);
                        ans.put(userName, workInfo);
                        break;
                    }
                }
            });
            tmpFuture.join();
        });

        return ans;
    }

    /**
     * 获取考勤情况
     * @return
     */
    public Map<String, List<WorkInfo>> getWorkInfos(Map<String, WorkInfo> workInfoMap){
        Map<String, List<WorkInfo>> ans = new LinkedHashMap<>(workInfoMap.size());
        workInfoMap.forEach((key, val) -> {
                WorkInfo workInfo = workInfoMap.get(key);
                if (workInfo != null) {
                    try {
                        //获取execl中考勤情况
                        List<WorkInfo> workInfos = readExcel(workInfo.getPath());
                        ans.put(key, workInfos);
                    } catch (IOException | ParseException e) {
                        log.error("获取用户[{}]考勤时，报错[{}]", key, e.getCause().getMessage(), e);
                    }
                }
        });
        return ans;
    }
}
