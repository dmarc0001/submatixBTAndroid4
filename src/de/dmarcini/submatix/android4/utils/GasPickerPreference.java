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
  private static final String TAG                = GasPickerPreference.class.getSimpleName();
  private NumberPicker        o2Picker           = null;
  private NumberPicker        hePicker           = null;
  private NumberPicker        n2Picker           = null;
  private int                 o2Current          = 0;
  private int                 heCurrent          = 0;
  private int                 n2Current          = 0;
  private AttributeSet        attrs              = null;
  private static String       defaultReturnValue = "100:0:0";

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
    String value;

    public SavedState( Parcelable superState )
    {
      super( superState );
    }

    public SavedState( Parcel source )
    {
      super( source );
      // Get the current preference's value
      value = source.readString();
    }

    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
      super.writeToParcel( dest, flags );
      // Write the preference's value
      dest.writeString( value );
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
    setPositiveButtonText( R.string.conf_gaslist_button_positive );
    setNegativeButtonText( R.string.conf_gaslist_button_negative );
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
    setPositiveButtonText( R.string.conf_gaslist_button_positive );
    setNegativeButtonText( R.string.conf_gaslist_button_negative );
    setDialogIcon( null );
  }

  @Override
  public void onValueChange( NumberPicker picker, int oldVal, int newVal )
  {
    int id = picker.getId();
    //
    switch ( id )
    {
      case R.id.o2NumberPicker:
        Log.v( TAG, "onValueChange: O2 oldVal: <" + oldVal + ">, newVal: <" + newVal + ">" );
        o2Current = newVal;
        break;
      //
      case R.id.heNumberPicker:
        Log.v( TAG, "onValueChange: HE oldVal: <" + oldVal + ">, newVal: <" + newVal + ">" );
        heCurrent = newVal;
        break;
      //
      case R.id.n2NumberPicker:
        Log.v( TAG, "onValueChange: N2 oldVal: <" + oldVal + ">, newVal: <" + newVal + ">" );
        n2Current = newVal;
        break;
      //
      default:
        Log.e( TAG, "onValueChange: unknown event source! call programmer!" );
    }
  }

  @Override
  protected View onCreateDialogView()
  {
    Log.v( TAG, "onCreateDialogView()..." );
    setDialogLayoutResource( R.layout.gas_picker_layout );
    return super.onCreateDialogView();
  }

  @Override
  protected void onBindDialogView( View v )
  {
    Log.v( TAG, "onBindDialogView()..." );
    //
    // O2 Picker initialisieren
    //
    o2Picker = ( NumberPicker )v.findViewById( R.id.o2NumberPicker );
    o2Picker.setOnValueChangedListener( this );
    o2Picker.setMinValue( 0 );
    o2Picker.setMaxValue( 100 );
    o2Picker.setValue( o2Current );
    o2Picker.setWrapSelectorWheel( false );
    //
    // he Picker initialisieren
    //
    hePicker = ( NumberPicker )v.findViewById( R.id.heNumberPicker );
    hePicker.setOnValueChangedListener( this );
    hePicker.setMinValue( 0 );
    hePicker.setMaxValue( 100 );
    hePicker.setValue( heCurrent );
    hePicker.setWrapSelectorWheel( false );
    //
    // n2 Picker initialisieren
    //
    n2Picker = ( NumberPicker )v.findViewById( R.id.n2NumberPicker );
    n2Picker.setOnValueChangedListener( this );
    n2Picker.setMinValue( 0 );
    n2Picker.setMaxValue( 100 );
    n2Picker.setValue( n2Current );
    n2Picker.setWrapSelectorWheel( false );
    Log.v( TAG, "onBindDialogView()...OK" );
  }

  @Override
  protected void onSetInitialValue( boolean restoreValue, Object def )
  {
    String defaultValueStr = "100:0:0";
    //
    super.onSetInitialValue( restoreValue, def );
    Log.d( TAG, "onSetInitialValue: restore:<" + restoreValue + ">" );
    if( restoreValue )
    {
      // es soll restored werden
      try
      {
        defaultValueStr = getPersistedString( defaultReturnValue );
      }
      catch( Exception ex )
      {
        Log.e( TAG, "Ops, an exception.... Was saved an other type of content?" );
        defaultValueStr = defaultReturnValue;
      }
      makeValuesFromString( defaultValueStr );
    }
    else
    {
      makeValuesFromString( defaultReturnValue );
    }
  }

  /**
   * 
   * Mach aus dem Parameterstring die Werte für die Gasanteile
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   * @param defaultValueStr
   * @return ja oder nicht
   */
  private boolean makeValuesFromString( String defaultValueStr )
  {
    Log.d( TAG, "makeValuesFromString: String to split <" + defaultValueStr + ">" );
    String fields[] = defaultValueStr.split( ":" );
    if( ( fields != null ) && ( fields.length == 3 ) )
    {
      Log.d( TAG, String.format( "makeValuesFromString: <%s> <%s> <%s>", fields[0], fields[1], fields[2] ) );
      Log.d( TAG, "makeValuesFromString: successful split default value!" );
      try
      {
        o2Current = Integer.parseInt( fields[0] );
        heCurrent = Integer.parseInt( fields[1] );
        n2Current = Integer.parseInt( fields[2] );
        Log.d( TAG, "makeValuesFromString: successful set Values" );
        return( true );
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAG, "makeValuesFromString: <" + ex.getLocalizedMessage() + ">" );
        return( false );
      }
    }
    else
    {
      Log.w( TAG, "makeValuesFromString: not correct default Value loadet (" + defaultValueStr + ")" );
    }
    return( false );
  }

  @Override
  protected Object onGetDefaultValue( TypedArray a, int index )
  {
    // int retValue = 10;
    String defaultString = null;
    //
    super.onGetDefaultValue( a, index );
    Log.d( TAG, "onGetDefaultValue()..." );
    //
    // versuche aus einer Stringresource einen defaultwert zu machen
    //
    Log.d( TAG, "onGetDefaultValue:...try read string resource..." );
    defaultString = a.getString( index );
    if( defaultString != null )
    {
      defaultReturnValue = defaultString;
    }
    Log.d( TAG, "onGetDefaultValue: defaultString<" + defaultString + ">" );
    return( defaultString );
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
      persistString( String.format( "%d:%d:%d", o2Picker.getValue(), hePicker.getValue(), n2Picker.getValue() ) );
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
    myState.value = String.format( "%d:%d:%d", o2Picker.getValue(), hePicker.getValue(), n2Picker.getValue() );
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
    makeValuesFromString( myState.value );
    try
    {
      o2Picker.setValue( o2Current );
      hePicker.setValue( heCurrent );
      n2Picker.setValue( n2Current );
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "onRestoreInstanceState: NumberPicker was not initialized yet." );
    }
  }
}
