package nodebox.client;

import nodebox.node.Macro;
import nodebox.node.Node;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

public class NetworkPane extends Pane implements PropertyChangeListener {

    private PaneHeader paneHeader;
    private NetworkView networkView;
    private Macro macro;
    private NButton newNodeButton;


    public NetworkPane(NodeBoxDocument document) {
        super(document);
        setLayout(new BorderLayout(0, 0));
        paneHeader = new PaneHeader(this);
        newNodeButton = new NButton("New Node", "res/network-new-node.png");
        newNodeButton.setToolTipText("New Node (TAB)");
        newNodeButton.setActionMethod(this, "createNewNode");
        paneHeader.add(newNodeButton);
        networkView = new NetworkView(this, null);
        networkView.addPropertyChangeListener(this);
        add(paneHeader, BorderLayout.NORTH);
        add(networkView, BorderLayout.CENTER);
        setMacro(document.getActiveMacro());
    }

    public Pane clone() {
        return new NetworkPane(getDocument());
    }

    public PaneHeader getPaneHeader() {
        return paneHeader;
    }

    public String getPaneName() {
        return "Network";
    }

    public PaneView getPaneView() {
        return networkView;
    }

    public Node getMacro() {
        return macro;
    }

    public void setMacro(Macro macro) {
        this.macro = macro;
        networkView.setMacro(macro);
        networkView.select(getDocument().getActiveNode());
    }

    @Override
    public void currentMacroChanged(Macro macro) {
        setMacro(macro);
    }

    @Override
    public void focusedNodeChanged(Node activeNode) {
        // If the active node is already selected, don't change the selection.
        // This avoids nasty surprises when multiple nodes (including the active one)
        // are selected.
        if (networkView.isSelected(activeNode)) return;
        networkView.singleSelect(activeNode);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals(NetworkView.SELECT_PROPERTY)) return;
        Set<NodeView> selection = (Set<NodeView>) evt.getNewValue();
        // If there is no selection, set the active node to null.
        if (selection == null || selection.isEmpty()) {
            getDocument().setActiveNode(null);
        } else {
            // If the active node is in the new selection leave the active node as is.
            NodeView nv = networkView.getNodeView(getDocument().getActiveNode());
            if (selection.contains(nv)) return;
            // If there are multiple elements selected, the first one will be the active node.
            NodeView firstElement = selection.iterator().next();
            getDocument().setActiveNode(firstElement.getNode());
        }
    }

    public void createNewNode() {
        networkView.showNodeSelectionDialog();
    }
}
