package bemtevi.model.campos;

public class CampoVerificado implements ICampoCertidao {
	public enum Status {VAZIO, OK, PROBLEMA, INFO, DESCONHECIDO}; 
	
	private String situacao;
	private String observacoes;
	private Status status;
	
	public Status getStatus() {
		return status;
	}

	public CampoVerificado(String situacao) {
		this(situacao, Status.VAZIO, null);
	}	
	
	public CampoVerificado(String situacao, Status status) {
		this(situacao, status, null);
	}
	
	public CampoVerificado(String situacao, Status status, String observacoes) {
		this.situacao = situacao;
		this.status = status;
		this.observacoes = observacoes;
	}
	
	public CampoVerificado(CampoVerificado copia, String observacoes) {
		this.status = copia.status;
		this.situacao = copia.situacao;
		this.observacoes = observacoes;
	}
	
	public String getSituacao() {
		return situacao;
	}

	public String getObservacoes() {
		return observacoes;
	}
	
	@Override
	public String toString() {
		return situacao;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CampoVerificado)) {
			return false;
		}
		CampoVerificado campo = (CampoVerificado) obj;
		return status.equals(campo.status)
				&& situacao.equals(campo.situacao)
				&& (observacoes == null ? campo.observacoes == null
						: observacoes.equals(campo.observacoes));
	}
}
