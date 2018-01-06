package bemtevi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Seção contendo certidões semelhantes. As certidões de uma mesma seção
 * deve possuir o mesmo nome e ser gerada pelo mesmo parser.
 * 
 * @author edans
 */
public class CertidoesSecao implements Comparable<CertidoesSecao> {
	private List<Certidao> certidoes = new ArrayList<Certidao>();
	private String nomeCertidao;
	private int parserId = 0;

	public CertidoesSecao(String nomeCertidao) {
		this.nomeCertidao = nomeCertidao;
	}
	
	public String getNomeCertidao() {
		return nomeCertidao;
	}

	public int size() {
		return certidoes.size();
	}

	public Certidao get(int i) {
		return certidoes.get(i);
	}

	public void add(Certidao certidao) {
		if (!nomeCertidao.equals(certidao.getNomeCertidao())) {
			throw new RuntimeException("Certidões na mesma sessão devem possuir o mesmo nome");
		} else if (certidoes.size() == 0) {
			parserId = certidao.getParserId();
			nomeCertidao = certidao.getNomeCertidao();
		} else {
			if (parserId != certidao.getParserId()) {
				throw new RuntimeException("Certidões na mesma sessão devem ser do mesmo parser");
			}
		}
		certidoes.add(certidao);
	}
	
	public int compareTo(CertidoesSecao other) {
		int i;
		i = parserId - other.parserId;
		if (i != 0) {
			return i;
		}
		i = nomeCertidao.compareToIgnoreCase(other.nomeCertidao);
		return i;
	}

	public void sort() {
		Collections.sort(certidoes);	
	}
	
	public List<String> getHeaders() {
		Set<String> header = new LinkedHashSet<String>();
		
		header.add(Certidao.CAMPO_NOME);
		header.add(Certidao.CAMPO_CPF_CNPJ);
		header.add(Certidao.CAMPO_CODIGO_AUTENTICACAO);
		for (Certidao certidao : certidoes) {
			header.addAll(certidao.getInfoKeys());
		}
		
		return new ArrayList<String>(header);
	}

}
