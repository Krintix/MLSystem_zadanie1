package mlsystem.shared.util;

import java.util.Properties;

public class PropertiesUtil {
    public static boolean CheckForKeys(Properties properties, String[] keys)
    {
        for (String key : keys) {
            if(!properties.containsKey(key))
                return false;
        }
        return true;
    }
}
