package bemtevi.utils;

import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesUtil {

	public static Properties trimPrefixProperties(Properties properties,
			String profilePrefix) {
		if (!profilePrefix.endsWith(".")) {
			profilePrefix += "."; 
		}
		Properties profileProperties = new Properties();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String)entry.getKey();
			if (key.startsWith(profilePrefix)) {
				profileProperties.put(key.substring(profilePrefix.length()), entry.getValue());
			}
		}
		return profileProperties;
	}
	
	public static Properties addPrefixProperties(Properties profileProperties,
			String profilePrefix) {
		if (!profilePrefix.endsWith(".")) {
			profilePrefix += "."; 
		}
		Properties properties = new Properties();
		if (profileProperties != null) {
			for (Entry<Object, Object> prop : profileProperties.entrySet()) {
				properties.setProperty(profilePrefix + prop.getKey(), (String)prop.getValue());
			}
		}		
		return properties;
	}
}
