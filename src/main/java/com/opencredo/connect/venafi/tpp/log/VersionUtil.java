package com.opencredo.connect.venafi.tpp.log;

import java.io.IOException;
import java.util.Properties;

public class VersionUtil {
    public static String getVersion() {
        final Properties properties = new Properties();
        try {
            properties.load(VersionUtil.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty("project.version","0.0.1");

    }
}
