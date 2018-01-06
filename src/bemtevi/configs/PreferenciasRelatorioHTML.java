package bemtevi.configs;

import java.util.Properties;

/**
 * Classe contendo as preferências do relatório HTML.
 * 
 * @author edans
 */
public class PreferenciasRelatorioHTML {
	/*
	 * Chaves do arquivo de propriedades.
	 */
	
	/**
	 * Chave do arquivo de propriedades que indica o rodapé do relatório.
	 */
	private static final String KEY_REPORT_HTML_FOOTER = "reportHTML.footer";
	
	/**
	 * Chave do arquivo de propriedades que indica o cabeçalho do relatório.
	 */
	private static final String KEY_REPORT_HTML_HEADER = "reportHTML.header";
	
	/**
	 * Cabeçalho do relatório.
	 */
	private String header = "";
	
	/**
	 * Rodapé do relatório.
	 */
	private String footer = "";
	
	/**
	 * Retorna o objeto {@link Properties} contendo as configurações que serão
	 * salvas em arquivo. 
	 * 
	 * @return objeto {@link Properties} com as configurações
	 */
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty(KEY_REPORT_HTML_HEADER, header);
		properties.setProperty(KEY_REPORT_HTML_FOOTER, footer);
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
		header = properties.getProperty(KEY_REPORT_HTML_HEADER, "");
		footer = properties.getProperty(KEY_REPORT_HTML_FOOTER, "");
	}

	/**
	 * Retorna o cabeçalho do relatório.
	 * @return o cabeçalho do relatório.
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Define o cabeçalho do relatório. 
	 * @param header o cabeçalho do relatório.
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * Retorna o rodapé do relatório.
	 * @return o rodapé do relatório.
	 */
	public String getFooter() {
		return footer;
	}

	/**
	 * Define o rodapé do relatório. 
	 * @param header o rodapé do relatório.
	 */
	public void setFooter(String footer) {
		this.footer = footer;
	}
	
}
