package semestralka;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.util.ArrayList;


/**
 * Created by smartine on 8.3.2016.
 */
public class Main extends Application
{
    private final ObservableList<Player> hraci = FXCollections.observableArrayList();
    private final TableView tableView = new TableView();
    private Stage scoreStage;
    private Stage historyStage;

    @Override
    public void start(Stage primaryStage)// throws Exception
    {
        primaryStage.setTitle("Semestralka main");

        BorderPane root = new BorderPane();
        root.setTop(createTop());
        root.setCenter(createTable());
        root.setBottom(createBottom());

        primaryStage.setScene(new Scene(root,250,500));
        primaryStage.setMinWidth(210);
        hraci.addListener(new ListChangeListener<Player>()
        {
            @Override
            public void onChanged(Change<? extends Player> c)
            {
                System.out.println("change");
            }
        });
        hraci.addListener(new InvalidationListener()
        {
            @Override
            public void invalidated(Observable observable)
            {
                System.out.println("invalid");
            }
        });

        //clean created windows when main window close
        primaryStage.setOnCloseRequest(event ->
        {
            if (scoreStage != null) scoreStage.close();
            if (historyStage != null) historyStage.close();
        });

        primaryStage.show();
    }

    private Node createTop()
    {
        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("Soubor");
        MenuItem open = new MenuItem("Otevrit");
        MenuItem save = new MenuItem("Ulozit");
        MenuItem saveAs = new MenuItem("Ulozit jako");
        file.getItems().addAll(open,save,saveAs);

        Menu control = new Menu("Data");
        MenuItem add = new MenuItem("Pridat řádek");
        add.setOnAction(event -> addPlayer(false));
        MenuItem delete = new MenuItem("Odebrat řádky");
        delete.setOnAction(event -> delPlayers());
        control.getItems().addAll(add,delete);

        menuBar.getMenus().addAll(file, control);

        return menuBar;
    }

    private Node createBottom()
    {
        FlowPane controlPane = new FlowPane();

        // Table is now keyboard controlable so i dont use add and del buttons + their alternative is in menu
        /*
        Button buttAdd = new Button("Pridat hrace");
        Button buttDel = new Button("Odebrat hrace");
        buttAdd.setOnAction(event -> addPlayer(false));
        buttDel.setOnAction(event -> delPlayers());
        controlPane.getChildren().addAll(buttAdd, buttDel);
        */

        Button buttAddScore = new Button("Přidat body");
        buttAddScore.setOnAction(event -> addScore());
        controlPane.getChildren().addAll(buttAddScore);

        Button buttShowGraph = new Button("Zobrazit historii");
        buttShowGraph.setOnAction(event -> showGraph());
        controlPane.getChildren().addAll(buttShowGraph);

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

    private void showGraph()
    {
        if (historyStage != null)
        {
            historyStage.toFront();
            historyStage.show();
            return;
        }
        historyStage = new TreeStage(tableView.getItems());
        historyStage.show();
    }

    // Allert when no players were selected
    private void throwAllert()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chyba přidání bodů");
        alert.setHeaderText("Nebyly vybráni žádní hráči!");
        alert.show();
    }

    //Create new window for adding score
    private void addScore()
    {
        if (scoreStage != null)
            scoreStage.close();

        BorderPane root = new BorderPane();

        final StringBuilder text = new StringBuilder("Vybraní: ");
        ObservableList<Player> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty())
        {
            throwAllert();
            return;
        }

        // Fill text
        selected.forEach(o -> text.append(o.name + ", "));

        // Create text
        Label label = new Label(text.toString().substring(0,text.length()-2));
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(5));

        //Create numeric text field
        NumericField<Integer> numericField = new NumericField<>(0);
        numericField.setAlignment(Pos.CENTER);

        //Create button
        Button butt = new Button("Přidat");
        butt.setAlignment(Pos.CENTER);

        //Create stage
        scoreStage = new Stage();

        //Set on click action
        final Stage stage = scoreStage;
        butt.setOnAction(event ->
        {
            for (Player player : hraci)
                if (selected.contains(player))
                    player.setPoints(player.getPoints() + numericField.value);
            tableView.refresh();
            stage.close();
        });

        //Create layout
        VBox box = new VBox(10);

        box.setAlignment(Pos.CENTER);
        box.getChildren().add(label);
        box.getChildren().add(numericField);
        box.getChildren().add(butt);
        root.setTop(box);

        //Show
        scoreStage.setTitle("Přidat body");
        scoreStage.setScene(new Scene(root, 200, 100));
        scoreStage.show();
    }

    //Delete selected players
    private void delPlayers()
    {
        ObservableList<Player> list = tableView.getSelectionModel().getSelectedItems();
        hraci.removeAll(list);

        tableView.getSelectionModel().clearSelection();
    }

    //Add new row with empty name and 0 points
    private void addPlayer(boolean checkSelection)
    {
        //Check if selected item is last row (for addPlayer called from down arrow event)
        if (checkSelection)
        {
            ObservableList<Player> list = tableView.getSelectionModel().getSelectedItems();
            if (list.size() > 1 && list.get(0) != hraci.get(hraci.size() - 1))
                return;
        }

        //Check if row without player name isnt already created
        int onlyFocus = -1;
        for (int i=0;i<hraci.size();i++)
        {
            if (hraci.get(i).name.equals("HRAC"))
            {
                onlyFocus = i;
                break;
            }
        }

        //Create new row only in !onlyFocus case
        if (onlyFocus == -1)
        {
            hraci.add(new Player("HRAC", 0));
            tableView.scrollTo(hraci.size()-1);
        }

        //Scroll and focus new row
        tableView.getSelectionModel().clearSelection();
        tableView.requestFocus();
        tableView.getSelectionModel().select(hraci.size() - 1);
        tableView.getFocusModel().focus(hraci.size()-1,(TableColumn)tableView.getColumns().get(0));

    }

    private Node createTable()
    {
        //Name column create and settings
        TableColumn<Player,String> nameCol = new TableColumn<>("Jmeno");
        //Factories
        nameCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().name));
        nameCol.setCellFactory(TextFieldTableCell.<Player>forTableColumn());
        //Change data on edit
        nameCol.setOnEditCommit(t ->  t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue()));
        //Size
        nameCol.setMaxWidth( 1f * Integer.MAX_VALUE * 70 ); // 70% width
        nameCol.setMinWidth(100);

        //Points column create and settings
        TableColumn<Player,Number> body = new TableColumn<>("Body");
        //Factories
        body.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().points));
        body.setCellFactory(TextFieldTableCell.<Player,Number>forTableColumn(new NumberStringConverter()));
        //Change data on edit
        body.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setPoints(t.getNewValue().intValue()));
        //Default sort
        body.setSortType(TableColumn.SortType.DESCENDING);
        //Size
        body.setMaxWidth( 1f * Integer.MAX_VALUE * 30 ); // 30% width
        body.setMinWidth(50);

        //Table settings
        tableView.getColumns().addAll(nameCol,body);
        //Init data for testing
        tableView.setItems(createInitData(5));
        tableView.setPrefWidth(Double.MAX_VALUE);
        tableView.setEditable(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);;
        //Keyboard control
        tableView.setOnKeyPressed(event ->
        {
            if (event.getCode().equals(KeyCode.DELETE))
                delPlayers();
            if (event.getCode().equals(KeyCode.DOWN))
                addPlayer(true);
        });

        BorderPane.setMargin(tableView, new Insets(5));

        return tableView;
    }

    // Init test data
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


    public class Player implements Observable
    {
        private String name;
        private int points;
        private ArrayList<Integer> pointsHistory = new ArrayList<>();

        public Player(String name, int points)
        {
            this.name = name;
            this.points = points;
            pointsHistory.add(points);
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            if (name.isEmpty())
                return;
            this.name = name;
            notifyListeners();
        }

        public int getPoints()
        {
            return points;
        }

        public void setPoints(int points)
        {
            pointsHistory.add(points - this.points);
            this.points = points;
            notifyListeners();
        }

        public ArrayList<Integer> getPointsHistory()
        {
            return pointsHistory;
        }

        private void notifyListeners()
        {
            for (InvalidationListener listener : listeners)
                listener.invalidated(this);
        }

        private ArrayList<InvalidationListener> listeners = new ArrayList<>();
        @Override
        public void addListener(InvalidationListener listener)
        {
            listeners.add(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener)
        {
            listeners.remove(listener);
        }
    }
}