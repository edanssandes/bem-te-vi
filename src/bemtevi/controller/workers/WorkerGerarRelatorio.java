package bemtevi.controller.workers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import bemtevi.controller.AbstractWorker;
import bemtevi.controller.MainController;
import bemtevi.relatorios.RelatorioHTML;
import bemtevi.relatorios.RelatorioPDF;


/**
 * Tarefa que gera um relatório PDF ou HTML.
 *  
 * @author edans
 */
public class WorkerGerarRelatorio extends AbstractWorker {
	/**
	 * Enumeração de tipos de Relatório. 
	 */
	public static enum TipoRelatorio {
		RELATORIO_HTML, 
		RELATORIO_PDF; 
	};
	
	/**
	 * 
	 */
	private TipoRelatorio tipoRelatorio;

	/**
	 * Construtor.
	 * @param mainController Controlador principal
	 * @param tipoRelatorio PDF ou HTML
	 */
	public WorkerGerarRelatorio(MainController mainController, TipoRelatorio tipoRelatorio) {
		super(mainController);
		this.tipoRelatorio = tipoRelatorio;
	}

	/*
	 * (non-Javadoc)
	 * @see certidao.controller.AbstractWorker#executeJob()
	 */
	@Override
	protected void executeJob() throws Exception {
		startProgress("Iniciando Exportação...");

		switch (tipoRelatorio) {
			case RELATORIO_HTML:
				gerarRelatorioHTML();
				break;
			case RELATORIO_PDF:
				gerarRelatorioPDF();
				break;
		}

		setStatus("Concluído!\n");
	}

	/**
	 * Gera relatório PDF e abre no leitor padrão do sistema. 
	 * @throws IOException
	 */
	private void gerarRelatorioPDF() throws IOException {
		File pdfFile = File
				.createTempFile("export-bemtevi", ".pdf");
		RelatorioPDF relatorioPDF = new RelatorioPDF(
				this.mainController.getCertidoes());
		relatorioPDF.save(pdfFile);
		
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().open(pdfFile);
		}
	}

	/**
	 * Gera relatório HTML e abre no navegador padrão do sistema. 
	 * @throws IOException
	 */
	private void gerarRelatorioHTML() throws IOException {
		File htmlFile = File.createTempFile("export-bemtevi",
				".html");
		RelatorioHTML relatorioHTML = new RelatorioHTML(
				this.mainController.getCertidoes(), 
				this.mainController.getPreferencias().getPreferenciasRelatorioHTML(),
				null);
		relatorioHTML.save(htmlFile);
		
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().open(htmlFile);
		}		
	}

}