package zigzag;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import zigzag.StampFile.Status;

public class StampItemModel extends AbstractTableModel {
	
	private static final long serialVersionUID = -2678412861179051038L;

	private ArrayList<StampFile> fileList;
		
	public JProgressBar progressBar;
	public JobSetting defaultSetting;
	
	private Object[] columnNames= {"File", "Size", " ", "Status"};
	
	public StampItemModel() {
		fileList = new ArrayList<StampFile>();
		
		defaultSetting = new JobSetting();
		progressBar = new JProgressBar();
		progressBar.setForeground(new Color(255, 200, 0));
		progressBar.setMinimum(0);
		progressBar.setMaximum(0);
	}
	
	public void addStamp(StampFile sf) {
		fileList.add(sf);
		int s = (int) (sf.getFile().length()/1024);
		progressBar.setMaximum((int) (progressBar.getMaximum()+s));
	}
	
	public void clear(){
        fileList.clear();
        fireTableDataChanged();
        progressBar.setValue(0);
        progressBar.setMinimum(0);
		progressBar.setMaximum(0);
	}
	
	public void execute() {
		
		// find text size > pause in system (resource hog)
		// String text = defaultSetting.get("trailing") + " " + defaultSetting.get("customername");
		// defaultSetting.put("textBox", StampFile.getTexBox(text));
		
		// merge default settings to each files		
 		for (StampFile sf : fileList) {
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
				
				if (sf.getState() != Thread.State.NEW)
					continue;
												
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
						
						fireTableRowsUpdated(rowIndex, rowIndex);
						//fireTableDataChanged();
						System.out.println("Now: " + progressBar.getValue());
					}					
				});
				
				
				
				sf.start();
			}
		}
		
		
	}
	
	public boolean isCellEditable(int row, int col) {
        if (col == 2)
        	return true;
        else
        	return false;
        
        
    }
	
	@Override
	public String getColumnName(int column) {
	    return (String) columnNames[column];
	}
	
	@Override
	public int getColumnCount() {
		
		return 4;
	}

	@Override
	public int getRowCount() {
		return fileList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		Object value = "??";
		StampFile sf = fileList.get(rowIndex);
        switch (colIndex) {
            case 0:
            	value =  sf.getFile().getName();
                break;
            case 1:
                value = StampFile.readableFileSize(sf.getFile().length());
                break;
            case 2:
                value = sf.getSetting("enabled");
                break;
            case 3:
                value = sf.getStatus() != Status.UNKNOWN ? sf.getStatus().toString() : "";
                break;
        }
        return value;
	}
	
	@Override
	public void setValueAt (Object value, int row, int col){
		StampFile sf = fileList.get(row);
		if (isCellEditable(row, col)){
			if (col == 2){				
				if ( (Boolean) value == true)
					progressBar.setMaximum((int) (progressBar.getMaximum()+sf.getFile().length()/1024));
				else 
					progressBar.setMaximum((int) (progressBar.getMaximum()-sf.getFile().length()/1024));
				
				sf.setSetting("enabled", value);
				
			}
				
		}
		if (col == 3)
			sf.setStatus((Status) value);
	}
	
	@Override
    public Class<?> getColumnClass(int col) {
        if (col == 2)
        	return Boolean.class;
        else
        	return String.class;
    }
	

	

}
