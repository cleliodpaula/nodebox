/*
 * This file is part of NodeBox.
 *
 * Copyright (C) 2008 Frederik De Bleser (frederik@pandora.be)
 *
 * NodeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NodeBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NodeBox. If not, see <http://www.gnu.org/licenses/>.
 */
package nodebox.node;

import nodebox.node.event.ConnectionAddedEvent;
import nodebox.node.event.ConnectionRemovedEvent;

public class ConnectTest extends NodeTestCase {

    private class ConnectListener implements NodeEventListener {
        public int connectCounter = 0;
        public int disconnectCounter = 0;

        public void receive(NodeEvent event) {
            if (event instanceof ConnectionAddedEvent) {
                connectCounter++;
            } else if (event instanceof ConnectionRemovedEvent) {
                disconnectCounter++;
            }
        }
    }

    public void testConnect() {
        Node number1 = rootMacro.createChild(numberNode, "number1");
        Node multiply1 = rootMacro.createChild(multiplyNode, "multiply1");
        Node upper1 = rootMacro.createChild(convertToUppercaseNode, "upper1");
        Port number1Result = number1.getPort("result");
        Port multiply1v1 = multiply1.getPort("v1");
        Port multiply1v2 = multiply1.getPort("v2");

        assertFalse(rootMacro.isConnected(multiply1.getPort("v1")));

        assertTrue(multiply1v1.canConnectTo(number1Result));
        assertTrue(multiply1v2.canConnectTo(number1Result));
        assertFalse(upper1.getPort("value").canConnectTo(number1Result));

        Connection conn = rootMacro.connect(multiply1v1, number1Result);
        assertTrue(rootMacro.isConnected(multiply1v1));
        assertTrue(rootMacro.isConnectedTo(multiply1v1, number1Result));
        assertTrue(rootMacro.isConnected(multiply1));
        assertTrue(rootMacro.isConnected(number1));
        assertEquals(multiply1v1, conn.getInput());
        assertEquals(number1Result, conn.getOutput());
        assertEquals(multiply1, conn.getInputNode());
        assertEquals(number1, conn.getOutputNode());

        assertConnectionError(upper1, "value", number1, "result", "Value is of the wrong type and should not be connectable to IntVariable's output.");
    }

    /**
     * Test if output values can be cast to their superclass.
     */
    public void testConnectCasting() {
//        Node upstream, downstream;
//        // Both are of the same type. Should be able to connect.
//        upstream = Node.ROOT_NODE.newInstance(testLibrary, "upstream", HashMap.class);
//        downstream = Node.ROOT_NODE.newInstance(testLibrary, "downstream", HashMap.class);
//        downstream.addPort("value");
//        downstream.getPort("value").connect(upstream);
//        // Reset the library
//        testLibrary = new NodeLibrary("test");
//        // Upstream is a more specific type, which is allowed.
//        upstream = Node.ROOT_NODE.newInstance(testLibrary, "upstream", LinkedHashMap.class);
//        downstream = Node.ROOT_NODE.newInstance(testLibrary, "downstream", HashMap.class);
//        downstream.addPort("value");
//        downstream.getPort("value").connect(upstream);
//        // Reset the library
//        testLibrary = new NodeLibrary("test");
//        // Now downstream is more specific, which is NOT allowed.
//        upstream = Node.ROOT_NODE.newInstance(testLibrary, "upstream", HashMap.class);
//        downstream = Node.ROOT_NODE.newInstance(testLibrary, "downstream", LinkedHashMap.class);
//        downstream.addPort("value");
//        assertConnectionError(downstream, "value", upstream, "Downstream is a more specific type.");
//        // Reset the library
//        testLibrary = new NodeLibrary("test");
//        // Downstream is an interface which upstream implements.
//        upstream = Node.ROOT_NODE.newInstance(testLibrary, "upstream", LinkedHashMap.class);
//        downstream = Node.ROOT_NODE.newInstance(testLibrary, "downstream", Map.class);
//        downstream.addPort("value");
//        downstream.getPort("value").connect(upstream);
    }

    public void testCycles() {
//        Node number1 = numberNode.newInstance(testLibrary, "number1");
//        assertConnectionError(ng, "number", ng, "Nodes cannot connect to themselves.");
        // TODO: more complex cyclic checks (A->B->A)
    }

    public void testValuePropagation() {
        Node number1 = rootMacro.createChild(numberNode, "number1");
        Node number2 = rootMacro.createChild(numberNode, "number2");
        Node m = rootMacro.createChild(multiplyNode, "multiply1");
        rootMacro.connect(m.getPort("v1"), number1.getPort("result"));
        rootMacro.connect(m.getPort("v2"), number2.getPort("result"));
        assertNotNull(m.getPort("result"));
        assertNull(m.getValue("result"));
        
        // TODO implement rest of test
//        assertNull(m.getOutputValue());
//        number1.setValue("value", 3);
//        number2.setValue("value", 2);
//        assertTrue(m.isDirty());
//        assertNull(m.getOutputValue());
//        // Updating the IndexValue node has no effect on the multiplier node.
//        number1.update();
//        assertTrue(m.isDirty());
//        assertNull(m.getOutputValue());
//        m.update();
//        assertFalse(m.isDirty());
//        assertEquals(6, m.getOutputValue());
//        // Test if value stops propagating after disconnection.
//        m.getPort("v1").disconnect();
//        assertFalse(m.getPort("v1").isConnected());
//        assertTrue(m.isDirty());
//        // The value is still the old value because the node has not been updated yet.
//        assertEquals(6, m.getOutputValue());
//        assertFalse(number1.isDirty());
//        number1.setValue("value", 3);
//        assertProcessingError(m, NullPointerException.class);
//        assertNull(m.getOutputValue());
    }

    public void testDisconnect() {
//        Node number1 = numberNode.newInstance(testLibrary, "number1");
//        Node number2 = numberNode.newInstance(testLibrary, "number2");
//        Node m = multiplyNode.newInstance(testLibrary, "multiply1");
//        number1.setValue("value", 5);
//        number2.setValue("value", 2);
//        m.getPort("v1").connect(number1);
//        m.getPort("v2").connect(number2);
//        assertTrue(m.getPort("v1").isConnected());
//        assertTrue(number1.isOutputConnected());
//        m.update();
//        assertEquals(5, m.getPort("v1").getValue());
//        assertEquals(10, m.getOutputValue());
//        assertNotNull(m.getPort("v1").getConnection());
//
//        // Disconnecting a port makes the dependent nodes dirty, but not the upstream nodes.
//        // "Dirt flows downstream"
//        m.getPort("v1").disconnect();
//        assertTrue(m.isDirty());
//        assertFalse(number1.isDirty());
//        assertFalse(m.getPort("v1").isConnected());
//        assertFalse(number1.isOutputConnected());
//        assertNull(m.getPort("v1").getConnection());
//        // The value of the input port is set to null after disconnection.
//        // Since our simple multiply node doesn't handle null, it throws
//        // a NullPointerException, which gets wrapped in a ExecuteException.
//        assertProcessingError(m, NullPointerException.class);
//        assertNull(m.getOutputValue());
    }

    /**
     * Disconnect a specific output node.
     */
    public void testDisconnectOutputNode() {
//        // Setup a simple network where number1 <- addConstant1.
//        Node root = testLibrary.getRootMacro();
//        Node number1 = root.create(numberNode);
//        Node addConstant1 = root.create(addConstantNode);
//        Port pValue = addConstant1.getPort("value");
//        pValue.connect(number1);
//        // Remove the specific connection and check if everything was removed.
//        addConstant1.disconnect(pValue, number1);
//        assertFalse(number1.isConnected());
//        assertFalse(addConstant1.isConnected());
//        assertNull(pValue.getConnection());
    }

    /**
     * Check if all connections are destroyed when removing a node.
     */
    public void testRemoveNode() {
//        // Create a basic connection.
//        Node number1 = numberNode.newInstance(testLibrary, "number1");
//        Node negate1 = negateNode.newInstance(testLibrary, "negate1");
//        negate1.getPort("value").connect(number1);
//        // Remove the node. This should also remove all connections.
//        testLibrary.getRootMacro().remove(number1);
//        assertFalse(number1.isConnected());
//        assertFalse(negate1.isConnected());
    }

    public void testOnlyOneConnect() {
//        Node number1 = numberNode.newInstance(testLibrary, "number1");
//        Node number2 = numberNode.newInstance(testLibrary, "number2");
//        Node negate1 = negateNode.newInstance(testLibrary, "negate1");
//        negate1.getPort("value").connect(number1);
//        assertTrue(number1.isConnected());
//        assertFalse(number2.isConnected());
//        assertTrue(negate1.isConnected());
//        // Now change the connection to number2.
//        negate1.getPort("value").connect(number2);
//        assertFalse(number1.isConnected());
//        assertTrue(number2.isConnected());
//        assertTrue(negate1.isConnected());
    }

    public void testRemove() {
//        // First add a node
//        Node net = testNetworkNode.newInstance(testLibrary, "net1");
//        Node number1 = net.create(numberNode);
//        // We add a second node to check for the processing error.
//        // If there are no nodes in the parent, the root will just return null.
//        net.create(numberNode);
//        number1.setValue("value", 42);
//        number1.setRendered();
//        net.update();
//        assertEquals(42, net.getOutputValue());
//        // Now remove and update again
//        net.remove(number1);
//        assertNull(net.getRenderedChild());
//        // This should cause the network to complain that there is no node to render.
//        assertProcessingError(net, ExecuteException.class);
//        // The output value should revert to null.
//        assertEquals(null, net.getOutputValue());
    }

    public void testMultiConnect() {
//        Node net = testNetworkNode.newInstance(testLibrary, "net1");
//        Node number1 = net.create(numberNode);
//        number1.setValue("value", 5);
//        Node number2 = net.create(numberNode);
//        number2.setValue("value", 10);
//        Node multiAdd1 = net.create(multiAddNode);
//        multiAdd1.getPort("values").connect(number1);
//        multiAdd1.getPort("values").connect(number2);
//        assertTrue(number1.isConnected());
//        assertTrue(number2.isConnected());
//        multiAdd1.setRendered();
//        // Test default behaviour
//        net.update();
//        assertFalse(net.isDirty());
//        assertFalse(multiAdd1.isDirty());
//        assertEquals(15, net.getOutputValue());
//        // Change number1 and see if the change propagates.
//        number1.setValue("value", 3);
//        assertTrue(net.isDirty());
//        assertTrue(multiAdd1.isDirty());
//        net.update();
//        assertEquals(13, net.getOutputValue());
//        // change number2 and see if the change propagates.
//        number2.setValue("value", 4);
//        assertTrue(net.isDirty());
//        assertTrue(multiAdd1.isDirty());
//        net.update();
//        assertEquals(7, net.getOutputValue());
    }

    public void testConnectionEvents() {
//        ConnectListener l = new ConnectListener();
//        // Setup a basic network with number1 <- addConstant1
//        Node root = testLibrary.getRootMacro();
//        testLibrary.addListener(l);
//        Node number1 = root.create(numberNode);
//        Node addConstant1 = root.create(addConstantNode);
//        // No connect/disconnect events have been fired.
//        assertEquals(0, l.connectCounter);
//        assertEquals(0, l.disconnectCounter);
//        // Creating a connection fires the event.
//        addConstant1.getPort("value").connect(number1);
//        assertEquals(1, l.connectCounter);
//        assertEquals(0, l.disconnectCounter);
//        // Create a second number and connect it to the add constant.
//        // This should fire a disconnect event from number1, and a connect
//        // event to number2.
//        Node number2 = root.create(numberNode);
//        addConstant1.getPort("value").connect(number2);
//        assertEquals(2, l.connectCounter);
//        assertEquals(1, l.disconnectCounter);
//        // Disconnect the constant node. This should remove all (1) connections,
//        // and cause one disconnect event.
//        addConstant1.disconnect();
//        assertEquals(2, l.connectCounter);
//        assertEquals(2, l.disconnectCounter);
//        testLibrary.removeListener(l);
    }

    public void testReorder() {
//        Node root = testLibrary.getRootMacro();
//        Node number1 = root.create(numberNode);
//        Node number2 = root.create(numberNode);
//        Node number3 = root.create(numberNode);
//        Node multiAdd = root.create(multiAddNode);
//        Port pValues = multiAdd.getPort("values");
//        pValues.connect(number1);
//        pValues.connect(number2);
//        pValues.connect(number3);
//        assertOrder(pValues, number1, number2, number3);
//
//        // Move number 2 up.
//        pValues.getConnection().reorderOutput(number2.getOutputPort(), -1);
//        assertOrder(pValues, number2, number1, number3);
//        assertDirtyAndUpdate(multiAdd);
//
//        // Move number 3 down. It was already last, so shouldn't change anything.
//        pValues.getConnection().reorderOutput(number3.getOutputPort(), 1);
//        assertOrder(pValues, number2, number1, number3);
//        assertFalse(multiAdd.isDirty());
//
//        // Move number 3 up by a large amount. It should just move it to the first position.
//        pValues.getConnection().reorderOutput(number3.getOutputPort(), -5000);
//        assertOrder(pValues, number3, number2, number1);
//        assertDirtyAndUpdate(multiAdd);
//
//        // Move number 2 by a large amount, moving it to the end.
//        pValues.getConnection().reorderOutput(number2.getOutputPort(), 5000);
//        assertOrder(pValues, number3, number1, number2);
//        assertDirtyAndUpdate(multiAdd);
//
//        // Move number 1 by zero places. This should not move anything and not mark the node as dirty.
//        pValues.getConnection().reorderOutput(number1.getOutputPort(), 0);
//        assertOrder(pValues, number3, number1, number2);
//        assertFalse(multiAdd.isDirty());
    }

    public void assertConnectionError(Node inputNode, String inputPort, Node outputNode, String outputPort, String message) {
        try {
            Macro macro = inputNode.getParent();
            Port input = inputNode.getPort(inputPort);
            Port output = outputNode.getPort(outputPort);
            macro.connect(input, output);
            fail(message);
        } catch (Exception ignored) {
        }
    }

    /**
     * Assert that the given node is dirty, then update and assert it is clean.
     *
     * @param node the node.
     */
    private void assertDirtyAndUpdate(Node node) {
//        assertTrue(node.isDirty());
//        node.update();
//        assertFalse(node.isDirty());
    }

}
