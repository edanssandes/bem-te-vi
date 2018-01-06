package bemtevi.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import bemtevi.controller.AbstractWorker;


/**
 * Painel de vidro (GlassPanel) contendo uma animação de carregamento.
 * Esta animação é ativada quando algum worker está realizando uma tarefa
 * em background (ver {@link AbstractWorker}).
 * 
 * @author edans
 */
public class GlassPanelLoading extends JPanel {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Horário de início para determinar os intervalos de animação. 
	 */
	private static long startTime = System.currentTimeMillis();
	
	/**
	 * Timer que ativa a animação.
	 */
	private Timer timer;
	
	/**
	 * Intervalo de animação em milisegundos
	 */
	private static final int ANIMATION_INTERVAL_MILLISECONDS = 20;
	

	/**
	 * Construtor.
	 */
	public GlassPanelLoading() {
		setOpaque(false);
		timer = new Timer(ANIMATION_INTERVAL_MILLISECONDS, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				repaint();
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        //g2d.setComposite(AlphaComposite.getInstance(
        //        AlphaComposite.SRC_OVER, 0.4f));
        
		Dimension d = getSize();
		int cx = d.width / 2;
		int cy = d.height / 2;
		
		int numCircle = 12;
		int r = 14;
		int rr = 70;
		for (int i=0; i<numCircle; i++) {
			float diff = (System.currentTimeMillis() - startTime)/1000.0f;
			float state = (1 + diff - i*1.0f/numCircle)%1;
			
			int x = (int)(cx + Math.cos(2*Math.PI*i/numCircle)*rr);
			int y = (int)(cy + Math.sin(2*Math.PI*i/numCircle)*rr);
			
			g2d.setColor(new Color(0.0f, 0.2f, 0.8f, (float)(0.8-0.8*Math.pow(state, 0.3))));
			g2d.fillOval(x - r, y - r, r * 2, r * 2);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			timer.start();
		} else {
			timer.stop();
		}
	}
	
}
