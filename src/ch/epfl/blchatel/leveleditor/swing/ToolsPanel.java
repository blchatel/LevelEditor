package ch.epfl.blchatel.leveleditor.swing;

import javax.swing.*;
import java.awt.*;

/**
 * The Tools Panel is a BoxLayout with :
 * - a Scroll Panel with all Brush tools (BrushDropList) for 60% of the whole height
 * - a Options Panel with all options for 40% of the whole height
 */
public class ToolsPanel extends JPanel {

	/// Y separation to implement the 60/40 height repartition
	private final static double Y_SEPARATION = 0.6;
	/// The two components
	private final BrushDropList brushDropList;
	private final OptionsPanel optionPanel;

	/**
	 * Default ToolsPanel Constructor
	 * @param d (Dimension) : dimension of this
	 */
	ToolsPanel(Dimension d) {

		// Define this ToolsPanel info
		// Define fixed dimension :
		// - if the parent does't use layout manager
		setSize(d);
		// - if the parent use a layout manager
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Define the Brush Scroll Panel
		Dimension brushDimension = new Dimension(d.width, (int)(d.height*Y_SEPARATION));
		brushDropList = new BrushDropList(brushDimension);
		add(brushDropList);

		Dimension optionDimension = new Dimension(d.width, (int)(d.height*(1.0-Y_SEPARATION)));
		optionPanel = new OptionsPanel(optionDimension);
		add(optionPanel);
	}

	/**
	 * Add a new Listener to the BrushDropList component
	 * @param listener (BrushDropList.Listener): the new Listener
	 */
	public void addFileDropListListener(BrushDropList.Listener listener){
		brushDropList.addListener(listener);
	}

	/**
	 * Add a new Listener to the OptionsPanel component
	 * @param listener (OptionsPanel.Listener): the new Listener
	 */
	public void addOptionsListener(OptionsPanel.Listener listener){
		optionPanel.addListener(listener);
	}
}
