package zigzag;

import javax.swing.*;

public class PDFApplet extends JApplet {
	public PDFApplet() {
	}
	
	MPanel panel; 
	
	public void init() {
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                	createGUI();
                	initialize();
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
            e.printStackTrace();
        }
    }

	/**
	 * Create the applet.
	 */
	private void createGUI() {
        //Create and set up the content pane.
        panel = new MPanel();
        panel.setOpaque(true);
        setContentPane(panel);
    }
	
	private void initialize () {
		
		if (getParameter("name") != null)
			panel.setCustomerName(getParameter("name"));
	}

}
