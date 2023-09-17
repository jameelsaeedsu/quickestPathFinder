import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class PathFinder extends Application {
    private final Graph<Node> graph = new ListGraph<>();
    private boolean unsavedChanges;
    private Node nodeA;
    private Node nodeB;

    private Stage stage;
    private Pane center;
    private Button newPlaceButton;
    private final ImageView imageView = new ImageView();
    private final NodeSelectorHandler selector = new NodeSelectorHandler();

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-size: 14");

        // Holder for menuBar and buttonsMenu
        VBox menus = new VBox();
        root.setTop(menus);

        // menuBar area
        MenuBar menuBar = new MenuBar();
        menus.getChildren().add(menuBar);
        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);
        MenuItem newMapItem = new MenuItem("New Map");
        MenuItem openItem = new MenuItem("Open");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveImageItem = new MenuItem("Save Image");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(newMapItem, openItem, saveItem, saveImageItem, exitItem);
        newMapItem.setOnAction(new NewMapHandler());
        openItem.setOnAction(new OpenItemHandler());
        saveItem.setOnAction(new SaveItemHandler());
        saveImageItem.setOnAction(new SaveImageItemHandler());
        exitItem.setOnAction(new ExitItemHandler());

        // buttonsMenu area
        HBox buttonsMenu = new HBox();
        Button findPathButton = new Button("Find Path");
        findPathButton.setOnAction(new FindPathHandler());
        Button showConnectionsButton = new Button("Show Connection");
        showConnectionsButton.setOnAction(new ShowConnectionsHandler());
        newPlaceButton = new Button("New Place");
        newPlaceButton.setOnAction(new NewPlaceHandler());
        Button newConnectionButton = new Button("New Connection");
        newConnectionButton.setOnAction(new NewConnectionHandler());
        Button changeConnectionButton = new Button("Change Connection");
        changeConnectionButton.setOnAction(new ChangeConnectionHandler());
        buttonsMenu.getChildren().addAll(findPathButton, showConnectionsButton, newPlaceButton, newConnectionButton, changeConnectionButton);
        buttonsMenu.setAlignment(Pos.CENTER);
        buttonsMenu.setPadding(new Insets(10));
        buttonsMenu.setSpacing(5);
        menus.getChildren().add(buttonsMenu);

        // center area
        center = new Pane();
        root.setCenter(center);
        center.getChildren().add(imageView);

        Scene scene = new Scene(root);
        stage.sizeToScene();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("PathFinder");
        stage.show();

        // IDs
        fileMenu.setId("menuFile");
        menuBar.setId("menu");
        center.setId("outputArea");
        newMapItem.setId("menuNewMap");
        openItem.setId("menuOpenFile");
        saveItem.setId("menuSaveFile");
        saveImageItem.setId("menuSaveImage");
        exitItem.setId("menuExit");
        findPathButton.setId("btnFindPath");
        showConnectionsButton.setId("btnShowConnection");
        newPlaceButton.setId("btnNewPlace");
        newConnectionButton.setId("btnNewConnection");
        changeConnectionButton.setId("btnChangeConnection");
    }

    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            discardUnsavedChanges();
            if(unsavedChanges)
                return;

            imageView.setImage(new Image("File:europa.gif"));
            stage.sizeToScene();
            unsavedChanges = true;
        }
    }

    class OpenItemHandler implements EventHandler<ActionEvent>{
        @Override public void handle(ActionEvent event){
            discardUnsavedChanges();
            if(unsavedChanges)
                return;

            Map<String, Node> imports = new HashMap<>();
            BufferedReader reader = null;

            try {
                FileReader dataFile = new FileReader("europa.graph");
                reader = new BufferedReader(dataFile);

                // Add image from filename on line 1.
                imageView.setImage(new Image(reader.readLine()));
                stage.sizeToScene();

                // Add all locations on line 2.
                LinkedList<String> queue = new LinkedList<>(Arrays.asList(reader.readLine().trim().split(";")));
                while(!queue.isEmpty()) {
                    String name = queue.remove();
                    double x = Double.parseDouble(queue.remove());
                    double y = Double.parseDouble(queue.remove());

                    if(!imports.containsKey(name)) {
                        Node node = new Node(name, x, y);
                        graph.add(node);
                        center.getChildren().add(node);
                        center.getChildren().add(node.getLabel());
                        node.setOnMouseClicked(selector);
                        imports.put(name, node);
                    }
                }

                // Add connections between nodes from line 3 and onwards.
                String nextLine = reader.readLine().trim();
                do {
                    String[] current = nextLine.split(";");
                    Node from = imports.get(current[0]);
                    Node to = imports.get(current[1]);
                    String name = current[2];
                    int weight = Integer.parseInt(current[3]);
                    try {
                        graph.connect(from, to, name, weight);
                        Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
                        line.setMouseTransparent(true);
                        line.setStrokeWidth(3);
                        line.setFill(Color.BLACK);
                        center.getChildren().add(line);
                    } catch (IllegalStateException e) {
                        // graph.connect() will return IllegalStateException if connection already exist.
                    }
                    nextLine = reader.readLine();
                } while (nextLine != null && !nextLine.equals(""));
                unsavedChanges = false;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SaveItemHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            PrintWriter pw = null;
            try {
                FileWriter fw = new FileWriter("europa.graph");
                pw = new PrintWriter(fw);

                pw.println("file:europa.gif");
                StringBuilder nodeStr = new StringBuilder();
                StringBuilder edgeStr = new StringBuilder();
                for (Node node : graph.getNodes()) {
                    if(nodeStr.length() != 0)
                        nodeStr.append(";");
                    nodeStr.append(node);

                    Collection<Edge<Node>> connections = graph.getEdgesFrom(node);
                    for (Edge<Node> dest : connections) {
                        edgeStr.append("\n");
                        edgeStr.append(node.getName()).append(";");
                        edgeStr.append(dest.getDestination().getName()).append(";");
                        edgeStr.append(dest.getName()).append(";");
                        edgeStr.append(dest.getWeight());
                    }
                }

                pw.print(nodeStr.toString());
                pw.print(edgeStr.toString());
                unsavedChanges = false;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        }
    }

    class SaveImageItemHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            try {
                WritableImage image = center.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image,null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "IO-fel: " + e.getMessage()).showAndWait();
            }
        }
    }

    class ExitItemHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            discardUnsavedChanges();
            if(!unsavedChanges)
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            if(nodeA == null || nodeB == null) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Warning!", "Two places must be selected!").showAndWait();
                return;
            }
            List<Edge<Node>> path = graph.getPath(nodeA, nodeB);
            if(path == null)
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Warning!", "Two places must be selected!").showAndWait();
            else
                new CustomAlert.FindPath(nodeA, nodeB, path);
        }
    }

    class ShowConnectionsHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            if(nodeA == null || nodeB == null) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Warning!", "Two places must be selected!").showAndWait();
                return;
            }
            Edge<Node> edge = graph.getEdgeBetween(nodeA, nodeB);
            if(edge == null)
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "These two places are not connected!").showAndWait();
            else
                new CustomAlert.Connection.Show(nodeA.getName(), nodeB.getName(), edge.getName(), edge.getWeight()).showAndWait();
        }
    }

    class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            imageView.setOnMouseClicked(new NewNodeHandler());
            newPlaceButton.setDisable(true);
            center.setCursor(Cursor.CROSSHAIR);
        }
    }

    class NewConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            if(nodeA == null || nodeB == null) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Warning!", "Two places must be selected!").showAndWait();
                return;
            } else if (graph.getEdgeBetween(nodeA, nodeB) != null) {
                new CustomAlert.General(Alert.AlertType.ERROR, "Error!", "Connection already exists!").showAndWait();
                return;
            }
            try {
                CustomAlert.Connection.New dialog = new CustomAlert.Connection.New(nodeA.getName(), nodeB.getName());
                dialog.showAndWait();
                try {
                    graph.connect(nodeA, nodeB, dialog.getName(), dialog.getWeight());
                    Line line = new Line(nodeA.getX(), nodeA.getY(), nodeB.getX(), nodeB.getY());
                    line.setMouseTransparent(true);
                    line.setStrokeWidth(3);
                    line.setFill(Color.BLACK);
                    center.getChildren().add(line);
                    unsavedChanges = true;
                } catch (IllegalArgumentException e) {
                    new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Time must be a numerical value of zero or more.").showAndWait();
                }
            } catch (NumberFormatException e) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Time must be a numerical value of zero or more.").showAndWait();
            }
        }
    }

    class ChangeConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            if(nodeA == null || nodeB == null) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Warning!", "Two places must be selected!").showAndWait();
                return;
            }

            Edge<Node> edge = graph.getEdgeBetween(nodeA, nodeB);
            if(edge == null) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "These two places are not connected!").showAndWait();
                return;
            }

            try {
                CustomAlert.Connection.Change dialog = new CustomAlert.Connection.Change(nodeA.getName(), nodeB.getName(), edge.getName(), edge.getWeight());
                dialog.showAndWait();
                graph.setConnectionWeight(nodeA, nodeB, dialog.getNewWeight());
            } catch (NumberFormatException e) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Time must be a numerical value of zero or more.").showAndWait();
                return;
            }

            unsavedChanges = true;
        }
    }

    class NewNodeHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            String name;
            double x = event.getX();
            double y = event.getY();

            CustomAlert.NewNode dialog = new CustomAlert.NewNode();
            dialog.showAndWait();
            name = dialog.getName();

            if(name.isEmpty()) {
                new CustomAlert.GeneralNoHeader(Alert.AlertType.ERROR, "Name cannot be empty!").showAndWait();
                return;
            }

            Node node = new Node(name, x, y);
            graph.add(node);

            node.setOnMouseClicked(selector);
            center.getChildren().add(node);
            center.getChildren().add(node.getLabel());
            unsavedChanges = true;

            // Restore previous state
            center.setCursor(Cursor.DEFAULT);
            newPlaceButton.setDisable(false);
            imageView.setOnMouseClicked(null);
        }
    }

    class NodeSelectorHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            Node node = (Node)event.getSource();
            if(nodeA == null && nodeB != node) {
                nodeA = node;
                node.setSelected(true);
            } else if (nodeB == null && node != nodeA) {
                nodeB = node;
                node.setSelected(true);
            } else {
                assert node != null;
                node.setSelected(false);
                if (nodeA == node) {
                    nodeA = nodeB;
                    nodeB = null;
                }
                if (nodeB == node)
                    nodeB = null;
            }
        }
    }

    private void discardUnsavedChanges() {
        boolean discard = true;
        if(unsavedChanges)
            discard = new CustomAlert.DiscardUnsavedChanges().getAnswer();
        if(!discard)
            return;

        center.getChildren().removeIf(e->!(e instanceof ImageView));
        graph.removeAll();
        nodeA = null;
        nodeB = null;
        unsavedChanges = false;
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}