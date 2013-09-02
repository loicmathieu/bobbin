package fr.loicmathieu.bobbin.gui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.pironet.tda.ThreadInfo;

/**
 * @author lmathieu
 *
 */
public class LinesTableModel extends AbstractTableModel {

	private Vector elements;

	private String[] columnNames = null;

	/**
	 * 
	 * @param root
	 */
	public LinesTableModel(DefaultMutableTreeNode rootNode) {
		// transform child nodes in proper vector.
		if(rootNode != null) {
			elements = new Vector();
			for(int i = 0; i < rootNode.getChildCount(); i++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
				elements.add(childNode.getUserObject());
				ThreadInfo ti = (ThreadInfo) childNode.getUserObject();
				if(columnNames == null) {
					columnNames = new String[] {"Name", "State", "Nb"};
				}
			}
		}
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public int getRowCount() {
		return(elements.size());
	}

	public int getColumnCount() {
		return(columnNames.length);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		ThreadInfo ti = ((ThreadInfo) elements.elementAt(rowIndex));
		String[] columns = ti.getTokens();
		return columns[columnIndex];
	}

	/**
	 * get the thread info object at the specified line
	 * @param rowIndex the row index
	 * @return thread info object at this line.
	 */
	public ThreadInfo getInfoObjectAtRow(int rowIndex) {
		return(rowIndex >= 0 && rowIndex < getRowCount() ? (ThreadInfo) elements.get(rowIndex) : null);
	}

	/**
	 * @inherited
	 */
	@Override
	public Class getColumnClass(int columnIndex) {
		if(columnIndex > 1 && columnIndex < 5) {
			return Integer.class;
		} else {
			return String.class;
		}
	}

	/**
	 * search for the specified (partial) name in thread names
	 * 
	 * @param startRow row to start the search
	 * @param name the (partial) name
	 * @return the index of the row or -1 if not found.
	 */
	public int searchRowWithName(int startRow, String name) {
		int i = startRow;
		boolean found = false;
		while(!found && (i < getRowCount())) {
			found = getInfoObjectAtRow(i++).getTokens()[0].indexOf(name) >= 0;
		}

		return(found ? i-1 : -1);
	}
}