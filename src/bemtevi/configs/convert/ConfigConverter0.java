package bemtevi.configs.convert;

import java.util.Properties;

import bemtevi.configs.Preferencias;


/**
 * Converte um arquivo de configuração da versão 0 pra versão 1.
 * Esta classe é utilizada apenas para testes.
 * 
 * @author edans
 */
public class ConfigConverter0 implements IConfigConverter {

	public Properties update(Properties old) {
		old.setProperty(Preferencias.KEY_CONFIG_VERSION, "1"); // Próxima versão
		return old; // Dummy
	}

}
