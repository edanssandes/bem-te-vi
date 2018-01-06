package bemtevi.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IParserConfigurable;
import bemtevi.parsers.ParserSingletons;
import bemtevi.utils.PropertiesUtil;


/**
 * Configurações de um perfil de análise de certidões. Cada perfil possui
 * informações sobre quais analisadores de certidões estão ativos. Além disso,
 * cada analisador pode possuir configurações específicas associados a cada
 * perfil. Para um analisador possuir configurações específicas, ele precisa
 * implementar a interface {@link IParserConfigurable}.
 * 
 * @author edans
 */
public class PreferenciasPerfil {
	/*
	 * Chaves do arquivo de propriedades.
	 */

	/**
	 * Chave do arquivo de propriedades que indica o nome do perfil.
	 */
	private static final String KEY_PROFILE_NAME = "name";

	/**
	 * Prefixo de chave do arquivo de propriedades que indica as configurações
	 * de cada analisador.
	 */
	private static final String KEY_PARSER_PREFIX = "parsers";

	/**
	 * Nome deste perfil.
	 */
	private String nomePerfil;

	/**
	 * Lista contendo as configurações de cada perfil.
	 */
	private List<PreferenciaParser> preferencias = new ArrayList<PreferenciaParser>();

	/**
	 * Cria um objeto sem informações de preferência.
	 */
	public PreferenciasPerfil() {
		// Nenhuma informação
	}

	/**
	 * Cria um objeto de preferências com um nome específico e com todas as
	 * configurações padrões dos analisadores.
	 * 
	 * @param nomePerfil
	 *            Nome do novo perfil a ser criado.
	 */
	public PreferenciasPerfil(String nomePerfil) {
		this.nomePerfil = nomePerfil;
		for (IParserCertidao parser : ParserSingletons.getParsers()) {
			preferencias.add(new PreferenciaParser(parser));
		}
	}

	/**
	 * Retorna todas as preferências dos analisadores (parser), ativos o não.
	 * 
	 * @return todas as preferências dos analisadores.
	 */
	public List<PreferenciaParser> getPreferencias() {
		return preferencias;
	}

	/**
	 * Retorna todas as preferências dos analisadores ativos.
	 * 
	 * @return todas as preferências dos analisadores ativos.
	 */
	public List<PreferenciaParser> getPreferenciasAtivas() {
		ArrayList<PreferenciaParser> preferenciasAtivas = new ArrayList<PreferenciaParser>();
		for (PreferenciaParser preferencia : preferencias) {
			if (preferencia.isAtivo()) {
				// Seleciona apenas as preferencias ativas
				preferenciasAtivas.add(preferencia);
			}
		}
		return preferenciasAtivas;
	}

	/**
	 * Adiciona a preferencia de um analisador na lista de preferências do
	 * perfil.
	 * 
	 * @param preferencia
	 *            preferência do analisador a ser adicionada.
	 */
	public void addPreferencia(PreferenciaParser preferencia) {
		preferencias.add(preferencia);
	}

	/**
	 * Retorna o objeto {@link Properties} contendo as configurações que serão
	 * salvas em arquivo.
	 * 
	 * @return objeto {@link Properties} com as configurações
	 */
	public Properties getProperties() {
		Properties properties = new Properties();
		properties.put(KEY_PROFILE_NAME, nomePerfil);
		for (PreferenciaParser preferencia : preferencias) {
			properties.putAll(PropertiesUtil.addPrefixProperties(
					preferencia.getProperties(), KEY_PARSER_PREFIX));
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
		nomePerfil = properties.getProperty(KEY_PROFILE_NAME);
		for (IParserCertidao parser : ParserSingletons.getParsers()) {
			PreferenciaParser preferencia = new PreferenciaParser(parser);
			preferencia.setProperties(PropertiesUtil.trimPrefixProperties(
					properties, KEY_PARSER_PREFIX));
			preferencias.add(preferencia);
		}
	}

	/**
	 * Retorna o nome do perfil.
	 * 
	 * @return o nome do perfil.
	 */
	public String getNomePerfil() {
		return nomePerfil;
	}

	/**
	 * Define o nome do perfil.
	 * 
	 * @param nomePerfil
	 *            o nome do perfil.
	 */
	public void setNomePerfil(String nomePerfil) {
		this.nomePerfil = nomePerfil;
	}

	@Override
	public String toString() {
		return this.nomePerfil;
	}
}
