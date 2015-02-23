package zigzag;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

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

public class _StampLicense extends DefaultTableModel {
	
	
	private HashMap<Integer, StampFile> fileList;
	
	public JProgressBar progressBar;
	
	private JobSetting defaultSetting = new JobSetting();
	
	public _StampLicense(){
		super(new String[]{"File", "Size", " ", "Status"}, 0);
		
		fileList = new HashMap<Integer, StampFile>();
		progressBar = new JProgressBar();
		
		progressBar.setForeground(new Color(255, 200, 0));
		progressBar.setMinimum(0);
		
	}   
	
	public JobSetting getSetting() {
		return defaultSetting;
	}
	
	public boolean validateSetting(){
		return false;
	}

	public synchronized void addSFile(final StampFile sf){
			
		int s = (int) (sf.getFile().length()/1024);
		progressBar.setMaximum((int) (progressBar.getMaximum()+s));
		
		fileList.put(getRowCount(), sf);
		// addRow(sf.getRowData());
		
	}
	
	
	
	public synchronized void addSFile(File file){
		
		final StampFile sf = new StampFile(defaultSetting, file);
		
		int s = (int) (sf.getFile().length()/1024);
		progressBar.setMaximum((int) (progressBar.getMaximum()+s));
		
		fileList.put(getRowCount(), sf);
		// addRow(sf.getRowData());
		
	}
	
	public void clear(){
		getDataVector().removeAllElements();
        fireTableDataChanged();
        fileList.clear();
        progressBar.setValue(0);
	}
	
	
	public void execute() {
		
		// find text size > pause in system (resource hog)
		String text = defaultSetting.get("trailing") + " " + defaultSetting.get("customername");
		// defaultSetting.put("textBox", StampFile.getTexBox(text));
		
		// merge default settings to each files
		for (Entry<Integer, StampFile> fe : fileList.entrySet()) {
			StampFile sf = fe.getValue();
			for (Entry<String, Object> ts  : defaultSetting.entrySet()){
				sf.setSetting(ts.getKey(), ts.getValue());
			}
		}
		
		System.out.println("Max: " + progressBar.getMaximum());
		

		for (int i = 0; i < getRowCount(); i++){					
			Object enabled = getValueAt(i, 2);			
			if (Boolean.class.isInstance(enabled) &&  (boolean) enabled == true){				
				// thread pooling ?!				
				final int rowIndex = i;
				StampFile sf = fileList.get(i);
				
								
				sf.addListener(new ThreadCompleteListener(){
					@Override
					public synchronized void notifyOfThreadComplete(final StampFile t) {
						
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run() {
								setValueAt(t.getStatus(), rowIndex, 3);
								int s = (int) (t.getFile().length()/1024);	
								progressBar.setValue((int) (progressBar.getValue()+s));
							}
							
						});	
						
						System.out.println("Now: " + progressBar.getValue());
					}					
				});
				
				sf.start();
				
			}
		}
		
		
	}


	protected Object getStatus() {
		// TODO Auto-generated method stub
		return null;
	}


	
	

}
