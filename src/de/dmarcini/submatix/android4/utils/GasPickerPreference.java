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
 * 
 * Klasse für einen Dreifach-Numberpicker für Gasmischungen TRIMIX
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.01.2013
 */
public class GasPickerPreference extends DialogPreference implements OnValueChangeListener
{
  private static final String TAG          = GasPickerPreference.class.getSimpleName();
  private NumberPicker        O2Picker     = null;
  private final NumberPicker  HEPicker     = null;
  private AttributeSet        attrs        = null;
  private int                 currentValue = 0;
  private int                 defaultValue = 50;

  /**
   * 
   * Private Klasse (nach Android Developers) zum sichern des aktuellen Status
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 29.12.2012
   */
  private static class SavedState extends BaseSavedState
  {
    // Member that holds the setting's value
    // Change this data type to match the type saved by your Preference
    int value;

    public SavedState( Parcelable superState )
    {
      super( superState );
    }

    public SavedState( Parcel source )
    {
      super( source );
      // Get the current preference's value
      value = source.readInt();
    }

    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
      super.writeToParcel( dest, flags );
      // Write the preference's value
      dest.writeInt( value );
    }

    // Standard creator object using an instance of this class
    @SuppressWarnings( "unused" )
    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
                                                                 @Override
                                                                 public SavedState createFromParcel( Parcel in )
                                                                 {
                                                                   return new SavedState( in );
                                                                 }

                                                                 @Override
                                                                 public SavedState[] newArray( int size )
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
  public GasPickerPreference( Context context, AttributeSet attrs )
  {
    super( context, attrs );
    this.attrs = attrs;
    setPositiveButtonText( R.string.conf_deco_gradient_button_positive );
    setNegativeButtonText( R.string.conf_deco_gradient_button_negative );
    setDialogIcon( null );
  }

  /**
   * alternativer Konstruktor mit Style-Attributen
   * 
   * @author Dirk Marciniak 28.12.2012
   * @param context
   * @param attrs
   * @param defStyle
   */
  public GasPickerPreference( Context context, AttributeSet attrs, int defStyle )
  {
    super( context, attrs, defStyle );
    this.attrs = attrs;
    setPositiveButtonText( R.string.conf_deco_gradient_button_positive );
    setNegativeButtonText( R.string.conf_deco_gradient_button_negative );
    setDialogIcon( null );
  }

  @Override
  public void onValueChange( NumberPicker picker, int oldVal, int newVal )
  {
    Log.v( TAG, "onValueChange: oldVal: <" + oldVal + ">, newVal: <" + newVal + ">" );
    currentValue = newVal;
  }

  @Override
  protected View onCreateDialogView()
  {
    super.onCreateDialogView();
    Log.v( TAG, "onCreateDialogView()..." );
    O2Picker = new NumberPicker( getContext(), this.attrs );
    O2Picker.setOnValueChangedListener( this );
    O2Picker.setMinValue( 0 );
    O2Picker.setMaxValue( 100 );
    O2Picker.setWrapSelectorWheel( false );
    this.currentValue = getPersistedInt( defaultValue );
    O2Picker.setValue( this.currentValue );
    return O2Picker;
  }

  @Override
  protected void onSetInitialValue( boolean restoreValue, Object def )
  {
    super.onSetInitialValue( restoreValue, def );
    Log.v( TAG, "onSetInitialValue: restore:<" + restoreValue + ">" );
    if( restoreValue )
    {
      this.currentValue = getPersistedInt( this.defaultValue );
      if( O2Picker != null )
      {
        O2Picker.setValue( this.currentValue );
      }
    }
    else
    {
      persistInt( this.currentValue );
    }
  }

  @Override
  protected Object onGetDefaultValue( TypedArray a, int index )
  {
    int retValue = 10;
    String defaultString = null;
    //
    super.onGetDefaultValue( a, index );
    Log.v( TAG, "onGetDefaultValue()..." );
    try
    {
      //
      // versuche aus einer Stringresource einen defaultwert zu machen
      //
      Log.v( TAG, "onGetDefaultValue...try read string resource..." );
      defaultString = a.getString( index );
      // nur, wenn da ein String zu finden war...
      if( defaultString != null )
      {
        retValue = Integer.parseInt( defaultString );
      }
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "onGetDefaultValue: wrong string: <" + defaultString + "> it's not an integer representation." );
    }
    catch( Exception ex )
    {
      Log.e( TAG, "onGetDefaultValue: <" + ex.getLocalizedMessage() + ">" );
    }
    finally
    {
      defaultValue = retValue;
    }
    return( retValue );
  }

  /**
   * Called when the dialog is closed. If the close was by pressing "OK" it saves the value.
   */
  @Override
  protected void onDialogClosed( boolean shouldSave )
  {
    super.onDialogClosed( shouldSave );
    Log.v( TAG, "onDialogClosed()..." );
    if( shouldSave )
    {
      Log.v( TAG, "onDialogClosed: should save..." );
      if( O2Picker != null )
      {
        currentValue = O2Picker.getValue();
      }
      Log.v( TAG, "onDialogClosed: save value <" + currentValue + ">..." );
      persistInt( currentValue );
    }
  }

  @Override
  protected Parcelable onSaveInstanceState()
  {
    super.onSaveInstanceState();
    final Parcelable superState = super.onSaveInstanceState();
    //
    Log.v( TAG, "onSaveInstanceState()..." );
    // Check whether this Preference is persistent (continually saved)
    if( isPersistent() )
    {
      // No need to save instance state since it's persistent, use superclass state
      return superState;
    }
    // Create instance of custom BaseSavedState
    final SavedState myState = new SavedState( superState );
    // Set the state's value with the class member that holds current setting value
    myState.value = currentValue;
    return myState;
  }

  @Override
  protected void onRestoreInstanceState( Parcelable state )
  {
    super.onRestoreInstanceState( state );
    Log.v( TAG, "onRestoreInstanceState()..." );
    // Check whether we saved the state in onSaveInstanceState
    if( state == null || !state.getClass().equals( SavedState.class ) )
    {
      // Didn't save the state, so call superclass
      super.onRestoreInstanceState( state );
      return;
    }
    // Cast state to custom BaseSavedState and pass to superclass
    SavedState myState = ( SavedState )state;
    super.onRestoreInstanceState( myState.getSuperState() );
    // Set this Preference's widget to reflect the restored state
    O2Picker.setValue( myState.value );
  }
}
