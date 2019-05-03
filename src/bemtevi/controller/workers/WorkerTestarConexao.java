package bemtevi.controller.workers;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

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
public class WorkerTestarConexao extends AbstractWorker {
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
	 * Configurações de proxy. Se essa variável for nula, as configurações de
	 * proxy atuais não são alteradas.
	 */
	private PreferenciasProxy preferenciasProxy;

	/**
	 * Construtor.
	 * 
	 * @param mainController
	 *            Controlador principal
	 * @param preferenciasProxy
	 *            preferências de proxy a serem utilizadas. Se esse parâmetro
	 *            for nulo, as preferências de proxy atuais não são alteradas.
	 */
	public WorkerTestarConexao(MainController mainController,
			PreferenciasProxy preferenciasProxy) {
		super(mainController);
		this.preferenciasProxy = preferenciasProxy;
	}

	/*
	 * (non-Javadoc)
	 * @see certidao.controller.AbstractWorker#executeJob()
	 */
	@Override
	protected void executeJob() throws Exception {
		Properties prevProperties = new Properties(System.getProperties());
		try {
			startProgress("Testando Conexao...");

			if (preferenciasProxy != null) {
				this.mainController.applyProxyConfiguration(preferenciasProxy);
			}

			setProgressMax(URLS.length);

			StringBuilder sb = new StringBuilder();
			int ok = 0;
			int count = 0;
			for (String urlStr : URLS) {
				String msg = "";
				try {
					URL url = new URL(urlStr);
					int ret = ParserUtil.testUrl(url, 1000);
					msg = String.format(
							"<font color='green'>[ OK ]</font> %20s: %3d",
							urlStr, ret);
					ok++;
				} catch (Exception e) {
					e.printStackTrace();
					msg = String.format(
							"<font color='red'>[ERRO]</font> %20s: %10s",
							urlStr, e.getClass().getName());
				}
				sb.append(String.format("<p style='font-family:monospace'>"
						+ msg + "</p>"));
				String status = String
						.format("Testes de conexão com sucesso: %d/%d", ok,
								URLS.length);
				count++;
				setProgress(count, status);

			}
			setStatus(String.format("Testes de conexão concluídos: %d/%d\n",
					ok, count));

			String summary;
			int type;
			if (ok == 0) {
				summary = "Teste falhou. Verifique as suas configurações de conexão.";
				type = JOptionPane.ERROR_MESSAGE;
			} else if (ok == URLS.length) {
				summary = "Teste bem sucedido.";
				type = JOptionPane.INFORMATION_MESSAGE;
			} else {
				summary = "Ocorreram falhas em algumas conexões. Isso pode"
						+ " ter sido causado por algum problema temporário"
						+ " e talvez não impactará no uso do aplicativo.";
				type = JOptionPane.WARNING_MESSAGE;
			}
			JOptionPane.showMessageDialog(null,
					"<html><p style='text-align:justify' width='300px'>"
							+ summary + "</p><hr>" + sb + "</html>",
					"Teste de Conexão", type);
		} finally {
			if (preferenciasProxy != null) {
				System.setProperties(prevProperties);
			}
		}
	}

}