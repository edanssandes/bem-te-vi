package bemtevi.controller.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import bemtevi.controller.AbstractWorker;
import bemtevi.controller.MainController;
import bemtevi.model.Certidao;
import bemtevi.model.CertidaoFactory;
import bemtevi.model.Certidoes;
import bemtevi.parsers.ParserCertidaoInvalida;


/**
 * Tarefa que carrega e processa as certidões de um diretório
 *  
 * @author edans
 */
public class WorkerCarregarCertidoes extends AbstractWorker {
	/**
	 * Diretório a ser lido recursivamente
	 */
	private File diretorio;

	/**
	 * Construtor.
	 * @param mainController Controlador principal
	 * @param diretorio diretório de leitura dos PDFs
	 */
	public WorkerCarregarCertidoes(MainController mainController, File diretorio) {
		super(mainController);
		this.diretorio = diretorio;
	}

	/*
	 * (non-Javadoc)
	 * @see certidao.controller.AbstractWorker#executeJob()
	 */
	@Override
	protected void executeJob() throws Exception {
		startProgress("Listando Arquivos...");

		Collection<File> _files = FileUtils.listFiles(diretorio,
				new String[] { "pdf" }, true);
		ArrayList<File> files = new ArrayList<File>(_files);
		Collections.sort(files);

		setProgressMax(files.size());
		Certidoes certidoes = new Certidoes(); // Nova lista de certidoes
		int count = 0;
		int countOk = 0;
		List<Certidao> certidoesInvalidas = new ArrayList<Certidao>();
		for (File file : files) {
			System.out.println(file);
			Certidao certidao = CertidaoFactory.parse(file);
			count++;
			if (certidao.getParser().getClass() != ParserCertidaoInvalida.class) {
				countOk++;
				certidoes.add(certidao);
			} else {
				certidoesInvalidas.add(certidao);
			}
			String status = String.format("Certidões identificadas: %d/%d",
					countOk, files.size());
			setProgress(count, status);
		}

		certidoes.add(certidoesInvalidas);
		certidoes.sort();

		setStatus(String.format("Certidões identificadas: %d/%d\n", countOk,
				count));

		
		this.mainController.setCertidoes(certidoes);

	}
}