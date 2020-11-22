package common;

import client.view.manager.ResourceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    Properties prop;
    InputStream inputStream;

    private static Configuration instance;

    private Configuration () {
        try {
            prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Configuration getInstance () {
        if (Configuration.instance == null) {
            Configuration.instance = new Configuration ();
        }
        return Configuration.instance;
    }

    public String getProperty(String propertyName) {
        String property = prop.getProperty(propertyName);
        if(property == null) {
            throw new RuntimeException("Property " + propertyName + " not found");
        }
        return property;
    }
}
