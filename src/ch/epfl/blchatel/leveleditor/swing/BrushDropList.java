package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;

import java.awt.*;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * The BrushDropList represents a scroll panel with a list of all available brushes
 * - the brush are binary resources (.png files inside the output directory / jar file etc...)
 * - the list is represented as |brushIcon|brushName
 * - the icons are 64x64 pixels
 */
public class BrushDropList extends JPanel {

    /// The local binary resource brush path
    private final static String BRUSHES_BACKGROUND_PATH = "Brushes/Background/";
    private final static String BRUSHES_FOREGROUND_PATH = "Brushes/Foreground/";
    private final static String BRUSHES_BEHAVIOR_PATH = "Brushes/Behavior/";

    /// Listener interface to react to brush selection
    public interface Listener{
        /**
         * React to a brush selection in the drop list
         * @param brush (LayerImage): the selected Image
         */
        void onBrushSelected(LayerImage brush);
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

    /// Map of the brushes String -> LayerImage
    private final Map<String, LayerImage> brushesMap;

    /**
     * Default BrushDropList Constructor
     * @param d (Dimension) : dimension of this
     */
    BrushDropList(Dimension d) {
        // Define this BrushDropList info
        // Define fixed dimension :
        // - if the parent does't use layout manager
        setSize(d);
        // - if the parent use a layout manager
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
        setBorder(BorderFactory.createTitledBorder("Brushes"));

        // - Init listeners list
        listeners = new LinkedList<>();

        // Read the local brush path
        ClassLoader cl = BrushDropList.class.getClassLoader();
        File backgroundFolder = new File(Objects.requireNonNull(cl.getResource(BRUSHES_BACKGROUND_PATH)).getFile());
        File[] backgroundFiles = backgroundFolder.listFiles();
        File foregroundFolder = new File(Objects.requireNonNull(cl.getResource(BRUSHES_FOREGROUND_PATH)).getFile());
        File[] foregroundFiles = foregroundFolder.listFiles();
        File behaviorFolder = new File(Objects.requireNonNull(cl.getResource(BRUSHES_BEHAVIOR_PATH)).getFile());
        File[] behaviorFiles = behaviorFolder.listFiles();

        // Init the map and the list
        List<String> nameList = new LinkedList<>();
        brushesMap = new HashMap<>();

        // Add all png files from the folder
        if(backgroundFiles == null || foregroundFiles == null || behaviorFiles == null){
            System.out.println("At least one the following folders is not found : \n" +
                    BRUSHES_BACKGROUND_PATH + ",\n" + BRUSHES_FOREGROUND_PATH + ",\n" + BRUSHES_BEHAVIOR_PATH);
        }else if(backgroundFiles.length != behaviorFiles.length){
            System.out.println("Please check brush, some are missing : \n" +
            "#Background: "+ backgroundFiles.length +", #Foreground: "+ foregroundFiles.length + ", #behavior: "+behaviorFiles.length);
        }
        else{
            Comparator<File> fileComparator = Comparator.comparing(File::getName);
            Arrays.sort(backgroundFiles, fileComparator);
            Arrays.sort(behaviorFiles, fileComparator);
            Arrays.sort(foregroundFiles, fileComparator);

            int foregroundIndex = 0;
            for(int i = 0; i < backgroundFiles.length; i++){

                File backgroundFile = backgroundFiles[i];
                File foregroundFile = foregroundIndex >= foregroundFiles.length ? null : foregroundFiles[foregroundIndex];
                File behaviorFile = behaviorFiles[i];

                if(!backgroundFile.getName().equals(behaviorFile.getName())){
                    System.out.println("Brush name must be the sames int Background and Behavior folders");
                    break;
                }else if((backgroundFile.isFile() && backgroundFile.getName().toLowerCase().endsWith(".png"))){

                    Image backgroundImage = new ImageIcon(backgroundFile.getAbsolutePath()).getImage();
                    Image behaviorImage = new ImageIcon(behaviorFile.getAbsolutePath()).getImage();
                    Image foregroundImage = null;
                    if(foregroundFile != null && backgroundFile.getName().equals(foregroundFile.getName())){
                       foregroundIndex++;
                       foregroundImage = new ImageIcon(foregroundFile.getAbsolutePath()).getImage();
                    }

                    String name = backgroundFile.getName().replaceFirst("[.][^.]+$", "");
                    brushesMap.put(name, new LayerImage(backgroundImage, foregroundImage, behaviorImage));
                    nameList.add(name);
                }
            }
        }
        // Sort the list
        nameList.sort(String::compareTo);

        // Create the JList with a custom ListRenderer
        JList<Object> list = new JList<>(nameList.toArray());
        list.setCellRenderer(new BrushesListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(UIManager.getColor ( "Panel.background" ));

        list.addListSelectionListener(listSelectionEvent -> {
            boolean adjust = listSelectionEvent.getValueIsAdjusting();
            if (!adjust) {
                JList list1 = (JList) listSelectionEvent.getSource();
                Object selectionValue = list1.getSelectedValue();
                for(Listener l : listeners){
                    l.onBrushSelected(brushesMap.get(selectionValue));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setSize(d);
        scroll.setMinimumSize(d);
        scroll.setMaximumSize(d);
        scroll.setPreferredSize(d);
        add(scroll);
    }

    private class BrushesListRenderer extends DefaultListCellRenderer {

        private final Font font = new Font("helvitica", Font.BOLD, 18);

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setFont(font);
            LayerImage image = brushesMap.get(value);
            label.setIcon(image.vsIcon);
            label.setText(label.getText() + " ("+image.cellWidth+"x"+image.cellHeight+")");
            return label;
        }
    }
}