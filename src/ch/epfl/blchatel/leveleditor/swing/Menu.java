package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;
import ch.epfl.blchatel.leveleditor.LevelEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Extension of JMenuBar with a specific JMenu
 * - File
 *     - New
 *     - Open
 *     - Save As
 *     - Save
 */
public class Menu extends JMenuBar {

    /// Listener interface to react to menu selection
	public interface Listener{
        /**
         * React to an opened Image
         * @param file (File): the opened file
         * @param lve (LayerImage): the opened LayeredImage
         */
		void onOpenLVE(File file, LayerImage lve);
        /**
         * React to the creation of a new LayeredImage
         * @param lve (LayerImage): the opened LayeredImage
         */
		void onNewLVE(LayerImage lve);
        /**
         * React to a save request of the current LayeredImage
         * @return (boolean): true if the save succeed, false otherwise
         */
		boolean onSaveLVE();
        /**
         * React to a save request of the current image to the given file
         * @param file (File): the file to save into
         * @return (boolean): true if the save succeed, false otherwise
         */
		boolean onSaveAsLVE(File file);
	}
	/// List of listeners
	private final List<Listener> listeners;
    /**
     * Add a new Listener to the listeners list
     * @param l (Listener): the new Listener
     */
	public void addListener(Listener l){
		listeners.add(l);
	}

    /** Default Menu Bar constructor */
	Menu() {

        // Define this BrushDropList info
        // - Init listeners list
		listeners= new LinkedList<>();

		// File Menu
        final JMenu fileMenu = new JMenu("File");

		// - New Item
		JMenuItem newFileItem = new JMenuItem("New");
		newFileItem.addActionListener(e -> {

            final SpinnerNumberModel intModelW = new SpinnerNumberModel(20, 1, Integer.MAX_VALUE, 1);
            final SpinnerNumberModel intModelH = new SpinnerNumberModel(20, 1, Integer.MAX_VALUE, 1);
            final JSpinner wField = new JSpinner(intModelW);
            final JSpinner hField = new JSpinner(intModelH);

            final JPanel options = new JPanel();
            options.add(new JLabel("w:"));
            options.add(wField);
            options.add(Box.createHorizontalStrut(15)); // a spacer
            options.add(new JLabel("h:"));
            options.add(hField);

            int result = JOptionPane.showConfirmDialog(null, options,"New Image", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                for(Listener l : listeners){
                    l.onNewLVE(LayerImage.createImage((Integer) wField.getValue(), (Integer) hField.getValue()));
                }
            }
        });
		fileMenu.add(newFileItem);

		// - Open Item
		JMenuItem openFileItem = new JMenuItem("Open");
		openFileItem.addActionListener(e -> {
            File workingDirectory = new File(System.getProperty("user.dir"));
            final JFileChooser chooser = new JFileChooser(workingDirectory);
            final FileNameExtensionFilter filter = new FileNameExtensionFilter("Level Editor Files", "lve");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showOpenDialog(fileMenu);
            if(returnVal == JFileChooser.APPROVE_OPTION) {

                File file = chooser.getSelectedFile();

                for(Listener l : listeners){
                    l.onOpenLVE(file, LayerImage.getFromFile(file));
                }
            }
        });
		fileMenu.add(openFileItem);

		// - Save Item
		JMenuItem saveFileItem = new JMenuItem("Save");
		saveFileItem.addActionListener(e -> {
            for(Listener l : listeners){
                if(!l.onSaveLVE()){
                    File workingDirectory = new File(System.getProperty("user.dir"));
                    final JFileChooser chooser = new JFileChooser(workingDirectory);
                    final FileNameExtensionFilter filter = new FileNameExtensionFilter("Level Editor Images", "lve");
                    chooser.setFileFilter(filter);
                    chooser.setAcceptAllFileFilterUsed(false);
                    int returnVal = chooser.showSaveDialog(fileMenu);
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        if(!l.onSaveAsLVE(chooser.getSelectedFile()))
                            JOptionPane.showMessageDialog(null,"the save fails", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
		fileMenu.add(saveFileItem);

		// - Save As Item
		JMenuItem saveAsFileItem = new JMenuItem("Save As");
		saveAsFileItem.addActionListener(e -> {
            File workingDirectory = new File(System.getProperty("user.dir"));
            final JFileChooser chooser = new JFileChooser(workingDirectory);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Level Editor Images", "lve");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showSaveDialog(fileMenu);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                for(Listener l : listeners){
                    if(!l.onSaveAsLVE(chooser.getSelectedFile()))
                        JOptionPane.showMessageDialog(null,"the save fails", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
		fileMenu.add(saveAsFileItem);

		add(fileMenu);


		final JMenu helpMenu = new JMenu("Help");

		JMenuItem aboutHelpItem = new JMenuItem("About");
        aboutHelpItem.addActionListener(e -> JOptionPane.showMessageDialog(null, LevelEditor.appName + " " + LevelEditor.versionId, "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutHelpItem);

        JMenuItem colorMapHelpItem = new JMenuItem("Color Map");
        colorMapHelpItem.addActionListener(e -> ColorMap.showMap());
        helpMenu.add(colorMapHelpItem);

        add(helpMenu);
	}
}
