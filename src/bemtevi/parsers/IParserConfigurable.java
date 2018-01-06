package bemtevi.parsers;

/**
 * Esta interface deve ser implementada por todos os parsers que possuem
 * algum tipo de configuração específica.
 * 
 * @author edans
 */
public interface IParserConfigurable<T extends AbstractParserConfig> {
	/**
	 * Obtém a janela contendo as configurações específicas desse parser.
	 * @return Janela que extende {@link AbstractParserDialog}
	 */
	public AbstractParserDialog<T> getConfigDialog();
	
	/**
	 * Define as configurações específicas deste parser.
	 * @param config configurações específicas deste parser.
	 */
	public void setConfig(T config);
	
	/**
	 * Cria um objeto de configuração para este parser, contendo as configurações
	 * padrões.
	 * @return as configurações específicas deste parser.
	 */
	public T createConfig();
	
}
