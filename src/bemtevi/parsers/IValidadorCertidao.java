package bemtevi.parsers;

import java.io.IOException;

import bemtevi.model.Certidao;


/**
 * Esta interface deve ser implementada por todos os parsers que validam
 * certidões.
 * 
 * @author edans
 */
public interface IValidadorCertidao {
	/**
	 * Valida uma certidão. O controle de validação é realizado por meio de
	 * exceções. Assim, este método não deve retornar nenhuma exceção caso a
	 * certidão seja válida
	 * 
	 * @param certidao
	 *            certidão a ser analisada.
	 * @throws ValidationException
	 *             caso seja identificado que a certidão é inválida.
	 * @throws IOException
	 *             caso ocorra um erro durante a validação.
	 */
	public void validate(Certidao certidao) throws ValidationException,
			IOException;
}
