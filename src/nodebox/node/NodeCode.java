package nodebox.node;

/**
 * A code object that can execute using a certain node and context.
 */
public interface NodeCode {

    public Object cook(Node node, CookContext context);

    public String getSource();

    public String getType();

}
