package zigzag;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.ListSelectionModel;

import java.awt.GridLayout;

public class MPanel extends JPanel {
	
	private JScrollPane scrollPane;
	private JTable table;
	private JButton btnProcess;
	
	private StampItemModel tm = new StampItemModel();
	
	
	private ArrayList<File> fileList = new ArrayList<File>();
	private JTextField txtCustomerName;
	
	
	
	
	public ArrayList<File> getFileList() {
		return fileList;
	}

	public String getCustomerName() {
		return txtCustomerName.getText();
	}

	public void setCustomerName(String n){
		txtCustomerName.setText(n);
	}
	
	
	/**
	 * Create the panel.
	 */
	public MPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel header = new JPanel();
		add(header, BorderLayout.NORTH);
		
		JLabel lblPdfLicense = new JLabel("Personalize PDF");
		lblPdfLicense.setFont(new Font("Tiger", Font.BOLD, 16));
		header.add(lblPdfLicense);
		
		JPanel center = new JPanel();
		add(center, BorderLayout.WEST);
		center.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("70px"),
				ColumnSpec.decode("61px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("24px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblSelectFolder = new JLabel("Folder location");
		lblSelectFolder.setVerticalAlignment(SwingConstants.TOP);
		center.add(lblSelectFolder, "1, 1, 2, 1, right, center");
		
		JButton btnNewButton = new JButton("Select");
		btnNewButton.setVerticalAlignment(SwingConstants.TOP);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new java.io.File("."));
	            fc.setDialogTitle("Browse the folder to process");
	            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	            fc.setAcceptAllFileFilterUsed(false);

	            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	                tm.clear(); // new list 
	                btnProcess.setEnabled(true);
	                for (File file : fc.getSelectedFile().listFiles()){
	                    if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
	                        System.out.println(file.getName());	                            					
	    					tm.addStamp(new StampFile(file));      					
	                    }
	                }
	            } else {
	                System.out.println("No Selection ");
	            }
			}
		});
		
		
		center.add(btnNewButton, "4, 1, left, center");
		
		btnProcess = new JButton("Import");
		btnProcess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {

				if (getCustomerName().length() > 0 && tm.getRowCount() > 0) {
					// compile settings 
					tm.execute();
					btnProcess.setEnabled(false);
					
					
					Desktop.isDesktopSupported();
					Desktop desktop = Desktop.getDesktop();
					try {
						// checks ?!
						desktop.open(new File((String) tm.defaultSetting.get("output")));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				} else {
					if (getCustomerName().length() <= 0)
						JOptionPane.showMessageDialog(null, "Cutomer Name is Empty !" , "Warning", JOptionPane.WARNING_MESSAGE);
					if (tm.getRowCount() <= 0)
						JOptionPane.showMessageDialog(null, "Select a folder with pdf files" , "Warning", JOptionPane.WARNING_MESSAGE);
					
				}
				
			}
		});
		
		JLabel lblCustomerName = new JLabel("School ");
		lblCustomerName.setHorizontalAlignment(SwingConstants.RIGHT);
		center.add(lblCustomerName, "1, 3, right, default");
		
		txtCustomerName = new JTextField();
		txtCustomerName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}
		    private void update(){
		    	tm.defaultSetting.put("customername", txtCustomerName.getText());
		    }
		});
		
		
		//if (getParameter("name") != null)
			//txtSchool.setText(getParameter("name"));
		center.add(txtCustomerName, "2, 3, 3, 1, fill, default");
		txtCustomerName.setColumns(10);
		
		JButton button = new JButton("Output Folder");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new java.io.File("."));
	            fc.setDialogTitle("Select a folder for output");
	            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	            fc.setAcceptAllFileFilterUsed(false);

	            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	            	 tm.defaultSetting.put("output", fc.getSelectedFile().getAbsolutePath());

	            }
			}
		});
		button.setVerticalAlignment(SwingConstants.TOP);
		center.add(button, "2, 5, 3, 1");
		center.add(btnProcess, "2, 11, 3, 1");
		
		
		
		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		table = new JTable();
		new FileDrop( null, scrollPane, new FileDrop.Listener()
        {   public void filesDropped( java.io.File[] files )
            {   for( int i = 0; i < files.length; i++ ) 
            	{            		
            		if (files[i].getName().toLowerCase().endsWith(".pdf")){}
            			// tm.addStamp(new StampFile(files[i]));
            		// System.out.println(files[i].getName());
                } 
            }
        }); 
		
		table.setModel(tm);
		table.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		        JTable table =(JTable) me.getSource();
		        Point p = me.getPoint();
		        int row = table.rowAtPoint(p);
		        if (me.getClickCount() == 2) {
		            System.out.println(me);
		        }
		    }
		});
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setFont(new Font("Times New Roman", Font.PLAIN, 11));
		
		DefaultTableCellRenderer sizeRend = new DefaultTableCellRenderer();
		sizeRend.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(sizeRend);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(30);
        
        DefaultTableCellRenderer statRend = new DefaultTableCellRenderer();
        statRend.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(statRend);
		
		
		// DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer) table.getDefaultRenderer(Object.class);
        // defaultRenderer.setHorizontalAlignment(JLabel.CENTER);
       
		scrollPane.setViewportView(table);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(scrollPane);
		add(panel, BorderLayout.CENTER);
		
		JPanel progressPanel = new JPanel();
		add(progressPanel, BorderLayout.SOUTH);
		
		
		progressPanel.add(tm.progressBar);
		progressPanel.setLayout(new GridLayout(1, 0, 0, 0));

	}

}
