package com.example.test.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "cmd工具类")
public class CmdUtil {
    public static void runCmd(String command) {
        try {
            Process process = Runtime.getRuntime().exec("cmd /C " + command);
        } catch (Exception e) {
            log.error("执行cmd任务时报错，[{}]", e.getCause().getMessage());
        }
    }
}
