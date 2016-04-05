package semestralka;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by smartine on 5.4.2016.
 */
public class TreeStage extends Stage
{
    ObservableList<Main.Player> players;

    public TreeStage(ObservableList<Main.Player> players)
    {
        this.players = players;


        setTitle("Historie bodů");

        BorderPane root = new BorderPane();

        TreeItem<TreeItemType> dummyRoot = new TreeItem<>();
        TreeView<TreeItemType> treeView = new TreeView<>(dummyRoot);
        treeView.setShowRoot(false);
        players.forEach(player ->
        {
            TreeItem<TreeItemType> playerItem = new TreeItem<TreeItemType>(new TreeItemType(player));
            //player.getPointsHistory().forEach(integer -> playerItem.getChildren().add(new TreeItem<>(new TreeItemType(integer))));
            dummyRoot.getChildren().add(playerItem);
        });

        treeView.setCellFactory(param -> new PlayerCell());

        root.setCenter(treeView);


        setScene(new Scene(root, 500, 200));
    }
}

class TreeItemType
{
    Main.Player player;
    int points;

    public TreeItemType(Main.Player player)
    {
        this.player = player;
        this.points = 0;
    }

    public TreeItemType(int points)
    {
        this.player = null;
        this.points = points;
    }
}