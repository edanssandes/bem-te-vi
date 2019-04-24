package bemtevi.relatorios;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

import bemtevi.model.Certidao;
import bemtevi.model.Certidoes;
import bemtevi.model.CertidoesSecao;
import bemtevi.parsers.IParserCertidao;
import bemtevi.parsers.ParserCertidaoInvalida;


/**
 * Classe que cria o relatório PDF
 * @author edans
 */
public class RelatorioPDF {

	private Certidoes nadaConstas;
	private PDPage currentPage = null;
	private PDPageContentStream contentStream = null;
	private PDDocument doc = null;
	private PDType0Font font = null;
	private static final int MARGIN_BOTTON = 50;
	private static final int MARGIN_TOP = 700;
	private static final int MARGIN_LEFT = 50;
	//private static final int MARGIN_RIGHT = 50;
	private static final int MIN_Y_SECTION = 200;

	public RelatorioPDF(Certidoes nadaConstas) {
		this.nadaConstas = nadaConstas;
	}

	public void save(File file) throws IOException {
		this.doc = new PDDocument();
		InputStream in = RelatorioPDF.class
				.getResourceAsStream("OpenSans-Regular.ttf");
		this.font = PDType0Font.load(doc, in);
		
		createTOC();
		for (Certidao nadaConsta : nadaConstas) {
			for (int i = 0; i < nadaConsta.getDocumento().getPDF().getNumberOfPages(); i++) {
				doc.addPage(nadaConsta.getDocumento().getPDF().getPage(i));
			}
		}
		doc.save(file);
		doc.close();
	}

	private void createTOC() throws IOException {
		float y = updatePage(0);
		drawTable(y, nadaConstas);
		contentStream.close();
	}

	public void drawTable(float y, Certidoes content) throws IOException {

		float posy = y;
		for (CertidoesSecao secao : content.getSecoes()) {
			int rows = secao.size();
			String[][] table = new String[rows][2];
			for (int i = 0; i < secao.size(); i++) {
				Certidao certidao = secao.get(i);
				if (certidao.getParser().getClass() == ParserCertidaoInvalida.class) {
					table[i][0] = certidao.getDocumento().getName();
					table[i][1] = "";
				} else {
					table[i][0] = certidao.getNome();
					table[i][1] = certidao.getCpfCnpj();
				}
			}
			System.out.println(posy);
			posy = drawSection(posy, secao, table);
			System.out.println(posy);
		}

	}	
	
	public float drawSection(float y, CertidoesSecao secao, String[][] table) throws IOException {
		int rows = table.length;
		int cols = table[0].length;

		final float rowHeight = 10f;
		//final float tableWidth = currentPage.getMediaBox().getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
		//final float tableHeight = rowHeight * rows;
		// final float colWidth = tableWidth / (float) cols;
		final float cellMargin = 5f;

		// now add the text
		PDFont fontHeader = PDType1Font.HELVETICA_BOLD;

		//PDFont font = PDType1Font.HELVETICA;

		float colWidth[] = new float[cols];
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				table[i][j] = fixString(table[i][j], font);  // Evita caracteres inválidos para a codificação usada
				String text = table[i][j];
				float textWidth = font.getStringWidth(text) / 1000 * 8
						+ cellMargin * 2;
				colWidth[j] = Math.max(colWidth[j], textWidth);
			}
		}

		PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
		borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
		borderULine.setWidth(0.5f); // 1/2 point

		float textx = MARGIN_LEFT + cellMargin;
		float texty = y - 15;
		
		if (texty < MIN_Y_SECTION) {
			texty = updatePage(-1);
		}

		contentStream.setFont(fontHeader, 14);
		contentStream.beginText();
		contentStream.newLineAtOffset(textx, texty);
		contentStream.showText(secao.getNomeCertidao());
		contentStream.endText();
		texty -= 16;

		contentStream.setFont(font, 8);
		IParserCertidao previousParser = null;
		for (int i = 0; i < rows; i++) {
			Certidao nadaConsta = secao.get(i);

			if (previousParser != null && previousParser != nadaConsta.getParser()) {
				texty -= 5; // New Subsection
			}
			previousParser = nadaConsta.getParser();
			
			for (int j = 0; j < cols; j++) {
				String text = table[i][j];
				contentStream.beginText();
				contentStream.newLineAtOffset(textx, texty);
				contentStream.showText(text);
				contentStream.endText();

				// Now add the link annotation, so the clickme works
				// PDAnnotationLink txtLink = new PDAnnotationLink();
				PDAnnotationLink txtLink = new PDAnnotationLink();
				txtLink.setBorderStyle(borderULine);

				// Set the rectangle containing the link

				if (j == 0) {
					PDRectangle position = new PDRectangle();
					position.setLowerLeftX(textx);
					position.setLowerLeftY(texty-0.7f); // down a couple of points
					float textWidth = font.getStringWidth(text) / 1000 * 8;
					float textHeight = font.getBoundingBox().getHeight() / 1000 * 8;
					position.setUpperRightX(textx + textWidth);
					position.setUpperRightY(texty + textHeight);
					txtLink.setRectangle(position);

					/*
					 * PDActionURI action = new PDActionURI();
					 * action.setURI("http://www.pdfbox.org");
					 * txtLink.setAction(action);
					 */
					PDPageDestination dest = new PDPageXYZDestination();
					PDPage linkPage = nadaConsta.getDocumento().getPDF().getPage(0);

					dest.setPage(linkPage);
					PDActionGoTo action = new PDActionGoTo();
					action.setDestination(dest);
					txtLink.setAction(action);
					List<PDAnnotation> annotations = currentPage.getAnnotations();
					annotations.add(txtLink);
				}

				textx += colWidth[j];
			}
			texty -= rowHeight;
			textx = MARGIN_LEFT + cellMargin;
			texty = updatePage(texty);
		}
		return texty;
	}

	private float updatePage(float y) throws IOException {
		if (y<MARGIN_BOTTON || this.currentPage == null) {
			if (contentStream != null) {
				contentStream.close();				
			}
			currentPage = new PDPage();
			this.doc.addPage(currentPage);
			contentStream = new PDPageContentStream(this.doc, currentPage);
			contentStream.setFont(font, 8);
			y = MARGIN_TOP;
		}
		return y;
	}
	
	private static String fixString(String text, PDType0Font font) throws IOException {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
        	char c = text.charAt(i);
            if (font.hasGlyph(c)) { // WinAnsiEncoding.INSTANCE.contains(c) || c==9567) {
                b.append(text.charAt(i));
            } else {
                b.append('_');
            }
        }
        return b.toString();
	}

}
