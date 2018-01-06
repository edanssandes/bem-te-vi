package bemtevi.parsers;

/**
 * Exceção que indica se a certidão é inválida.
 * 
 * @author edans
 */
public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ValidationException() {
	}

	public ValidationException(String msg) {
		super(msg);
	}
}
