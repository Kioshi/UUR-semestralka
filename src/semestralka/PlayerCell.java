package semestralka;

import javafx.scene.control.TreeItem;

/**
 * Created by smartine on 5.4.2016.
 * @author smartine
 */
public class PlayerCell extends javafx.scene.control.TreeCell<TreeItemType>
{
    @Override
    protected void updateItem(TreeItemType item, boolean empty)
    {
        super.updateItem(item, empty);

        if (item == null || empty)
        {
            setText("");
            return;
        }
        if (item.player != null)
        {
            setText(item.player.getName());
            getTreeItem().getChildren().clear();
            item.player.getPointsHistory().forEach(integer -> getTreeItem().getChildren().add(new TreeItem<>(new TreeItemType(integer))));
        } else
            setText("" + item.points);

    }
}
