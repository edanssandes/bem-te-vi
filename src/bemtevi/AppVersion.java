package bemtevi;

import java.util.Properties;

/**
 * Classe contendo a versão corrente do aplicativo.
 * @author edans
 */
public class AppVersion {
	public static final String VERSION;
	public static final String APP_NAME;
	public static final String BUILD_DATE;

	static {
		Properties props = new Properties();
		String version;
		String appName;
		String buildDate;
		try {
			props.load(AppVersion.class.getResourceAsStream("version.properties"));
			version = props.getProperty("version");
			appName = props.getProperty("app.name");
			buildDate = props.getProperty("build.date");
		} catch (Exception e1) {
			version = "(em desenvolvimento)";
			appName = "Bem-te-vi";
			buildDate = "(em desenvolvimento)";
			e1.printStackTrace();
		}
		VERSION = version;
		APP_NAME = appName;
		BUILD_DATE = buildDate;
	}

}