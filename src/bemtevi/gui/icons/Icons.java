package bemtevi.gui.icons;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Classe contendo os ícones do aplicativo
 * @author edans
 */
public class Icons {
	public static final String ICON_VALIDATE = "check1.png";
	public static final String ICON_DOC_1 = "doc1.png";
	public static final String ICON_DOC_2 = "doc2.png";
	public static final String ICON_OPEN = "open.png";
	public static final String ICON_PDF = "pdf.png";
	public static final String ICON_PDF_2 = "pdf2.png";
	public static final String ICON_GEAR = "gear.png";
	public static final String ICON_PLUS = "plus.png";
	public static final String ICON_MINUS = "minus.png";
	public static final String EDIT_MINUS = "edit.png";
		
	public static ImageIcon getMainLogo() {
		URL logo = Icons.class.getResource("logo.png");
		if (logo != null) {
			ImageIcon img = new ImageIcon(logo);
			Image dimg = img.getImage().getScaledInstance(-1,150,
			        Image.SCALE_SMOOTH);
			return new ImageIcon(dimg);	
		} else {
			return null;
		}
	}
	
	public static ImageIcon getIcon(String name) {
		URL logo = Icons.class.getResource(name);
		if (logo != null) {
			return new ImageIcon(logo);
		}
		return null;
	}
}
