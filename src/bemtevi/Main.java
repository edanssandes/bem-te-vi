package bemtevi;

import java.io.IOException;

import bemtevi.controller.MainController;


/**
 * Classe principal, contendo o ponto de entrada do programa (main).
 * @author edans
 */
public class Main {

	/**
	 * Ponto de entrada do programa
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Inicialização do controlador
		MainController controller = new MainController();
		controller.init();
	}

}
