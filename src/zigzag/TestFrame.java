package zigzag;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JScrollPane;

public class TestFrame extends JApplet {
	
	enum HandDesc {
		HIGH_CARD, PAIR, TWO_PAIR, THREE, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_AKIND,  STRAIGHT_FLUSH, ROYAL_FLUSH
	}
	final String[] values = new String[] { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
	
	
	private JTable table;

	/**
	 * Create the applet.
	 */
	public TestFrame() {
		
		table = new JTable();
		
		StampItemModel itemModel = new StampItemModel();
		itemModel.addStamp(new StampFile(new JobSetting(), new File("pdfauto/new/demo.pdf")));
		table.setModel(itemModel);
		
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
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		
		String[] suit = new String[] {"C", "D", "H", "S" };
		
		
		String line = "10C 6H 6C 7H 10D AS KH 4C AD 4S";
		
		String[] table = line.split(" ");
		
		Hand h1 = new Hand();
		h1.cards = Arrays.copyOfRange(table, 0, 5);
		
		Hand h2 = new Hand();
		h2.cards = Arrays.copyOfRange(table, 5, 10);
		
		
		int j = h1.compareTo(h1, h2);
		
		
		System.out.println(line+j);
		System.out.println(h1.getHand().toString());
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		

	}
	
	static int findIndex (String[] array, String a){
		
		
		for(int i = 0; i < array.length; i++)
			if (array[i].contains(a))
				return i;
		return -1;
		
		// return -1;
	}
	
	class Hand {
		String[] cards = new String[5];
		Map <HandDesc,String> myHand = new HashMap<HandDesc,String>();
		
		
		
		public HandDesc getHand(){
			
			Arrays.sort(cards, new Comparator<String>(){	
				@Override
				public int compare(String a, String b) {
					//System.out.println(a.substring(0, a.length()-1));
					//System.out.println(b.substring(0, b.length()-1));
					int i = findIndex(values, a.substring(0, a.length()-1));
					int j = findIndex(values, b.substring(0, b.length()-1));
					return (i-j) == 0 ? 0 : (i-j) > 0 ? 1 : -1;
				}
			});
			
			//hand descriptive 				
			HandDesc hd = HandDesc.HIGH_CARD;		
			// pair and kind
			Map<String, Integer> mapValues = new LinkedHashMap <String, Integer>();
			Map<String, Integer> mapSuit = new HashMap <String, Integer>();
			for(String card: cards){
				
				String v = card.substring(0, card.length()-1);
				String s = card.substring(card.length()-1, card.length());			
				
				mapValues.put(v, (mapValues.get(v) == null) ? 1 : mapValues.get(v)+1);			
				mapSuit.put(s, (mapSuit.get(s) == null) ? 1 : mapSuit.get(s)+1);
			}
			
			if (mapValues.containsValue(2))
				hd = HandDesc.PAIR;
			if (mapValues.containsValue(3))
				hd = HandDesc.THREE;
			if (mapValues.containsValue(4))
				hd = HandDesc.FOUR_OF_AKIND;
			
			HashSet<String> h = new LinkedHashSet<String>(mapValues.keySet());
			if (h.size() == 3)
				hd = HandDesc.TWO_PAIR;
			if (mapValues.containsValue(3) && mapValues.containsValue(2))
				hd = HandDesc.FULL_HOUSE;
			if (mapSuit.size() == 1)
				hd = HandDesc.FLUSH;
			if (h.size() == 5) {
				String v = cards[0].substring(0, cards[0].length()-1);
				int j = 1, i,  f = findIndex(values, v)+1;
				for(i = f; i < values.length; i++, j++){
					if (j > cards.length-1) break;
					if (values[i] != cards[j].substring(0, cards[j].length()-1))	break;
				}
				if (i-f == 4) {
					hd = HandDesc.STRAIGHT;
					if (mapSuit.size() == 1) {
						hd = HandDesc.STRAIGHT_FLUSH;
						if (v == "10")
							hd = HandDesc.ROYAL_FLUSH;
					}
				}
			}
			
			return hd;
			
		}
		
		public int compareTo(Hand a, Hand b){
			
			int k = a.getHand().ordinal();
			
			int z = b.getHand().ordinal();
			int c = k-z;
			if (c != 0 )
				return (c > 0) ? 1 : -1;
			else {
				
				// two pair
				if (k == 2){
					// a.cards[a.cards.length-1];
				}
				
			}
			
			return 0;
		}
		
	}

}


