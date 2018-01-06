package bemtevi.configs.convert;

import java.util.Properties;

/**
 * Interface utilizada para criar conversores de arquivo de configuração. Cada
 * conversor altera o arquivo de configuração de uma versão para a versão
 * imediatamente superior. Desta forma, o arquivo de configuração passa por uma
 * cadeia de transformação até chegar na versão atual.
 * 
 * @author edans
 */
public interface IConfigConverter {
	/**
	 * Altera o objeto {@link Properties} da versão anterior para a nova versão.
	 * 
	 * @param old
	 *            Versão anterior das configurações.
	 * @return versão nova das configurações.
	 */
	public Properties update(Properties old);
}
