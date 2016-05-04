package semestralka;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;


/**
 * Created by smartine on 8.3.2016.
 * @author smartine
 */
public class Main extends Application
{
    /** List of players */
    private final ObservableList<Player> playersList = FXCollections.observableArrayList();
    /** tableView reference */
    private final TableView tableView = new TableView();
    /** Score window reference */
    private Stage scoreStage;
    /** History window reference */
    private Stage historyStage;
    /** Main window reference */
    private Stage mainStage;
    /** File for saving/opening */
    private File file;

    @Override
    public void start(Stage primaryStage)// throws Exception
    {
        primaryStage.setTitle("Semestralka main");
        mainStage = primaryStage;

        BorderPane root = new BorderPane();
        root.setTop(createTop());
        root.setCenter(createTable());
        root.setBottom(createBottom());

        primaryStage.setScene(new Scene(root,250,500));
        primaryStage.setMinWidth(210);

        //clean created windows when main window close
        primaryStage.setOnCloseRequest(event ->
        {
            if (scoreStage != null) scoreStage.close();
            if (historyStage != null) historyStage.close();
        });

        primaryStage.show();
    }

    /**
     * Create menu bar for main window
     * @return MenuBar
     */
    private Node createTop()
    {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("_Soubor");
        MenuItem open = new MenuItem("_Otevrit");
        open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        open.setOnAction(event ->
        {
            FileChooser fileChooser = new FileChooser();
            File f = fileChooser.showOpenDialog(mainStage.getScene().getWindow());
            if (f != null)
            {
                file = f;
                loadFromFile();
            }
        });

        MenuItem save = new MenuItem("_Ulozit");
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        save.setOnAction(event ->
        {
            if (file == null)
                saveAs();
            else
                save();
        });
        MenuItem saveAs = new MenuItem("Ulo_zit jako");
        saveAs.setOnAction(event -> saveAs());
        fileMenu.getItems().addAll(open,save,saveAs);

        Menu control = new Menu("_Data");
        MenuItem add = new MenuItem("_Pridat řádek");
        add.setOnAction(event -> addPlayer(false));
        MenuItem delete = new MenuItem("Od_ebrat řádky");
        delete.setOnAction(event -> delPlayers());
        control.getItems().addAll(add,delete);

        menuBar.getMenus().addAll(fileMenu, control);

        return menuBar;
    }

    /**
     * Save players to selected file
     */
    private void save()
    {
        PrintStream out;
        try
        {
            out = new PrintStream(file);
        } catch (FileNotFoundException e)
        {
            throwAllert("File error.", "File not found.");
            return;
        }

        for (Player player: playersList)
        {
            out.println(player.getName());
            player.pointsHistory.stream().forEach(integer ->  out.print(integer+" "));
            out.println();
        }
    }

    /**
     * Open file chooser and call save function if file is selected
     */
    private void saveAs()
    {
        FileChooser fileChooser = new FileChooser();
        file = fileChooser.showSaveDialog(mainStage.getScene().getWindow());
        if (file != null)
            save();
    }

    /**
     * Pront confirmation and load data from file
     */
    private void loadFromFile()
    {
        if (!playersList.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Opravdu chcete prepsat data, daty ze souboru?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK)
            {
                file = null;
                return;
            }
        }

        Scanner scanner;
        try
        {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e)
        {
            throwAllert("File error.", "File not found.");
            return;
        }

        playersList.clear();
        while (scanner.hasNext())
        {
            String jmeno = scanner.nextLine();
            if (!scanner.hasNextLine())
            {
                throwAllert("File error.", "Invalid file format.");
                return;
            }
            Scanner sc = new Scanner(scanner.nextLine());

            ArrayList<Integer> list = new ArrayList<>();
            while (sc.hasNextInt())
                list.add(sc.nextInt());

            if (list.isEmpty())
            {
                throwAllert("File error.", "Invalid file format.");
                return;
            }

            playersList.add(new Player(jmeno,list));
        }
    }

    /**
     * Create buttons section of main window
     * @return FlowPane with buttons
     */
    private Node createBottom()
    {
        FlowPane controlPane = new FlowPane();

        Button buttAddScore = new Button("Přidat body");
        buttAddScore.setOnAction(event -> addScore());
        controlPane.getChildren().addAll(buttAddScore);

        Button buttShowGraph = new Button("Zobrazit historii");
        buttShowGraph.setOnAction(event -> showHistory());
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

    /**
     * Create history window
     */
    private void showHistory()
    {
        if (historyStage != null)
        {
            historyStage.toFront();
            historyStage.show();
            return;
        }
        historyStage = new TreeStage(playersList);
        historyStage.show();
    }

    /**
     * Create error alert
     * @param title
     * @param text
     */
    private void throwAllert(String title, String text)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(text);
        alert.show();
    }

    /**
     * Create add score window for adding score to multiple players.
     */
    private void addScore()
    {
        if (scoreStage != null)
            scoreStage.close();

        BorderPane root = new BorderPane();

        final StringBuilder text = new StringBuilder("Vybraní: ");
        ObservableList<Player> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected.isEmpty())
        {
            throwAllert("Chyba přidání bodů","Nebyly vybráni žádní hráči!");
            return;
        }

        // Fill text
        selected.forEach(o -> text.append(o.name + ", "));

        // Create text
        Label label = new Label(text.toString().substring(0,text.length()-2));
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(5));
        label.setWrapText(true);

        //Create numeric text field
        TextField textField = new TextField("0");
        textField.setAlignment(Pos.CENTER);

        //Create button
        Button butt = new Button("Přidat");
        butt.setAlignment(Pos.CENTER);

        //Create stage
        scoreStage = new Stage();

        //Set on click action
        final Stage stage = scoreStage;
        butt.setOnAction(event ->
        {
            int value;
            try
            {
                value = Integer.parseInt(textField.getText());
            }
            catch (Exception e)
            {
                return;
            }
            for (Player player : playersList)
                if (selected.contains(player))
                    player.setPoints(player.getPoints() + value);
            tableView.refresh();
            stage.close();
        });

        //Create layout
        VBox box = new VBox(10);

        box.setAlignment(Pos.CENTER);
        box.getChildren().add(label);
        box.getChildren().add(textField);
        box.getChildren().add(butt);
        root.setTop(box);

        //Show
        scoreStage.setTitle("Přidat body");
        scoreStage.setScene(new Scene(root));
        scoreStage.sizeToScene();
        scoreStage.show();
    }

    /**
     * Delete selected players from list
     */
    private void delPlayers()
    {
        ObservableList<Player> list = tableView.getSelectionModel().getSelectedItems();
        playersList.removeAll(list);

        tableView.getSelectionModel().clearSelection();
    }

    /**
     * Adds new player to list with placeholder name and 0 points
     * @param checkSelection Check if selected item is last row (for addPlayer called from down arrow event)
     */
    private void addPlayer(boolean checkSelection)
    {
        //Check if selected item is last row (for addPlayer called from down arrow event)
        if (checkSelection)
        {
            ObservableList<Player> list = tableView.getSelectionModel().getSelectedItems();
            if (list.size() > 1 && list.get(0) != playersList.get(playersList.size() - 1))
                return;
        }

        //Check if row without player name isnt already created
        int onlyFocus = -1;
        for (int i = 0; i< playersList.size(); i++)
        {
            if (playersList.get(i).name.equals("HRAC"))
            {
                onlyFocus = i;
                break;
            }
        }

        //Create new row only in !onlyFocus case
        if (onlyFocus == -1)
        {
            playersList.add(new Player("HRAC", 0));
            tableView.scrollTo(playersList.size()-1);
        }

        //Scroll and focus new row
        tableView.getSelectionModel().clearSelection();
        tableView.requestFocus();
        tableView.getSelectionModel().select(playersList.size() - 1);
        tableView.getFocusModel().focus(playersList.size()-1,(TableColumn)tableView.getColumns().get(0));

    }

    /**
     * Create tableView
     * @return Returns tableView
     */
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
        body.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
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

    /**
     * Create init data for testing
     * @param n number of players to create
     * @return ObservableList of players
     */
    private ObservableList createInitData(int n)
    {
        for (int  i = 0; i<n;i++)
        {
            playersList.add(new Player("Hrac "+(i+1), 78/(i+1)));
        }

        return playersList;
    }

    /**
     * Launch gui
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Observable player class, that will notify observer when setter is called
     */
    class Player implements Observable
    {
        /** Name of player */
        private String name;
        /** Points of player */
        private int points;
        /** List with point of player */
        private ArrayList<Integer> pointsHistory = new ArrayList<>();
        /** Listeners for update when name or points are changes */
        private ArrayList<InvalidationListener> listeners = new ArrayList<>();

        /**
         * Constructor when new player is created
         * @param name Name of player
         * @param points Initial points of player
         * */
        Player(String name, int points)
        {
            this.name = name;
            this.points = points;
            pointsHistory.add(points);
        }

        /**
         * Constructor when new player loaded from file
         * @param name Name of player
         * @param list List of player points
         * */
        Player(String name, ArrayList<Integer> list)
        {
            this.name = name;
            this.pointsHistory = list;
            points = 0;
            list.stream().forEach(integer -> points += integer);
        }

        String getName()
        {
            return name;
        }

        void setName(String name)
        {
            if (name.isEmpty())
                return;
            this.name = name;
            notifyListeners();
        }

        int getPoints()
        {
            return points;
        }

        void setPoints(int points)
        {
            if (points == this.points)
                return;
            pointsHistory.add(points - this.points);
            this.points = points;
            notifyListeners();
        }

        ArrayList<Integer> getPointsHistory()
        {
            return pointsHistory;
        }

        /**
         * Notify listeners called from setters.
         */
        private void notifyListeners()
        {
            for (InvalidationListener listener : listeners)
                listener.invalidated(this);
        }

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

        /**
         * Clear listeners
         */
        void clearListeners()
        {
            listeners.clear();
        }
    }
}