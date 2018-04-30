package bemtevi.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import bemtevi.AppVersion;
import bemtevi.configs.convert.ConfigConverter0;
import bemtevi.configs.convert.IConfigConverter;



/**
 * Classe contendo todas as preferências configuráveis da aplicação. As
 * preferências podem ser lidas ou salvas por meio de arquivos properties (
 * {@link #loadFile(File)} e {@link #saveFile(File)}). Os arquivos possuem um
 * controle de versão que permite atualização das configurações futuras (veja
 * {@link IConfigConverter}).
 * 
 * @author edans
 */
public class Preferencias {
	/**
	 * Chave para manter a versão do arquivo de preferências (controle de
	 * versão).
	 */
	public static final String KEY_CONFIG_VERSION = "config.version";

	/**
	 * Versão atual do arquivo de preferências.
	 */
	public static final String CONFIG_VERSION = "1";

	/**
	 * Conversores utilizados para atualizar versões antigas de preferências.
	 */
	private static Map<String, IConfigConverter> converters = new HashMap<String, IConfigConverter>();

	/**
	 * Configurações de proxy
	 */
	private PreferenciasProxy preferenciasProxy;

	/**
	 * Configurações de relatório
	 */
	private PreferenciasRelatorioHTML preferenciasRelatorioHTML;

	/**
	 * Configurações de perfies de certidões
	 */
	private PreferenciasPerfis preferenciasParsersPerfis;

	/*
	 * Inicializa os conversores.
	 */
	static {
		converters.put("0", new ConfigConverter0());
	}

	/**
	 * Inicializa as preferências com as configurações padrões.
	 */
	public Preferencias() {
		this(new PreferenciasProxy(), new PreferenciasRelatorioHTML(),
				new PreferenciasPerfis());
	}

	/**
	 * Inicializa as preferências com objetos de configurações personalizados.
	 */
	public Preferencias(PreferenciasProxy preferenciasProxy,
			PreferenciasRelatorioHTML preferenciasRelatorioHTML,
			PreferenciasPerfis preferenciasParsersPerfis) {
		this.preferenciasProxy = preferenciasProxy;
		this.preferenciasRelatorioHTML = preferenciasRelatorioHTML;
		this.preferenciasParsersPerfis = preferenciasParsersPerfis;
	}

	/**
	 * Carrega as configurações de um arquivo properties.
	 * 
	 * @param file
	 *            Arquivo a ser lido.
	 * @throws IOException
	 *             em caso de erro de leitura do arquivo.
	 */
	public void loadFile(File file) throws IOException {
		Properties properties = new Properties();
		if (file.exists()) {
			properties.load(new FileInputStream(file));
			String configVersion = properties.getProperty(KEY_CONFIG_VERSION);
			// TODO testar configVersion == null
			boolean updated = false;
			while (configVersion != CONFIG_VERSION
					&& converters.containsKey(configVersion)) {
				properties = converters.get(configVersion).update(properties);
				configVersion = properties.getProperty(KEY_CONFIG_VERSION);
				updated = true;
			}
			setProperties(properties);
			if (updated) {
				saveFile(file);
			}
		}
	}

	/**
	 * Salva as configurações em um arquivo properties.
	 * 
	 * @param file
	 *            Arquivo a ser salvo.
	 * @throws IOException
	 *             em caso de erro de escrita no arquivo.
	 */
	public void saveFile(File file) throws IOException {
		/*
		 * O arquivo de propriedades é salvo com as chaves ordenadas. Para isso,
		 * foi criada um objeto de propriedades com as chaves ordenadas.
		 */
		@SuppressWarnings("serial")
		Properties properties = new Properties() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Enumeration keys() {
				Enumeration<Object> keysEnum = super.keys();
				Vector<String> keyList = new Vector<String>();
				while (keysEnum.hasMoreElements()) {
					keyList.add((String) keysEnum.nextElement());
				}
				Collections.sort(keyList);
				return keyList.elements();
			}
		};
		properties.setProperty(KEY_CONFIG_VERSION, CONFIG_VERSION); // Versão
		properties.putAll(getProperties()); // Todas as propriedades
		properties.store(new FileOutputStream(file), "Config "
				+ AppVersion.APP_NAME + " - " + AppVersion.VERSION);
	}

	/**
	 * Retorna as preferências de proxy.
	 * 
	 * @return preferências de proxy.
	 */
	public PreferenciasProxy getPreferenciasProxy() {
		return preferenciasProxy;
	}

	/**
	 * Retorna as preferências dos relatórios HTML.
	 * 
	 * @return preferências dos relatórios HTML.
	 */
	public PreferenciasRelatorioHTML getPreferenciasRelatorioHTML() {
		return preferenciasRelatorioHTML;
	}

	/**
	 * Retorna as preferências dos perfis de certidões.
	 * 
	 * @return preferências dos perfis.
	 */
	public PreferenciasPerfis getPreferenciasParsersPerfis() {
		return preferenciasParsersPerfis;
	}
	
	/**
	 * Retorna um objeto {@link Properties} contendo todas as configurações.
	 * 
	 * @return o objeto com as configurações que serão salvas no arquivo.
	 */
	private Properties getProperties() {
		Properties properties = new Properties();
		properties.putAll(preferenciasProxy.getProperties());
		properties.putAll(preferenciasRelatorioHTML.getProperties());
		properties.putAll(preferenciasParsersPerfis.getProperties());
		return properties;
	}

	/**
	 * Atribui um objeto {@link Properties} para todas as configurações.
	 * 
	 * @param properties
	 *            Objeto com as configurações vindas do arquivo.
	 */
	private void setProperties(Properties properties) {
		preferenciasProxy.setProperties(properties);
		preferenciasRelatorioHTML.setProperties(properties);
		preferenciasParsersPerfis.setProperties(properties);
	}	
}
