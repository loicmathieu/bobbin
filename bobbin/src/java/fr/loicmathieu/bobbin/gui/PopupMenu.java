package fr.loicmathieu.bobbin.gui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.pironet.tda.TDA;
import com.pironet.tda.utils.PrefManager;

/**
 * Generale popup menu
 * TODO provide specific popup menu for each node
 * 
 * @author lmathieu
 *
 */
public class PopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 6487129526734029377L;

	private TDA listener;
	private boolean runningAsJConsolePlugin;
	private boolean runningAsVisualVMPlugin;
	private boolean isFoundClassHistogram;

	private JMenuItem showDumpMenuItem;

	public PopupMenu(TDA listener, boolean runningAsJConsolePlugin, boolean runningAsVisualVMPlugin, boolean isFoundClassHistogram){
		this.listener = listener;
		this.runningAsJConsolePlugin = runningAsJConsolePlugin;
		this.runningAsVisualVMPlugin = runningAsVisualVMPlugin;
		this.isFoundClassHistogram = isFoundClassHistogram;

		createPopupMenu();
	}

	public void setDumpMenuItemVisibility(boolean enable){
		showDumpMenuItem.setEnabled(enable);
	}


	private void createPopupMenu() {
		JMenuItem menuItem;

		menuItem = new JMenuItem("Diff Selection");
		menuItem.addActionListener(this.listener);
		this.add(menuItem);
		menuItem = new JMenuItem("Find long running threads...");
		menuItem.addActionListener(this.listener);
		this.add(menuItem);

		showDumpMenuItem = new JMenuItem("Show selected Dump in logfile");
		showDumpMenuItem.addActionListener(this.listener);
		showDumpMenuItem.setEnabled(false);
		if (!runningAsJConsolePlugin && !runningAsVisualVMPlugin) {
			this.addSeparator();
			menuItem = new JMenuItem("Parse loggc-logfile...");
			menuItem.addActionListener(this.listener);
			if (!PrefManager.get().getForceLoggcLoading()) {
				menuItem.setEnabled(!isFoundClassHistogram);
			}
			this.add(menuItem);

			menuItem = new JMenuItem("Close logfile...");
			menuItem.addActionListener(this.listener);
			this.add(menuItem);
			this.addSeparator();
			this.add(showDumpMenuItem);
		}
		else {
			this.addSeparator();
			if (!runningAsVisualVMPlugin) {
				menuItem = new JMenuItem("Request Thread Dump...");
				menuItem.addActionListener(this.listener);
				this.add(menuItem);
				this.addSeparator();
				menuItem = new JMenuItem("Preferences");
				menuItem.addActionListener(this.listener);
				this.add(menuItem);
				menuItem = new JMenuItem("Thread Filters");
				menuItem.addActionListener(this.listener);
				this.add(menuItem);
				this.addSeparator();
				menuItem = new JMenuItem("Save Logfile...");
				menuItem.addActionListener(this.listener);
				this.add(menuItem);
				this.addSeparator();
				menuItem = new JCheckBoxMenuItem("Show Toolbar", PrefManager.get().getShowToolbar());
				menuItem.addActionListener(this.listener);
				this.add(menuItem);
				this.addSeparator();
				menuItem = new JMenuItem("Help");
				menuItem.addActionListener(this.listener);
				this.add(menuItem);
				this.addSeparator();
			}
			menuItem = new JMenuItem("About TDA");
			menuItem.addActionListener(this.listener);
			this.add(menuItem);
		}
	}
}
