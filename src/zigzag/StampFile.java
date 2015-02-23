package zigzag;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.HashMap;






import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.content.CSContent;
import de.intarsys.pdf.content.common.CSCreator;
import de.intarsys.pdf.crypt.PasswordProvider;
import de.intarsys.pdf.font.PDFont;
import de.intarsys.pdf.font.PDFontType1;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDForm;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.tools.authenticate.IPasswordProvider;
import de.intarsys.tools.locator.FileLocator;


class JobSetting extends HashMap<String, Object>  {
		
};


interface ThreadCompleteListener {
	void notifyOfThreadComplete(final StampFile thread) throws InvocationTargetException, InterruptedException;
}

public class StampFile extends Thread {
	
	private final Set<ThreadCompleteListener> listeners = new  CopyOnWriteArraySet<ThreadCompleteListener>();
	
	public enum Status {
		DONE, ERROR, FAILED, SUCCESS, UNKNOWN
	}
	
	protected PDForm stamp = null;
	
	private File file = null;
	
	private Status status = Status.UNKNOWN;
	
	private JobSetting map;
		
	private StampFile () {
		
	}
	
	public StampFile (JobSetting setting) {
		
		this();
		
		map = setting;
		
		map.put("enabled", true);
		
		// default for now 
		map.put("trailing", "License to ");
		map.put("stampPos", "bottom-right");
		// map.put("output", "pdfauto/new/");
		
		map.put("password", "aaaabbbb".toCharArray());
		
		map.put("pagerange", "1");
		map.put("exclude", true);
		
		PasswordProvider.set(new IPasswordProvider() {
			public char[] getPassword() {
				return (char[]) map.get("password");
			}
		});
		
		// createForm();
		
	}
	
	public StampFile (JobSetting setting, File f) {
		this(setting);
		setFile(f);
	}
	
	
	public StampFile (File file){
		this(new JobSetting(), file);
	}
	
	public void setSetting(String key, Object value){
		map.put(key, value);
	}
	
	public Object getSetting(String key){
		return map.get(key);
	}
	
	public void setFile(File f){
		file = f;
	}
	
	public File getFile(){
		return file;
	}
	
	public Status getStatus(){
		return status;
	}
	
	public void setStatus(Status stat){
		status = stat;
	}
	

	public void execute() {
		
		FileLocator floc = new FileLocator(file.getAbsolutePath());
		FileLocator oloc = new FileLocator(map.get("output")+"/"+file.getName());
		
		System.out.println(file.getName()+"-Start: "+new java.util.Date().toString());
				
		createForm();

		try {
			PDDocument doc = PDDocument.createFromLocator(floc);
			PDPage page = doc.getPageTree().getFirstPage();
			while (page != null) {
				stampPage(page);
				page = page.getNextPage();
			}
			doc.save(oloc, null);
			doc.close();
			status = Status.DONE;
			
		} catch (IOException | COSLoadException e) {
			status = Status.ERROR;
			e.printStackTrace();
		}
		
		
		System.out.println(file.getName()+"-END: "+new java.util.Date().toString());
	}
	
	@Override
    public void run() {
        try {
			execute();
        } finally {
        	try {
				notifyListeners();
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
        }        
    }
	


	protected void stampPage(PDPage page) {
		float formWidth = stamp.getBoundingBox().getWidth();
		float formHeight = stamp.getBoundingBox().getHeight();
		CDSRectangle rect = page.getCropBox();
		float scale = 1f;
		float offsetY = (rect.getHeight() - (formHeight * scale)) / 2;
		float offsetX = (rect.getWidth() - (formWidth * scale)) ;

		CSContent content = CSContent.createNew();
		CSCreator creator = CSCreator.createFromContent(content, page);

		creator.saveState();
		
		// System.out.println(offsetX + " " + offsetY);
		creator.transform(scale, 0, 0, scale, offsetX, 10);
		creator.doXObject(null, stamp);
		
		creator.restoreState();
		creator.close();

		page.cosAddContents(content.createStream());
	}
	
	protected PDForm createForm() {
		stamp = (PDForm) PDForm.META.createNew();
		PDFont font = PDFontType1.createNew(PDFontType1.FONT_Times_Roman);

		CSCreator creator = CSCreator.createNew(stamp);
		creator.textBegin();
		creator.setNonStrokeColorRGB(1.0f, 204f / 256f, 0); // orange
		creator.setStrokeColorRGB(1.0f, 204f / 256f, 0);
		
		creator.textSetFont(null, font, 14);
		
		String text = ((String) map.get("trailing"));
		if (map.get("customername") != null)
			text += ((String) map.get("customername")).trim();
		
		creator.textShow(text);
		creator.textEnd();
		creator.close();
		
		float width;
		float height;
		if (Dimension.class.isInstance(map.get("textBox"))){
			Dimension td = (Dimension) map.get("textBox");
			width = (float) (td.getWidth()+10); // + margin
			height = (float) (td.getHeight()+1); // + margin
		} else {
			Dimension td = StampFile.getTexBox(text);
			width = (float) (td.getWidth()+10); // + margin
			height = (float) (td.getHeight()+1); // + margin
		}

		
		stamp.setBoundingBox(new CDSRectangle(0, 0, width, height));

		return stamp;
	}
	
	
	public static Dimension getTexBox(String text){
		// FontMetrics metrics = graphics.getFontMetrics(font);
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.SCALE_DEFAULT);
	    Graphics2D g = image.createGraphics();
	    g.setFont(new Font("Times Roman", Font.PLAIN, 14));
	    FontMetrics fm = g.getFontMetrics();
				
		return new Dimension(fm.stringWidth(text), 14);
		
	}
	
	/** Function to omit bytes to UI based string 
	 *  
	 *  Limited to 1000TB
	 *  
	 * @param size 
	 * @return formatted String from bytes
	 */
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	public final void addListener(final ThreadCompleteListener listener) {
		listeners.add(listener);
	}

	public final void removeListener(final ThreadCompleteListener listener) {
		listeners.remove(listener);
	}

	private final void notifyListeners() throws InvocationTargetException, InterruptedException {
		for (ThreadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this);
		}
	}

}
