package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;
import ch.epfl.blchatel.leveleditor.io.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.util.*;
import java.util.List;

/**
 * The BrushDropList represents a scroll panel with a list of all available brushes
 * - the brush are binary resources (.png files inside the output directory / jar file etc...)
 * - the list is represented as |brushIcon|brushName
 * - the icons are 64x64 pixels
 */
public class BrushDropList extends JPanel {

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

        // Init the map and the list
        brushesMap = new HashMap<>();
        Node brushesTree = new Node("Brushes", "Brushes");


        BrushesRes[] resources = BrushesRes.values();
        Comparator<BrushesRes> fileComparator = Comparator.comparing(o -> o.name);
        Arrays.sort(resources, fileComparator);

        FileSystem fileSystem = new ResourceFileSystem(DefaultFileSystem.INSTANCE);

        for (BrushesRes br : resources) {
            Image backgroundImage = fileSystem.readImage(br.background);
            Image behaviorImage = fileSystem.readImage(br.behavior);
            Image foregroundImage = fileSystem.readImage(br.foreground);

            String name = br.name.toLowerCase();
            brushesMap.put(name, new LayerImage(backgroundImage, foregroundImage, behaviorImage));
            brushesTree.addChild(name, name);
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