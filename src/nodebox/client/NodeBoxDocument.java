package nodebox.client;

import nodebox.node.*;
import nodebox.node.event.NodeEvent;
import nodebox.node.event.NodeDirtyEvent;
import nodebox.node.event.NodeEventListener;
import nodebox.node.event.NodeUpdatedEvent;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A NodeBoxDocument manages a NodeLibrary.
 */
public class NodeBoxDocument extends JFrame implements WindowListener, NodeEventListener {

    private final static String WINDOW_MODIFIED = "windowModified";

    public static String lastFilePath;
    public static String lastExportPath;

    private NodeLibrary nodeLibrary;
    private Macro activeMacro;
    private Node activeNode;
    private File documentFile;
    private boolean documentChanged;
    private static Logger logger = Logger.getLogger("nodebox.client.NodeBoxDocument");
    private EventListenerList documentFocusListeners = new EventListenerList();
    private UndoManager undoManager = new UndoManager();
    private AddressBar addressBar;
    private ArrayList<PortEditor> portEditors = new ArrayList<PortEditor>();
    private boolean loaded = false;

    public static NodeBoxDocument getCurrentDocument() {
        return Application.getInstance().getCurrentDocument();
    }

    private class DocumentObservable extends Observable {
        public void setChanged() {
            super.setChanged();
        }
    }

    public NodeBoxDocument(NodeLibrary library) {
        setNodeLibrary(library);
        JPanel rootPanel = new JPanel(new BorderLayout());
        ViewerPane viewPane = new ViewerPane(this);
        EditorPane editorPane = new EditorPane(this);
        ParameterPane parameterPane = new ParameterPane(this);
        NetworkPane networkPane = new NetworkPane(this);
        PaneSplitter viewEditorSplit = new PaneSplitter(NSplitter.Orientation.VERTICAL, viewPane, editorPane);
        PaneSplitter parameterNetworkSplit = new PaneSplitter(NSplitter.Orientation.VERTICAL, parameterPane, networkPane);
        PaneSplitter topSplit = new PaneSplitter(NSplitter.Orientation.HORIZONTAL, viewEditorSplit, parameterNetworkSplit);
        addressBar = new AddressBar(this);
        rootPanel.add(addressBar, BorderLayout.NORTH);
        rootPanel.add(topSplit, BorderLayout.CENTER);
        setContentPane(rootPanel);
        setLocationByPlatform(true);
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        updateTitle();
        setJMenuBar(new NodeBoxMenuBar(this));
        loaded = true;
        requestActiveMacroUpdate();
    }

    public NodeBoxDocument(File file) throws RuntimeException {
        this(NodeLibrary.fromFile(file, Application.getInstance().getManager()));
        lastFilePath = file.getParentFile().getAbsolutePath();
        setDocumentFile(file);
    }

    //// Document events ////

    public void addDocumentFocusListener(DocumentFocusListener l) {
        documentFocusListeners.add(DocumentFocusListener.class, l);
    }

    public void removeDocumentFocusListener(DocumentFocusListener l) {
        documentFocusListeners.remove(DocumentFocusListener.class, l);
    }

    public void fireActiveNetworkChanged() {
        for (EventListener l : documentFocusListeners.getListeners(DocumentFocusListener.class)) {
            ((DocumentFocusListener) l).currentMacroChanged(activeMacro);
        }
    }

    public void fireActiveNodeChanged() {
        for (EventListener l : documentFocusListeners.getListeners(DocumentFocusListener.class)) {
            ((DocumentFocusListener) l).focusedNodeChanged(activeNode);
        }
    }

    public NodeLibrary getNodeLibrary() {
        return nodeLibrary;
    }

    private void setNodeLibrary(NodeLibrary nodeLibrary) {
        this.nodeLibrary = nodeLibrary;
        setActiveMacro(nodeLibrary.getRootMacro());
        nodeLibrary.addListener(this);
    }

    public Macro getActiveMacro() {
        return activeMacro;
    }

    public void setActiveMacro(Macro activeMacro) {
        this.activeMacro = activeMacro;
        fireActiveNetworkChanged();
        setActiveNode(null);
        if (activeMacro != null) {
            requestActiveMacroUpdate();
        }
    }

    public Node getActiveNode() {
        return activeNode;
    }

    public void setActiveNode(Node activeNode) {
        this.activeNode = activeNode;
        fireActiveNodeChanged();
    }

    public NodeLibraryManager getManager() {
        return Application.getInstance().getManager();
    }

    //// Parameter editor actions ////

    public void addPortEditor(PortEditor editor) {
        if (portEditors.contains(editor)) return;
        portEditors.add(editor);
    }

    public void removeParameterEditor(PortEditor editor) {
        portEditors.remove(editor);
    }

    //// Document actions ////

    public File getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(File documentFile) {
        this.documentFile = documentFile;
        updateTitle();
    }

    public boolean isChanged() {
        return documentChanged;
    }

    public boolean shouldClose() {
        if (isChanged()) {
            SaveDialog sd = new SaveDialog();
            int retVal = sd.show(this);
            if (retVal == JOptionPane.YES_OPTION) {
                return save();
            } else if (retVal == JOptionPane.NO_OPTION) {
                return true;
            } else if (retVal == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        return true;
    }

    public boolean save() {
        if (documentFile == null) {
            return saveAs();
        } else {
            return saveToFile(documentFile);
        }
    }

    public boolean saveAs() {
        File chosenFile = FileUtils.showSaveDialog(this, lastFilePath, "ndbx", "NodeBox File");
        if (chosenFile != null) {
            if (!chosenFile.getAbsolutePath().endsWith(".ndbx")) {
                chosenFile = new File(chosenFile.getAbsolutePath() + ".ndbx");
            }
            lastFilePath = chosenFile.getParentFile().getAbsolutePath();
            setDocumentFile(chosenFile);
            NodeBoxMenuBar.addRecentFile(documentFile);
            return saveToFile(documentFile);
        }
        return false;
    }

    public void revert() {
        // TODO: Implement revert
        JOptionPane.showMessageDialog(this, "Revert is not implemented yet.", "NodeBox", JOptionPane.ERROR_MESSAGE);
    }

    public boolean saveToFile(File file) {
        try {
            nodeLibrary.store(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while saving the file.", "NodeBox", JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "An error occurred while saving the file.", e);
            return false;
        }
        documentChanged = false;
        updateTitle();
        return true;

    }

    public boolean exportToFile(File file) {
        throw new UnsupportedOperationException("File export is not supported yet.");
//        // Make sure the file ends with ".pdf".
//        String fullPath = null;
//        try {
//            fullPath = file.getCanonicalPath();
//        } catch (IOException e) {
//            throw new RuntimeException("Unable to access file " + file, e);
//        }
//        if (!fullPath.toLowerCase().endsWith(".pdf")) {
//            fullPath = fullPath.concat(".pdf");
//        }
//        file = new File(fullPath);
//
//        // todo: file export only works on grobs.
//        if (activeMacro == null || activeMacro.getRenderedChild() == null) return false;
//        Object outputValue = activeMacro.getRenderedChild().getOutputValue();
//        if (outputValue instanceof Grob) {
//            Grob g = (Grob) outputValue;
//            PDFRenderer.render(g, file);
//            return true;
//        } else {
//            throw new RuntimeException("This type of output cannot be exported " + outputValue);
//        }
    }


    public void markChanged() {
        if (!documentChanged && loaded) {
            documentChanged = true;
            updateTitle();
            getRootPane().putClientProperty(WINDOW_MODIFIED, Boolean.TRUE);
        }
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void undo() {
        // TODO: Implement undo
        JOptionPane.showMessageDialog(this, "Undo is not implemented yet.", "NodeBox", JOptionPane.ERROR_MESSAGE);
    }

    public void redo() {
        // TODO: Implement redo
        JOptionPane.showMessageDialog(this, "Redo is not implemented yet.", "NodeBox", JOptionPane.ERROR_MESSAGE);
    }

    public void cut() {
        NetworkView networkView = currentNetworkView();
        if (networkView == null) {
            beep();
            return;
        }
        networkView.cutSelected();
    }

    public void copy() {
        NetworkView networkView = currentNetworkView();
        if (networkView == null) {
            beep();
            return;
        }
        networkView.copySelected();
    }

    public void paste() {
        NetworkView networkView = currentNetworkView();
        if (networkView == null) {
            beep();
            return;
        }
        networkView.pasteSelected();
    }

    private NetworkView currentNetworkView() {
        // Find current network view.
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null) return null;
        Pane pane = (Pane) SwingUtilities.getAncestorOfClass(Pane.class, focusOwner);
        if (pane == null) return null;
        PaneView paneView = pane.getPaneView();
        if (!(paneView instanceof NetworkView)) return null;
        return (NetworkView) paneView;
    }


    public void deleteSelected() {
        NetworkView networkView = currentNetworkView();
        if (networkView == null) {
            beep();
            return;
        }
        networkView.deleteSelected();
    }

    private void updateTitle() {
        String postfix = "";
        if (!PlatformUtils.onMac()) { // todo: mac only code
            postfix = (documentChanged ? " *" : "");
        } else {
            getRootPane().putClientProperty("Window.documentModified", documentChanged);
        }
        if (documentFile == null) {
            setTitle("Untitled" + postfix);
        } else {
            setTitle(documentFile.getName() + postfix);
            getRootPane().putClientProperty("Window.documentFile", documentFile);
        }
    }

    public boolean export() {
        File chosenFile = FileUtils.showSaveDialog(this, lastExportPath, "pdf", "PDF file");
        if (chosenFile != null) {
            lastExportPath = chosenFile.getParentFile().getAbsolutePath();
            return exportToFile(chosenFile);
        }
        return false;
    }

    public boolean reloadActiveNode() {
        if (activeNode == null) return false;
        Pane p = SwingUtils.getPaneForComponent(getFocusOwner());
        if (p == null || !(p instanceof EditorPane)) return false;
        return ((EditorPane) p).reload();
    }

//    public void createNewLibrary(String libraryName) {
//        // First check if a library with this name already exists.
//        if (getManager().hasLibrary(libraryName)) {
//            JOptionPane.showMessageDialog(this, "A library with the name \"" + libraryName + "\" already exists.");
//            return;
//        }
//        getManager().createPythonLibrary(libraryName);
//    }


    public void close() {
        if (shouldClose()) {
            Application.getInstance().getManager().remove(nodeLibrary);
            Application.getInstance().removeDocument(NodeBoxDocument.this);
            for (PortEditor editor : portEditors) {
                editor.dispose();
            }
            dispose();
            // On Mac the application does not close if the last window is closed.
            if (PlatformUtils.onMac()) return;
            // If there are no more documents, exit the application.
            if (Application.getInstance().getDocumentCount() == 0) {
                System.exit(0);
            }
        }
    }

    private void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    //// Window events ////

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        close();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
        Application.getInstance().setCurrentDocument(this);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    //// Network events ////

    public void receive(NodeEvent event) {
        // Every event, except NodeDirty and NodeUpdated, will mark the document as changed.
        if (!(event instanceof NodeDirtyEvent) && !(event instanceof NodeUpdatedEvent)) {
            markChanged();
        }
        if (event instanceof NodeDirtyEvent && event.getSource() == activeMacro) {
            requestActiveMacroUpdate();
        }
    }

    private void requestActiveMacroUpdate() {
        if (!loaded) return;
        addressBar.setProgressVisible(true);


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // If meanwhile the node has been marked clean, ignore the event.
                // TODO if (!activeMacro.isDirty()) return;
                try {
                    activeMacro.cook(new CookContext());
                } catch (ExecuteException executeException) {
                    Logger.getLogger("NodeBoxDocument").log(Level.WARNING, "Error while processing", executeException);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            addressBar.setProgressVisible(false);
                        }
                    });
                }
            }
        });
    }

}
