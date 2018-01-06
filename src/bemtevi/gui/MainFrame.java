package bemtevi.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import bemtevi.AppVersion;
import bemtevi.controller.MainController;
import bemtevi.gui.icons.Icons;
import bemtevi.model.Certidao;
import bemtevi.model.Certidoes;



/**
 * Painel principal do programa.
 * 
 * @author edans
 */
public class MainFrame extends JFrame {
	/**
	 * Versão do aplicativo
	 */
	private static final String VERSION = AppVersion.VERSION;

	/**
	 * Nome do Aplicativo
	 */
	private static final String APP_NAME = AppVersion.APP_NAME;

	/**
	 * Título da Janela.
	 */
	private static final String TITULO = APP_NAME
			+ " - Organizador de Certidões";

	/**
	 * Mensagem apresentada pelo menu "Sobre"
	 */
	private static final String MSG_ABOUT = "<html>"
			+ TITULO
			+ "<br>"
			+ "Versão: "
			+ VERSION
			+ "<br><br>"
			+ "<p style='text-align:justify' width='400px'>"
			+ "<font color=#404040>Esta ferramenta tem por objetivo "
			+ "auxiliar no gerenciamento "
			+ "e catalogação de arquivos de certidões públicas, "
			+ "tais como nada-consta de órgãos do governo e certidões "
			+ "de distribuição do Poder Judiciário.</font>"
			+ "<p>"
			+ "<p><font size=2>Idealizadora: Luciana Oliveira</font><br>"
			+ "<font size=2>Desenvolvedor: Edans Sandes</font><br>"
			+ "</p><br>"
			+ "<p style='text-align:justify' width='400px'>"
			+ "<font size='2' color=#803030>Por se tratar de um software de uso gratuito, os "
			+ "desenvolvedores se eximem de qualquer responsabilidade "
			+ "por falhas e erros que possam ocorrer na ferramenta "
			+ "ou por problemas derivados a partir de seu uso. "
			+ "As certidões originais, emitidas pelos órgãos do poder público, "
			+ "devem ser consideradas as únicas fontes de informação "
			+ "juridicamente válidas. Deste modo, o uso das informações "
			+ "e relatórios produzidos pela ferramenta é de inteira "
			+ "responsabilidade do usuário e devem ser verificadas "
			+ "com as fontes originais.</font></p>" + "</html>";

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Controlador principal
	 */
	private MainController controller;

	/* Menus */
	private JMenuItem menuExportPDF;
	private JMenuItem menuExportHTML;
	private JMenuItem menuValidate;
	private JMenuItem menuPreferencias;

	/* Barra de progresso */
	private JProgressBar progressBar;

	/* Action Listeners */
	private ActionListener openListener;
	private ActionListener exportHTMLListener;
	private ActionListener exportPDFListener;
	private ActionListener preferencesListener;
	private ActionListener validateListener;

	/* Labels */
	private JLabel statusLabel;
	private JPanel statusPanel;
	private BemTeViPanel backgroundLabel;

	/* Botões */
	private JButton buttonHTML;
	private JButton buttonPDF;
	private JButton buttonValidate;

	/* Painel de carregamento */
	private GlassPanelLoading glassPane;

	/**
	 * Cria a janela principal e todos os seus componentes.
	 * 
	 * @param controller
	 *            controlador principal que receberá os comandos do usuário.
	 */
	public MainFrame(MainController controller) {
		super(TITULO);

		this.controller = controller;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		openListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.controller.carregarCertidoes();
			}
		};
		exportHTMLListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.controller.gerarRelatorioHTML();
			}
		};
		exportPDFListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.controller.gerarRelatorioPDF();
			}
		};
		validateListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.controller.validar();
			}
		};
		preferencesListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.this.controller.configurarPreferencias();
			}
		};

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().setPreferredSize(new Dimension(400, 260));
		this.setMinimumSize(new Dimension(300, 260));
		backgroundLabel = new BemTeViPanel(Icons.getMainLogo());
		this.getContentPane().add(backgroundLabel);
		glassPane = new GlassPanelLoading();
		this.setGlassPane(glassPane);

		this.getContentPane().add(createToolbar(), BorderLayout.PAGE_START);
		setJMenuBar(createMenuBar());
		setCertidoesMenusEnabled(false);

		statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(400, 20));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		this.getContentPane().add(statusPanel, BorderLayout.SOUTH);

		progressBar = new JProgressBar(0, 100);
		progressBar.setPreferredSize(new Dimension(300, 20));
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(false);
		progressBar.setString("Teste");
		progressBar.setValue(10);

		statusLabel = new JLabel("Não há certidões carregadas.");
		statusLabel.setForeground(new Color(0x808080));
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		showProgress(false);

		this.pack();
		setLocationRelativeTo(null);

	}

	/**
	 * Define as certidões atualmente carregadas na janela.
	 * 
	 * @param certidoes
	 *            certidões carregadas.
	 */
	public void setCertidoes(Certidoes certidoes) {
		String[] columnNames = { "Nome", "CPF/CNPJ" };

		Object[][] data = new Object[certidoes.size()][2];
		for (int i = 0; i < certidoes.size(); i++) {
			Certidao certidao = certidoes.get(i);
			data[i][0] = certidao.getNome();
			data[i][1] = certidao.getCpfCnpj();
		}

		JTable table = new JTable();
		table.setFocusable(false);

		table.setModel(new DefaultTableModel(data, columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});

		JScrollPane pane = new JScrollPane(table);
		BorderLayout layout = (BorderLayout) getContentPane().getLayout();
		getContentPane().remove(layout.getLayoutComponent(BorderLayout.CENTER));
		getContentPane().add(pane, BorderLayout.CENTER);
		setCertidoesMenusEnabled(certidoes.size() > 0);
		revalidate();
		getGlassPane().setVisible(true);
	}

	/**
	 * Define o texto do status no rodapé da janela.
	 * 
	 * @param status
	 *            texto do status.
	 */
	public void setStatus(String status) {
		statusLabel.setText(status);
	}

	/**
	 * Define o valor máximo de progresso.
	 * 
	 * @param max
	 *            valor máximo de progresso.
	 */
	public void setProgressMax(int max) {
		progressBar.setMaximum(max);
	}

	/**
	 * Define o progresso.
	 * 
	 * @param value
	 *            valor do progresso (deve ser menor que o valor definido por
	 *            {@link #setProgressMax(int)}).
	 * @param msg
	 *            Mensagem de status.
	 */
	public void setProgress(int value, String text) {
		progressBar.setString(text);
		progressBar.setValue(value);
	}

	/**
	 * Determina se a barra de progresso deve aparecer, caso contrário apresenta
	 * apenas o label com o status no rodapé.
	 * 
	 * @param progress
	 *            indica se a barra de progresso deve aparecer.
	 */
	public void showProgress(boolean progress) {
		statusPanel.removeAll();
		if (progress) {
			statusPanel.add(progressBar);
		} else {
			statusPanel.add(statusLabel);
		}
		this.getGlassPane().setVisible(progress);

		statusPanel.repaint();
		statusPanel.revalidate();
	}

	/**
	 * Criação do Menu.
	 * 
	 * @return JMenuBar com todos os menus.
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;

		// Create the menu bar.
		menuBar = new JMenuBar();

		/*
		 * Menu Arquivos
		 */
		menu = new JMenu("Arquivos");
		menu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(menu);

		// a group of JMenuItems
		menuItem = new JMenuItem("Carregar Certidões...");
		menuItem.setMnemonic(KeyEvent.VK_C); // used constructor instead
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));
		menuItem.addActionListener(openListener);
		menu.add(menuItem);

		menu.addSeparator();

		menuExportHTML = new JMenuItem("Gerar Relatório em HTML...",
				KeyEvent.VK_H);
		menuExportHTML.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				ActionEvent.CTRL_MASK));
		menu.add(menuExportHTML);
		menuExportHTML.addActionListener(exportHTMLListener);

		menuExportPDF = new JMenuItem("Gerar PDF Único...", KeyEvent.VK_F);
		menuExportPDF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				ActionEvent.CTRL_MASK));
		menu.add(menuExportPDF);
		menuExportPDF.addActionListener(exportPDFListener);

		menu.addSeparator();

		menuPreferencias = new JMenuItem("Preferências...", KeyEvent.VK_P);
		menuPreferencias.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));
		menu.add(menuPreferencias);
		menuPreferencias.addActionListener(preferencesListener);

		menu.addSeparator();

		// a group of JMenuItems
		menuItem = new JMenuItem("Sair", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		/*
		 * Menu Certidões
		 */
		menu = new JMenu("Certidões");
		menu.setMnemonic(KeyEvent.VK_C);
		menuBar.add(menu);

		menuValidate = new JMenuItem("Validar na Internet...", KeyEvent.VK_V);
		menuValidate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		menu.add(menuValidate);
		menuValidate.addActionListener(validateListener);

		/*
		 * Menu Ajuda
		 */
		menu = new JMenu("Ajuda");
		menu.setMnemonic(KeyEvent.VK_J);
		menuBar.add(menu);

		menuItem = new JMenuItem("Verificar Atualização...");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.verificarVersaoInternet();
			}
		});
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Diagnóstico de rede...");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.testConnection(null);
			}
		});
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Sobre...", KeyEvent.VK_S);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(MainFrame.this, MSG_ABOUT,
						"Sobre a Ferramenta", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		return menuBar;
	}

	/**
	 * Criação da barra de ferramentas.
	 * 
	 * @return barra de ferramentas.
	 */
	private JToolBar createToolbar() {
		JToolBar toolBar = new JToolBar("Still draggable");
		toolBar.setPreferredSize(new Dimension(300, 32));
		toolBar.setFloatable(false);

		JButton button = null;

		// Botão "Carregar Certidões"
		button = makeNavigationButton(Icons.ICON_OPEN, "Carregar Certidões",
				"Abrir");
		toolBar.add(button);
		button.addActionListener(openListener);

		toolBar.addSeparator();

		// Botão "Gerar Relatório HTML"
		buttonHTML = makeNavigationButton(Icons.ICON_DOC_1,
				"Gerar Relatório HTML", "Gerar Relatório");
		toolBar.add(buttonHTML);
		buttonHTML.addActionListener(exportHTMLListener);

		// Botão "Gerar PDF Único"
		buttonPDF = makeNavigationButton(Icons.ICON_PDF_2, "Gerar PDF Único",
				"Gerar PDF");
		toolBar.add(buttonPDF);
		buttonPDF.addActionListener(exportPDFListener);

		toolBar.addSeparator();

		// Botão "Validar Certidões"
		buttonValidate = makeNavigationButton(Icons.ICON_VALIDATE,
				"Validar Certidões", "Validar Certidões");
		toolBar.add(buttonValidate);
		buttonValidate.addActionListener(validateListener);

		return toolBar;
	}

	/**
	 * Cria um botão da barra de ferramentas
	 * 
	 * @param imageName
	 *            Nome do ícone
	 * @param toolTipText
	 *            ToolTip
	 * @param altText
	 *            Texto alternativo do botão, em caso do ícone não carregar.
	 * @return
	 */
	private JButton makeNavigationButton(String imageName, String toolTipText,
			String altText) {
		// Create and initialize the button.
		JButton button = new JButton();
		button.setPreferredSize(new Dimension(24, 24));
		button.setToolTipText(toolTipText);
		button.setFocusable(false);

		Border border = BorderFactory.createEtchedBorder(1);
		button.setBorder(border);

		ImageIcon image = Icons.getIcon(imageName);
		if (image != null) { // image found
			button.setIcon(image);
		} else { // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imageName);
		}

		return button;
	}

	/**
	 * Desabilita ou Habilita os menus relacionados com as certidões.
	 * 
	 * @param enabled
	 *            indica o estado de habilitação dos menus.
	 */
	private void setCertidoesMenusEnabled(boolean enabled) {
		menuExportHTML.setEnabled(enabled);
		menuExportPDF.setEnabled(enabled);
		menuValidate.setEnabled(enabled);

		buttonPDF.setEnabled(enabled);
		buttonHTML.setEnabled(enabled);
		buttonValidate.setEnabled(enabled);
	}

}
