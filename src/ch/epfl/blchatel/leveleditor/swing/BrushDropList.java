package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
    private final static String BRUSHES_BACKGROUND_PATH = "Brushes/Backgrounds/";
    private final static String BRUSHES_FOREGROUND_PATH = "Brushes/Foregrounds/";
    private final static String BRUSHES_BEHAVIOR_PATH = "Brushes/Behaviors/";

    public final static boolean BACKGROUND_CHECKBOX_DEFAULT = true;
    public final static boolean FOREGROUND_CHECKBOX_DEFAULT = true;
    public final static boolean BEHAVIOR_CHECKBOX_DEFAULT = true;


    /// Listener interface to react to brush selection
    public interface Listener{
        /**
         * React to a brush selection in the drop list
         * @param brush (LayerImage): the selected Image
         */
        void onBrushSelected(LayerImage brush);

        void onBackgroundToggeled(boolean isChecked);
        void onForegroundToggeled(boolean isChecked);
        void onBehaviorToggeled(boolean isChecked);
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

    // Active checkbox
    private final JCheckBox backgroundCheckBox;
    private final JCheckBox foregroundCheckBox;
    private final JCheckBox behaviorCheckBox;
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
        brushesMap = new HashMap<>();
        Node brushesTree = new Node("Brushes", "Brushes");


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

                    String name = backgroundFile.getName().toLowerCase();
                    brushesMap.put(name, new LayerImage(backgroundImage, foregroundImage, behaviorImage));
                    brushesTree.addChild(name, name);
                }
            }
        }

        Dimension d1 = new Dimension(d.width, (int)(d.height*0.05));
        Dimension d2 = new Dimension(d.width-10, (int)(d.height*0.85));

        JPanel checkboxPanel = new JPanel();
        backgroundCheckBox = new JCheckBox("Background", BACKGROUND_CHECKBOX_DEFAULT);
        backgroundCheckBox.addItemListener(e -> {
            for(Listener l : listeners)
                l.onBackgroundToggeled(backgroundCheckBox.isSelected());
        });
        checkboxPanel.add(backgroundCheckBox);
        foregroundCheckBox = new JCheckBox("Foreground", FOREGROUND_CHECKBOX_DEFAULT);
        foregroundCheckBox.addItemListener(e -> {
            for(Listener l : listeners)
                l.onForegroundToggeled(foregroundCheckBox.isSelected());
        });
        checkboxPanel.add(foregroundCheckBox);
        behaviorCheckBox = new JCheckBox("Behavior", BEHAVIOR_CHECKBOX_DEFAULT);
        behaviorCheckBox.addItemListener(e -> {
            for(Listener l : listeners)
                l.onBehaviorToggeled(behaviorCheckBox.isSelected());
        });
        checkboxPanel.add(behaviorCheckBox);
        checkboxPanel.setSize(d1);
        checkboxPanel.setMinimumSize(d1);
        checkboxPanel.setMaximumSize(d1);
        checkboxPanel.setPreferredSize(d1);
        add(checkboxPanel);


        // Create the JTable with a custom ListRenderer
        DefaultMutableTreeNode root = createNodes(brushesTree, null);

        JTree tree = new JTree(root);
        tree.setCellRenderer(new BrushesTreeRenderer());
        tree.addTreeSelectionListener(e -> {
            JTree tree1 = (JTree) e.getSource();
            if(tree1 != null && tree1.getSelectionPath() != null) {
                String selectionValue = ((Tree) ((DefaultMutableTreeNode) tree1.getSelectionPath().getLastPathComponent()).getUserObject()).path;
                for (Listener l : listeners) {
                    l.onBrushSelected(brushesMap.get(selectionValue));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tree);
        scroll.setSize(d2);
        scroll.setMinimumSize(d2);
        scroll.setMaximumSize(d2);
        scroll.setPreferredSize(d2);
        add(scroll);
    }

    private static DefaultMutableTreeNode createNodes(Node brushTree, DefaultMutableTreeNode parent) {

        DefaultMutableTreeNode that = new DefaultMutableTreeNode(brushTree);
        if(parent != null)
            parent.add(that);

        for(Map.Entry<String, Tree> child : brushTree.children.entrySet()){

            Tree t = child.getValue();

            if(t instanceof Node){
                createNodes((Node)t, that);
            }else{
                DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(t);
                that.add(leaf);
            }
        }

        return that;
    }


    private class BrushesTreeRenderer extends DefaultTreeCellRenderer {

        private final Font font = new Font("helvitica", Font.PLAIN, 12);
        private final Border selectedBorder = BorderFactory.createLineBorder(Color.BLACK);

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            //Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode n = (DefaultMutableTreeNode)value;
            Tree t = (Tree)n.getUserObject();
            JLabel label = new JLabel();
            label.setFont(font);
            label.setText(t.name);
            label.setForeground(Color.GRAY);
            if(leaf){
                LayerImage image = brushesMap.get(t.path);
                label.setIcon(image.vsIcon);
                label.setText(label.getText() + " ("+image.cellWidth+"x"+image.cellHeight+")");
                label.setText(label.getText());
            }
            if(selected) {
                label.setBorder(selectedBorder);
                label.setForeground(Color.BLACK);
            }
            return label;
        }
    }


    private abstract class Tree {
        final String name;
        final String path;
        Tree(String name, String path){
            this.name = name;
            this.path = path;
        }
    }

    private class Node extends Tree{

        final Map<String, Tree> children;

        Node(String name, String path){
            super(name, path);
            children = new TreeMap<>();
        }

        void addChild(String fullPath, String pathLeft) {

            // Get the first '.' index
            int i1 = pathLeft.indexOf('.');
            // if no '.' found -> it is a brush
            if (i1 == -1)
                return;
            if(i1 == pathLeft.lastIndexOf('.') && pathLeft.toLowerCase().endsWith(".png")){
                children.put(pathLeft, new Leaf(pathLeft, fullPath));
                return;
            }

            // Otherwise we have a prefix (Node) and child path
            String prefix = pathLeft.substring(0, i1);
            String suffix = pathLeft.substring(i1+1);

            Tree parent = children.get(prefix);

            if(parent == null){
                parent = new Node(prefix, path);
                children.put(prefix, parent);
            }

            ((Node)parent).addChild(fullPath, suffix);
        }
    }

    private class Leaf extends Tree{

        Leaf(String name, String path){
            super(name, path);
        }
    }

}