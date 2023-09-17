import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Node extends Circle {
    private boolean isSelected;
    private final Label label;
    private final String name;
    private final double x;
    private final double y;

    public Node(String name, double x, double y) {
        super(x, y, 10);
        this.setId(name);
        label = new Label(name);
        label.setLayoutX(x + 4);
        label.setLayoutY(y + 4);
        this.setFill(Color.BLUE);
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public Label getLabel() { return label; }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isSelected(){
        return isSelected;
    }

    public void setSelected(boolean b){
        if(b)
            setFill(Color.RED);
        else
            setFill(Color.BLUE);

        isSelected = b;
    }

    @Override
    public String toString() {
        return String.format("%s;%s;%s", name, x, y);
    }
}