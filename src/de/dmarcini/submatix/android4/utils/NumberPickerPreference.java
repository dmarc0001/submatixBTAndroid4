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
 * Eigene Klasse zum Einstellen einer Numnmer
 * 
 * @author dmarc
 */
public class NumberPickerPreference extends DialogPreference implements OnValueChangeListener
{
  private static final String TAG          = NumberPickerPreference.class.getSimpleName();
  private NumberPicker        nPicker      = null;
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
  public NumberPickerPreference( Context context, AttributeSet attrs )
  {
    super( context, attrs );
    this.attrs = attrs;
    setPositiveButtonText( R.string.config_decompression_gradient_button_positive );
    setNegativeButtonText( R.string.config_decompression_gradient_button_negative );
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
  public NumberPickerPreference( Context context, AttributeSet attrs, int defStyle )
  {
    super( context, attrs, defStyle );
    this.attrs = attrs;
    setPositiveButtonText( R.string.config_decompression_gradient_button_positive );
    setNegativeButtonText( R.string.config_decompression_gradient_button_negative );
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
    Log.v( TAG, "onCreateDialogView()..." );
    nPicker = new NumberPicker( getContext(), this.attrs );
    nPicker.setOnValueChangedListener( this );
    nPicker.setMinValue( 0 );
    nPicker.setMaxValue( 100 );
    this.currentValue = getPersistedInt( defaultValue );
    nPicker.setValue( this.currentValue );
    return nPicker;
  }

  @Override
  protected void onSetInitialValue( boolean restoreValue, Object def )
  {
    Log.v( TAG, "onSetInitialValue: restore:<" + restoreValue + ">" );
    if( restoreValue )
    {
      this.currentValue = getPersistedInt( this.defaultValue );
      if( nPicker != null )
      {
        nPicker.setValue( this.currentValue );
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
    this.defaultValue = a.getInteger( index, defaultValue );
    Log.v( TAG, "onGetDefaultValue: <" + defaultValue + ">" );
    return( defaultValue );
  }

  /**
   * Called when the dialog is closed. If the close was by pressing "OK" it saves the value.
   */
  @Override
  protected void onDialogClosed( boolean shouldSave )
  {
    Log.v( TAG, "onDialogClosed()..." );
    if( shouldSave )
    {
      if( nPicker != null )
      {
        currentValue = nPicker.getValue();
      }
      Log.v( TAG, "onDialogClosed: save value <" + currentValue + ">..." );
      persistInt( currentValue );
    }
  }

  @Override
  protected Parcelable onSaveInstanceState()
  {
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
    nPicker.setValue( myState.value );
  }
}
