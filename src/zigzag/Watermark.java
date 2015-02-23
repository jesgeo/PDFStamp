/*
 * Copyright (c) 2007, intarsys consulting GmbH
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of intarsys nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package zigzag;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.TextState;
import de.intarsys.pdf.content.common.CSCreator;
import de.intarsys.pdf.crypt.PasswordProvider;
import zigzag.CommonJPodExample;
import de.intarsys.pdf.encoding.WinAnsiEncoding;
import de.intarsys.pdf.font.PDFont;
import de.intarsys.pdf.font.PDFontType1;
import de.intarsys.pdf.pd.PDExtGState;
import de.intarsys.pdf.pd.PDForm;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.tools.authenticate.IPasswordProvider;

/**
 * Create a text watermark on every page of input document.
 */
public class Watermark extends CommonJPodExample {

	private static final float halfSqrt2 = (float) (0.5 * Math.sqrt(2));
	
	public static void main(String[] args) {
		
		System.out.println(new java.util.Date().toGMTString());
		
		Watermark client = new Watermark();
		
		args = new String[] { "pdfauto/demo.pdf", "pdfauto/new/demo.pdf" };
		try {
			client.run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(new java.util.Date().toGMTString());
	}

	public void run(String[] args) throws Exception {
		if (args.length < 2) {
			usage();
			return;
		}
		try {
			String inputFileName = args[0];
			String outputFileName = args[1];
			
			PasswordProvider.set(new IPasswordProvider() {
				public char[] getPassword() {
					return "aaaabbbb".toCharArray();
				}
			});
			
			open(inputFileName);
			PDForm form = createForm();
			markPages(form);
			save(outputFileName);
		} finally {
			close();
		}
	}

	protected void markPages(PDForm form) {
		PDPage page = getDoc().getPageTree().getFirstPage();
		while (page != null) {
			markPage(page, form);
			page = page.getNextPage();
		}
	}

	protected void markPage(PDPage page, PDForm form) {
		float formWidth = form.getBoundingBox().getWidth();
		float formHeight = form.getBoundingBox().getHeight();
		CDSRectangle rect = page.getCropBox();
		float scale = 1f;
		float offsetY = (rect.getHeight() - (formHeight * scale)) / 2;
		float offsetX = (rect.getWidth() - (formWidth * scale)) ;

		CSContent content = CSContent.createNew();
		CSCreator creator = CSCreator.createFromContent(content, page);

		creator.saveState();
		
		System.out.println(offsetX + " " + offsetY);
		creator.transform(scale, 0, 0, scale, offsetX, 10);
		creator.doXObject(null, form);
		
		creator.restoreState();
		creator.close();

		page.cosAddContents(content.createStream());
	}

	

	protected PDForm createForm() {
		PDForm form = (PDForm) PDForm.META.createNew();
		PDFont font = PDFontType1.createNew(PDFontType1.FONT_Times_Roman);

		CSCreator creator = CSCreator.createNew(form);
		creator.textBegin();
		creator.setNonStrokeColorRGB(1.0f, 204f / 256f, 0); // orange
		creator.setStrokeColorRGB(1.0f, 204f / 256f, 0);
		//creator.getGraphicsState().
		creator.textSetFont(null, font, 14);
		
		creator.textShow("Licensed to <School :)>");
		creator.textEnd();
		creator.close();
				

		form.setBoundingBox(new CDSRectangle(0, 0, 145, 15));

		return form;
	}
	
	private int getStringWidth(Font f, String s){
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setFont(f);
		return g.getFontMetrics().stringWidth(s);
	}

	/**
	 * Help the user.
	 */
	public void usage() {
		System.out.println("usage: java.exe " + getClass().getName() //$NON-NLS-1$
				+ " <input-filename> <output-filename>"); //$NON-NLS-1$
	}
}
