package bemtevi.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import bemtevi.gui.MainFrame;


/**
 * Classe abstrata que realiza as tarefas lentas do MainController em
 * background. Apenas uma tarefa é executada por vez.
 * 
 * @author edans
 */
public abstract class AbstractWorker extends SwingWorker<Void, Void> {

	/**
	 * Controlador relacionado a esta terefa. Este controlador é necessário para
	 * sincronizar as threads que chamam os workers, fazendo com que apenas uma
	 * terafa seja executada por vez.
	 */
	protected MainController mainController;

	/**
	 * Construtor padrão.
	 * 
	 * @param mainController
	 *            controlador desta classe.
	 */
	public AbstractWorker(MainController mainController) {
		this.mainController = mainController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		synchronized (mainController) { // Barreira de Sincronização
			try {
				executeJob();
			} catch (Exception e) {
				// Tratamento de exceção
				showErrorMessage(e);
			} finally {
				// Esconde a barra de progresso da janela
				this.mainController.getMainFrame().showProgress(false);
			}
		}
		return null;
	}

	/**
	 * Apresenta a mensagem de erro em um JOptionPane.
	 * 
	 * @param e
	 *            exceção a ser mostrada.
	 */
	protected void showErrorMessage(Exception e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		e.printStackTrace();
		JOptionPane.showMessageDialog(this.mainController.getMainFrame(),
				errors + "\n", "Erro de Execução", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Apresenta a barra de progresso.
	 * 
	 * @param msg
	 *            mensagem inicial.
	 */
	protected void startProgress(String msg) {
		this.mainController.getMainFrame().setProgressMax(1);
		this.mainController.getMainFrame().setProgress(0, msg);
		this.mainController.getMainFrame().showProgress(true);
	}

	/**
	 * Define o valor máximo de progresso.
	 * 
	 * @param max
	 *            valor máximo de progresso.
	 */
	protected void setProgressMax(int max) {
		this.mainController.getMainFrame().setProgressMax(max);
	}

	/**
	 * @see MainFrame#setProgress(int, String)
	 */
	protected void setProgress(int value, String msg) {
		this.mainController.getMainFrame().setProgress(value, msg);
	}

	/**
	 * Define a mensagem de status (sem barra de progresso).
	 * 
	 * @param status
	 *            mensagem de status.
	 */
	protected void setStatus(String status) {
		this.mainController.getMainFrame().setStatus(status);
	}

	/**
	 * Função abstrata que executa a tarefa lenta.
	 * 
	 * @throws Exception
	 *             qualquer exceção.
	 */
	protected abstract void executeJob() throws Exception;
}
