package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Main ContentPane.
 * - A Border layout with a Menu, a Grid panel and a Tool panel
 * - The menu is NORTH
 * - The Grid is CENTER, takes 67% of the width and full left height
 * - The Tools is EAST, takes 33% of the width and full left height
 */
public class ContentPane extends JPanel {

	/// X separation to implement the 80/20 width repartition
	private final static double X_SEPARATION = 0.67;

	/// The three components
	private final Menu menu;
	private final DisplayPanel grid;
	private final ToolsPanel tools;

	/**
	 * Default ContentPane Constructor
	 * @param d (Dimension) : dimension of this
	 */
	public ContentPane(Dimension d) {

		// Define this ContentPane info
		// Define fixed dimension :
		// - if the parent does't use layout manager
		setSize(d);
		// - if the parent use a layout manager
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
		setLayout(new BorderLayout());

		// Define the Grid
		final Dimension gridDimension = new Dimension((int)(d.width* X_SEPARATION), d.height);
		grid = new DisplayPanel(gridDimension);
		add(grid, BorderLayout.CENTER);

		// Define the Menu
		menu = new Menu();
		menu.addListener(grid.getMenuListener());
		add(menu, BorderLayout.NORTH);

		// Define the Tools
		final Dimension toolsDimension = new Dimension((int)(d.width*(1.0- X_SEPARATION)), d.height);
		tools= new ToolsPanel(toolsDimension);
		tools.addFileDropListListener(grid.getBrushDropListListener());
		tools.addOptionsListener(grid.getOptionsPanelListener());
		add(tools, BorderLayout.EAST);
	}

	public void initArgsDisplay(File file){
		if(file != null)
			grid.getMenuListener().onOpenLVE(file, LayerImage.getFromFile(file));
	}
}