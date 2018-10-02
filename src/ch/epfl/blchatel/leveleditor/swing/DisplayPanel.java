package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;


class DisplayPanel extends JTabbedPane {

	/**
	 * Compute a resized buffered image
	 * @param img (BufferedImage): the input image (may be null)
	 * @param factor (double): factor size. (factor > 0)
	 * @return (BufferedImage): the output resized image (maybe null)
	 */
	private static BufferedImage resize(BufferedImage img, double factor) {

		if (img == null || factor == 1.0)
			return img;

		int newW = (int) (img.getWidth() * factor);
		int newH = (int) (img.getHeight() * factor);

		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dImg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dImg;
	}

	///
	private final GridPanel gridPanel;
	///
	private int index;

	/**
	 * Default DisplayPanel Constructor
	 * @param d (Dimension) : dimension of this
	 */
	DisplayPanel(Dimension d){

		// Define this DisplayPanel info
		// Define fixed dimension :
		// - if the parent does't use layout manager
		setSize(d);
		// - if the parent use a layout manager
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);

		index = 0;

		gridPanel = new GridPanel();
		JScrollPane scrollGridPanel = new JScrollPane(gridPanel);
		scrollGridPanel.getVerticalScrollBar().setUnitIncrement(16);
		// Add the three tabs
		addTab("Background", scrollGridPanel);
		addTab("Foreground", null);
		addTab("Behavior", null);
		addChangeListener(e -> {
			index = getSelectedIndex();
			gridPanel.computeDisplayedImage();
			gridPanel.computeDisplayedBrush();
		});
	}

	private void setGridPanelSize(int w, int h){
		Dimension d = new Dimension(w, h);
		gridPanel.setSize(d);
		// - if the parent use a layout manager
		gridPanel.setMinimumSize(d);
		gridPanel.setMaximumSize(d);
		gridPanel.setPreferredSize(d);
	}

	/** @return (Menu.Listener): the gridPanel as menu listener abstraction */
	public Menu.Listener getMenuListener(){return gridPanel;}
	/** @return (BrushDropList.Listener): the gridPanel as brush drop list listener abstraction */
	public BrushDropList.Listener getBrushDropListListener(){return gridPanel;}
	/** @return (OptionsPanel.Listener): the gridPanel as option panel listener abstraction */
	public OptionsPanel.Listener getOptionsPanelListener(){return gridPanel;}

	/**
	 * The GridPanel is a panel containing a grid
	 * - the cell resolution is 16x16 pixels
	 * - the grid contained cannot overflow the panel dimension
	 * - so MAX_CELL and maxCellsY are defined
	 * The GridPanel is a Menu.Listener and BrushDropList.Listener
	 */
	private class GridPanel extends JPanel implements Menu.Listener, BrushDropList.Listener, OptionsPanel.Listener {

		// padding left (x) and top (y) in px
		private final static int PAD = 40;

		/// Max number of cells we can put in the X and Y direction
		private final static int MAX_CELL = 50;

		/// The file to save into (may be null)
		private File saveFile;
		/// The background image behind the grid (may be null)
		private LayerImage image;
		/// The brush image (may be null)
		private LayerImage mouseBrush;
		/// currently displayed image and brush (could be resize from image and mouseBrush)
		private BufferedImage currentDisplayedImage, currentDisplayedBrush;
		/// boundary coordinates of the displayed image
		private int minX, maxX, minY, maxY;
		/// Flag that indicate if modifications happen for background, foreground and behavior image
		private boolean drawBackground, drawForeground, drawBehavior;
		/// The current brush's position in the grid (cell coordinates)
		private int cellBrushX, cellBrushY;
		/// The current brush's position in the grid (pixel of the bottom left corner of the cell into the display)
		private int pixelBrushX, pixelBrushY;
		///
		private int lastDrawX, lastDrawY;
		///
		private int deltaDragX, deltaDragY;
		/// Current used tool
		private OptionsPanel.Tool tool;
		/// Magnifier factor (limited to values: 0.125, 0.25, 0.5, 1, 2, 4, 8, 16)
		private double magnifier;

		private GridPanel() {

			// Init the brush position (not in the grid)
			cellBrushX = -1;
			cellBrushY = -1;
			pixelBrushX = -1;
			pixelBrushY = -1;

			// Init other integers and option
			deltaDragX = OptionsPanel.DEFAULT_DRAG_X;
			deltaDragY = OptionsPanel.DEFAULT_DRAG_Y;
			tool = OptionsPanel.DEFAULT_TOOL;
			setCursor(Toolkit.getDefaultToolkit().createCustomCursor(tool.getScaledImage(
					16, 16), new Point(0, 0), "tool cursor"));

			// Add the three display tab

			drawBackground = BrushDropList.BACKGROUND_CHECKBOX_DEFAULT;
			drawForeground = BrushDropList.FOREGROUND_CHECKBOX_DEFAULT;
			drawBehavior = BrushDropList.BEHAVIOR_CHECKBOX_DEFAULT;

			magnifier = 1;

			// Add the mouse Listeners
			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					super.mouseDragged(e);

					if (SwingUtilities.isLeftMouseButton(e) && tool == OptionsPanel.Tool.BRUSH
							&& image != null && mouseBrush != null && isIn(e.getX(), e.getY())) {

						updateBrushPosition(e.getX(), e.getY());

						if (Math.abs(pixelBrushX - lastDrawX) >= deltaDragX * magnifier
								|| Math.abs(pixelBrushY - lastDrawY) >= deltaDragY * magnifier) {
							drawBrush(cellBrushX, cellBrushY - mouseBrush.cellHeight + 1);
							lastDrawX = pixelBrushX;
							lastDrawY = pixelBrushY;
						}
					}
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					super.mouseMoved(e);
					if (image != null && mouseBrush != null && isIn(e.getX(), e.getY()))
						updateBrushPosition(e.getX(), e.getY());
				}
			});

			addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {

					if (image != null && isIn(e.getX(), e.getY())) {

						if (SwingUtilities.isLeftMouseButton(e))
						switch (tool) {
							case BRUSH:
								if(mouseBrush == null) break;
								drawBrush(cellBrushX, cellBrushY - mouseBrush.cellHeight + 1);
								lastDrawX = pixelBrushX;
								lastDrawY = pixelBrushY;
								break;
							case FILL:
								if(mouseBrush == null) break;
								fillBrush(cellBrushX, cellBrushY - mouseBrush.cellHeight + 1);
								break;
							case ZOOM:
								magnifier = Math.min(16, magnifier * 2);
								computeDisplayedImage();
								computeDisplayedBrush();
								break;
							default:
								System.out.println("Unknown Tools");
								break;
						}
						else if (SwingUtilities.isRightMouseButton(e))
						switch (tool) {
							case BRUSH:
							case FILL:
								break;
							case ZOOM:
								magnifier = Math.max(0.125, magnifier / 2);
								computeDisplayedImage();
								computeDisplayedBrush();
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
		 * Indicate if the mouse is inside the Displayed image
		 *
		 * @param x (int): mouse x position in pixel
		 * @param y (int): mouse y position in pixel
		 * @return (boolean): true if the mouse is inside, false otherwise
		 */
		private boolean isIn(int x, int y) {
			return minX <= x && x < maxX && minY <= y && y < maxY;
		}

		/**
		 * Update the brush position (the bottom-left corner containing the mouse cursor)
		 *
		 * @param x (int) : X-position of the mouse in this GridPanel in pixel
		 * @param y (int) : Y-position of the mouse in this GridPanel in pixel
		 */
		private void updateBrushPosition(int x, int y) {
			cellBrushX = (x - PAD) / (int) (LayerImage.CELL_RESOLUTION * magnifier);
			cellBrushY = (y - PAD) / (int) (LayerImage.CELL_RESOLUTION * magnifier);

			pixelBrushX = PAD + cellBrushX * (int) (LayerImage.CELL_RESOLUTION * magnifier);
			pixelBrushY = PAD + (cellBrushY + 1) * (int) (LayerImage.CELL_RESOLUTION * magnifier);

			repaint();
		}

		/**
		 * Draw the brush image at position (x, y) which is the top-left corner of the drawn image
		 * and repaint the image
		 * Notice : probably need to adapt the brush position form bottom-left to top-left corner before calling this
		 *
		 * @param cellX (int) : x-coordinate of the cell in the grid (for behavior)
		 * @param cellY (int) : y-coordinate of the cell in the grid (for behavior)
		 */
		private void drawBrush(int cellX, int cellY) {
			// Draw the image on to the buffered image

			int pixelX = cellX * LayerImage.CELL_RESOLUTION;
			int pixelY = cellY * LayerImage.CELL_RESOLUTION;

			if (drawBackground) {
				Graphics2D bGr = image.background.createGraphics();
				bGr.drawImage(mouseBrush.background, pixelX, pixelY, null);
				bGr.dispose();
			}
			if (drawForeground && mouseBrush.foreground != null) {
				Graphics2D bGr = image.foreground.createGraphics();
				bGr.drawImage(mouseBrush.foreground, pixelX, pixelY, null);
				bGr.dispose();
			}

			if (drawBehavior) {
				Graphics2D bGr = image.behavior.createGraphics();
				bGr.drawImage(mouseBrush.behavior, cellX, cellY, null);
				bGr.dispose();
			}
			computeDisplayedImage();
		}

		/**
		 * Fill the image with the brush image from position (x, y) which is the top-left corner of the drawn image
		 * and repaint the image
		 * Notice : probably need to adapt the brush position form bottom-left to top-left corner before calling this
		 * Notice : this function will fill the whole image
		 *
		 * @param cellX (int) : x-coordinate of the cell in the grid (for behavior)
		 * @param cellY (int) : y-coordinate of the cell in the grid (for behavior)
		 */
		private void fillBrush(int cellX, int cellY) {

			// Find the firstX, firstY, lastX and last Y coordinates
			int firstCellX = cellX;
			while (firstCellX > 0) {
				firstCellX -= mouseBrush.cellWidth;
			}
			int firstCellY = cellY;
			while (firstCellY > 0) {
				firstCellY -= mouseBrush.cellHeight;
			}
			int lastCellX = cellX;
			while (lastCellX < image.cellWidth) {
				lastCellX += mouseBrush.cellWidth;
			}
			int lastCellY = cellY;
			while (lastCellY < image.cellHeight) {
				lastCellY += mouseBrush.cellHeight;
			}
			// Draw the image on to the buffered image foreach coordinates
			Graphics2D bGrBack = image.background.createGraphics();
			Graphics2D bGrFore = image.foreground.createGraphics();
			Graphics2D bGrBeha = image.behavior.createGraphics();
			// no need of the lastCellX and lastCellY because the end condition is the same
			for (int dcx = firstCellX; dcx < lastCellX; dcx += mouseBrush.cellWidth) {
				int dpx = dcx * LayerImage.CELL_RESOLUTION;
				for (int dcy = firstCellY; dcy < lastCellY; dcy += mouseBrush.cellHeight) {
					int dpy = dcy * LayerImage.CELL_RESOLUTION;

					bGrBack.drawImage(mouseBrush.background, dpx, dpy, null);

					if (mouseBrush.foreground != null)
						bGrFore.drawImage(mouseBrush.foreground, dpx, dpy, null);
					bGrBeha.drawImage(mouseBrush.behavior, dcx, dcy, null);
				}
			}
			bGrBack.dispose();
			bGrFore.dispose();
			bGrBeha.dispose();
			computeDisplayedImage();
		}


		private void computeDisplayedImage() {

			if (image == null)
				return;

			if (index == 0) {
				currentDisplayedImage = DisplayPanel.resize(image.background, magnifier);
			} else if (index == 1) {
				currentDisplayedImage = DisplayPanel.resize(image.foreground, magnifier);
			} else if (index == 2) {
				currentDisplayedImage = DisplayPanel.resize(image.behavior, magnifier * LayerImage.CELL_RESOLUTION);
			}

			minX = PAD;
			maxX = currentDisplayedImage.getWidth() + PAD;
			minY = PAD;
			maxY = currentDisplayedImage.getHeight() + PAD;

			setGridPanelSize(maxX+PAD, maxY+PAD);

			repaint();
		}

		private void computeDisplayedBrush() {

			if (mouseBrush == null)
				return;

			if (index == 0) {
				currentDisplayedBrush = DisplayPanel.resize(mouseBrush.background, magnifier);
			} else if (index == 1) {
				currentDisplayedBrush = DisplayPanel.resize(mouseBrush.foreground, magnifier);
			} else if (index == 2) {
				currentDisplayedBrush = DisplayPanel.resize(mouseBrush.behavior, magnifier * LayerImage.CELL_RESOLUTION);
			}

			repaint();
		}

		@Override
		public void paintComponent(Graphics g) {

			// Clean the panel
			super.paintComponent(g);

			if (image != null && currentDisplayedImage != null) {

				g.drawImage(currentDisplayedImage, PAD, PAD, null);

				// Draw the brush in overlay (pixelBrushX; pixelBrushY) are the bottom left corner, need to correct to the top left
				if (currentDisplayedBrush != null && mouseBrush != null && pixelBrushX >= PAD && pixelBrushY >= PAD)
					g.drawImage(currentDisplayedBrush, pixelBrushX, pixelBrushY - currentDisplayedBrush.getHeight(), null);

				//draw the grid (all the row borders and then all the col borders)

				int nCols = image.cellWidth;
				int nRows = image.cellHeight;
				int magnifiedWidth = currentDisplayedImage.getWidth();
				int magnifiedHeight = currentDisplayedImage.getHeight();
				double magnifiedResolutionX = magnifiedWidth / nCols;
				double magnifiedResolutionY = magnifiedHeight / nRows;

				for (int i = 0; i <= nRows; i++) {
					g.drawLine(PAD, PAD + (int) (i * magnifiedResolutionY), PAD + magnifiedWidth, PAD + (int) (i * magnifiedResolutionY));
					if(i != 0){
						g.drawString(""+(nRows-i), PAD/2, PAD + (int) (i * magnifiedResolutionY));
						g.drawString(""+(nRows-i), PAD + magnifiedWidth +5, PAD + (int) (i * magnifiedResolutionY));
					}
				}
				for (int j = 0; j <= nCols; j++) {
					g.drawLine(PAD + (int) (j * magnifiedResolutionX), PAD, PAD + (int) (j * magnifiedResolutionX), PAD + magnifiedHeight);
					if(j != nCols){
						g.drawString(""+(j), PAD + (int) (j * magnifiedResolutionX), PAD-5);
						g.drawString(""+(j), PAD + (int) (j * magnifiedResolutionX), PAD + PAD/2 + magnifiedHeight);
					}
				}
			}
		}


		/// GridPanel implements Menu.Listener

		@Override
		public void onOpenLVE(File file, LayerImage lve) {

			if (!lve.isValid(MAX_CELL, MAX_CELL)) {
				JOptionPane.showMessageDialog(this, "The image must be decomposed into cell of " +
						LayerImage.CELL_RESOLUTION + "x" + LayerImage.CELL_RESOLUTION + " pixels (max 50x50 cells)");
			} else {
				saveFile = file;
				this.image = lve;
				computeDisplayedImage();
			}
		}

		@Override
		public void onNewLVE(LayerImage lve) {

			if (!lve.isValid(MAX_CELL, MAX_CELL)) {
				JOptionPane.showMessageDialog(this, "Please enter value : max (" +
						MAX_CELL + "x" + MAX_CELL + ")");
			} else {
				saveFile = null;
				this.image = lve;
				computeDisplayedImage();
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
			computeDisplayedBrush();
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
					16, 16), new Point(0, 0), "tool cursor"));
		}

	}
}