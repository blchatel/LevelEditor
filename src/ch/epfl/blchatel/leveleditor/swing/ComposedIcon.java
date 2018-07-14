package ch.epfl.blchatel.leveleditor.swing;

import java.awt.*;
import javax.swing.*;

/**
 * The ComposedIcon will paint two, or more, Icons as a single Icon. The
 * Icons are painted in the order in which they are added.
 *
 * An adaptation of the CompoundIcon from :
 * https://tips4java.wordpress.com/2009/03/29/compound-icon/
 *
 */
public class ComposedIcon implements Icon{
	/// Array of all the icons
    private Icon[] icons;
    /// Gap space between the icons
	private int gap;

	/**
	 * Default ComposedIcon Constructor.
	 * @param gap (int): the gap between the icons in pixel
     * @param icons (Icon...): the Icons to be painted as part of the ComposedIcon
	 */
	public ComposedIcon(int gap, Icon... icons){
		this.gap = gap;

		for (int i = 0; i < icons.length; i++){
			if (icons[i] == null){
				String message = "Icon (" + i + ") cannot be null";
				throw new IllegalArgumentException( message );
			}
		}
		this.icons = icons;
	}

	/// ComposedIcon implements Icon

	@Override
    public int getIconWidth(){

	    // Add the gaps space
	    int width = (icons.length - 1) * gap;

	    // Sum all icons
        for (Icon icon : icons)
            width += icon.getIconWidth();

		return width;
    }

	@Override
    public int getIconHeight(){
		int height = 0;

        for (Icon icon : icons)
            height = Math.max(height, icon.getIconHeight());

		return height;
    }

	@Override
    public void paintIcon(Component c, Graphics g, int x, int y){

	    int height = getIconHeight();

        for (Icon icon : icons){
            int iconY = getOffset(height, icon.getIconHeight());
            icon.paintIcon(c, g, x, y + iconY);
            x += icon.getIconWidth() + gap;
        }
    }

	/*
	 *  When the icon value is smaller than the maximum value of all icons the
	 *  icon needs to be aligned appropriately. Calculate the offset to be used
	 *  when painting the icon to achieve the proper alignment.
	 */
	private int getOffset(int maxValue, int iconValue)
	{
		float offset = (maxValue - iconValue) * 0.5f;
		return Math.round(offset);
	}
}
