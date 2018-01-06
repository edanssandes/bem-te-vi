package bemtevi.model.campos;

/**
 * Campos da certidão que possuem formato de data e indicam a validade da
 * certidão. Estes campos são validados nos relatórios para identificar
 * certidões expiradas.
 * 
 * @author edans
 */
public class CampoDataValidade extends CampoData {

	/**
	 * Cria um campo de validade a partir de uma string.
	 * 
	 * @param dateStr
	 *            string da data de validade.
	 */
	public CampoDataValidade(String dateStr) {
		super(dateStr);
	}

	/**
	 * Cria um campo de validade a partir de uma outra data.
	 * 
	 * @param date
	 *            data de validade.
	 */
	public CampoDataValidade(CampoData date) {
		super(date);
	}

}
