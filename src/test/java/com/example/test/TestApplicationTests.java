package com.example.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.test.entity.API_TYPE;
import com.example.test.service.MailService;
import com.example.test.service.WorkService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.zip.ZipFileSystemConfigBuilder;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
class TestApplicationTests {


    @Autowired
    private StringEncryptor encryptor;

    @Autowired
    private MailService mailService;

    @Autowired
    private WorkService workService;

    @Test
    void contextLoads() {
        String id = "27238075";
        String encrypt = encryptor.encrypt(id);
        System.out.println(encrypt);
        /*CompletableFuture<Integer> objectCompletableFuture = CompletableFuture.supplyAsync(() -> {
            return 1;
        }).thenApply(ans -> {
            System.out.println(ans);
            return ++ans;
        });
        Object join = objectCompletableFuture.join();
        System.out.println(join);*/
    }

    @Test
    void apiTest(){
        String url = "https://www.mxnzp.com/api/holiday/list/month/202210/holiday?&app_id=qgeggjqpwknrrsiu&app_secret=cDd6N0NDSzVxSU1nUjgrOGdFMmh4Zz09";
        Response response;
        OkHttpClient client = new OkHttpClient();
        List<String> holiday = new ArrayList<>();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            response = client.newCall(request).execute();
            Map map = JSONObject.parseObject(response.body().string(), Map.class);
            Object data = ((JSONArray) map.get("data")).get(0);
            JSONArray days = (JSONArray) ((JSONObject) data).get("days");
            days.stream().forEach(e -> {
                String date = ((JSONObject) e).get("date").toString();
                holiday.add(date);
            });
            System.out.println("aaa");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void enumTest(){
        String aa = "WORKDAY";
        API_TYPE api_type = API_TYPE.valueOf(aa);
        System.out.println(api_type.getURL());
    }




    @Test
    void analyzeZip() throws FileSystemException {
        /**
         * 测试压缩文件
         */
        File file = new File("E:\\Desktop\\龙盈智达1月份考勤.zip");
        FileSystemManager manager = VFS.getManager();
        //FileObject fileObject = manager.resolveFile(file, "");
        String concat = "zip://".concat(file.getPath());
        FileSystemOptions fileSystemOptions = new FileSystemOptions();
        ZipFileSystemConfigBuilder.getInstance().setCharset(fileSystemOptions, Charset.forName("GBK"));
        FileObject fileObject = manager.resolveFile(concat, fileSystemOptions);
        FileObject[] children = fileObject.getChildren();
        System.out.println(children.length);
    }

    @Test
    void test(){
        //workService.getZipFile();
    }

}
