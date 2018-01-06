package bemtevi.model.campos;


public class CampoSituacao extends CampoVerificado {
	public static final CampoSituacao NADA_CONSTA = new CampoSituacao("NADA CONSTA", Status.OK);
	public static final CampoSituacao CONSTA = new CampoSituacao("CONSTA", Status.PROBLEMA);
	public static final CampoSituacao CERTIDAO_NEGATIVA = new CampoSituacao("CERT. NEGATIVA", Status.OK);
	public static final CampoSituacao CERTIDAO_POSITIVA_COM_EFEITOS_DE_NEGATIVA = new CampoSituacao("CERT. EF. DE NEGATIVA", Status.INFO);
	public static final CampoSituacao CERTIDAO_POSITIVA = new CampoSituacao("CERT. POSITIVA", Status.PROBLEMA);
	
	public CampoSituacao(String string, Status ok) {
		super(string, ok);
	}

	public CampoSituacao(CampoSituacao copia, String observacoes) {
		super(copia, observacoes);
	}
	
}
