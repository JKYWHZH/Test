package com.example.test.service;

import com.example.test.entity.Receiver;
import com.example.test.entity.WorkInfo;
import com.example.test.selector.UserSelector;
import com.example.test.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.zip.ZipFileObject;
import org.apache.commons.vfs2.provider.zip.ZipFileSystemConfigBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j(topic = "考勤业务类")
public class WorkService {

    /**
     * 邮件接收人
     */
    private static List<Receiver> receivers;

    /**
     * 下班时间
     */
    private static final String GO_HOME = "17:45:00";

    /**
     * 上班时间
     */
    private static final String GO_WORK = "08:45:00";

    /**
     * VFS访问zip前缀
     */
    private static final String ZIP_PREFIX = "zip://";

    public WorkService(List<Receiver> receivers) {
        this.receivers = receivers;
    }

    /**
     * 获取压缩文件信息
     * @param file 待处理压缩文件
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public List<Receiver> getZipFileInfo(MultipartFile file) throws Exception{
        FileSystemManager manager = VFS.getManager();
        File tempFile = FileUtil.MultipartFileToFile(file);
        if (null == tempFile) {
            return Collections.emptyList();
        }
        String concat = ZIP_PREFIX.concat(tempFile.getPath());
        FileSystemOptions fileSystemOptions = new FileSystemOptions();
        //设置字符集
        ZipFileSystemConfigBuilder.getInstance().setCharset(fileSystemOptions, Charset.forName("GBK"));
        FileObject fileObject = manager.resolveFile(concat, fileSystemOptions);
        //用户名称选择器
        UserSelector userSelector = new UserSelector(receivers);
        fileObject.findFiles(userSelector);
        List<Receiver> receivers = userSelector.getReceivers();
        for (Receiver receiver : receivers) {
            FileObject tempFileObject = receiver.getFileObject();
            if (null != tempFileObject) {
                List<WorkInfo> workInfos = readExcel((ZipFileObject) tempFileObject);
                //readExcel后 流会被关掉
                receiver.setWorkInfos(workInfos);
            }
        }
        return receivers;
    }


    /**
     * TODO 逻辑待优化
     * @param zipFileObject
     * @return
     * @throws IOException
     * @throws ParseException
     */
    private static List<WorkInfo> readExcel(ZipFileObject zipFileObject) throws IOException, ParseException {
        Workbook workbook = null;
        Sheet sheet = null;
        Row row = null;
        InputStream is = zipFileObject.getInputStream();
        if (zipFileObject.getName().toString().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(is);
        } else {
            workbook = new HSSFWorkbook(is);
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
            Date goHomeTime = format.parse(day.concat(" ").concat(GO_HOME));
            Date goWorkTime = format.parse(day.concat(" ").concat(GO_WORK));
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
}
