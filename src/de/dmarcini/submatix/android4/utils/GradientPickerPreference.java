/**
 * Objekt zum einstellen einer Zahl via NumberPicker TODO: Werte Persistent machen...
 */
package de.dmarcini.submatix.android4.utils;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import de.dmarcini.submatix.android4.R;

/**
 * Eigene Klasse zum Einstellen der Gradienten für decompression
 * 
 * @author dmarc
 */
public class GradientPickerPreference extends DialogPreference implements OnValueChangeListener
{
  private static final String TAG                = GradientPickerPreference.class.getSimpleName();
  private NumberPicker        lowPicker          = null;
  private NumberPicker        highPicker         = null;
  private String              defaultReturnValue = "30:80";
  private int                 highGradient       = 0;
  private int                 lowGradient        = 0;

  /**
   * Private Klasse (nach Android Developers) zum sichern des aktuellen Status Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 29.12.2012
   */
  private static class SavedState extends BaseSavedState
  {
    // Member that holds the setting's value
    // Change this data type to match the type saved by your Preference
    String value;

    public SavedState(Parcelable superState)
    {
      super(superState);
    }

    public SavedState(Parcel source)
    {
      super(source);
      // Get the current preference's value
      value = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
      super.writeToParcel(dest, flags);
      // Write the preference's value
      dest.writeString(value);
    }

    // Standard creator object using an instance of this class
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>()
                                                               {
                                                                 @Override
                                                                 public SavedState createFromParcel(Parcel in)
                                                                 {
                                                                   return new SavedState(in);
                                                                 }

                                                                 @Override
                                                                 public SavedState[] newArray(int size)
                                                                 {
                                                                   return new SavedState[size];
                                                                 }
                                                               };
  }

  /**
   * Der Konstruktor
   * 
   * @author Dirk Marciniak 28.12.2012
   * @param context
   * @param attrs
   */
  public GradientPickerPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setPositiveButtonText(R.string.conf_deco_gradient_button_positive);
    setNegativeButtonText(R.string.conf_deco_gradient_button_negative);
    setDialogIcon(null);
  }

  /**
   * alternativer Konstruktor mit Style-Attributen
   * 
   * @author Dirk Marciniak 28.12.2012
   * @param context
   * @param attrs
   * @param defStyle
   */
  public GradientPickerPreference(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    setPositiveButtonText(R.string.conf_deco_gradient_button_positive);
    setNegativeButtonText(R.string.conf_deco_gradient_button_negative);
    setDialogIcon(null);
  }

  @Override
  public void onValueChange(NumberPicker picker, int oldVal, int newVal)
  {
    Log.v(TAG, "onValueChange: oldVal: <" + oldVal + ">, newVal: <" + newVal + ">");
    // currentValue = newVal;
  }

  @Override
  protected View onCreateDialogView()
  {
    Log.d(TAG, "onCreateDialogView()...");
    setDialogLayoutResource(R.layout.gradient_picker_layout);
    return super.onCreateDialogView();
  }

  @Override
  protected void onBindDialogView(View v)
  {
    Log.d(TAG, "onBindDialogView()...");
    //
    // LOW Gradient Picker initialisieren
    //
    lowPicker = (NumberPicker) v.findViewById(R.id.gradientLowPicker);
    lowPicker.setOnValueChangedListener(this);
    lowPicker.setMinValue(0);
    lowPicker.setMaxValue(100);
    lowPicker.setWrapSelectorWheel(false);
    //
    // HIGH Gradient Picker initialisieren
    //
    highPicker = (NumberPicker) v.findViewById(R.id.gradientHighPicker);
    highPicker.setOnValueChangedListener(this);
    highPicker.setMinValue(0);
    highPicker.setMaxValue(100);
    highPicker.setWrapSelectorWheel(false);
    Log.d(TAG, "onBindDialogView()...OK");
  }

  @Override
  protected void onSetInitialValue(boolean restoreValue, Object def)
  {
    String defaultValueStr;
    //
    super.onSetInitialValue(restoreValue, def);
    Log.d(TAG, "onSetInitialValue: restore:<" + restoreValue + ">");
    if (restoreValue)
    {
      // es soll restored werden
      try
      {
        defaultValueStr = getPersistedString(defaultReturnValue);
      }
      catch (Exception ex)
      {
        Log.e(TAG, "Ops, an exception.... Was saved an other type of content?");
        defaultValueStr = defaultReturnValue;
      }
      makeValuesFromString(defaultValueStr);
    }
    else
    {
      makeValuesFromString(defaultReturnValue);
    }
  }

  /**
   * Mach aus dem Parameterstring die Werte für die Gradienten Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 02.01.2013
   * @param defaultValueStr
   * @return ja oder nicht
   */
  private boolean makeValuesFromString(String defaultValueStr)
  {
    Log.d(TAG, "makeValuesFromString: String to split <" + defaultValueStr + ">");
    String fields[] = defaultValueStr.split(":");
    if ((fields != null) && (fields.length >= 2))
    {
      Log.d(TAG, String.format("makeValuesFromString: <%s> <%s>", fields[0], fields[1]));
      Log.d(TAG, "makeValuesFromString: successful split default value!");
      try
      {
        lowGradient = Integer.parseInt(fields[0]);
        highGradient = Integer.parseInt(fields[1]);
        Log.d(TAG, "makeValuesFromString: successful set Values");
        return (true);
      }
      catch (NumberFormatException ex)
      {
        Log.e(TAG, "makeValuesFromString: <" + ex.getLocalizedMessage() + ">");
        return (false);
      }
    }
    else
    {
      Log.w(TAG, "makeValuesFromString: not correct default Value loadet (" + defaultValueStr + ")");
    }
    return (false);
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index)
  {
    String defaultString = null;
    //
    super.onGetDefaultValue(a, index);
    Log.d(TAG, "onGetDefaultValue()...");
    //
    // versuche aus einer Stringresource einen defaultwert zu machen
    //
    Log.d(TAG, "onGetDefaultValue:...try read string resource and index <" + index + ">...");
    defaultString = a.getString(index);
    if (defaultString != null)
    {
      defaultReturnValue = defaultString;
    }
    Log.d(TAG, "onGetDefaultValue: defaultString<" + defaultString + ">");
    return (defaultString);
  }

  /**
   * Called when the dialog is closed. If the close was by pressing "OK" it saves the value.
   */
  @Override
  protected void onDialogClosed(boolean shouldSave)
  {
    super.onDialogClosed(shouldSave);
    Log.v(TAG, "onDialogClosed()...");
    if (shouldSave)
    {
      Log.v(TAG, "onDialogClosed: should save...");
      persistString(String.format("%d:%d", lowPicker.getValue(), highPicker.getValue()));
      // DEBUG
      // setTitle( "LowHelium <" + heCurrent + ">" );
      setSummary(String.format("low: %d%%  high: %d%%", lowPicker.getValue(), highPicker.getValue()));
    }
  }


  @Override
  protected Parcelable onSaveInstanceState()
  {
    super.onSaveInstanceState();
    final Parcelable superState = super.onSaveInstanceState();
    //
    Log.d(TAG, "onSaveInstanceState()...");
    // Check whether this Preference is persistent (continually saved)
    if (isPersistent())
    {
      // No need to save instance state since it's persistent, use superclass state
      return superState;
    }
    // Create instance of custom BaseSavedState
    final SavedState myState = new SavedState(superState);
    // Set the state's value with the class member that holds current setting value
    myState.value = String.format("%d:%d", lowPicker.getValue(), highPicker.getValue());
    return myState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state)
  {
    super.onRestoreInstanceState(state);
    Log.d(TAG, "onRestoreInstanceState()...");
    // Check whether we saved the state in onSaveInstanceState
    if (state == null || !state.getClass().equals(SavedState.class))
    {
      // Didn't save the state, so call superclass
      super.onRestoreInstanceState(state);
      return;
    }
    // Cast state to custom BaseSavedState and pass to superclass
    SavedState myState = (SavedState) state;
    super.onRestoreInstanceState(myState.getSuperState());
    // Set this Preference's widget to reflect the restored state
    makeValuesFromString(myState.value);
    try
    {
      lowPicker.setValue(lowGradient);
      highPicker.setValue(highGradient);
    }
    catch (NullPointerException ex)
    {
      Log.e(TAG, "onRestoreInstanceState: NumberPicker was not initialized yet.");
    }
  }

}
