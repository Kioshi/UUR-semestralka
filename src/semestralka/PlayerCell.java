package semestralka;

import javafx.scene.control.TreeItem;

/**
 * Created by smartine on 5.4.2016.
 */
public class PlayerCell extends javafx.scene.control.TreeCell<TreeItemType>
{

    @Override
    protected void updateItem(TreeItemType item, boolean empty)
    {

        if (item == null || empty)
            return;

        if (item.player != null)
        {
            setText(item.player.getName());
            System.out.println(getChildren().size() + " " + getTreeItem().getChildren().size());
            getTreeItem().getChildren().clear();
            System.out.println(getChildren().size() + " " + getTreeItem().getChildren().size());
            item.player.getPointsHistory().forEach(integer -> getTreeItem().getChildren().add(new TreeItem<>(new TreeItemType(integer))));
        }
        else
            setText(""+item.points);

        super.updateItem(item, empty);
    }
}
