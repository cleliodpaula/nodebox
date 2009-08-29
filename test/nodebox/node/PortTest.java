package nodebox.node;

import nodebox.graphics.*;

public class PortTest extends NodeTestCase {

    /**
     * Ports follow the same naming rules as parameters and nodes.
     */
    public void testNaming() {
        Node n = numberNode.newInstance(testLibrary, "number1");
        assertInvalidName(n, "1234", "names cannot start with a digit.");
        assertInvalidName(n, "node", "names can not be one of the reserved words.");
        assertInvalidName(n, "context", "names can not be one of the reserved words.");
        assertValidName(n, "radius");
        assertInvalidName(n, "radius", "port  names must be unique for the node");
        n.addParameter("myparam", Parameter.Type.INT);
        assertInvalidName(n, "myparam", "port names must be unique across parameters and ports");
    }

    /**
     * This test checks if the correct error message is thrown for a Connection with no output ports.
     */
    public void testGetOutput() {
        NodeLibraryManager manager = new NodeLibraryManager();
        Node mergeNode = manager.getNode("vector/merge");

        //assertNull(port.getOutput());
    }

    /**
     * Checks if isAssignableFrom works when validating.
     */
    public void testDowncasting() {
        Node n = numberNode.newInstance(testLibrary, "number1");
        Port ptGrob = n.addPort("grob", Grob.class);
        Port ptCanvas = n.addPort("canvas", Canvas.class);
        Port ptImage = n.addPort("image", Image.class);
        Port ptPath = n.addPort("path", Path.class);
        Port ptText = n.addPort("text", Text.class);

        Canvas canvas = new Canvas();
        Image image = new Image();
        Path path = new Path();
        Text text = new Text("", 0, 0);

        assertValidValue(ptGrob, canvas);
        assertValidValue(ptGrob, image);
        assertValidValue(ptGrob, path);
        assertValidValue(ptGrob, text);

        assertValidValue(ptCanvas, canvas);
        assertInvalidValue(ptCanvas, image);
        assertInvalidValue(ptCanvas, path);
        assertInvalidValue(ptCanvas, text);

        assertInvalidValue(ptImage, canvas);
        assertValidValue(ptImage, image);
        assertInvalidValue(ptImage, path);
        assertInvalidValue(ptImage, text);

        assertInvalidValue(ptPath, canvas);
        assertInvalidValue(ptPath, image);
        assertValidValue(ptPath, path);
        assertInvalidValue(ptPath, text);

        assertInvalidValue(ptText, canvas);
        assertInvalidValue(ptText, image);
        assertInvalidValue(ptText, path);
        assertValidValue(ptText, text);

        //TODO: These tests would only work if input and outputs are not checked, which they are.
//        assertTrue(ptGrob.canConnectTo(ptCanvas));
//        assertTrue(ptGrob.canConnectTo(ptGroup));
//        assertTrue(ptGrob.canConnectTo(ptImage));
//        assertTrue(ptGrob.canConnectTo(ptPath));
//        assertTrue(ptGrob.canConnectTo(ptText));
//        assertTrue(ptGrob.canConnectTo(ptGrob));
//
//        assertTrue(ptCanvas.canConnectTo(ptCanvas));
//        assertFalse(ptCanvas.canConnectTo(ptGroup));
//
//        assertTrue(ptGroup.canConnectTo(ptGroup));
//        assertTrue(ptGroup.canConnectTo(ptCanvas));
//
//        assertTrue(ptPath.canConnectTo(ptPath));
//        assertFalse(ptPath.canConnectTo(ptGrob));
//        assertFalse(ptPath.canConnectTo(ptImage));
    }

    public void testCardinality() {
        Node test = Node.ROOT_NODE.newInstance(testLibrary, "test");
        test.addPort("single", Object.class);
        test.addPort("multiple", Object.class, Port.Cardinality.MULTIPLE);
        assertEquals(Port.Cardinality.SINGLE, test.getPort("single").getCardinality());
        assertEquals(Port.Cardinality.MULTIPLE, test.getPort("multiple").getCardinality());
        // Now clone this instance and check cardinality.
        Node cloned = test.newInstance(testLibrary, "cloned");
        assertEquals(Port.Cardinality.SINGLE, cloned.getPort("single").getCardinality());
        assertEquals(Port.Cardinality.MULTIPLE, cloned.getPort("multiple").getCardinality());
    }

    public void testAccessors() {
        Node rect1 = rectNode.newInstance(testLibrary, "rect1");
        Node trans1 = translateNode.newInstance(testLibrary, "trans1");
        Port pPolygon = trans1.getPort("polygon");
        assertNull(pPolygon.getConnection());
        pPolygon.connect(rect1);
        Connection c = pPolygon.getConnection();
        assertNotNull(c);
        assertEquals(rect1, c.getOutputNode());
        assertEquals(trans1, c.getInputNode());
        assertEquals(pPolygon, c.getInput());
    }


    //// Custom assertions ////

    private void assertInvalidName(Node n, String newName, String reason) {
        try {
            n.addPort(newName, Integer.class);
            fail("the following condition was not met: " + reason);
        } catch (InvalidNameException ignored) {
        }
    }

    private void assertValidName(Node n, String newName) {
        try {
            n.addPort(newName, Integer.class);
        } catch (InvalidNameException e) {
            fail("The name \"" + newName + "\" should have been accepted.");
        }
    }

    private void assertValidValue(Port p, Object value) {
        try {
            p.validate(value);
        } catch (IllegalArgumentException e) {
            fail("The value '" + value + "' should have been accepted: " + e);
        }
    }

    private void assertInvalidValue(Port p, Object value) {
        try {
            p.validate(value);
            fail("The value '" + value + "' should not have been accepted.");
        } catch (IllegalArgumentException ignored) {
        }
    }

}
