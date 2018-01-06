package bemtevi.parsers;

/**
 * Retorna informações estáticas sobre um parser.
 * @author edans
 */
public class ParserInfo {
	/**
	 * Nome do parser.
	 */
	private String name;

	/**
	 * Construtor.
	 * @param name nome do parser.
	 */
	public ParserInfo(String name) {
		this.name = name;
	}

	/**
	 * Retorna o nome do parser.
	 * @return nome do parser
	 */
	public String getName() {
		return name;
	}
}
