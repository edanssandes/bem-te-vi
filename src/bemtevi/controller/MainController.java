package bemtevi.controller;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import bemtevi.AppVersion;
import bemtevi.configs.Preferencias;
import bemtevi.configs.PreferenciasPerfil;
import bemtevi.configs.PreferenciasProxy;
import bemtevi.controller.workers.WorkerBaixarAtualizacao;
import bemtevi.controller.workers.WorkerCarregarCertidoes;
import bemtevi.controller.workers.WorkerGerarRelatorio;
import bemtevi.controller.workers.WorkerTestarConexao;
import bemtevi.controller.workers.WorkerValidarCertidoes;
import bemtevi.controller.workers.WorkerGerarRelatorio.TipoRelatorio;
import bemtevi.gui.MainFrame;
import bemtevi.gui.UserPasswordDialog;
import bemtevi.gui.UserPasswordDialog.UserPasswordResponse;
import bemtevi.gui.preferences.PreferencesDialog;
import bemtevi.model.CertidaoFactory;
import bemtevi.model.Certidoes;



/**
 * Classe controladora (MVC) da aplicação.
 * 
 * @author edans
 */
public class MainController {

	/**
	 * Link do projeto no Github.
	 */
	public static final String PROJECT_WEBSITE = "https://github.com/edanssandes/bem-te-vi";

	/**
	 * Link para verificação da última versão disponível no site.
	 */
	private static final String PROJECT_LATEST_VERSION = "https://raw.githubusercontent.com/edanssandes/bem-te-vi/master/versoes/latest.version";

	/**
	 * Arquivo de preferências.
	 */
	private static final File FILE_PREFERENCIAS = new File("preferencias.cfg");

	// Objetos do Modelo

	/**
	 * Certidões atualmente carregadas.
	 */
	private Certidoes certidoesCarregadas = new Certidoes();

	/**
	 * Preferências atualmente carregadas.
	 */
	private Preferencias preferencias = new Preferencias();

	/**
	 * Índice do último perfil escolhido.
	 */
	private int ultimoPerfilEscolhido = -1;

	// Visão (GUI)

	/**
	 * Janela principal.
	 */
	private MainFrame mainFrame;

	/**
	 * Janela de configurações.
	 */
	private PreferencesDialog preferencesDialog;

	/**
	 * Componente de seleção de diretórios de certidões.
	 */
	private JFileChooser chooser;

	/**
	 * Constrói e inicializa todos as camadas da aplicação.
	 */
	public void init() {
		mainFrame = new MainFrame(this);
		preferencesDialog = new PreferencesDialog(this, getMainFrame());

		loadPreferencias();
		setAuthenticator();

		getMainFrame().setVisible(true);
	}

	/**
	 * Solicita um diretório do usuário e carrega todos os arquivos pdf deste
	 * diretório.
	 */
	public void carregarCertidoes() {
		JFileChooser chooser = getFileChooser();
		if (chooser.showOpenDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
			if (showSelecionarPerfil()) {
				WorkerCarregarCertidoes worker = new WorkerCarregarCertidoes(
						this, chooser.getSelectedFile());
				worker.execute();
			}
		} else {
			System.out.println("No Selection ");
		}

	}

	/**
	 * Retorna o componente de seleção de diretórios (para carregamento
	 * de certidẽos).
	 * 
	 * @return componente de seleção de diretórios.
	 */
	private JFileChooser getFileChooser() {
		if (chooser == null) {
			// Lazy Creationg
			chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Informe o diretório raiz das certidões (PDF)");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory();
				}
				public String getDescription() {
					return "Diretório";
				}
			});
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setApproveButtonText("Selecionar");
		}
		return chooser;
	}

	/**
	 * Gera o relatório HTML.
	 */
	public void gerarRelatorioHTML() {
		// Operações lentas são executadas via SwingWorker
		WorkerGerarRelatorio worker = new WorkerGerarRelatorio(this,
				TipoRelatorio.RELATORIO_HTML);
		worker.execute();
	}

	/**
	 * Gera o relatório PDF.
	 */
	public void gerarRelatorioPDF() {
		// Operações lentas são executadas via SwingWorker
		WorkerGerarRelatorio worker = new WorkerGerarRelatorio(this,
				TipoRelatorio.RELATORIO_PDF);
		worker.execute();
	}

	/**
	 * Executa a validação das certidões.
	 */
	public void validar() {
		// Operações lentas são executadas via SwingWorker
		WorkerValidarCertidoes worker = new WorkerValidarCertidoes(this);
		worker.execute();
	}

	/**
	 * Testa as configurações de rede.
	 * 
	 * @param preferenciasProxy
	 *            preferencias de proxy para serem testadas, ou null para
	 *            utilizas as configurações atualmente ativas.
	 */
	public void testConnection(PreferenciasProxy preferenciasProxy) {
		// Operações lentas são executadas via SwingWorker
		WorkerTestarConexao worker = new WorkerTestarConexao(this,
				preferenciasProxy);
		worker.execute();
	}

	/**
	 * Retorna a janela principal do programa.
	 * @return a janela principal do programa.
	 */
	public MainFrame getMainFrame() {
		return mainFrame;
	}
	
	/**
	 * Obtém a lista de certidões.
	 * @return lista de certidões.
	 */
	public Certidoes getCertidoes() {
		return certidoesCarregadas;
	}

	/**
	 * Define a lista de certidões.
	 * @param certidoes lista de certidões.
	 */
	public void setCertidoes(Certidoes certidoes) {
		this.certidoesCarregadas = certidoes;
		this.getMainFrame().setCertidoes(certidoes);
	}	
	
	/**
	 * Retorna as preferências de configuração carregadas.
	 * @return preferências de configuração carregadas.
	 */
	public Preferencias getPreferencias() {
		return preferencias;
	}
	
	/**
	 * Apresenta a janela de preferências com as configurações atuais.
	 */
	public void configurarPreferencias() {
		preferencesDialog.setPreferencias(preferencias);
		preferencesDialog.setLocationRelativeTo(getMainFrame());
		preferencesDialog.setVisible(true);
	}

	/**
	 * Salva as preferências passadas por parêmetro no arquivo de configuração
	 * {@link #FILE_PREFERENCIAS}.
	 * 
	 * @param preferencias
	 *            preferências a serem salvas.
	 */
	public void savePreferencias(Preferencias preferencias) {
		try {
			preferencias.saveFile(FILE_PREFERENCIAS);
			this.preferencias = preferencias;
			preferencesDialog.dispose();
			applyConfigurations();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(getMainFrame(),
					"Não foi possível salvar as configurações.\n" + e);
		}
	}

	/**
	 * Carrega as preferências do arquivo de configurações
	 * {@link #FILE_PREFERENCIAS}.
	 */
	private void loadPreferencias() {
		try {
			preferencias.loadFile(FILE_PREFERENCIAS);
			applyConfigurations();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(getMainFrame(),
					"Não foi possível salvar as configurações.\n" + e);
		}
	}

	/**
	 * Verifica se o aplicativo possui uma versão mais nova na internet.
	 */
	public void verificarVersaoInternet() {
		try {
			URL url = new URL(PROJECT_LATEST_VERSION);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String latestVersion = reader.readLine();
			// String buildDate = reader.readLine();
			reader.close();

			if (!(latestVersion.equals(AppVersion.VERSION))) {
				int ret = JOptionPane
						.showConfirmDialog(
								getMainFrame(),
								"<html>Existe uma versão mais atual do aplicativo: "
										+ latestVersion
										+ "<br><br>"
										+ "Clique em \"Ok\" para baixar a última versão ou<br>" 
										+ "efetue o download a partir da página do projeto<br>"
										+ "<a href='" + PROJECT_WEBSITE + "'>"
										+ PROJECT_WEBSITE + "</a><html>",
								"Verificação de Versão",
								JOptionPane.OK_CANCEL_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					WorkerBaixarAtualizacao worker = new WorkerBaixarAtualizacao(this, latestVersion);
					worker.execute();
				} else {
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(new URI(PROJECT_WEBSITE));
					}
				}
			} else {
				JOptionPane
						.showMessageDialog(
								getMainFrame(),
								"<html>A versão deste aplicativo é a mais recente.<html>",
								"Verificação de Versão",
								JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(PROJECT_WEBSITE));
				}
			} catch (Exception e2) {
				JOptionPane.showMessageDialog(getMainFrame(),
						"<html>Acesse a página do projeto:<br><a href='"
								+ PROJECT_WEBSITE + "'>" + PROJECT_WEBSITE
								+ "</a><html>", "Página do Projeto",
						JOptionPane.INFORMATION_MESSAGE);
				e2.printStackTrace();
			}
		}
	}

	/**
	 * Atribui um autenticador para conexões HTTP/HTTPS via proxy.
	 */
	private void setAuthenticator() {
		Authenticator.setDefault(new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				UserPasswordResponse loginInfo = UserPasswordDialog
						.showLoginDialog();
				return new PasswordAuthentication(loginInfo.getUsername(),
						loginInfo.getPassword().toCharArray());
			}
		});
	}

	/**
	 * Aplica as preferências atuais nos diversos componentes do aplicativo.
	 */
	private void applyConfigurations() {
		// Proxy
		PreferenciasProxy preferenciasProxy = preferencias
				.getPreferenciasProxy();
		applyProxyConfiguration(preferenciasProxy);
	}

	/**
	 * Aplica as preferências de proxy.
	 * 
	 * @param preferenciasProxy
	 *            preferências de proxy.
	 */
	public void applyProxyConfiguration(PreferenciasProxy preferenciasProxy) {
		// Obtem as propriedades atuais do sistema
		Properties systemProperties = new Properties(System.getProperties());

		// Limpa do sistema todas as propriedades relacionadas com proxy.
		systemProperties.remove("http.proxyHost");
		systemProperties.remove("http.proxyPort");
		systemProperties.remove("https.proxyHost");
		systemProperties.remove("https.proxyPort");
		systemProperties.remove("java.net.useSystemProxies");

		// Aplica as novas propriedades de proxy
		switch (preferenciasProxy.getProxyTipo()) {
		case SISTEMA:
			// Utilizar configurações do sitema
			systemProperties.setProperty("java.net.useSystemProxies", "true");
			break;
		case MANUAL:
			// Proxy manual
			systemProperties.setProperty("http.proxyHost",
					preferenciasProxy.getProxyHost());
			systemProperties.setProperty("http.proxyPort",
					preferenciasProxy.getProxyPort());
			systemProperties.setProperty("https.proxyHost",
					preferenciasProxy.getProxyHost());
			systemProperties.setProperty("https.proxyPort",
					preferenciasProxy.getProxyPort());
			break;
		case NENHUM:
			// Não utiliza proxy
			break;
		}

		// Atualiza as propriedades do sistema
		System.setProperties(systemProperties);
	}

	/**
	 * Seleciona o perfil de certidões durante a análise dos documentos
	 * 
	 * @return true se o perfil foi selecionado; false se o usuário cancelou o
	 *         procedimento.
	 */
	private boolean showSelecionarPerfil() {
		if (preferencias.getPreferenciasParsersPerfis().getPerfis().size() > 1) {
			JPanel panel = new JPanel(new BorderLayout());
			// JCheckBox cbLembrar = new
			// JCheckBox("Não perguntar novamente para esta sessão");
			JComboBox<String> cbPerfis = new JComboBox<String>(preferencias
					.getPreferenciasParsersPerfis().getNomesPerfis());
			if (ultimoPerfilEscolhido >= 0
					&& ultimoPerfilEscolhido < cbPerfis.getItemCount()) {
				cbPerfis.setSelectedIndex(ultimoPerfilEscolhido);
			}
			panel.add(
					new JCheckBox("Não perguntar novamente para esta sessão"),
					BorderLayout.SOUTH);
			int ret = JOptionPane.showConfirmDialog(null,
					new Object[] { "Selecione o perfil de certidões", cbPerfis,
					// cbLembrar
					}, "Selecione", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (ret == JOptionPane.YES_OPTION) {
				int index = cbPerfis.getSelectedIndex();
				this.ultimoPerfilEscolhido = index;
				PreferenciasPerfil preferenciasPerfil = preferencias
						.getPreferenciasParsersPerfis().getPerfil(index);
				CertidaoFactory.setPerfil(preferenciasPerfil);
				return true;
			} else {
				return false;
			}
		} else {
			PreferenciasPerfil preferenciasPerfil = preferencias
					.getPreferenciasParsersPerfis().getPerfilPadrao();
			CertidaoFactory.setPerfil(preferenciasPerfil);
			return true;
		}
	}


}
