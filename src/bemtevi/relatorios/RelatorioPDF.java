package bemtevi.relatorios;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
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

	public RelatorioPDF(Certidoes nadaConstas) {
		this.nadaConstas = nadaConstas;
	}

	public void save(File file) throws IOException {
		PDDocument doc = new PDDocument();
		PDPage page = new PDPage();
		doc.addPage(page);
		for (Certidao nadaConsta : nadaConstas) {
			for (int i = 0; i < nadaConsta.getDocumento().getPDF().getNumberOfPages(); i++) {
				doc.addPage(nadaConsta.getDocumento().getPDF().getPage(i));
			}
		}

		PDPageContentStream contentStream = new PDPageContentStream(doc, page);

		drawTable(page, contentStream, 700, 100, nadaConstas);
		contentStream.close();

		doc.save(file);
	}

	public static float drawSection(PDPage page,
			PDPageContentStream contentStream, float y, float margin,
			CertidoesSecao secao, String[][] table) throws IOException {
		int rows = table.length;
		int cols = table[0].length;

		final float rowHeight = 10f;
		final float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
		final float tableHeight = rowHeight * rows;
		// final float colWidth = tableWidth / (float) cols;
		final float cellMargin = 5f;

		// now add the text
		PDFont fontHeader = PDType1Font.HELVETICA_BOLD;

		PDFont font = PDType1Font.HELVETICA;

		float colWidth[] = new float[cols];
		for (int j = 0; j < cols; j++) {
			for (int i = 0; i < rows; i++) {
				String text = table[i][j];
				float textWidth = font.getStringWidth(text) / 1000 * 8
						+ cellMargin * 2;
				colWidth[j] = Math.max(colWidth[j], textWidth);
			}
		}

		PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
		borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
		borderULine.setWidth(0.5f); // 1/2 point

		float textx = margin + cellMargin;
		float texty = y - 15;

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
					List<PDAnnotation> annotations = page.getAnnotations();
					annotations.add(txtLink);
				}

				textx += colWidth[j];
			}
			texty -= rowHeight;
			textx = margin + cellMargin;
		}
		return texty;
	}

	public static void drawTable(PDPage page,
			PDPageContentStream contentStream, float y, float margin,
			Certidoes content) throws IOException {

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

			posy = drawSection(page, contentStream, posy, margin,
					secao, table);
		}

	}

}
