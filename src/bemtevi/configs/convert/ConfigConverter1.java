package bemtevi.configs.convert;

import java.util.Properties;

import bemtevi.configs.Preferencias;
import bemtevi.configs.PreferenciasPerfis;


/**
 * Converte um arquivo de configuração da versão 0 pra versão 1.
 * Esta classe é utilizada apenas para testes.
 * 
 * @author edans
 */
public class ConfigConverter1 implements IConfigConverter {

	public Properties update(Properties old) {
		old.setProperty(Preferencias.KEY_CONFIG_VERSION, "2"); // Próxima versão
		
		for (int i = 1; i <= 999; i++) {
			String keyOrgaos = "profile." + i + ".parsers.bemtevi.parsers.judiciario.trf1.ParserCertidaoTRF1.orgaosValidos";
			if (old.containsKey(keyOrgaos)) {
				String value = old.getProperty(keyOrgaos);
				old.setProperty(keyOrgaos, value.replace(',', ';'));
			}
			String keyTipos = "profile." + i + ".parsers.bemtevi.parsers.judiciario.trf1.ParserCertidaoTRF1.tiposValidos";
			if (old.containsKey(keyTipos)) {
				String value = old.getProperty(keyTipos);
				old.setProperty(keyTipos, value.replace(',', ';').replace("Cíveis; Criminais e JEF", "Cíveis, Criminais e JEF"));
				
			}			
		}

		return old; // Dummy
	}

}
