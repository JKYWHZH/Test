package com.example.test.utils;

import com.example.test.entity.WorkInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class ExeclUtil {

    public static List<WorkInfo> readExcel(String fileName) throws IOException, ParseException {
        Workbook workbook = null;
        Sheet sheet = null;
        Row row = null;

        //读取Excel文件
        File excelFile = new File(fileName.trim());
        InputStream is = new FileInputStream(excelFile);

        //获取Excel工作薄
        if (excelFile.getName().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(is);
        } else {
            workbook = new HSSFWorkbook(is);
        }
        if (workbook == null) {
            System.err.println("Excel文件有问题,请检查！");
            return null;
        }
        //获取Excel表单
        sheet = workbook.getSheetAt(0);
        Map<String, WorkInfo> tmpAns = new LinkedHashMap<>();
        for(int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            //获取一行
            row = sheet.getRow(rowNum);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String data = row.getCell(4).toString();
            String[] s = data.split(" ");
            //no contain this time
            String day = s[0];
            Date goHomeTime = format.parse(day.concat(" 17:45:00"));
            Date goWorkTime = format.parse(day.concat(" 08:45:00"));
            Date sourceTime = format.parse(data);
            WorkInfo workInfo;
            if (!tmpAns.containsKey(day)) {
                workInfo = new WorkInfo();
                workInfo.setDate(day);
                //date1.cpmpareTo(date2) date1小于date2返回-1，date1大于date2返回1，相等返回0
                //大于回家时间为正常打卡
                if (sourceTime.compareTo(goHomeTime) == 1) {
                    workInfo.setDesc("下班正常");
                }
                //小于上班时间为正常上班
                if (sourceTime.compareTo(goWorkTime) == -1){
                    workInfo.setDesc("上班正常");
                }
            }else{
                workInfo = tmpAns.get(day);
                //大于回家时间为正常打卡
                if (sourceTime.compareTo(goHomeTime) == 1) {
                    if (workInfo.getDesc() == null) {
                        workInfo.setDesc("下班正常");
                    }else{
                        workInfo.setDesc(workInfo.getDesc().concat("下班正常"));
                    }
                }
                //小于上班时间为正常上班
                if (sourceTime.compareTo(goWorkTime) == -1){
                    if (workInfo.getDesc() == null) {
                        workInfo.setDesc("上班正常");
                    }else{
                        workInfo.setDesc(workInfo.getDesc().concat("上班正常"));
                    }
                }
            }
            tmpAns.put(day, workInfo);
        }
       List<WorkInfo> ans = new LinkedList<>();
        tmpAns.forEach((key, val) -> {
            String desc = val.getDesc();
            if (desc.contains("上班正常")){
                val.setDesc("上班正常");
                val.setAns(true);
            }else{
                val.setDesc("上班异常");
                val.setAns(false);
            }
            if (desc.contains("下班正常")){
                val.setDesc(val.getDesc().concat(", 下班正常"));
                if (!val.getAns()) {
                    val.setAns(false);
                }else {
                    val.setAns(true);
                }
            }else{
                val.setDesc(val.getDesc().concat(", 下班异常"));
                val.setAns(false);
            }
            ans.add(val);
        });
        is.close();
        return ans;
    }

    /**
     * 保存zip文件到本地并调用解压方法并返回解压出的文件的路径集合
     *
     * @param file 文件
     * @return list //解压出的文件的路径合集
     */
    public static List<String> batchadd(MultipartFile file, String zipPath, String tmpFolder) {
        /*
         *创建临时文件夹
         * 解压文件
         */
        String fileName = file.getOriginalFilename();
        File dir = new File(zipPath);
        dir.mkdirs();
        String filePath = zipPath + tmpFolder;
        File fileDir = new File(filePath);
        fileDir.mkdirs();
        File saveFile = new File(fileDir, fileName);//将压缩包解析到指定位置
        List<String> paths = new ArrayList<>();
        String newFilePath = "";
        try {
            file.transferTo(saveFile);
            newFilePath = filePath + fileName;
            //开启异步线程解压文件
            //String commond = "unzip -O CP936 ".concat(newFilePath);
            String[] cmd = new String[] { "/bin/sh", "-c", "unzip -O CP936 -d ".concat(filePath+" ").concat(newFilePath)};
            log.info("解压命令[{}]", Arrays.deepToString(cmd));
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (Exception e) {
                    log.error("执行cmd任务时报错，[{}]", e.getCause().getMessage());
                }
            });
            future.join();
            ZipFile zipFile = new ZipFile(newFilePath, Charset.forName("GBK"));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String concat = filePath.concat(entry.getName());
                paths.add(concat);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("解压执行失败");
        }
        return paths;
    }
}
