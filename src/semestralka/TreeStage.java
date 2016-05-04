package semestralka;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by smartine on 5.4.2016.
 * @author smartine
 */
public class TreeStage extends Stage
{
    /** treeView reference */
    TreeView<TreeItemType> treeView;

    /**
     * Constructor where treeview is created and all lambdas/callbacks are set
     * @param players List of players to add lambdas
     */
    public TreeStage(ObservableList<Main.Player> players)
    {
        setTitle("Historie bodů");

        BorderPane root = new BorderPane();

        TreeItem<TreeItemType> dummyRoot = new TreeItem<>();
        treeView = new TreeView<>(dummyRoot);
        treeView.setShowRoot(false);
        players.forEach(player ->
        {
            TreeItem<TreeItemType> playerItem = new TreeItem<TreeItemType>(new TreeItemType(player));
            player.getPointsHistory().forEach(integer -> playerItem.getChildren().add(new TreeItem<>(new TreeItemType(integer))));
            dummyRoot.getChildren().add(playerItem);
        });

        treeView.setCellFactory(param -> new PlayerCell());
        Stage s = this;
        players.addListener((ListChangeListener<Main.Player>) c -> {


            if (treeView.getRoot().getChildren().size() > players.size())
            {
                ArrayList<TreeItem<TreeItemType>> toRemove = new ArrayList<TreeItem<TreeItemType>>();
                for (TreeItem<TreeItemType> item : treeView.getRoot().getChildren())
                {
                    if (!players.contains(item.getValue().player))
                    {
                        toRemove.add(item);
                    }
                }

                treeView.getRoot().getChildren().removeAll(toRemove);
            }
            else
            {
                Main.Player player = players.get(players.size()-1);
                TreeItem<TreeItemType> playerItem = new TreeItem<>(new TreeItemType(player));
                player.getPointsHistory().forEach(integer -> playerItem.getChildren().add(new TreeItem<>(new TreeItemType(integer))));
                treeView.getRoot().getChildren().add(playerItem);
                player.addListener(observable -> treeView.refresh());
            }
            treeView.refresh();

        });


        for (Main.Player player : players)
        {
            player.clearListeners();
            player.addListener(observable -> {
                treeView.refresh();
                setAlwaysOnTop(true);
                setAlwaysOnTop(false);
            });
        }

        root.setCenter(treeView);


        setScene(new Scene(root));
        sizeToScene();
    }
}

/**
 * @author smartine
 */
class TreeItemType
{
    /** Reference to player, can be null */
    Main.Player player;
    /** Number of points, used when player is null */
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