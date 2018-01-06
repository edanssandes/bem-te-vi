package bemtevi.parsers;

import java.util.Properties;

/**
 * Objeto com as configurações específicas de um parser.
 * @author edans
 */
public abstract class AbstractParserConfig {
	/**
	 * Obtém as propriedades deste parser em um objeto {@link Properties}.
	 * @return propriedades deste parser.
	 */
	public abstract Properties getProperties();
	
	/**
	 * Define as propriedades deste parser utilizando um objeto {@link Properties}.
	 * @param properties propriedades deste parser.
	 */
	public abstract void setProperties(Properties properties);

}
