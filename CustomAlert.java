import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import java.util.Optional;
import java.util.List;

abstract class CustomAlert {

    static class General extends Alert {
        General(AlertType type, String title, String message) {
            super(type, message);
            setTitle(title);
        }
    }

    static class GeneralNoHeader extends Alert {
        GeneralNoHeader(AlertType type, String title, String message) {
            super(type, message);
            setHeaderText(null);
            setTitle(title);
        }

        GeneralNoHeader(AlertType type, String title) {
            super(type, title);
        }
    }

    static class NewNode extends Alert {
        private final TextField name = new TextField();

        NewNode() {
            super(AlertType.CONFIRMATION);
            GridPane grid = new GridPane();
            TextField textFieldName = new TextField();
            grid.addRow(0, new Label("Name of place: "), name);
            getDialogPane().setContent(grid);
            setHeaderText(null);
            setTitle("Name");
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);
        }

        String getName(){
            return name.getText().trim();
        }
    }

    static class FindPath extends Alert {

        FindPath(Node from, Node to, List<Edge<Node>> path) {
            super(Alert.AlertType.INFORMATION);
            setHeaderText("The Path from " + from.getName() + " to " + to.getName() + ":");

            FlowPane pane = new FlowPane();
            getDialogPane().setContent(pane);

            StringBuilder message = new StringBuilder();
            int totalTime = 0;
            for(Edge<Node> e : path) {
                message.append(" to ").append(e.getDestination().getName()).append(" by ").append(e).append("\n");
                totalTime += e.getWeight();
            }
            message.append("Total ").append(totalTime);
            TextArea textArea = new TextArea(message.toString());
            pane.getChildren().add(textArea);
            textArea.setEditable(false);
            showAndWait();
        }

    }

    static class DiscardUnsavedChanges extends Alert {
        DiscardUnsavedChanges() {
            super(Alert.AlertType.CONFIRMATION, "Unsaved changes, continue anyways?");
            setTitle("Warning!");
            setHeaderText(null);
        }

        boolean getAnswer() {
            Optional<ButtonType> answer = showAndWait();
            return answer.isPresent() && answer.get() == ButtonType.OK;
        }

    }

    static class Connection extends Alert {

        private final GridPane grid;

        Connection(GridPane grid, String from, String to) {
            super(AlertType.CONFIRMATION);
            this.grid = grid;
            setTitle("Connection");
            setHeaderText("Connection from " + from + " to " + to);
            getDialogPane().setContent(grid);
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);
        }

        protected GridPane getGrid() {
            return grid;
        }
        static class New extends Connection {
            private final TextField name = new TextField();

            private final TextField weight = new TextField();

            New(String from, String to) {
                super(new GridPane(), from, to);
                GridPane grid = super.getGrid();
                grid.addRow(0, new Label("Name: "), name);
                grid.addRow(1, new Label("Time: "), weight);
            }

            String getName(){
                return name.getText().trim();
            }
            int getWeight() throws NumberFormatException {
                return Integer.parseInt(weight.getText());
            }

        }
        static class Show extends Connection {
            Show(String from, String to, String name, int time) {
                super(new GridPane(), from, to);
                setTitle("Connection");
                GridPane grid = super.getGrid();
                TextField textFieldName = new TextField(name);
                TextField textFieldTime = new TextField(Integer.toString(time));
                grid.addRow(0, new Label("Name: "), textFieldName);
                grid.addRow(1, new Label("Time: "), textFieldTime);
                textFieldName.setDisable(true);
                textFieldTime.setDisable(true);
            }

        }
        static class Change extends Connection {

            private final TextField newWeight;

            Change(String from, String to, String name, int time) {
                super(new GridPane(), from, to);
                GridPane grid = super.getGrid();
                TextField textFieldName = new TextField(name);
                newWeight = new TextField(Integer.toString(time));
                grid.addRow(0, new Label("Name: "), textFieldName);
                grid.addRow(1, new Label("Time: "), newWeight);
                textFieldName.setDisable(true);
            }
            int getNewWeight() throws NumberFormatException {
                return Integer.parseInt(newWeight.getText());
            }

        }

    }

}
