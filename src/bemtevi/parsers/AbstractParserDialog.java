package bemtevi.parsers;

import javax.swing.JPanel;

/**
 * Janela que permite editar as configurações deste parser.
 * @author edans
 *
 * @param <T> configurações deste parser.
 */
public abstract class AbstractParserDialog<T extends AbstractParserConfig> extends JPanel {
	/**
	 * Obtém as configurações definidas pelo usuário nesta janela.
	 * @return configurações confirmadas pelo usuário.
	 */
	public abstract T getConfig();
	
	/**
	 * Define os campos desta janela a partir das configurações passadas
	 * por parâmetro.
	 * @param config configurações a serem editadas.
	 */
	public abstract void setConfig(T config);
}
