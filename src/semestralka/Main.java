package semestralka;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;


/**
 * Created by smartine on 8.3.2016.
 */
public class Main extends Application
{
    private final ObservableList<Player> hraci = FXCollections.observableArrayList();
    private final TableView tableView = new TableView();
    private Stage scoreStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Semestralka main");

        BorderPane root = new BorderPane();
        root.setCenter(createTable());
        root.setBottom(createBottom());

        primaryStage.setScene(new Scene(root,400,500));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {if (scoreStage != null) scoreStage.close();} );
    }

    private Node createBottom()
    {
        FlowPane controlPane = new FlowPane();

        // creating buttons
        Button add = new Button("Pridat hrace");
        Button del = new Button("Odebrat hrace");
        Button addScore = new Button("Pridat score");

        add.setOnAction(event -> addPlayer());
        del.setOnAction(event -> delPlayers());
        addScore.setOnAction(event -> addScore());

        controlPane.getChildren().addAll(add, del, addScore);

        for (Node node : controlPane.getChildren())
        {
            FlowPane.setMargin(node, new Insets(3));
            ((Button)node).setPrefWidth(100);
        }

        // moving all buttons to center in parent layout
        controlPane.setAlignment(Pos.CENTER);
        controlPane.setPadding(new Insets(5));

        return controlPane;
    }

    private void addScore()
    {
        if (scoreStage != null)
            scoreStage.close();

        BorderPane root = new BorderPane();

        final StringBuilder text = new StringBuilder("Vybrani: ");
        ObservableList<Player> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Chyba pridani score");
            alert.setHeaderText("Nebyly vybrani zadni hraci!");
            alert.show();
            return;
        }
        selected.forEach(o -> {
            text.append(((Player) o).jmeno + ", ");
        });
        Label label = new Label(text.toString().substring(0,text.length()-2));
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(5));

        TextField textField = new TextField("0");
        textField.setAlignment(Pos.CENTER);
        Button butt = new Button("Pridat");
        butt.setAlignment(Pos.CENTER);
        scoreStage = new Stage();
        final Stage stage = scoreStage;
        butt.setOnAction(event ->
        {
            for (Player player : hraci)
            {
                if (selected.contains(player))
                    player.body += Integer.parseInt(textField.getText());
            }
            tableView.refresh();
            stage.close();
        });

        VBox box = new VBox(10);

        box.setAlignment(Pos.CENTER);
        box.getChildren().add(label);
        box.getChildren().add(textField);
        box.getChildren().add(butt);

        root.setTop(box);
        scoreStage.setTitle("My New Stage Title");
        scoreStage.setScene(new Scene(root, 200, 100));
        scoreStage.show();
    }

    private void delPlayers()
    {
        ObservableList<Player> list = tableView.getSelectionModel().getSelectedItems();
        hraci.removeAll(list);

        tableView.getSelectionModel().clearSelection();
    }

    private void addPlayer()
    {
        int onlyFocus = -1;
        for (int i=0;i<hraci.size();i++)
        {
            //System.out.println(hraci.get(i).jmeno);
            if (hraci.get(i).jmeno.isEmpty())
            {
                onlyFocus = i;
                break;
            }
        }

        if (onlyFocus == -1)
        {
            hraci.add(new Player("", 0));
            tableView.scrollTo(hraci.size()-1);
        }

        tableView.getSelectionModel().clearSelection();
        tableView.requestFocus();
        tableView.getSelectionModel().select(hraci.size() - 1);
        tableView.getFocusModel().focus(hraci.size()-1,(TableColumn)tableView.getColumns().get(0));

    }

    private Node createTable()
    {
        TableColumn<Player,String> jmeno = new TableColumn<>("Jmeno");
        TableColumn<Player,Number> body = new TableColumn<>("Body");
        tableView.getColumns().addAll(jmeno,body);
        tableView.setItems(createInitData(5));
        tableView.setPrefWidth(Double.MAX_VALUE);
        tableView.setEditable(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        body.setSortType(TableColumn.SortType.DESCENDING);

        jmeno.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Player,String>, ObservableValue<String>>()
        {
        @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Player, String> p)
            {
                return new SimpleStringProperty(p.getValue().jmeno);
            }
        });

        jmeno.setCellFactory(TextFieldTableCell.<Player>forTableColumn());
        jmeno.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Player,String>>()
        {
            @Override
            public void handle(TableColumn.CellEditEvent<Player, String> t) {
                ((Player) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                ).setJmeno(t.getNewValue());
            }
        });


        body.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Player,Number>, ObservableValue<Number>>()
        {
             @Override
             public ObservableValue<Number>
             call(TableColumn.CellDataFeatures<Player, Number> p) {
                 return new SimpleIntegerProperty(p.getValue().body);
             }

         });
        body.setCellFactory(TextFieldTableCell.<Player,Number>forTableColumn(new NumberStringConverter()));

        body.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Player,Number>>()
        {
            @Override
            public void handle(TableColumn.CellEditEvent<Player, Number> t) {
                ((Player) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())
                ).setBody(t.getNewValue().intValue());
            }
        });


        BorderPane.setMargin(tableView, new Insets(5));

        return tableView;
    }

    private ObservableList createInitData(int n)
    {
        for (int  i = 0; i<n;i++)
        {
            hraci.add(new Player("Hrac "+(i+1), 78/(i+1)));
        }

        return hraci;
    }


    public static void main(String[] args) {
        launch(args);
    }


    public class Player
    {
        private String jmeno;
        private int body;

        public Player(String jmeno, int body)
        {
            this.jmeno = jmeno;
            this.body = body;
        }

        public String getJmeno()
        {
            return jmeno;
        }

        public void setJmeno(String jmeno)
        {
            this.jmeno = jmeno;
        }

        public int getBody()
        {
            return body;
        }

        public void setBody(int body)
        {
            this.body = body;
        }
    }
}