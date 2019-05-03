package bemtevi.controller.workers;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

import bemtevi.Main;
import bemtevi.configs.PreferenciasProxy;
import bemtevi.controller.AbstractWorker;
import bemtevi.controller.MainController;
import bemtevi.utils.ParserUtil;
import bemtevi.utils.WebResponse;


/**
 * Tarefa que testa a conexão de internet.
 * 
 * @author edans
 */
public class WorkerBaixarAtualizacao extends AbstractWorker {
	/**
	 * URLs que serão testadas.
	 */
	private static String[] URLS = new String[] { 
			"http://gmail.com",
			"http://www.terra.com.br",
			"http://www.receita.fazenda.gov.br", 
			"http://www.fazenda.df.gov.br",
			"https://www.google.com", 
			"https://receita.fazenda.gov.br",
			"https://consulta-crf.caixa.gov.br",
			"https://portal.trf1.jus.br",
			"https://autenticador.trt10.jus.br",
			MainController.PROJECT_WEBSITE 
			};

	/**
	 * Versão a ser baixada
	 */
	private String versao;

	/**
	 * Construtor.
	 * 
	 * @param mainController
	 *            Controlador principal
	 */
	public WorkerBaixarAtualizacao(MainController mainController, String versao) {
		super(mainController);
		this.versao = versao;
	}

	/*
	 * (non-Javadoc)
	 * @see certidao.controller.AbstractWorker#executeJob()
	 */
	@Override
	protected void executeJob() throws Exception {
		startProgress("Baixando Atualização...");
		
		String diretorio = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
			    .toURI()).getPath();
		String arquivo = "bemtevi-" + versao + ".jar";
		
		URL url = new URL("https://raw.githubusercontent.com/edanssandes/bem-te-vi/master/versoes/" + arquivo);
		System.out.println(diretorio);
		
		WebResponse response = ParserUtil.downloadFromURL(url, null, null);
		
		File destino = new File(diretorio, arquivo);
		if (destino.exists()) {
			JOptionPane.showMessageDialog(mainController.getMainFrame(), 
					"<html>O aplicativo mais atual já se encontra no diretório: <br>"
					+ destino.getAbsolutePath() + "<br><br>"
					+ "Execute este novo arquivo na próxima execução.</br>"
					+ "</html>");
		} else {
			response.saveFile(destino);
			JOptionPane.showMessageDialog(mainController.getMainFrame(), 
					"<html>O aplicativo mais atual foi baixado com sucesso: <br>"
					+ destino.getAbsolutePath() + "<br><br>"
					+ "Execute este novo arquivo na próxima execução.</br>"
					+ "</html>");
		}
		
	}

}