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
		
		BaseFont base = BaseFont.createFont("src/main/resources/Consolas.ttf", BaseFont.CP1250,false);
		Font font = new Font(base);
		
		font.setStyle(Font.NORMAL);
		font.setSize(10);
		
		BufferedReader br = new BufferedReader(new FileReader(basicFilePath+".txt"));
		String strLine;
		while ((strLine = br.readLine()) != null) {
		    Paragraph para = new Paragraph(strLine + "\n", font);
		    para.setAlignment(Element.ALIGN_JUSTIFIED);
		    pdfDoc.add(para);
		}   
		pdfDoc.close();
		br.close();
	}

}
