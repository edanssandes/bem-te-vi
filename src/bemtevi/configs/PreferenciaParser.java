package bemtevi.configs;

import java.util.Properties;

import bemtevi.parsers.AbstractParserConfig;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IParserConfigurable;
import bemtevi.utils.PropertiesUtil;


/**
 * Configurações de um analisador (parser) de certidões. Todo parser é associado
 * a um objeto {@link PreferenciaParser}, que possui configurações comuns a
 * todos eles (e.g. se o parser está ativo ou não). Entretanto, alguns parsers
 * podem possuir configurações adicionais. Neste caso, o parser deve implementar
 * a interface {@link IParserConfigurable} e associar uma subclasse
 * {@link AbstractParserConfig} com as suas configurações específicas.
 * 
 * @author edans
 */
public class PreferenciaParser {

	/**
	 * Chave do arquivo de propriedades que indica se o parser está ativo.
	 */
	private static final String KEY_PARSER_ACTIVE = "active";

	/**
	 * Indica se o parser está ativo
	 */
	private boolean ativo = true;

	/**
	 * Objeto relacionado com o parser desta configuração.
	 */
	private IParserCertidao parser = null;

	/**
	 * Configurações específicas do parser.
	 */
	private AbstractParserConfig config = null;

	/**
	 * Constroi um objeto de preferências para um parser específico.
	 * 
	 * @param parser
	 */
	public PreferenciaParser(IParserCertidao parser) {
		this.parser = parser;
	}

	/**
	 * Indica se um parser está ativo ou não.
	 * 
	 * @return booleano que indica se o parser está ativo.
	 */
	public boolean isAtivo() {
		return ativo;
	}

	/**
	 * Define se um parser está ativo ou não.
	 * 
	 * @param ativo
	 *            indica se o parser está ativo.
	 */
	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	/**
	 * Retorna as configurações específicas deste parser (ou nulo caso esse
	 * parser não possua configurações específicas).
	 * 
	 * @return configurações específicas do parser.
	 */
	public AbstractParserConfig getConfig() {
		return config;
	}

	/**
	 * Define as configurações espefícicas deste parser.
	 * 
	 * @param config
	 *            configurações específicas.
	 */
	public void setConfig(AbstractParserConfig config) {
		this.config = config;
	}

	/**
	 * Retorna o nome do parser.
	 * 
	 * @return nome do parser.
	 */
	public String getName() {
		return parser.getParserInfo().getName();
	}

	/**
	 * Retorna o objeto {@link Properties} contendo as configurações que serão
	 * salvas em arquivo.
	 * 
	 * @return objeto {@link Properties} com as configurações
	 */
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty(getKeyPrefix() + "." + KEY_PARSER_ACTIVE,
				Boolean.toString(ativo));
		if (parser instanceof IParserConfigurable) {
			Properties parserProperties = config.getProperties();
			properties.putAll(PropertiesUtil.addPrefixProperties(
					parserProperties, getKeyPrefix()));
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
		String ativoStr = properties.getProperty(getKeyPrefix() + "."
				+ KEY_PARSER_ACTIVE, "true");
		ativo = Boolean.parseBoolean(ativoStr);
		Properties profileProperties = PropertiesUtil.trimPrefixProperties(
				properties, getKeyPrefix());
		if (parser instanceof IParserConfigurable) {
			config = ((IParserConfigurable) parser).createConfig();
			config.setProperties(profileProperties);
		}
	}

	/**
	 * Retorna o parser relacionado.
	 * 
	 * @return o parser relacionado.
	 */
	public IParserCertidao getParser() {
		return parser;
	}
	
	/**
	 * Prefixo da chave de configuração. Este prefixo é a classe do parser.
	 * 
	 * @return prefixo da chave de configurações.
	 */
	private String getKeyPrefix() {
		return parser.getClass().getCanonicalName();
	}

}
