package fr.loicmathieu.bobbin.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.pironet.tda.Logfile;
import com.pironet.tda.TDA;
import com.pironet.tda.TableCategory;
import com.pironet.tda.ThreadDumpInfo;
import com.pironet.tda.TreeCategory;
import com.pironet.tda.utils.ColoredTable;
import com.pironet.tda.utils.TreeRenderer;

/**
 * This class represent the dump tree (on the top left of the GUI)
 * 
 * @author lmathieu
 *
 */
public class DumpTree extends JTree {
	private static final long serialVersionUID = 6324865598145547679L;

	private boolean runningAsJConsolePlugin;
	private boolean runningAsVisualVMPlugin;
	private boolean isFoundClassHistogram;
	private TDA tda;


	/**
	 * Constructor
	 * 
	 * @param tda
	 * @param root
	 * @param rootVisible
	 * @param runningAsJConsolePlugin
	 * @param runningAsVisualVMPlugin
	 * @param isFoundClassHistogram
	 */
	public DumpTree(TDA tda, TreeModel root, boolean rootVisible, boolean runningAsJConsolePlugin, boolean runningAsVisualVMPlugin, boolean isFoundClassHistogram) {
		super(root);
		this.setRootVisible(rootVisible);

		this.tda = tda;
		this.runningAsJConsolePlugin = runningAsJConsolePlugin;
		this.runningAsVisualVMPlugin = runningAsVisualVMPlugin;
		this.isFoundClassHistogram = isFoundClassHistogram;

		createTree();
	}


	/**
	 * Create the tree : called by the constructor
	 */
	protected void createTree() {

		//addTreeListener(tree);
		this.setShowsRootHandles(true);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		this.setCellRenderer(new TreeRenderer());

		// Listen for when the selection changes.
		this.addTreeSelectionListener(tda);

		// Create the popup menu of the tree.
		PopupMenu popup = new PopupMenu(tda, runningAsJConsolePlugin, runningAsVisualVMPlugin, isFoundClassHistogram);

		// Add listener to the text area so the popup menu can come up.
		MouseListener popupListener = new PopupListener(popup);
		this.addMouseListener(popupListener);
	}


	/**
	 * Navigate to path
	 * 
	 * @param path
	 */
	public void navigateToPath(TreeNode[] path){
		TreePath threadPath = new TreePath(path);
		this.setSelectionPath(threadPath);
		this.scrollPathToVisible(threadPath);
	}


	/**
	 * navigate to root node of currently active dump
	 */
	public void navigateToDump() {
		TreePath currentPath = this.getSelectionPath();
		this.setSelectionPath(currentPath.getParentPath());
		this.scrollPathToVisible(currentPath.getParentPath());
	}


	/**
	 * expand all dump node
	 * 
	 * @param expand
	 */
	public void expandAllDumpNodes(boolean expand) {
		TreeNode root = (TreeNode) this.getModel().getRoot();
		expandAll(this, new TreePath(root), expand);
	}


	/**
	 * expand or collapse all nodes of the specified tree
	 * 
	 * @param tree the tree to expand all/collapse all
	 * @param parent the parent to start with
	 * @param expand expand=true, collapse=false
	 */
	private void expandAll(JTree catTree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(catTree, path, expand);
			}
		}

		if (parent.getPathCount() > 1) {
			// Expansion or collapse must be done bottom-up
			if (expand) {
				catTree.expandPath(parent);
			}
			else {
				catTree.collapsePath(parent);
			}
		}
	}


	/**
	 * expand all nodes of the currently selected category, only works for tree categories.
	 */
	public void expandAllCatNodes(boolean expand) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
		JTree catTree = (JTree) ((TreeCategory) node.getUserObject()).getCatComponent(tda);
		if (expand) {
			for (int i = 0; i < catTree.getRowCount(); i++) {
				catTree.expandRow(i);
			}
		}
		else {
			for (int i = 0; i < catTree.getRowCount(); i++) {
				catTree.collapseRow(i);
			}
		}
	}


	/**
	 * navigate to child of currently selected node with the given prefix in name
	 * 
	 * @param startsWith node name prefix (e.g. "Threads waiting")
	 */
	public void navigateToChild(String startsWith) {
		TreePath currentPath = this.getSelectionPath();
		DefaultMutableTreeNode dumpNode = (DefaultMutableTreeNode) currentPath.getLastPathComponent();
		Enumeration childs = dumpNode.children();

		TreePath searchPath = null;
		while ((searchPath == null) && childs.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) childs.nextElement();
			String name = child.toString();
			if (name != null && name.startsWith(startsWith)) {
				searchPath = new TreePath(child.getPath());
			}
		}

		if (searchPath != null) {
			this.makeVisible(searchPath);
			this.setSelectionPath(searchPath);
			this.scrollPathToVisible(searchPath);
		}
	}

	/**
	 * navigate to monitor
	 * 
	 * @param monitorLink the monitor link to navigate to
	 * 
	 * @return the node navigated to
	 */
	public DefaultMutableTreeNode navigateToMonitor(String monitorLink) {
		String monitor = monitorLink.substring(monitorLink.lastIndexOf('/') + 1);

		// find monitor node for this thread info
		DefaultMutableTreeNode dumpNode = null;
		if (monitorLink.indexOf("Dump No.") > 0) {
			dumpNode = getDumpRootNode(monitorLink.substring(monitorLink.indexOf('/') + 1, monitorLink.lastIndexOf('/')),
					(DefaultMutableTreeNode) this.getLastSelectedPathComponent());
		}
		else {
			dumpNode = getDumpRootNode((DefaultMutableTreeNode) this.getLastSelectedPathComponent());
		}
		Enumeration childs = dumpNode.children();
		DefaultMutableTreeNode monitorNode = null;
		DefaultMutableTreeNode monitorWithoutLocksNode = null;
		while (childs.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) childs.nextElement();
			if (child.getUserObject() instanceof TreeCategory) {
				if (((TreeCategory) child.getUserObject()).getName().startsWith("Monitors (")) {
					monitorNode = child;
				}
				else if (((TreeCategory) child.getUserObject()).getName().startsWith("Monitors without")) {
					monitorWithoutLocksNode = child;
				}
			}
		}

		// highlight chosen monitor
		JTree searchTree = (JTree) ((TreeCategory) monitorNode.getUserObject()).getCatComponent(tda);
		TreePath searchPath = searchTree.getNextMatch(monitor, 0, Position.Bias.Forward);
		if ((searchPath == null) && (monitorWithoutLocksNode != null)) {
			searchTree = (JTree) ((TreeCategory) monitorWithoutLocksNode.getUserObject()).getCatComponent(tda);
			searchPath = searchTree.getNextMatch(monitor, 0, Position.Bias.Forward);
			monitorNode = monitorWithoutLocksNode;
		}

		if (searchPath != null) {
			this.navigateToPath(monitorNode.getPath());

			TreePath threadInMonitor = searchPath.pathByAddingChild(((DefaultMutableTreeNode) searchPath.getLastPathComponent()).getLastChild());
			searchTree.setSelectionPath(threadInMonitor);
			searchTree.scrollPathToVisible(searchPath);
			searchTree.setSelectionPath(searchPath);

			return monitorNode;
		}

		return null;
	}

	/**
	 * addon LMA : navigate to the thread from a link
	 * @param threadLink
	 * @return the node navigated to
	 */
	public DefaultMutableTreeNode navigateToThread(String threadLink){
		String tid = threadLink.substring(9);

		//find dump node
		DefaultMutableTreeNode dumpNode = null;
		if (threadLink.indexOf("Dump No.") > 0) {
			dumpNode = getDumpRootNode(threadLink.substring(threadLink.indexOf('/') + 1, threadLink.lastIndexOf('/')),
					(DefaultMutableTreeNode) this.getLastSelectedPathComponent());
		}
		else {
			dumpNode = getDumpRootNode((DefaultMutableTreeNode) this.getLastSelectedPathComponent());
		}

		//find Thread node
		Enumeration childs = dumpNode.children();
		DefaultMutableTreeNode threadNode = null;
		while (childs.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) childs.nextElement();
			if (child.getUserObject() instanceof TableCategory) {
				if (((TableCategory) child.getUserObject()).getName().startsWith("Threads (")) {
					threadNode = child;
				}
			}
		}

		//jump to thread node
		this.navigateToPath(threadNode.getPath());

		//highlight chosen thread
		ColoredTable searchTable = (ColoredTable) ((TableCategory) threadNode.getUserObject()).getCatComponent(tda);
		for (int row = 0; row < searchTable.getRowCount(); row++) {
			if (tid.equals(searchTable.getValueAt(row, 3).toString())) {

				// this will automatically set the view of the scroll in the location of the value
				searchTable.scrollRectToVisible(searchTable.getCellRect(row, 0, true));

				// this will automatically set the focus of the searched/selected row/value
				searchTable.setRowSelectionInterval(row, row);
			}
		}

		return threadNode;
	}

	/**
	 * search for dump root node of for given node
	 * 
	 * @param node starting to search for
	 * @return root node returns null, if no root was found.
	 */
	private DefaultMutableTreeNode getDumpRootNode(DefaultMutableTreeNode node) {
		// search for starting node
		DefaultMutableTreeNode rootNode = node;
		while (rootNode != null && !(rootNode.getUserObject() instanceof ThreadDumpInfo)) {
			rootNode = (DefaultMutableTreeNode) rootNode.getParent();
		}

		return rootNode;
	}

	/**
	 * get the dump with the given name, starting from the provided node.
	 * 
	 * @param dumpName
	 * @return
	 */
	private DefaultMutableTreeNode getDumpRootNode(String dumpName, DefaultMutableTreeNode node) {
		DefaultMutableTreeNode lastNode = null;
		DefaultMutableTreeNode currentNode = node;
		DefaultMutableTreeNode dumpNode = null;

		// search for starting node
		while (currentNode != null && !(currentNode.getUserObject() instanceof Logfile)) {
			lastNode = currentNode;
			currentNode = (DefaultMutableTreeNode) currentNode.getParent();
		}

		if (currentNode == null) {
			currentNode = lastNode;
		}

		for (int i = 0; i < currentNode.getChildCount(); i++) {
			Object userObject = ((DefaultMutableTreeNode) currentNode.getChildAt(i)).getUserObject();
			if ((userObject instanceof ThreadDumpInfo) && ((ThreadDumpInfo) userObject).getName().startsWith(dumpName)) {
				dumpNode = (DefaultMutableTreeNode) currentNode.getChildAt(i);
				break;
			}
		}


		return (dumpNode);
	}

	/**
	 * Listener that defined a popup menu on all node of the tree
	 * 
	 * @author lmathieu
	 *
	 */
	class PopupListener extends MouseAdapter {

		PopupMenu popup;

		PopupListener(PopupMenu popupMenu) {
			popup = popupMenu;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup.show(e.getComponent(), e.getX(), e.getY());

				//TODO find a better way to do this
				boolean enableDumpMenu = (getSelectionPath() != null)
						&& ((DefaultMutableTreeNode)getSelectionPath().getLastPathComponent()).getUserObject() instanceof ThreadDumpInfo;
				popup.setDumpMenuItemVisibility(enableDumpMenu);

			}
		}
	}

}
