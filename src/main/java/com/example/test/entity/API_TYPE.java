package com.example.test.entity;

public enum API_TYPE {

    WORKDAY{
        @Override
        public String getURL() {
            return "/workday?app_id=".concat(APP_ID).concat("&app_secret=").concat(SECRET_KEY);
        }
    },
    HOLIDAY {
        @Override
        public String getURL() {
            return "/holiday?app_id=".concat(APP_ID).concat("&app_secret=").concat(SECRET_KEY);
        }
    },
    REST {
        @Override
        public String getURL() {
            return "/rest?&app_id=".concat(APP_ID).concat("&app_secret=").concat(SECRET_KEY);
        }
    },
    FESTIVAL {
        @Override
        public String getURL() {
            return "/festival?&app_id=".concat(APP_ID).concat("&app_secret=").concat(SECRET_KEY);
        }
    };

    public abstract String getURL();

    final static String APP_ID = "qgeggjqpwknrrsiu";

    final static String SECRET_KEY = "cDd6N0NDSzVxSU1nUjgrOGdFMmh4Zz09";
}
