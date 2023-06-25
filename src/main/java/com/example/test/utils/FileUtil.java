package com.example.test.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j(topic = "文件工具类")
public class FileUtil {

    /**
     * 合并文件
     *
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
                            System.out.println("合并文件块:" + path.getFileName());
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
     *
     * @param multiFile 多媒体文件
     * @return 本地文件
     */
//    public static File MultipartFileToFile(MultipartFile multiFile) {
//        // 获取文件名
//        String fileName = multiFile.getOriginalFilename();
//        // 获取文件后缀
//        String prefix = fileName.substring(fileName.lastIndexOf("."));
//        // 若须要防止生成的临时文件重复,能够在文件名后添加随机码
//        String uuid = UUID.randomUUID().toString();
//        try {
//            File file = File.createTempFile(fileName.concat(uuid), prefix);
//            System.out.println(file.getAbsolutePath());
//            multiFile.transferTo(file);
//            return file;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    public static File MultipartFileToFile(MultipartFile file) throws Exception {
        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
