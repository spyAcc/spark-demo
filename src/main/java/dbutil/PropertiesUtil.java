package dbutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件读工具类，单例模式
 */
public class PropertiesUtil {

    private Properties prop;

    public Properties getProp() {
        return prop;
    }

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private PropertiesUtil() {
        prop = new Properties();
    }

    public static PropertiesUtil propertiesUtil = null;

    public static synchronized PropertiesUtil getPropertiesUtilInstance(String path) {
        if(propertiesUtil == null) {
            propertiesUtil = new PropertiesUtil();
            propertiesUtil.loadProperites(path);
        }
        return propertiesUtil;
    }


    private void loadProperites(String path) {
        InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream(path);
        try {
            prop.load(in);
        } catch (IOException e) {
            logger.error("load kafka config error: " , e);
        }
    }


}
