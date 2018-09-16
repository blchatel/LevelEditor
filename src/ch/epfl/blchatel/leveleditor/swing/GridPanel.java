package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;


/**
 * The GridPanel is a panel containing a grid
 * - the cell resolution is 16x16 pixels
 * - the grid contained cannot overflow the panel dimension
 *     - so maxCellsX and maxCellsY are defined
 * The GridPanel is a Menu.Listener and BrushDropList.Listener
 */
public class GridPanel extends JTabbedPane implements Menu.Listener, BrushDropList.Listener, OptionsPanel.Listener{

	// delta x and y in px
	private final static int DELTA = 40;

	/// Max number of cells we can put in the X direction without overflow
	private final int maxCellsX;
	/// Max number of cells we can put in the Y direction without overflow
	private final int maxCellsY;

	/// The file to save into (may be null)
	private File saveFile;
	/// The background image behind the grid (may be null)
	private LayerImage image;
	/// The brush image (may be null)
	private LayerImage mouseBrush;
	//
	private boolean drawBackground, drawForeground, drawBehavior;
    /// The current brush's position in the grid (cell coordinates)
    private int cellBrushX, cellBrushY;
	/// The current brush's position in the grid (pixel of the bottom left corner of the cell)
	private int pixelBrushX, pixelBrushY;
	///
	private int lastDrawX, lastDrawY;
	///
	private int deltaDragX, deltaDragY;
	///
	private OptionsPanel.Tool tool;

	/**
	 * Default GridPanel Constructor
	 * @param d (Dimension) : dimension of this
	 */
	GridPanel(Dimension d) {

		// Define this GridPanel info
		// Define fixed dimension :
		// - if the parent does't use layout manager
		setSize(d);
		// - if the parent use a layout manager
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);

		maxCellsX = (d.width-2*DELTA) / LayerImage.CELL_RESOLUTION;
		maxCellsY = (d.height-2*DELTA) / LayerImage.CELL_RESOLUTION;

		// Init the brush position (not in the grid)
		pixelBrushX = -1;
		pixelBrushY = -1;

		// Init other integers and option
		deltaDragX = OptionsPanel.DEFAULT_DRAG_X;
		deltaDragY = OptionsPanel.DEFAULT_DRAG_Y;
		tool = OptionsPanel.DEFAULT_TOOL;
		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(tool.getScaledImage(
		        LayerImage.CELL_RESOLUTION, LayerImage.CELL_RESOLUTION), new Point(0,0),"tool cursor"));

		addTab("Background", null);
		addTab("Foreground", null);
		addTab("Behavior", null);

		drawBackground = true;
		drawForeground = true;
		drawBehavior = true;

		// Add the mouse Listeners
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if(SwingUtilities.isLeftMouseButton(e) && image != null && mouseBrush != null
						&& e.getX() > DELTA && e.getX() < image.pixelWidth+DELTA
						&& e.getY() > DELTA && e.getY() < image.pixelHeight+DELTA) {

					updateBrushPosition(e.getX(), e.getY());

					if(Math.abs(pixelBrushX - lastDrawX) >= deltaDragX
						|| Math.abs(pixelBrushY - lastDrawY) >= deltaDragY) {
						drawBrush(pixelBrushX-DELTA, pixelBrushY-DELTA - mouseBrush.pixelHeight + 1,
                                cellBrushX, cellBrushY - mouseBrush.cellHeight + 1);
						lastDrawX = pixelBrushX;
						lastDrawY = pixelBrushY;
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				if(image != null && mouseBrush != null
						&& e.getX() > DELTA && e.getX() < image.pixelWidth+DELTA
						&& e.getY() > DELTA && e.getY() < image.pixelHeight+DELTA)
					updateBrushPosition(e.getX(), e.getY());
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e){

				if(SwingUtilities.isLeftMouseButton(e) && image != null && mouseBrush != null
						&& e.getX() > DELTA && e.getX() < image.pixelWidth+DELTA
						&& e.getY() > DELTA && e.getY() < image.pixelHeight+DELTA) {

					switch(tool){
						case BRUSH:
							drawBrush(pixelBrushX-DELTA, pixelBrushY-DELTA - mouseBrush.pixelHeight + 1,
                                    cellBrushX, cellBrushY - mouseBrush.cellHeight + 1);
							lastDrawX = pixelBrushX;
							lastDrawY = pixelBrushY;
							break;
						case FILL:
							fillBrush(pixelBrushX-DELTA, pixelBrushY-DELTA -mouseBrush.pixelHeight+1,
                                    cellBrushX, cellBrushY - mouseBrush.cellHeight + 1);
							break;
						default:
							System.out.println("Unknown Tools");
							break;
					}
				}
			}
		});
	}




	/**
	 * Update the brush position (the bottom-left corner containing the mouse cursor)
	 * @param x (int) : X-position of the mouse in this GridPanel in pixel
	 * @param y (int) : Y-position of the mouse in this GridPanel in pixel
	 */
	private void updateBrushPosition(int x, int y){
        cellBrushX = ((x-DELTA) / LayerImage.CELL_RESOLUTION);
        cellBrushY = ((y-DELTA) / LayerImage.CELL_RESOLUTION);

		pixelBrushX = DELTA + cellBrushX * LayerImage.CELL_RESOLUTION;
		pixelBrushY = DELTA + cellBrushY * LayerImage.CELL_RESOLUTION + LayerImage.CELL_RESOLUTION - 1;

		repaint();
	}

	/**
	 * Draw the brush image at position (x, y) which is the top-left corner of the drawn image
	 * and repaint the image
	 * Notice : probably need to adapt the brush position form bottom-left to top-left corner before calling this
	 * @param pixelX (int) : x-coordinate of the top-left corner
	 * @param pixelY (int) : y-coordinate of the top-left corner
	 * @param cellX (int) : x-coordinate of the cell in the grid (for behavior)
	 * @param cellY (int) : y-coordinate of the cell in the grid (for behavior)
	 */
	private void drawBrush(int pixelX, int pixelY, int cellX, int cellY) {
        // Draw the image on to the buffered image
        Graphics2D bGr = image.background.createGraphics();
        if(drawBackground) {
			bGr.drawImage(mouseBrush.background, pixelX, pixelY, null);
			bGr.dispose();
			repaint();
		}
        if (drawForeground && mouseBrush.foreground != null){
            bGr = image.foreground.createGraphics();
            bGr.drawImage(mouseBrush.foreground, pixelX, pixelY, null);
            bGr.dispose();
        }

        if(drawBehavior) {
			bGr = image.behavior.createGraphics();
			bGr.drawImage(mouseBrush.behavior, cellX, cellY, null);
			bGr.dispose();
		}
		repaint();
	}

	/**
	 * Fill the image with the brush image from position (x, y) which is the top-left corner of the drawn image
	 * and repaint the image
	 * Notice : probably need to adapt the brush position form bottom-left to top-left corner before calling this
	 * Notice : this function will fill the whole image
     * @param pixelX (int) : x-coordinate of the top-left corner
     * @param pixelY (int) : y-coordinate of the top-left corner
     * @param cellX (int) : x-coordinate of the cell in the grid (for behavior)
     * @param cellY (int) : y-coordinate of the cell in the grid (for behavior)
	 */
	private void fillBrush(int pixelX, int pixelY, int cellX, int cellY){

		// Find the firstX, firstY, lastX and last Y coordinates
		int firstPixelX = pixelX;
		int firstCellX = cellX;
		while(firstPixelX > 0) {
            firstPixelX -= mouseBrush.pixelWidth;
            firstCellX -= mouseBrush.cellWidth;
        }
		int firstPixelY = pixelY;
		int firstCellY = cellY;
		while(firstPixelY > 0) {
            firstPixelY -= mouseBrush.pixelHeight;
            firstCellY -= mouseBrush.cellHeight;
        }
		int lastPixelX = pixelX;
		while(lastPixelX < image.pixelWidth) {
            lastPixelX += mouseBrush.pixelWidth;
        }
		int lastPixelY = pixelY;
		while(lastPixelY < image.pixelHeight) {
            lastPixelY += mouseBrush.pixelHeight;
        }
		// Draw the image on to the buffered image foreach coordinates
		Graphics2D bGrBack = image.background.createGraphics();
		Graphics2D bGrFore = image.foreground.createGraphics();
		Graphics2D bGrBeha = image.behavior.createGraphics();
        // no need of the lastCellX and lastCellY because the end condition is the same
		for(int dpx = firstPixelX, dcx = firstCellX; dpx < lastPixelX; dpx += mouseBrush.pixelWidth, dcx++){
			for(int dpy = firstPixelY, dcy=firstCellY; dpy < lastPixelY; dpy += mouseBrush.pixelHeight, dcy++) {
                bGrBack.drawImage(mouseBrush.background, dpx, dpy, null);
                if(mouseBrush.foreground != null)
                    bGrFore.drawImage(mouseBrush.foreground, dpx, dpy, null);
                bGrBeha.drawImage(mouseBrush.behavior, dcx, dcy, null);
			}
		}
        bGrBack.dispose();
        bGrFore.dispose();
        bGrBeha.dispose();
		repaint();
	}

	@Override
	public void paintComponent(Graphics g){

		// Clean the panel
		super.paintComponent(g);

		if(image != null) {


			int index = getSelectedIndex();
			if(index == 0) {
				g.drawImage(image.background, DELTA, DELTA, null);
				// Draw the brush in overlay (pixelBrushX; pixelBrushY) are the bottom left corner, need to correct to the top left
				if(mouseBrush != null && pixelBrushX >= DELTA && pixelBrushY >= DELTA)
					g.drawImage(mouseBrush.background, pixelBrushX, pixelBrushY - mouseBrush.pixelHeight + 1, null);

			}else if(index == 1){
				g.drawImage(image.foreground, DELTA, DELTA, null);
				// Draw the brush in overlay (pixelBrushX; pixelBrushY) are the bottom left corner, need to correct to the top left
				if(mouseBrush != null && pixelBrushX >= 0 && pixelBrushY >= 0)
					g.drawImage(mouseBrush.foreground, pixelBrushX, pixelBrushY - mouseBrush.pixelHeight + 1, null);

			}else if(index == 2) {
				g.drawImage(resize(image.behavior, LayerImage.CELL_RESOLUTION), DELTA, DELTA, null);
				// Draw the brush in overlay (pixelBrushX; pixelBrushY) are the bottom left corner, need to correct to the top left
				if(mouseBrush != null && pixelBrushX >= 0 && pixelBrushY >= 0)
					g.drawImage(resize(mouseBrush.behavior, LayerImage.CELL_RESOLUTION), pixelBrushX, pixelBrushY - mouseBrush.pixelHeight + 1, null);
			}

			//draw the grid (all the row borders and then all the col borders)
			for (int i = 0; i <= image.pixelHeight / LayerImage.CELL_RESOLUTION; i++) {
				g.drawLine(DELTA, DELTA + i * LayerImage.CELL_RESOLUTION, DELTA+image.pixelWidth, DELTA+i * LayerImage.CELL_RESOLUTION);
			}
			for (int j = 0; j <= image.pixelWidth / LayerImage.CELL_RESOLUTION; j++) {
				g.drawLine(DELTA + j * LayerImage.CELL_RESOLUTION, DELTA, DELTA + j * LayerImage.CELL_RESOLUTION, DELTA+image.pixelHeight);
			}
		}
	}


	/// GridPanel implements Menu.Listener

	@Override
	public void onOpenLVE(LayerImage lve) {

		if(!lve.isValid(maxCellsX, maxCellsY)){
			JOptionPane.showMessageDialog(this, "The image must be decomposed into cell of "+
                    LayerImage.CELL_RESOLUTION+ "x"+ LayerImage.CELL_RESOLUTION+" pixels (max 50x50 cells)");
		}
		else{
			saveFile = null;
			this.image = lve;
			repaint();
		}
	}

	@Override
	public void onNewLVE(LayerImage lve) {

        if(!lve.isValid(maxCellsX, maxCellsY)){
			JOptionPane.showMessageDialog(this, "Please enter value : max (" +
					maxCellsX +"x"+ maxCellsY +")");
		}
		else {
			saveFile = null;
			this.image = lve;
			repaint();
		}
	}

	@Override
	public boolean onSaveLVE() {
		return LayerImage.saveToFile(image, saveFile);
	}

	@Override
	public boolean onSaveAsLVE(File file) {
		saveFile = file;
		return onSaveLVE();
	}

	/// GridPanel implements BrushDropList.Listener

	@Override
	public void onBrushSelected(LayerImage brush) {
		this.mouseBrush = brush;
	}

	@Override
	public void onBackgroundToggeled(boolean isChecked) {
		drawBackground = isChecked;
	}


	@Override
	public void onForegroundToggeled(boolean isChecked) {
		drawForeground = isChecked;
	}

	@Override
	public void onBehaviorToggeled(boolean isChecked) {
		drawBehavior = isChecked;
	}


	/// GridPanel implements OptionsPanel.Listener

	@Override
	public void onDragXChange(int newValue) {
		deltaDragX = newValue;
	}

	@Override
	public void onDragYChange(int newValue) {
		deltaDragY = newValue;
	}

	@Override
	public void onToolChange(OptionsPanel.Tool newTool) {
		tool = newTool;
		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(newTool.getScaledImage(
		        LayerImage.CELL_RESOLUTION, LayerImage.CELL_RESOLUTION), new Point(0,0),"tool cursor"));
	}


	private static BufferedImage resize(BufferedImage img, int factor) {

		int newW = img.getWidth()*factor;
		int newH = img.getHeight() * factor;

		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}
}
