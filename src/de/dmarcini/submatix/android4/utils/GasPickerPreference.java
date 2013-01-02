package de.dmarcini.submatix.android4.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
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
public class GasPickerPreference extends DialogPreference implements OnValueChangeListener, OnCheckedChangeListener
{
  private static final String TAG                = GasPickerPreference.class.getSimpleName();
  private boolean             noAction           = false;
  private NumberPicker        o2Picker           = null;
  private NumberPicker        hePicker           = null;
  private NumberPicker        n2Picker           = null;
  private CheckBox            d1Checkbox         = null;
  private CheckBox            d2Checkbox         = null;
  private CheckBox            bailoutCheckbox    = null;
  private int                 o2Current          = 0;
  private int                 heCurrent          = 0;
  private int                 n2Current          = 0;
  private boolean             d1Current          = false;
  private boolean             d2Current          = false;
  private boolean             bailoutCurrent     = false;
  private EditText            o2EditText         = null;
  private EditText            heEditText         = null;
  private static String       defaultReturnValue = "100:0:0:0:0:0";

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
   * 
   * private Klase zur formatierung der Zahlen auf mindestens zweistellig
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.01.2013
   */
  @SuppressLint( "DefaultLocale" )
  private class TwoDigitFormatter implements Formatter
  {
    @Override
    public String format( int value )
    {
      return( String.format( "%02d", value ) );
    }
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
    Log.d( TAG, "GasPickerPreference(Context,AttributeSet)..." );
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
    Log.d( TAG, "GasPickerPreference(Context,AttributeSet,int)..." );
    setPositiveButtonText( R.string.conf_gaslist_button_positive );
    setNegativeButtonText( R.string.conf_gaslist_button_negative );
    setDialogIcon( null );
  }

  @Override
  public void onValueChange( NumberPicker picker, int oldVal, int newVal )
  {
    Log.d( TAG, "onValueChange()..." );
    int id = picker.getId();
    if( noAction ) return;
    //
    switch ( id )
    {
      case R.id.o2NumberPicker:
        Log.d( TAG, "onValueChange: O2 oldVal: <" + oldVal + ">, newVal: <" + newVal + ">" );
        onSetO2Value( newVal );
        setO2PickerColor( o2Current );
        setHePickerColor( heCurrent );
        break;
      //
      case R.id.heNumberPicker:
        Log.d( TAG, "onValueChange: HE oldVal: <" + oldVal + ">, newVal: <" + newVal + ">" );
        onSetHeValue( newVal );
        setHePickerColor( heCurrent );
        setO2PickerColor( o2Current );
        break;
      //
      case R.id.n2NumberPicker:
        Log.d( TAG, "onValueChange: N2 oldVal: <" + oldVal + ">, newVal: <" + newVal + ">" );
        // eh, das stellen wir nicht um!
        n2Current = oldVal;
        setPickerWoEvent();
        break;
      //
      default:
        Log.e( TAG, "onValueChange: unknown event source! call programmer!" );
    }
  }

  /**
   * 
   * Wenn der user am Helium rumdreht
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   */
  private void onSetHeValue( int newVal )
  {
    // ist der neue Heliumwert 0
    if( newVal == 0 )
    {
      heCurrent = 0;
      o2Current = 100 - n2Current;
    }
    // wenn der Wert gestiegen ist
    else if( newVal > heCurrent )
    {
      // wenn noch Stickstoff zum verringern vorhanden ist
      if( n2Current > ( newVal - heCurrent ) )
      {
        heCurrent = newVal;
        n2Current = 100 - heCurrent - o2Current;
      }
      else
      {
        n2Current = 0;
        heCurrent = 100 - o2Current;
      }
    }
    // wenn der Wert gesunken ist (weniger Helium)
    else
    {
      // ich ersetzte das Helium mit Stickstoff
      heCurrent = newVal;
      n2Current = 100 - o2Current - heCurrent;
    }
    setPickerWoEvent();
  }

  /**
   * 
   * Was passiert, wenn der Sauerstoff verändert wird
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   * @param newVal
   * @param oldVal
   */
  private void onSetO2Value( int newVal )
  {
    // wenn das bei 100 angekommen ist
    if( newVal >= 100 )
    {
      heCurrent = 0;
      n2Current = 0;
      o2Current = 100;
    }
    // wenn es mehr O2 geworden ist
    else if( newVal > o2Current )
    {
      // wenn noch Stickstoff zum entfernen vorhanden ist
      if( n2Current >= ( newVal - o2Current ) )
      {
        o2Current = newVal;
        n2Current = 100 - o2Current - heCurrent;
      }
      else
      {
        o2Current = newVal;
        n2Current = 0;
        heCurrent = 100 - o2Current;
      }
    }
    else
    {
      // es ist also weniger O2 geworden
      if( newVal <= 1 )
      {
        o2Current = 1;
      }
      else
      {
        o2Current = newVal;
      }
      n2Current = 100 - o2Current - heCurrent;
    }
    // setze die Werte in der Anzeige
    setPickerWoEvent();
  }

  /**
   * 
   * Setze die Spinner neu, aber ignoriere events währendessen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   */
  private void setPickerWoEvent()
  {
    Log.d( TAG, "setPickerWoEvent()..." );
    this.noAction = true;
    o2Picker.setValue( o2Current );
    hePicker.setValue( heCurrent );
    n2Picker.setValue( n2Current );
    this.noAction = false;
    Log.d( TAG, "setPickerWoEvent()...OK" );
  }

  @Override
  protected View onCreateDialogView()
  {
    Log.d( TAG, "onCreateDialogView()..." );
    setDialogLayoutResource( R.layout.gas_picker_layout );
    return super.onCreateDialogView();
  }

  @Override
  protected void onBindDialogView( View v )
  {
    Log.d( TAG, "onBindDialogView()..." );
    noAction = true;
    //
    // O2 Picker initialisieren
    //
    o2Picker = ( NumberPicker )v.findViewById( R.id.o2NumberPicker );
    o2Picker.setFormatter( new TwoDigitFormatter() );
    o2Picker.setOnValueChangedListener( this );
    o2Picker.setMinValue( 0 );
    o2Picker.setMaxValue( 100 );
    o2Picker.setWrapSelectorWheel( false );
    //
    // he Picker initialisieren
    //
    hePicker = ( NumberPicker )v.findViewById( R.id.heNumberPicker );
    hePicker.setFormatter( new TwoDigitFormatter() );
    hePicker.setOnValueChangedListener( this );
    hePicker.setMinValue( 0 );
    hePicker.setMaxValue( 100 );
    hePicker.setWrapSelectorWheel( false );
    //
    // n2 Picker initialisieren
    //
    n2Picker = ( NumberPicker )v.findViewById( R.id.n2NumberPicker );
    n2Picker.setFormatter( new TwoDigitFormatter() );
    n2Picker.setOnValueChangedListener( this );
    n2Picker.setMinValue( 0 );
    n2Picker.setMaxValue( 100 );
    n2Picker.setWrapSelectorWheel( false );
    //
    // Checkboxen für Gasdefinition benennen
    //
    d1Checkbox = ( CheckBox )v.findViewById( R.id.diluent1CheckBox );
    d1Checkbox.setChecked( d1Current );
    d1Checkbox.setOnCheckedChangeListener( this );
    d2Checkbox = ( CheckBox )v.findViewById( R.id.diluent2CheckBox );
    d2Checkbox.setChecked( d2Current );
    d2Checkbox.setOnCheckedChangeListener( this );
    bailoutCheckbox = ( CheckBox )v.findViewById( R.id.bailoutCheckBox );
    bailoutCheckbox.setChecked( bailoutCurrent );
    bailoutCheckbox.setOnCheckedChangeListener( this );
    //
    // setzte initial die Farben der picker
    //
    setO2PickerColor( o2Current );
    setHePickerColor( heCurrent );
    noAction = true;
    onSetO2Value( o2Current );
    Log.d( TAG, "onBindDialogView()...OK" );
  }

  /**
   * 
   * Setze die Farbe des Heliumpickers nach Heliumgehalt
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.01.2013
   * @param he
   */
  private void setHePickerColor( int he )
  {
    int index = 0;
    int color;
    int rest;
    //
    // wenn o2EditText nicht da ist...
    //
    if( this.heEditText == null )
    {
      for( index = 0; index < hePicker.getChildCount(); index++ )
      {
        if( hePicker.getChildAt( index ) instanceof EditText )
        {
          this.heEditText = ( EditText )hePicker.getChildAt( index );
          heEditText.setFocusable( false );
          break;
        }
      }
    }
    //
    // Na dann...
    //
    if( this.heEditText != null )
    {
      // je mehr he desto grüner
      // also desto weniger blau and red
      // also he vom weiss abziehen
      rest = 200 - ( he * 2 );
      color = 0xff00ff00 | ( rest << 16 ) | rest;
      heEditText.setTextColor( color );
      heEditText.setHintTextColor( color );
    }
  }

  /**
   * 
   * Setze die Farbe des Sauerstoffpickers nach Sauerstoffgehelt
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.01.2013
   * @param o2
   */
  private void setO2PickerColor( int o2 )
  {
    int index = 0;
    int color;
    int rest;
    //
    // wenn o2EditText nicht da ist...
    //
    if( this.o2EditText == null )
    {
      for( index = 0; index < o2Picker.getChildCount(); index++ )
      {
        if( o2Picker.getChildAt( index ) instanceof EditText )
        {
          this.o2EditText = ( EditText )o2Picker.getChildAt( index );
          o2EditText.setFocusable( false );
          break;
        }
      }
    }
    //
    // Na dann...
    //
    if( this.o2EditText != null )
    {
      // je mehr O2 desto blauer
      // also desto weniger green and red
      // also o2 vom weiss abziehen
      rest = 200 - ( o2 * 2 );
      color = 0xff0000ff | ( rest << 16 ) | ( rest << 8 );
      o2EditText.setTextColor( color );
      o2EditText.setHintTextColor( color );
    }
  }

  @Override
  protected void onSetInitialValue( boolean restoreValue, Object def )
  {
    String defaultValueStr;
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
    if( ( fields != null ) && ( fields.length >= 3 ) )
    {
      Log.d( TAG, String.format( "makeValuesFromString: <%s> <%s> <%s>", fields[0], fields[1], fields[2] ) );
      Log.d( TAG, "makeValuesFromString: successful split default value!" );
      try
      {
        o2Current = Integer.parseInt( fields[0] );
        heCurrent = Integer.parseInt( fields[1] );
        n2Current = Integer.parseInt( fields[2] );
        Log.d( TAG, "makeValuesFromString: successful set Values" );
        if( fields.length == 6 )
        {
          Log.d( TAG, "makeValuesFromString: found diluent and bailout markers..." );
          d1Current = Boolean.parseBoolean( fields[3] );
          d2Current = Boolean.parseBoolean( fields[4] );
          bailoutCurrent = Boolean.parseBoolean( fields[5] );
        }
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
    Log.d( TAG, "onGetDefaultValue:...try read string resource and index <" + index + ">..." );
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
      persistString( String.format( "%d:%d:%d:%b:%b:%b", o2Picker.getValue(), hePicker.getValue(), n2Picker.getValue(), d1Checkbox.isChecked(), d2Checkbox.isChecked(),
              bailoutCheckbox.isChecked() ) );
    }
  }

  @Override
  protected Parcelable onSaveInstanceState()
  {
    super.onSaveInstanceState();
    final Parcelable superState = super.onSaveInstanceState();
    //
    Log.d( TAG, "onSaveInstanceState()..." );
    // Check whether this Preference is persistent (continually saved)
    if( isPersistent() )
    {
      // No need to save instance state since it's persistent, use superclass state
      return superState;
    }
    // Create instance of custom BaseSavedState
    final SavedState myState = new SavedState( superState );
    // Set the state's value with the class member that holds current setting value
    myState.value = String.format( "%d:%d:%d:%b:%b:%b", o2Picker.getValue(), hePicker.getValue(), n2Picker.getValue(), d1Checkbox.isChecked(), d2Checkbox.isChecked(),
            bailoutCheckbox.isChecked() );
    return myState;
  }

  @Override
  protected void onRestoreInstanceState( Parcelable state )
  {
    super.onRestoreInstanceState( state );
    Log.d( TAG, "onRestoreInstanceState()..." );
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
      d1Checkbox.setChecked( d1Current );
      d2Checkbox.setChecked( d2Current );
      bailoutCheckbox.setChecked( bailoutCurrent );
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "onRestoreInstanceState: NumberPicker/Checkboxes was not initialized yet." );
    }
  }

  @Override
  public void onCheckedChanged( CompoundButton v, boolean isChecked )
  {
    if( v instanceof CheckBox )
    {
      CheckBox cb = ( CheckBox )v;
      switch ( cb.getId() )
      {
        case R.id.diluent1CheckBox:
          Log.d( TAG, "onCheckedChanged: diluent 1 <" + isChecked + ">" );
          d1Current = isChecked;
          if( isChecked && d2Checkbox.isChecked() )
          {
            d2Checkbox.setChecked( false );
          }
          break;
        //
        case R.id.diluent2CheckBox:
          Log.d( TAG, "onCheckedChanged: diluent 2 <" + isChecked + ">" );
          d2Current = isChecked;
          if( isChecked && d1Checkbox.isChecked() )
          {
            d1Checkbox.setChecked( false );
          }
          break;
        //
        case R.id.bailoutCheckBox:
          Log.d( TAG, "onCheckedChanged: bailout <" + isChecked + ">" );
          bailoutCurrent = isChecked;
          break;
        //
        default:
          Log.e( TAG, "onCheckedChanged: unknown event source! call programmer!" );
      }
    }
  }
}
