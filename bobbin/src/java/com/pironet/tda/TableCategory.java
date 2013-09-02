/*
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * TDA is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * TDA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with TDA; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: TableCategory.java,v 1.7 2008-03-09 06:36:51 irockel Exp $
 */
package com.pironet.tda;

import java.util.EventListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.pironet.tda.filter.FilterChecker;
import com.pironet.tda.utils.ColoredTable;
import com.pironet.tda.utils.PrefManager;
import com.pironet.tda.utils.TableSorter;
import com.pironet.tda.utils.ThreadsTableModel;
import com.pironet.tda.utils.ThreadsTableSelectionModel;

import fr.loicmathieu.bobbin.gui.LinesTableModel;

/**
 * table category type, displays its content in a table.
 * @author irockel
 * @author lmathieu
 */
public class TableCategory extends AbstractCategory {
	private static final long serialVersionUID = 6781027138814158776L;
	private transient JTable filteredTable;
	private Model model;

	/**
	 * Creates a new instance of TableCategory
	 */
	public TableCategory(String name, int iconID) {
		this(name, iconID, true);
	}

	/**
	 * Creates a new instance of TableCategory
	 */
	public TableCategory(String name, int iconID, Model m) {
		this(name, iconID, true, m);
	}

	/**
	 * Creates a new instance of TableCategory
	 */
	public TableCategory(String name, int iconID, boolean filtering) {
		this(name, iconID, filtering, Model.THREADS);
	}

	/**
	 * Creates a new instance of TableCategory
	 */
	public TableCategory(String name, int iconID, boolean filtering, Model m) {
		setName(name);
		setFilterEnabled(filtering);
		setIconID(iconID);
		this.model = m;
	}

	/**
	 * @inherited
	 */
	public JComponent getCatComponent(EventListener listener) {
		if(isFilterEnabled() && ((filteredTable == null) || (getLastUpdated() < PrefManager.get().getFiltersLastChanged()))) {
			// first refresh filter checker with current filters
			setFilterChecker(FilterChecker.getFilterChecker());

			// apply new filter settings.
			DefaultMutableTreeNode filteredRootNode = filterNodes(getRootNode());
			if(filteredRootNode != null && filteredRootNode.getChildCount() > 0) {
				//addon LMA : enable different display for threads or lines
				TableModel ttm = null;
				if(model == Model.LINES){
					ttm = new LinesTableModel(filterNodes(getRootNode()));
				}else {
					ttm = new ThreadsTableModel(filterNodes(getRootNode()));
				}

				// create table instance (filtered)
				setupTable(ttm, listener);
			} else {
				// just an empty table
				filteredTable = new JTable();
			}

			setLastUpdated();
		} else if (!isFilterEnabled() && ((filteredTable == null) || (getLastUpdated() < PrefManager.get().getFiltersLastChanged()))) {
			// create unfiltered table view.
			if(getRootNode().getChildCount() > 0) {
				//addon LMA : enable different display for threads or lines
				TableModel ttm = null;
				if(model == Model.LINES){
					ttm = new LinesTableModel(filterNodes(getRootNode()));
				}else {
					ttm = new ThreadsTableModel(filterNodes(getRootNode()));
				}

				// create table instance (unfiltered)
				setupTable(ttm, listener);
			}
		}
		return(filteredTable);
	}

	/**
	 * setup the table instance with the specified table model
	 * (either filtered or none-filtered).
	 * @param ts the table sorter/model to use.
	 * @param listener the event listener to add to the table
	 */
	private void setupTable(TableModel tm, EventListener listener) {
		TableSorter ts = new TableSorter(tm);
		filteredTable = new ColoredTable(ts);
		ts.setTableHeader(filteredTable.getTableHeader());
		filteredTable.setSelectionModel(new ThreadsTableSelectionModel(filteredTable));
		filteredTable.getSelectionModel().addListSelectionListener((ListSelectionListener) listener);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.RIGHT);

		// currently only two different views have to be dealt with,
		// with more the model should be subclassed.
		if(tm.getColumnCount() > 3) {
			filteredTable.getColumnModel().getColumn(0).setPreferredWidth(300);
			filteredTable.getColumnModel().getColumn(1).setPreferredWidth(30);
			filteredTable.getColumnModel().getColumn(2).setPreferredWidth(15);

			filteredTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
			filteredTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
			filteredTable.getColumnModel().getColumn(4).setCellRenderer(renderer);
		} else {
			filteredTable.getColumnModel().getColumn(0).setPreferredWidth(300);
			filteredTable.getColumnModel().getColumn(1).setPreferredWidth(30);
			filteredTable.getColumnModel().getColumn(2).setPreferredWidth(50);

			filteredTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		}
	}

	/**
	 * get the currently selected user object.
	 * @return the selected object or null otherwise.
	 */
	public ThreadInfo getCurrentlySelectedUserObject() {
		return(filteredTable == null || filteredTable.getSelectedRow() < 0 ? null : (ThreadInfo) ((DefaultMutableTreeNode) getRootNode().getChildAt(filteredTable.getSelectedRow())).getUserObject());
	}

	/**
	 * Enum for the type of table category : threads or lines
	 * @author lmathieu
	 *
	 */
	public static enum Model {
		THREADS, LINES
	}

}
