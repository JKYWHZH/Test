package com.example.test.service;

import com.example.test.entity.Receiver;
import com.example.test.entity.WorkInfo;
import com.example.test.service.process.AbstractWorkProcess;
import com.example.test.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.zip.ZipFileObject;
import org.apache.commons.vfs2.provider.zip.ZipFileSystemConfigBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j(topic = "考勤业务类")
public class WorkService {

    /**
     * 邮件接收人
     */
    private static List<Receiver> receivers;

    private static AbstractWorkProcess workProcess;

    /**
     * VFS访问zip前缀
     */
    private static final String ZIP_PREFIX = "zip://";

    public WorkService(List<Receiver> receivers, AbstractWorkProcess workProcess) {
        this.workProcess = workProcess;
        /**
         * 通过重写clone方法 进行深拷贝 避免修改bean中用户值
         */
        this.receivers = IntStream.rangeClosed(0, receivers.size() - 1)
                .mapToObj(i -> receivers.get(i))
                .parallel()
                .map(Receiver::clone)
                .collect(Collectors.toList());
    }

    /**
     * 获取压缩文件信息
     * @param file 待处理压缩文件
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public List<Receiver> getZipFileInfo(MultipartFile file) throws Exception {
        FileSystemManager manager = VFS.getManager();
        File tempFile = FileUtil.MultipartFileToFile(file);
        if (null == tempFile) {
            return Collections.emptyList();
        }
        String concat = ZIP_PREFIX.concat(tempFile.getAbsolutePath());
        FileSystemOptions fileSystemOptions = new FileSystemOptions();
        //设置字符集
        ZipFileSystemConfigBuilder.getInstance().setCharset(fileSystemOptions, Charset.forName("GBK"));
        FileObject fileObject = manager.resolveFile(concat, fileSystemOptions);
        //用户名称选择器
        FileObject[] files = fileObject.findFiles(new FileTypeSelector(FileType.FILE));

        List<Receiver> collect = Arrays.stream(files)
                .parallel()
                .map(tempFileObject -> {
                    String baseName = tempFileObject.getName().getBaseName();
                    Receiver build = null;
                    try {
                        String username = baseName.substring(baseName.lastIndexOf("-") + 1, baseName.lastIndexOf("."));
                        Optional<Receiver> first = receivers
                                .stream()
                                .parallel()
                                .filter(receiver -> receiver.getName().equals(username))
                                .findFirst();
                        build = Receiver
                                .builder()
                                .name(username)
                                .workInfos(readExcel((ZipFileObject) tempFileObject))
                                .mailAddress(first.isPresent() ? first.get().getMailAddress() : null)
                                .build();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return build;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        tempFile.delete();
        return collect;
    }


    /**
     * TODO: 逻辑待优化
     * @param zipFileObject
     * @return
     * @throws IOException
     */
    private static List<WorkInfo> readExcel(ZipFileObject zipFileObject) throws IOException {
        Workbook workbook = null;
        Sheet sheet = null;
        InputStream is = zipFileObject.getInputStream();
        if (zipFileObject.getName().toString().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(is);
        } else {
            workbook = new HSSFWorkbook(is);
        }
        //获取Excel表单
        sheet = workbook.getSheetAt(0);
        log.info(zipFileObject.getName().toString());
        Map<String, WorkInfo> process = workProcess.process(sheet);
        List<WorkInfo> collect = process
                .values()
                .stream()
                .parallel()
                .collect(Collectors.toList());
        is.close();
        workbook.close();
        return collect;
    }
}
