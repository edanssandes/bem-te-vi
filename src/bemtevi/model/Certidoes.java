package bemtevi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Lista de certidões organizada em seções.
 * 
 * @author edans
 */
public class Certidoes implements Iterable<Certidao> {
	private List<Certidao> certidoes = new ArrayList<Certidao>();
	private List<CertidoesSecao> secoes = new ArrayList<CertidoesSecao>();
	private Map<String, CertidoesSecao> secoesMap = new HashMap<String, CertidoesSecao>();

	public Certidoes() {
		// TODO Auto-generated constructor stub
	}

	public int size() {
		return certidoes.size();
	}
	
	public void clear() {
		certidoes.clear();
		secoes.clear();
		secoesMap.clear();
	}
	
	public Certidao get(int i) {
		return certidoes.get(i);
	}

	public void add(Certidao certidao) {
		certidoes.add(certidao);
		String nomeCertidao = certidao.getNomeCertidao();
		CertidoesSecao secao = null;  
		if (!secoesMap.containsKey(nomeCertidao)) {
			secao = new CertidoesSecao(nomeCertidao);
			secoesMap.put(nomeCertidao, secao);
			secoes.add(secao);
		} else {
			secao = secoesMap.get(nomeCertidao);
		}
		secao.add(certidao);
	}
	
	

	public void sort() {
		Collections.sort(certidoes);
		Collections.sort(secoes);
		for (CertidoesSecao sessao : secoes) {
			sessao.sort();
		}		
	}

	public Iterator<Certidao> iterator() {
		return certidoes.iterator();
	}

	public List<CertidoesSecao> getSecoes() {
		return secoes;
	}
	
	public CertidoesSecao getSecao(String key) {
		return secoesMap.get(key);
	}

	public void add(Collection<? extends Certidao> c) {
		for (Certidao certidao : c) {
			this.add(certidao);
		}
	}
}
