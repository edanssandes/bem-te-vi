package bemtevi.model.campos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Campo que agrega vários objetos em uma lista. Este tipo de campo é
 * apresentado em várias linhas.
 * 
 * @author edans
 */
public class CampoLista implements ICampoCertidao {
	private List<Object> lista = new ArrayList<Object>();

	public CampoLista(Collection<? extends Object> lista) {
		this.lista.addAll(lista);
	}

	public CampoLista() {
	}

	public void addItem(Object item) {
		lista.add(item);
	}

	public int size() {
		return lista.size();
	}

	public Object getItem(int i) {
		return lista.get(i);
	}

	public List<Object> getItems() {
		return lista;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CampoLista)) {
			return false;
		}
		CampoLista campo = (CampoLista) obj;
		return lista.equals(campo.lista);
	}
}
