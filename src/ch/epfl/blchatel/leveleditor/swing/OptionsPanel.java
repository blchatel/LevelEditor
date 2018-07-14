package ch.epfl.blchatel.leveleditor.swing;

import ch.epfl.blchatel.leveleditor.LayerImage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * The Option Panel is a ... TODO
 */
class OptionsPanel extends JPanel {

    /// Default options values
    public final static int DEFAULT_DRAG_X = 16;
    public final static int DEFAULT_DRAG_Y = 16;
    public final static Tool DEFAULT_TOOL = Tool.BRUSH;


    /// Listener interface to react to option change
    public interface Listener{
        /**
         * React when the drag X value change
         * @param newValue (int): the new drag x value
         */
        void onDragXChange(int newValue);

        /**
         * React when the drag Y value change
         * @param newValue (int): the new drag y value
         */
        void onDragYChange(int newValue);

        /**
         * React when the tool change
         * @param newTool (Tool): the new tool
         */
        void onToolChange(Tool newTool);

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

    public enum Tool{

        BRUSH("icons/paint_32.png"),
        FILL("icons/fill_32.png");

        private final ImageIcon icon;

        Tool(String iconPath){
            File file = null;
            try {
                ClassLoader cl = OptionsPanel.class.getClassLoader();
                file = new File(Objects.requireNonNull(cl.getResource(iconPath)).getFile());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            assert file != null;
            icon = new ImageIcon(file.getAbsolutePath());
        }
        /**@return (ImageIcon): The tool's icon */
        public ImageIcon getIcon() {
            return icon;
        }
        /**@return (Image): The tool's image */
        public Image getImage(){
            return icon.getImage();
        }

        /**
         * Getter for a scaled tool's Image
         * @param w (int): width of the scaled output image
         * @param h (int): Height of the scaled output image
         * @return (Image): The tool's scaled image
         */
        public Image getScaledImage(int w, int h){
            return icon.getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT);
        }
    }

	/**
	 * Default OptionPanel Constructor
	 * @param d (Dimension) : dimension of this
	 */
	OptionsPanel(Dimension d) {

		// Define this ToolsPanel info
        // Define fixed dimension :
        // - if the parent does't use layout manager
        setSize(d);
        // - if the parent use a layout manager
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);

        // - Init listeners list
        listeners = new LinkedList<>();

        setBorder(BorderFactory.createTitledBorder("Options"));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        Dimension underDimension = new Dimension(d.width/3*2, d.height/2);
        add(buildToolPanel(underDimension));
        add(buildInputFieldsPanel(underDimension));
	}




    private JPanel buildToolPanel(Dimension d) {

	    JPanel toolPanel = new JPanel();
        toolPanel.setPreferredSize(d);
        toolPanel.setMaximumSize(d);
        toolPanel.setMinimumSize(d);
        toolPanel.setLayout(new GridLayout(4, 6));

        final JButton brushButton = new JButton();
        brushButton.setIcon(Tool.BRUSH.getIcon());
        brushButton.setBackground(Color.gray);
        final JButton fillButton = new JButton();
        fillButton.setIcon(Tool.FILL.getIcon());
        fillButton.setBackground(Color.lightGray);

        brushButton.addActionListener(e -> {
            brushButton.setBackground(Color.gray);
            fillButton.setBackground(Color.lightGray);
            for(Listener l : listeners){
                l.onToolChange(Tool.BRUSH);
            }
        });

        fillButton.addActionListener(e -> {
            brushButton.setBackground(Color.lightGray);
            fillButton.setBackground(Color.gray);
            for(Listener l : listeners){
                l.onToolChange(Tool.FILL);
            }
        });

        toolPanel.add(brushButton);
        toolPanel.add(fillButton);
        for(int i = 0; i < 22; i++){
            toolPanel.add(new JButton());
        }

        return toolPanel;
    }

    private JPanel buildInputFieldsPanel(Dimension d) {

        final JLabel dragXLabel = new JLabel("Drag X: ");
        final JLabel drayYLabel = new JLabel("Drag Y: ");

        final SpinnerNumberModel intModelX = new SpinnerNumberModel(DEFAULT_DRAG_X, LayerImage.CELL_RESOLUTION, Integer.MAX_VALUE, LayerImage.CELL_RESOLUTION);
        final JSpinner dragXField = new JSpinner(intModelX);
        dragXField.addChangeListener(e -> {

            int newValue = (Integer)dragXField.getValue();
            int rest = newValue % LayerImage.CELL_RESOLUTION;
            if(rest != 0){
                newValue -= rest;
                dragXField.setValue(newValue);
            }
            for(Listener l : listeners){
                l.onDragXChange(newValue);
            }
        });

        final SpinnerNumberModel intModelY = new SpinnerNumberModel(DEFAULT_DRAG_Y, LayerImage.CELL_RESOLUTION, Integer.MAX_VALUE, LayerImage.CELL_RESOLUTION);
        final JSpinner dragYField = new JSpinner(intModelY);
        dragYField.addChangeListener(e -> {
            int newValue = (Integer)dragYField.getValue();
            int rest = newValue % LayerImage.CELL_RESOLUTION;
            if(rest != 0){
                newValue -= rest;
                dragYField.setValue(newValue);
            }
            for(Listener l : listeners){
                l.onDragYChange(newValue);
            }
        });

        JPanel inputFieldsPanel = new JPanel();
        inputFieldsPanel.setPreferredSize(d);
        inputFieldsPanel.setMaximumSize(d);
        inputFieldsPanel.setMinimumSize(d);

        GroupLayout groupLayout = new GroupLayout(inputFieldsPanel);
        GroupLayout.Group h = groupLayout.createSequentialGroup();
        GroupLayout.Group v = groupLayout.createSequentialGroup();

        GroupLayout.Group[] hs = new GroupLayout.Group[2];
        GroupLayout.Group[] vs = new GroupLayout.Group[2];
        hs[0] = groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        h.addGroup(hs[0]);
        for (int i = 1; i < hs.length; i++) {
            hs[i] = groupLayout.createParallelGroup();
            h.addGroup(hs[i]);
        }
        for (int i = 0; i < vs.length; i++) {
            vs[i] = groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            v.addGroup(vs[i]);
        }

        hs[0].addComponent(dragXLabel).addComponent(drayYLabel);
        hs[1].addComponent(dragXField).addComponent(dragYField);

        vs[0].addComponent(dragXLabel).addComponent(dragXField);
        vs[1].addComponent(drayYLabel).addComponent(dragYField);

        groupLayout.setHorizontalGroup(h);
        groupLayout.setVerticalGroup(v);

        inputFieldsPanel.setLayout(groupLayout);

        return inputFieldsPanel;
    }
}