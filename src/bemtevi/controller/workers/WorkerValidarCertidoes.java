package bemtevi.controller.workers;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JOptionPane;

import bemtevi.controller.AbstractWorker;
import bemtevi.controller.MainController;
import bemtevi.model.Certidao;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.IValidadorCertidao;
import bemtevi.parsers.ValidationException;


/**
 * Tarefa que valida todas as certidões ainda não validadas.
 * 
 * @author edans
 */
public class WorkerValidarCertidoes extends AbstractWorker {

	/**
	 * Construtor.
	 * 
	 * @param mainController
	 *            Controlador principal
	 */
	public WorkerValidarCertidoes(MainController mainController) {
		super(mainController);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see certidao.controller.AbstractWorker#executeJob()
	 */
	@Override
	protected void executeJob() throws Exception {
		startProgress("Validando Certidões...");

		int count = 0;
		int countOk = 0;
		int countError = 0;
		int countValidationError = 0;

		setProgressMax(this.mainController.getCertidoes().size());

		for (Certidao certidao : this.mainController.getCertidoes()) {
			if (certidao.getResultadoValidacao() == Certidao.StatusValidacao.CERTIDAO_VALIDA
					|| certidao.getResultadoValidacao() == Certidao.StatusValidacao.CERTIDAO_INVALIDA) {
				// certidões já validadas (ou invalidadas) são ignoradas.
				continue;
			}
			IParserCertidao parser = certidao.getParser();
			count++;
			try {
				// Apenas certidões com parsers validadores podem ser validadas.
				if (parser instanceof IValidadorCertidao) {
					((IValidadorCertidao) parser).validate(certidao);
					certidao.setResultadoValidacao(Certidao.StatusValidacao.CERTIDAO_VALIDA);
					countOk++;
				} else {
					certidao.setResultadoValidacao(null);
				}
			} catch (ValidationException e) {
				e.printStackTrace();
				countValidationError++;
				certidao.setResultadoValidacao(Certidao.StatusValidacao.CERTIDAO_INVALIDA);
				certidao.setMensagemValidacao(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				countError++;
				certidao.setResultadoValidacao(Certidao.StatusValidacao.ERRO_DE_VALIDACAO);
				certidao.setMensagemValidacao(e.getMessage());
			} catch (RuntimeException e) {
				showErrorMessage(e);
			}
			setProgress(count, String.format(
					"Certidões válidas: (%d); inválidas (%d); erros (%d)\n",
					countOk, countValidationError, countError));
		}

		showValidationSummary(mainController.getMainFrame(), countOk,
				countValidationError, countError);
	}

	/**
	 * Apresenta uma mensagem contendo o sumário da validação.
	 * 
	 * @param parent
	 *            Janela pai da mensagem (popup)
	 * @param countOk
	 *            número de certidões válidas
	 * @param countValidationError
	 *            número de certidões inválidas
	 * @param countError
	 *            número de erros de validação
	 */
	private static void showValidationSummary(Component parent, int countOk,
			int countValidationError, int countError) {
		String summary;
		String obs;
		int type;
		if (countValidationError == 0 && countError == 0) {
			summary = "<font color='green'>Não foram identificados problemas nas certidões.</font>";
			obs = "";
			type = JOptionPane.INFORMATION_MESSAGE;
		} else {
			if (countValidationError == 0 && countError != 0) {
				summary = "<font color='olive'>Ocorreram falhas durante a validação que impediram"
						+ " a validação de todas as certidões.</font>";
				type = JOptionPane.WARNING_MESSAGE;
			} else {
				summary = "<font color='red'>ATENÇÃO: Foram identificadas certidões inválidas!</font>";
				type = JOptionPane.ERROR_MESSAGE;
			}
			obs = "";
			if (countValidationError > 0) {
				obs += "<font size=2><font color='red'>*</font> Certidões inválidas são aquelas que apresentam"
						+ " divergências de campos durante a verificação nos sites"
						+ " de origem. Para verificar as "
						+ "certidões com problema, gere um relatório HTML na tela "
						+ "principal do programa.</font>";

			}
			if (countError > 0) {
				if (countValidationError > 0) {
					obs += "<br>";
				}
				obs += "<font size=2><font color='olive'>*</font> As validações podem ser abortadas em caso erro de "
						+ "conexão de rede, sistemas externos fora do ar ou condições não previstas pelo programa. "
						+ " Efetue uma nova validação para verificar se "
						+ " a falha persiste.</font>";
			}
		}
		if (!obs.isEmpty()) {
			obs = "<hr><p style='text-align:justify' width='300px'>" + obs + "</p>";
		}
		String html = "<html><p style='text-align:justify' width='300px'>"
				+ summary
				+ "</p><hr>"
				+ "<p style='font-family:monospace'>"
				+ String.format(
						"<font color='green'>Certidões Válidas: </font> %d",
						countOk)
				+ "</p><p style='font-family:monospace'>"
				+ String.format(
						"<font color='red'>Certidões Inválidas%s: </font> %d",
						countValidationError==0?"":"*",
						countValidationError)
				+ "</p><p style='font-family:monospace'>"
				+ String.format(
						"<font color='olive'>Validações abortadas%s: </font> %d",
						countError==0?"":"*",
						countError)
				+ "</p>" + obs + "</html>";
		JOptionPane.showMessageDialog(parent, html, "Resultado da Validação",
				type);
	}

	public static void main(String[] args) {
		showValidationSummary(null, 0, 0, 0);
		showValidationSummary(null, 1, 0, 0);
		showValidationSummary(null, 0, 1, 0);
		showValidationSummary(null, 0, 0, 1);
		showValidationSummary(null, 2, 0, 0);
		showValidationSummary(null, 0, 2, 0);
		showValidationSummary(null, 0, 0, 2);
		showValidationSummary(null, 2, 1, 0);
		showValidationSummary(null, 2, 0, 1);
		showValidationSummary(null, 0, 2, 1);
		showValidationSummary(null, 0, 0, 2);
		showValidationSummary(null, 3, 2, 1);
	}

}