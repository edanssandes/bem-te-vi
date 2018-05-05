package bemtevi.model;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import bemtevi.utils.ParserUtil;


/**
 * Classe que abstrai as extrações de texto dos arquivos e algumas manipulações
 * de texto básicas.
 * 
 * @author edans
 */
public class Documento {
	/**
	 * Conversor de texto utilizando o PDFBox.
	 */
	private static PDFTextStripper pdfStripper;
	
	/**
	 * Inicialização do PDFBox.
	 */
	static {
		try {
			pdfStripper = new PDFTextStripper();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String textSorted;
	private String textUnsorted;
	private PDDocument pdf;
	private File file;

	private Documento() {
	}
	
	public static Documento readPDF(File file) throws IOException {
		Documento doc = new Documento();
		doc.file = file;
		doc.parsePDF();
		
		return doc;
	}

	private void parsePDF() throws IOException {
		// Converte o PDF em texto

		/*PDFParser parser = new PDFParser(new RandomAccessFile(file, "r"));
		parser.parse();
		COSDocument cosDoc = parser.getDocument();
		this.pdf = new PDDocument(cosDoc);*/
		this.pdf = PDDocument.load(file);
		
		this.textUnsorted = stripPDF(false);
		this.textSorted = stripPDF(true);
	}
	
	private String stripPDF(boolean sorted) throws IOException {
		pdfStripper.setSortByPosition(sorted);			
		String text  = pdfStripper.getText(pdf);
		text = ParserUtil.simplifyText(text);
		
		return text;

	}
	
	public String doOCR() {
		/*if (instance == null) {
			instance = new Tesseract();
			instance.setDatapath(new File("/usr/share/tesseract-ocr/tessdata").getPath());
			instance.setLanguage("por");
			//instance.setTessVariable("tessedit_parallelize", "2");
			//instance.setTessVariable("load_unambig_dawg", "F");
			//instance.setTessVariable("stopper_ambiguity_threshold_offset", "10.0");
			//instance.setTessVariable("load_system_dawg", "F");
			//instance.setTessVariable("load_freq_dawg", "F");			
		}
		
		try {
			PDFRenderer pdfRenderer = new PDFRenderer(pddoc);
			StringBuffer text = new StringBuffer();
			for (int i=0; i<pddoc.getNumberOfPages(); i++) {
				BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300, ImageType.GRAY);
				//ImageIO.write(bim, "png", new FileOutputStream(imageFile));
			
		        String result = instance.doOCR(bim, null);
		        text.append(result);
			}
			System.out.println(text);
			return text.toString();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}		*/
		return null;
	}

	public String getText() {
		return getText(false);
	}

	public String getTextInOrder() {
		return getText(true);
	}

	private String getText(boolean sorted) {
		if (sorted) {
			return textSorted;
		} else {
			return textUnsorted;
		}
	}
	
	public PDDocument getPDF() {
		return pdf;
	}

	public String getName() {
		return file.getName();
	}
}
