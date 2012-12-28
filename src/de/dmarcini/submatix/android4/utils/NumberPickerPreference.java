/**
 * Objekt zum einstellen einer Zahl via NumberPicker TODO: Werte Persistent machen...
 */
package de.dmarcini.submatix.android4.utils;


import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

/**
 * Eigene Klasse zum Einstellen einer Numnmer
 * 
 * @author dmarc
 */
public class NumberPickerPreference extends DialogPreference implements OnValueChangeListener
{
  private NumberPicker nPicker      = null;
  private AttributeSet attrs        = null;
  private int          currentValue = 0;

  /**
   * Der Konstruktor
   * 
   * @author Dirk Marciniak 28.12.2012
   * @param context
   * @param attrs
   */
  public NumberPickerPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    this.attrs = attrs;
  }

  /**
   * alterneativer Konstruktor mit Style-Attributen
   * 
   * @author Dirk Marciniak 28.12.2012
   * @param context
   * @param attrs
   * @param defStyle
   */
  public NumberPickerPreference(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    this.attrs = attrs;
  }

  @Override
  public void onValueChange(NumberPicker picker, int oldVal, int newVal)
  {
    currentValue = newVal;
  }

  @Override
  protected View onCreateDialogView()
  {
    nPicker = new NumberPicker(getContext(), this.attrs);
    nPicker.setMinValue(0);
    nPicker.setMaxValue(100);
    this.currentValue = getPersistedInt(defaultValue());
    nPicker.setValue(this.currentValue);
    return nPicker;
  }

  @Override
  protected void onSetInitialValue(boolean restoreValue, Object def)
  {
    if (restoreValue)
    {
      this.currentValue = getPersistedInt(defaultValue());
      if (nPicker != null)
      {
        nPicker.setValue(this.currentValue);
      }
    }
    else
    {
      persistInt(currentValue);
    }
  }

  private int defaultValue()
  {
    // TODO: sinnvol machen..
    return 30;
  }

  /**
   * Called when the dialog is closed. If the close was by pressing "OK" it saves the value.
   */
  @Override
  protected void onDialogClosed(boolean shouldSave)
  {
    if (shouldSave)
    {
      persistInt(currentValue);
    }
  }
}
