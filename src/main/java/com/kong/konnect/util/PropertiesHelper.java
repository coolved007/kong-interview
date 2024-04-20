package com.kong.konnect.util;

import com.kong.konnect.config.Params;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHelper {

    public static Properties getProperties() {

        Properties props = null;
        try (InputStream input = PropertiesHelper.class.getClassLoader().getResourceAsStream(Params.PROPS_FILE_NAME)) {
            props = new Properties();
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return props;
    }

}
