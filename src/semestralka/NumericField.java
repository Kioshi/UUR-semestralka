package semestralka;

import javafx.scene.control.TextField;

/**
 * Created by smartine on 21.3.2016.
 * Chtel jsem to udelat primo pro Number ale java je co se tyce templatu oproti c++ velice limitovana
 */
public class NumericField<T extends Integer> extends TextField
{
    T value;

    public NumericField(T value)
    {
        super(value.toString());
        this.value = value;

        this.textProperty().addListener((observable, oldValue, newValue) ->
            {
                try
                {
                    changeValue((T)(Integer)T.parseInt(newValue));
                }
                catch (Exception e)
                {
                    setText(oldValue);
                }
            });

        this.setOnKeyReleased(event ->
        {
            switch(event.getCode())
            {
                case UP:
                    inc();
                    break;
                case DOWN:
                    dec();
                    break;
                case LEFT:
                    shiftLeft();
                    break;
                case RIGHT:
                    shiftRight();
                    break;
            }
        });
    }

    private void inc()
    {
        Integer i = value.intValue() + 1;
        changeValue((T)i);
    }

    private void dec()
    {
        Integer i = value.intValue() - 1;
        changeValue((T)i);
    }

    private void shiftLeft()
    {
        Integer i = value.intValue() * 10;
        if (i == 0)
            i = 1;
        changeValue((T)i);
    }

    private void shiftRight()
    {
        Integer i = value.intValue() / 10;
        changeValue((T)i);
    }

    public void changeValue(T newValue)
    {
        value = newValue;;
        setText(value.toString());
        positionCaret(getText().length());
    }

    // block methods
    private NumericField() {}
    private NumericField(String s) {}
}
