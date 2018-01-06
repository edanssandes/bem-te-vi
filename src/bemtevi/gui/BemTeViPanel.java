package bemtevi.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Painel de fundo com o logo do bem-te-vi. O bem-te-vi pisca em intervalos
 * aleatórios.
 * 
 * @author edans
 */
public class BemTeViPanel extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Imagem de background
	 */
	private ImageIcon background;
	
	/**
	 * Indica se o olho do bem-te-vi está piscando
	 */
	private boolean blinkingEye = false;
	
	/**
	 * Timer para piscar o olho do bem-te-vi 
	 */
	private Timer blinkingTimer;
	
	/**
	 * Construtor
	 * @param background imagem de background
	 */
	public BemTeViPanel(ImageIcon background) {
		this.background = background;
		
		ActionListener timerAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				blinkingEye = !blinkingEye;
				if (blinkingEye) {
					blinkingTimer.setDelay(500);
				} else {
					blinkingTimer.setDelay(3000 + (int) (27000 * Math.pow(
							Math.random(), 0.3)));
				}
				blinkingTimer.restart();
				repaint();
			}
		};
		blinkingTimer = new Timer(2000, timerAction);
		blinkingTimer.start();
		blinkingTimer.setInitialDelay(0);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			blinkingTimer.start();
		} else {
			blinkingTimer.stop();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Dimension d = getSize();
		int cx = d.width / 2;
		int cy = d.height / 2;
		background.paintIcon(this, g, cx-background.getIconWidth()/2, cy-background.getIconHeight()/2);
		if (blinkingEye) {
			int r = 5;
			g.setColor(Color.BLACK);
			g.fillOval(cx - 5 - r, cy - 56 - r, r * 2, r * 2);
		}
	}	
}
