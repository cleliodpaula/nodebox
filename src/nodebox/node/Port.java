package nodebox.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static nodebox.base.Preconditions.checkState;

/**
 * A connectable object on a node. Ports provide input and output capabilities between nodes.
 * <p/>
 * Ports have a certain data class. Only ports with the same class of data can be connected together.
 */
public class Port {

    public enum Direction {
        IN, OUT
    }

    /**
     * The cardinality of a port defines if it can store a single value or multiple values.
     * <p/>
     * When the cardinality is single, use getValue() and setValue() to access the data.
     * For ports with multiple cardinality, use getValues(), addValue() and clearValues().
     */
    public enum Cardinality {
        SINGLE, MULTIPLE
    }

    private Node node;
    private String name;
    private Cardinality cardinality;
    private Direction direction;
    // Depending on the cardinality, either value or values is used.
    private Object value;
    private List<Object> values;

    public Port(Node node, String name) {
        this(node, name, Cardinality.SINGLE, Direction.IN);
    }

    public Port(Node node, String name, Cardinality cardinality) {
        this(node, name, cardinality, Direction.IN);
    }

    public Port(Node node, String name, Direction direction) {
        this(node, name, Cardinality.SINGLE, direction);
    }

    public Port(Node node, String name, Cardinality cardinality, Direction direction) {
        if (direction == Direction.OUT && cardinality != Cardinality.SINGLE)
            throw new IllegalArgumentException("Output ports can't have multiple cardinality.");
        this.node = node;
        validateName(name);
        this.name = name;
        this.cardinality = cardinality;
        this.direction = direction;
    }

    public Node getNode() {
        return node;
    }

    public Node getParentNode() {
        return node.getParent();
    }

    public boolean hasParentNode() {
        return getParentNode() != null;
    }

    public String getName() {
        return name;
    }

    public void validateName(String name) {
        if (name == null || name.trim().length() == 0)
            throw new InvalidNameException(this, name, "Name cannot be null or empty.");
        if (node.hasPort(name))
            throw new InvalidNameException(this, name, "There is already a port named " + name + ".");
        if (node.hasParameter(name))
            throw new InvalidNameException(this, name, "There is already a parameter named " + name + ".");
        // Use the same validation as for nodes.
        Node.validateName(name);
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public Direction getDirection() {
        return direction;
    }

    public void validate(Object value) throws IllegalArgumentException {
        node.validate(value);
    }

    /**
     * Gets the value of this port.
     * <p/>
     * This value will be null if the port is disconnected
     * or an error occured during processing.
     *
     * @return the value for this port.
     */
    public Object getValue() {
        if (cardinality != Cardinality.SINGLE)
            throw new AssertionError("You can only call getValue when cardinality is SINGLE.");
        return value;
    }

    /**
     * Gets a list of values for this port.
     * <p/>
     * This method is guaranteed to return a list, although it can be empty.
     *
     * @return the values for this port.
     */
    public List<Object> getValues() {
        if (cardinality != Cardinality.MULTIPLE)
            throw new AssertionError("You can only call getValues when cardinality is MULTIPLE.");
        if (values == null) return new ArrayList<Object>();
        return values;
    }

    /**
     * Set the value for this port.
     * <p/>
     * This method should not be called directly. Instead, values are set automatically when nodes are updated.
     * <p/>
     * This method can only be used when cardinality is set to single.
     * <p/>
     * Setting this value will not trigger any notifications or dirty flags.
     *
     * @param value the value for this port.
     * @throws IllegalArgumentException if the value is not of the required data class.
     */
    public void setValue(Object value) throws IllegalArgumentException {
        if (cardinality != Cardinality.SINGLE)
            throw new AssertionError("You can only call setValue when cardinality is SINGLE.");
        validate(value);
        this.value = value;
    }

    /**
     * Add a value to this port.
     * <p/>
     * This method should not be called directly. Instead, values are added automatically when nodes are updated.
     * <p/>
     * This method can only be used when cardinality is set to multiple.
     * <p/>
     * Adding a value will not trigger any notifications or dirty flags.
     *
     * @param value the value to add for this port.
     * @throws IllegalArgumentException if the value is not of the required data class.
     */
    public void addValue(Object value) throws IllegalArgumentException {
        if (cardinality != Cardinality.MULTIPLE)
            throw new AssertionError("You can only call addValue when cardinality is MULTIPLE.");
        validate(value);
        if (values == null)
            values = new ArrayList<Object>();
        values.add(value);
    }

    /**
     * Reset the value(s) of the port.
     * This method is called automatically when nodes are updated or disconnected.
     */
    public void reset() {
        value = null;
        values = null;
    }

    //// Connections ////

    public boolean isInputPort() {
        return direction == Direction.IN;
    }

    public boolean isOutputPort() {
        return direction == Direction.OUT;
    }

    /**
     * Checks if this port is connected to another port.
     *
     * @return true if this port is connected.
     */
    public boolean isConnected() {
        checkState(hasParentNode(), "Port %s has no parent node.", this);
        return getParentNode().isChildConnected(this);
    }

    /**
     * Checks if this port is connected to the given port.
     *
     * @param port the other port to check.
     * @return true if a connection exists between this port and the given port.
     */
    public boolean isConnectedTo(Port port) {
        if (!isConnected()) return false;
        if (!hasParentNode()) return false;
        return getParentNode().isChildConnectedTo(this, port);
    }

    /**
     * Checks if this port is connected to the output port of the given node.
     *
     * @param outputNode the node whose output port will be checked.
     * @return true if a connection exists between this port and the given node.
     */
    public boolean isConnectedTo(Node outputNode) {
        return isConnectedTo(outputNode.getOutputPort());
    }

    /**
     * Get the connections on this port.
     *
     * @return a list of connections, possibly empty.
     */
    public List<Connection> getConnections() {
        Node parent = getParentNode();
        if (parent == null) return Collections.emptyList();
        List<Connection> connections = new ArrayList<Connection>();
        for (Connection c : parent.getConnections()) {
            if (this == c.getInput() || this == c.getOutput()) {
                connections.add(c);
            }
        }
        return connections;
    }

    /**
     * Checks if this port can connect to the output port of the given node.
     * <p/>
     * This method does not check for cyclic dependencies.
     *
     * @param outputNode the output (upstream) node.
     * @return true if the node can be connected.
     */
    public boolean canConnectTo(Node outputNode) {
        if (outputNode == null) return false;
        if (getNode() == outputNode) return false;
        return canConnectTo(outputNode.getOutputPort());
    }

    /**
     * Check if this port can connect to the given output port.
     * <p/>
     * This method does not check for cyclic dependencies.
     *
     * @param outputPort the upstream output port.
     * @return true if this port can connect to the given port.
     */
    public boolean canConnectTo(Port outputPort) {
        if (outputPort == null) return false;
        if (outputPort == this) return false;
        if (outputPort.getDirection() != Direction.OUT) return false;
        // An input port can only be connected to an output port.
        // Since we just checked the direction of the output port,
        // we need to make sure if this port is an input.
        if (direction != Direction.IN) return false;
        // Check if the data classes match.
        // They can either be equal, or the output type can be downcasted to the input type.
        Class inputClass = node.getDataClass();
        Class outputClass = outputPort.node.getDataClass();
        return inputClass.isAssignableFrom(outputClass);
    }

    /**
     * Connect this (input) port to the given output node.
     *
     * @param outputNode the output node
     * @return the Connection objects
     * @throws IllegalArgumentException if the connection could not be made (because of cyclic dependency)
     * @see Node#connectChildren(Port, Port)
     */
    public Connection connect(Node outputNode) throws IllegalArgumentException {
        if (outputNode == null)
            throw new IllegalArgumentException("Output node cannot be null.");
        if (getParentNode() == null)
            throw new IllegalArgumentException("This port has no parent node.");
        return getParentNode().connectChildren(this, outputNode.getOutputPort());
    }

    /**
     * Disconnects this port.
     */
    public void disconnect() {
        getParentNode().disconnectChildPort(this);
    }

    /**
     * Create a clone of this port that can be set on the given node.
     * This new port is not added to the given node.
     * <p/>
     * The value of this port is not cloned, since values cannot be cloned.
     *
     * @param n the node to clone the port onto.
     * @return a new Port object
     */
    public Port clone(Node n) {
        return new Port(n, getName(), getCardinality(), getDirection());
    }

    /**
     * Copy this port onto the new node.
     * <p/>
     * Also clones any upstream connections.
     *
     * @param newNode the new node
     * @return a new, cloned port.
     */
    public Port copy(Node newNode) {
        return new Port(newNode, getName(), getCardinality(), getDirection());
    }

    /**
     * Clone the connections of this port onto the new port.
     * <p/>
     * If the old connection points to nodes within the copy map they will be replaced with connections to the new node.
     *
     * @param newPort the new port to clone the connections onto.
     * @param copyMap a mapping between the old nodes and the new nodes.
     */
    public void cloneConnection(Port newPort, Map<Node, Node> copyMap) {
        assert newPort.name.equals(getName());
        assert newPort.cardinality == cardinality;
        assert newPort.node != node;
        checkState(newPort.getConnections().isEmpty(), "The new port should not be connected to anything.");
        for (Connection c : getConnections()) {
            Node n = c.getOutputNode();
            Node outputNode;
            if (copyMap.containsKey(n)) {
                outputNode = copyMap.get(n);
            } else {
                outputNode = n;
            }
            // If the new node is under a different parent connections cannot be retained, and no
            // connections are created.
            if (outputNode.getParent() == newPort.getNode().getParent()) {
                newPort.connect(outputNode);
            }
        }
    }

    @Override
    public String toString() {
        return node.getName() + "." + getName();
    }
}
