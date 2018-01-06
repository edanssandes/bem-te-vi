package bemtevi.configs;

import java.util.Properties;

/**
 * Classe contendo as preferências de proxy.
 * 
 * @author edans
 */
public class PreferenciasProxy {

	/**
	 * Enumeração de tipos de Proxy (nenhum, manual ou proxy do sistema). 
	 */
	public static enum ProxyType {
		NENHUM("none"), SISTEMA("system"), MANUAL("manual"); 
		String value;  // String a ser salva no arquivo de propriedades
		ProxyType(String value) {
			this.value = value;
		}
	};
	
	/*
	 * Chaves do arquivo de propriedades.
	 */
	
	/**
	 * Chave do arquivo de propriedades que indica o tipo de proxy.
	 */
	private static final String KEY_PROXY_TYPE = "proxy.type";

	/**
	 * Chave do arquivo de propriedades que indica a porta do proxy.
	 */
	private static final String KEY_PROXY_PORT = "proxy.port";
	
	/**
	 * Chave do arquivo de propriedades que indica o endereço do proxy.
	 */
	private static final String KEY_PROXY_HOST = "proxy.host";

	/**
	 * Tipo de proxy.
	 */
	private ProxyType proxyType = ProxyType.NENHUM;
	
	/**
	 * Endereço do proxy.
	 */
	private String proxyHost = "";
	
	/**
	 * Porta do proxy.
	 */
	private String proxyPort = "";
	
	/**
	 * Retorna o objeto {@link Properties} contendo as configurações que serão
	 * salvas em arquivo. 
	 * 
	 * @return objeto {@link Properties} com as configurações
	 */	
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty(KEY_PROXY_TYPE, proxyType.value);
		if (proxyType == ProxyType.MANUAL) {
			properties.setProperty(KEY_PROXY_HOST, proxyHost);
			properties.setProperty(KEY_PROXY_PORT, proxyPort);
		}
		return properties;
	}
	
	/**
	 * Atribui um objeto {@link Properties} para todas as configurações.
	 * 
	 * @param properties
	 *            Objeto com as configurações provenientes do arquivo de
	 *            configurações.
	 */	
	public void setProperties(Properties properties) {
		String proxyTypeStr = properties.getProperty(KEY_PROXY_TYPE);
		proxyType = ProxyType.NENHUM; // Default
		if (proxyTypeStr != null) {
			for (ProxyType e : ProxyType.values()) {
				if (proxyTypeStr.equals(e.value)) {
					proxyType = e;
					break;
				}
			}
		}
		if (proxyType == ProxyType.MANUAL) {
			proxyHost = properties.getProperty(KEY_PROXY_HOST);
			proxyPort = properties.getProperty(KEY_PROXY_PORT);
		} else {
			// Defaults
			proxyHost = "";
			proxyPort = "";
		}
	}

	/**
	 * Retorna o tipo de proxy selecionado. Ver {@link ProxyType}.
	 * @return o tipo de proxy selecionado.
	 */
	public ProxyType getProxyTipo() {
		return proxyType;
	}

	/**
	 * Define o tipo de proxy selecionado. Ver {@link ProxyType}.
	 * @param proxyTipo o tipo de proxy selecionado.
	 */
	public void setProxyTipo(ProxyType proxyTipo) {
		this.proxyType = proxyTipo;
	}

	/**
	 * Retorna o hostname do proxy, quando houver.
	 * @return o hostname do proxy.
	 */
	public String getProxyHost() {
		return proxyHost;
	}

	/**
	 * Define o hostname do proxy.
	 * @param proxyHost o hostname do proxy.
	 */
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	/**
	 * Retorna a porta do proxy.
	 * @return a porta do proxy.
	 */
	public String getProxyPort() {
		return proxyPort;
	}

	/**
	 * Define a porta do proxy.
	 * @param proxyPort a porta do proxy.
	 */
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}
}
