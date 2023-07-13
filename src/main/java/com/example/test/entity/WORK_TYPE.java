package com.example.test.entity;

import com.example.test.utils.DayUtil;

/**
 * 考勤情况类型
 */
public enum WORK_TYPE {

    /**
     * 无打卡记录
     */
    NULL {
        @Override
        public String getInfo() {
            return "无打卡记录";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NEED_DEAL;
        }

        @Override
        public boolean judge(String time) {
            return false;
        }
    },
    /**
     * 非工作日考勤
     */
    NON_WORK {
        @Override
        public String getInfo() {
            return "非工作日考勤";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NON_WORK;
        }

        @Override
        public boolean judge(String time) {
            return false;
        }
    },
    /**
     * 工作日加班
     */
    OVER_WORK {
        @Override
        public String getInfo() {
            return "正常上班";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.OVER_WORK;
        }

        @Override
        public boolean judge(String time) {
            String begin = "00:00:00", end = "00:05:00";
            return DayUtil.judge(begin, end, time);
        }
    },
    /**
     * 正常上班
     */
    NORMAL_WORK {
        @Override
        public String getInfo() {
            return "正常上班";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NORMAL;
        }

        @Override
        public boolean judge(String time) {
            String begin = "06:45:00", end = "08:45:59";
            return DayUtil.judge(begin, end, time);
        }
    },
    /**
     * 正常下班，但5分钟内下班
     */
    WORRY_HOME {
        @Override
        public String getInfo() {
            return "正常下班";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.WORRY_HOME;
        }

        @Override
        public boolean judge(String time) {
            String begin = "17:45:00", end = "17:49:59";
            return DayUtil.judge(begin, end, time);
        }
    },
    /**
     * 正常下班
     */
    NORMAL_HOME {
        @Override
        public String getInfo() {
            return "正常下班";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NORMAL;
        }

        @Override
        public boolean judge(String time) {
            String begin = "17:50:00", end = "23:59:59";
            return DayUtil.judge(begin, end, time);
        }
    },
    /**
     * 上班迟到
     */
    LATE_WORK {
        @Override
        public String getInfo() {
            return "上班迟到";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NO_NEED_DEAL;
        }

        @Override
        public boolean judge(String time) {
            String begin = "08:46:00", end = "09:45:59";
            return DayUtil.judge(begin, end, time);
        }
    },
    /**
     * 上班未打卡
     */
    NO_CLOCK_WORK {
        @Override
        public String getInfo() {
            return "上班打卡异常";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NEED_DEAL;
        }

        @Override
        public boolean judge(String time) {
            String begin01 = "00:00:00", end01 = "06:44:59";
            String begin02 = "09:46:00", end02 = "13:45:59";
            return DayUtil.judge(begin01, end01, time) || DayUtil.judge(begin02, end02, time);
        }
    },
    /**
     * 下班早退
     */
    EARLY_HOME {
        @Override
        public String getInfo() {
            return "下班早退";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NO_NEED_DEAL;
        }

        @Override
        public boolean judge(String time) {
            String begin = "16:45:00", end = "17:44:59";
            return DayUtil.judge(begin, end, time);
        }
    },
    /**
     * 下班未打卡
     */
    NO_CLOCK_HOME {
        @Override
        public String getInfo() {
            return "下班打卡异常";
        }

        @Override
        public WORK_TYPE_LEVEL getLevel() {
            return WORK_TYPE_LEVEL.NEED_DEAL;
        }

        @Override
        public boolean judge(String time) {
            String begin = "13:46:00", end = "16:44:59";
            return DayUtil.judge(begin, end, time);
        }
    };

    /**
     * 获取考勤情况信息
     *
     * @return 考勤情况信息
     */
    abstract public String getInfo();

    /**
     * 获取考勤情况异常等级
     *
     * @return 考勤情况异常等级
     */
    abstract public WORK_TYPE_LEVEL getLevel();

    /**
     * 判断时间是否属于当前考勤情况
     *
     * @param time 时分秒
     * @return 是否属于当前考勤情况
     */
    abstract public boolean judge(String time);

    /**
     * 考勤情况处理等级
     */
    enum WORK_TYPE_LEVEL {

        /**
         * 工作日加班
         */
        OVER_WORK {
            @Override
            public Integer getLevel() {
                return -2;
            }
        },

        /**
         * 非工作日考勤 （加班）
         */
        NON_WORK {
            @Override
            public Integer getLevel() {
                return -1;
            }
        },
        /**
         * 上下班正常
         */
        NORMAL {
            @Override
            public Integer getLevel() {
                return 0;
            }
        },
        /**
         * 正常下班，但在规定时间5分钟内打卡
         */
        WORRY_HOME {
            @Override
            public Integer getLevel() {
                return 1;
            }
        },
        /**
         * 上下班异常，但无需处理
         */
        NO_NEED_DEAL {
            @Override
            public Integer getLevel() {
                return 10;
            }
        },
        /**
         * 上下班异常，需要处理
         */
        NEED_DEAL {
            @Override
            public Integer getLevel() {
                return 20;
            }
        };

        abstract public Integer getLevel();
    }
}
