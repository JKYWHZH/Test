package com.example.test.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j(topic = "文件工具类")
public class FileUtil {

    /**
     * 合并文件
     * @param targetFile 合并文件存放地址
     * @param folder     带合并文件地址
     * @param filename   合并后文件名
     */
    public static void merge(String targetFile, String folder, String filename) {
        try {
            //Files.createDirectory(Paths.get(targetFile));
            Files.createFile(Paths.get(targetFile.concat("\\").concat(filename)));
            Files.list(Paths.get(folder))
                    .filter(path -> !path.getFileName().toString().equals(filename))
                    .sorted((o1, o2) -> {
                        String p1 = o1.getFileName().toString();
                        String p2 = o2.getFileName().toString();
                        return Integer.valueOf(p1).compareTo(Integer.valueOf(p2));
                    })
                    .forEach(path -> {
                        try {
                            //以追加的形式写入文件
                            Files.write(Paths.get(targetFile.concat("\\").concat(filename)), Files.readAllBytes(path), StandardOpenOption.APPEND);
                            System.out.println("合并文件块:"+path.getFileName());
                            //合并后删除该块
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 将MultipartFile转换为File
     * @param multiFile
     * @return
     */
    public static File MultipartFileToFile(MultipartFile multiFile) {
        // 获取文件名
        String fileName = multiFile.getOriginalFilename();
        // 获取文件后缀
        String prefix = fileName.substring(fileName.lastIndexOf("."));
        // 若须要防止生成的临时文件重复,能够在文件名后添加随机码
        try {
            File file = File.createTempFile(fileName, prefix);
            multiFile.transferTo(file);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
