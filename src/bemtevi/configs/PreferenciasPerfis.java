package bemtevi.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import bemtevi.utils.PropertiesUtil;


/**
 * Classe contendo uma lista com todos os perfis de análise de certidão 
 * ({@link PreferenciasPerfil}). O primeiro perfil da lista é sempre
 * um perfil padrão, que não deve ser editado.
 * 
 * @author edans
 */
public class PreferenciasPerfis {
	/**
	 * Prefixo de chave do arquivo de propriedades que indica as configurações
	 * de cada perfil.
	 */
	private static final String KEY_PROFILE_PREFIX = "profile";
	
	/**
	 * Número máximo de perfis permitidos.
	 */
	private static final int MAX_PROFILES = 999;
	
	/**
	 * Lista contando a lista de perfis.
	 */
	private List<PreferenciasPerfil> perfis = new ArrayList<PreferenciasPerfil>();

	/**
	 * Construtor das preferências, onde é criado apenas o perfil padrão.
	 */
	public PreferenciasPerfis() {
		createPerfilPadrao();
	}

	/**
	 * Retorna a lista com todos os perfis de análise.
	 * @return a lista com todos os perfis de análise.
	 */
	public List<PreferenciasPerfil> getPerfis() {
		return perfis;
	}

	/**
	 * Retorna o perfil no índice i da lista.
	 * @param i índice do perfil a ser retornado.
	 * @return perfil no índice i.
	 */
	public PreferenciasPerfil getPerfil(int i) {
		return perfis.get(i);
	}

	/**
	 * Retorna o perfil padrão (primeiro da lista). Este perfil não deve
	 * ser editado.
	 * 
	 * @return perfil padrão.
	 */
	public PreferenciasPerfil getPerfilPadrao() {
		return getPerfil(0);
	}

	/**
	 * Cria um novo perfil com todas as configurações padrões.
	 * @param nome nome do novo perfil.
	 */
	public void createPerfil(String nome) {
		perfis.add(new PreferenciasPerfil(nome));
	}

	/**
	 * Exclui um determinado perfil.
	 * @param i índice do perfil a ser deletad.
	 */
	public void deletePerfil(int i) {
		perfis.remove(i);
	}

	/**
	 * Retorna o objeto {@link Properties} contendo as configurações que serão
	 * salvas em arquivo. O perfil padrão (idx=0) nunca é salvo.
	 * 
	 * @return objeto {@link Properties} com as configurações
	 */
	public Properties getProperties() {
		Properties properties = new Properties();
		for (int i = 1; i < perfis.size(); i++) {
			PreferenciasPerfil perfil = perfis.get(i);
			Properties perfilProperties = perfil.getProperties();
			properties.putAll(PropertiesUtil.addPrefixProperties(
					perfilProperties, getProfilePrefix(i)));
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
		perfis.clear();
		createPerfilPadrao();
		for (int i = 1; i <= MAX_PROFILES; i++) {
			Properties profileProperties = PropertiesUtil.trimPrefixProperties(
					properties, getProfilePrefix(i));
			if (profileProperties.size() > 0) {
				PreferenciasPerfil perfil = new PreferenciasPerfil();
				perfil.setProperties(profileProperties);
				perfis.add(perfil);
			} else {
				break;
			}
		}
	}

	/**
	 * Retorna um array com o nome de todos os perfis.
	 * @return um array com o nome de todos os perfis.
	 */
	public String[] getNomesPerfis() {
		String[] nomes = new String[perfis.size()];
		for (int i = 0; i < perfis.size(); i++) {
			PreferenciasPerfil perfil = perfis.get(i);
			nomes[i] = perfil.getNomePerfil();
		}
		return nomes;
	}

	/**
	 * Cria o perfil padrão. Este perfil nunca deve ser alterado.
	 */
	private void createPerfilPadrao() {
		createPerfil("Padrão");
	}

	/**
	 * Retorna o prefixo da chave que será salva no arquivo de configurações.
	 * @param i índice do prefixo.
	 * @return prefixo da chave de um determinado perfil. 
	 */
	private String getProfilePrefix(int i) {
		return KEY_PROFILE_PREFIX + "." + i;
	}
}
