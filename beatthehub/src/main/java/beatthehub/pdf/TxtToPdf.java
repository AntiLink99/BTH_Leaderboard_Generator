package beatthehub.pdf;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

public class TxtToPdf {

	public static void convertTxtToPdf(String basicFilePath) throws IOException, DocumentException {

		File output = new File(basicFilePath+".pdf");
		output.createNewFile();
		
		Document pdfDoc = new Document(PageSize.A4);
		PdfWriter.getInstance(pdfDoc, new FileOutputStream(basicFilePath+".pdf"))
		  .setPdfVersion(PdfWriter.PDF_VERSION_1_7);
		pdfDoc.open();

		BaseFont base = BaseFont.createFont("src/main/resources/Consolas.ttf", BaseFont.IDENTITY_H,false);
		Font font = new Font(base);
		
		font.setStyle(Font.NORMAL);
		font.setSize(10);

		Image bthImage = Image.getInstance(ClassLoader.getSystemResource("src/main/resources/BTH.png"));
		bthImage.setAbsolutePosition(456, 678);
		bthImage.scaleAbsolute(100f,100f);
		bthImage.setScaleToFitHeight(true);
		
	    boolean firstParagraph = true;    
		BufferedReader br = new BufferedReader(new FileReader(basicFilePath+".txt"));
		String strLine;
		while ((strLine = br.readLine()) != null) {			
			pdfDoc.getPageNumber();
		    Paragraph para = new Paragraph(strLine + "\n", font);
		    para.setAlignment(Element.ALIGN_JUSTIFIED);
		    
		    if (firstParagraph) {
			    para.add(bthImage);
			    firstParagraph = false;
		    }
		    pdfDoc.add(para);
		}
		pdfDoc.close();
		br.close();
	}

}
