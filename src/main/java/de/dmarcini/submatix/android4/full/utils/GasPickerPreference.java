//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.submatix.android4.full.utils;

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
import android.widget.TextView;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.gui.MainActivity;

/**
 * Klasse für einen Dreifach-Numberpicker für Gasmischungen TRIMIX
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class GasPickerPreference extends DialogPreference implements OnValueChangeListener, OnCheckedChangeListener
{
  private static final String        TAG                = GasPickerPreference.class.getSimpleName();
  private static       String        defaultReturnValue = "100:0:0:0:0:0";
  private              boolean       noAction           = false;
  private              NumberPicker  o2Picker           = null;
  private              NumberPicker  hePicker           = null;
  private              NumberPicker  n2Picker           = null;
  private              CheckBox      d1Checkbox         = null;
  private              CheckBox      d2Checkbox         = null;
  private              CheckBox      bailoutCheckbox    = null;
  private              SPX42GasParms gasParms           = null;
  // private int gasParms.o2 = 0;
  // private int gasParms.he = 0;
  // private int gasParms.n2 = 0;
  // private boolean gasParms.d1 = false;
  // private boolean gasParms.d2 = false;
  // private boolean gasParms.bo = false;
  private              TextView      o2TextView         = null;
  private              TextView      heTextView         = null;
  private              String        gasTitle           = null;
  private              TextView      gasNameTextView    = null;
  private              int           currentStyleId     = R.style.AppDarkTheme;

  /**
   * Der Konstruktor
   *
   * @param context
   * @param attrs
   */
  public GasPickerPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "GasPickerPreference(Context,AttributeSet)...");
    }
    gasParms = new SPX42GasParms();
    setPositiveButtonText(R.string.conf_gaslist_button_positive);
    setNegativeButtonText(R.string.conf_gaslist_button_negative);
    setDialogIcon(null);
  }

  /**
   * alternativer Konstruktor mit Style-Attributen
   *
   * @param context
   * @param attrs
   * @param defStyle
   */
  public GasPickerPreference(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "GasPickerPreference(Context,AttributeSet,int)...");
    }
    gasParms = new SPX42GasParms();
    setPositiveButtonText(R.string.conf_gaslist_button_positive);
    setNegativeButtonText(R.string.conf_gaslist_button_negative);
    setDialogIcon(null);
  }

  /**
   * Mach aus dem Parameterstring die Werte für die Gasanteile
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 01.01.2013
   *
   * @param defaultValueStr
   * @return ja oder nicht
   */
  private boolean makeValuesFromString(String defaultValueStr)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "makeValuesFromString: String to split <" + defaultValueStr + ">");
    }
    String fields[] = defaultValueStr.split(":");
    if( (fields != null) && (fields.length >= 3) )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, String.format("makeValuesFromString: <%s> <%s> <%s>", fields[ 0 ], fields[ 1 ], fields[ 2 ]));
      }
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "makeValuesFromString: successful split default value!");
      }
      try
      {
        gasParms.o2 = Integer.parseInt(fields[ 0 ]);
        gasParms.he = Integer.parseInt(fields[ 1 ]);
        gasParms.n2 = Integer.parseInt(fields[ 2 ]);
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "makeValuesFromString: successful set Values");
        }
        if( fields.length == 6 )
        {
          if( BuildConfig.DEBUG )
          {
            Log.d(TAG, "makeValuesFromString: found diluent and bailout markers...");
          }
          gasParms.d1 = Boolean.parseBoolean(fields[ 3 ]);
          gasParms.d2 = Boolean.parseBoolean(fields[ 4 ]);
          gasParms.bo = Boolean.parseBoolean(fields[ 5 ]);
        }
        return (true);
      }
      catch( NumberFormatException ex )
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
  protected void onBindDialogView(View v)
  {
    int index;
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onBindDialogView()...");
    }
    noAction = true;
    gasNameTextView = ( TextView ) v.findViewById(R.id.GasNameTextView);
    if( gasNameTextView != null )
    {
      gasTitle = ( String ) getTitle();
      Log.i(TAG, "big screen found by dialog");
    }
    else
    {
      Log.i(TAG, "small screen found by dialog");
    }
    //
    // O2 Picker initialisieren
    //
    o2Picker = ( NumberPicker ) v.findViewById(R.id.o2NumberPicker);
    o2Picker.setFormatter(new TwoDigitFormatter());
    o2Picker.setOnValueChangedListener(this);
    o2Picker.setMinValue(0);
    o2Picker.setMaxValue(100);
    o2Picker.setWrapSelectorWheel(false);
    for( index = 0; index < o2Picker.getChildCount(); index++ )
    {
      if( o2Picker.getChildAt(index) instanceof EditText )
      {
        EditText o2EditText = ( EditText ) o2Picker.getChildAt(index);
        o2EditText.setClickable(false);
        o2EditText.setFocusable(false);
        o2EditText.setFocusableInTouchMode(false);
        break;
      }
    }
    //
    // he Picker initialisieren
    //
    hePicker = ( NumberPicker ) v.findViewById(R.id.heNumberPicker);
    hePicker.setFormatter(new TwoDigitFormatter());
    hePicker.setOnValueChangedListener(this);
    hePicker.setMinValue(0);
    hePicker.setMaxValue(100);
    hePicker.setWrapSelectorWheel(false);
    for( index = 0; index < hePicker.getChildCount(); index++ )
    {
      if( hePicker.getChildAt(index) instanceof EditText )
      {
        EditText heEditText = ( EditText ) hePicker.getChildAt(index);
        heEditText.setClickable(false);
        heEditText.setFocusable(false);
        heEditText.setFocusableInTouchMode(false);
        break;
      }
    }
    //
    // n2 Picker initialisieren
    //
    n2Picker = ( NumberPicker ) v.findViewById(R.id.n2NumberPicker);
    n2Picker.setFormatter(new TwoDigitFormatter());
    n2Picker.setOnValueChangedListener(this);
    n2Picker.setMinValue(0);
    n2Picker.setMaxValue(100);
    n2Picker.setWrapSelectorWheel(false);
    for( index = 0; index < n2Picker.getChildCount(); index++ )
    {
      if( n2Picker.getChildAt(index) instanceof EditText )
      {
        EditText n2EditText = ( EditText ) n2Picker.getChildAt(index);
        n2EditText.setClickable(false);
        n2EditText.setFocusable(false);
        n2EditText.setFocusableInTouchMode(false);
        break;
      }
    }
    //
    // Checkboxen für Gasdefinition benennen
    //
    d1Checkbox = ( CheckBox ) v.findViewById(R.id.diluent1CheckBox);
    d1Checkbox.setChecked(gasParms.d1);
    d1Checkbox.setOnCheckedChangeListener(this);
    d2Checkbox = ( CheckBox ) v.findViewById(R.id.diluent2CheckBox);
    d2Checkbox.setChecked(gasParms.d2);
    d2Checkbox.setOnCheckedChangeListener(this);
    bailoutCheckbox = ( CheckBox ) v.findViewById(R.id.bailoutCheckBox);
    bailoutCheckbox.setChecked(gasParms.bo);
    bailoutCheckbox.setOnCheckedChangeListener(this);
    //
    // Labels für Picker suchen
    //
    o2TextView = ( TextView ) v.findViewById(R.id.o2TextView);
    heTextView = ( TextView ) v.findViewById(R.id.heTextView);
    //
    // setzte initial die Farben der picker
    //
    setO2PickerColor(gasParms.o2);
    setHePickerColor(gasParms.he);
    noAction = true;
    onSetO2Value(gasParms.o2);
    setGasNameToTitle(gasParms.o2, gasParms.he);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onBindDialogView()...OK");
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton v, boolean isChecked)
  {
    if( v instanceof CheckBox )
    {
      CheckBox cb = ( CheckBox ) v;
      switch( cb.getId() )
      {
        case R.id.diluent1CheckBox:
          if( BuildConfig.DEBUG )
          {
            Log.d(TAG, "onCheckedChanged: diluent 1 <" + isChecked + ">");
          }
          gasParms.d1 = isChecked;
          if( isChecked && d2Checkbox.isChecked() )
          {
            d2Checkbox.setChecked(false);
          }
          break;
        //
        case R.id.diluent2CheckBox:
          if( BuildConfig.DEBUG )
          {
            Log.d(TAG, "onCheckedChanged: diluent 2 <" + isChecked + ">");
          }
          gasParms.d2 = isChecked;
          if( isChecked && d1Checkbox.isChecked() )
          {
            d1Checkbox.setChecked(false);
          }
          break;
        //
        case R.id.bailoutCheckBox:
          if( BuildConfig.DEBUG )
          {
            Log.d(TAG, "onCheckedChanged: bailout <" + isChecked + ">");
          }
          gasParms.bo = isChecked;
          break;
        //
        default:
          Log.e(TAG, "onCheckedChanged: unknown event source! call programmer!");
      }
    }
  }

  @Override
  protected View onCreateDialogView()
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreateDialogView()...");
    }
    setDialogLayoutResource(R.layout.gas_picker_layout);
    currentStyleId = MainActivity.getAppStyle();
    return super.onCreateDialogView();
  }

  /**
   * Called when the dialog is closed. If the close was by pressing "OK" it saves the value.
   */
  @Override
  protected void onDialogClosed(boolean shouldSave)
  {
    super.onDialogClosed(shouldSave);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onDialogClosed()...");
    }
    if( shouldSave )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "onDialogClosed: should save...");
      }
      persistString(String.format("%d:%d:%d:%b:%b:%b", o2Picker.getValue(), hePicker.getValue(), n2Picker.getValue(), d1Checkbox.isChecked(), d2Checkbox.isChecked(), bailoutCheckbox.isChecked()));
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index)
  {
    // int retValue = 10;
    String defaultString = null;
    //
    super.onGetDefaultValue(a, index);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onGetDefaultValue()...");
    }
    //
    // versuche aus einer Stringresource einen defaultwert zu machen
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onGetDefaultValue:...try read string resource and index <" + index + ">...");
    }
    defaultString = a.getString(index);
    if( defaultString != null )
    {
      defaultReturnValue = defaultString;
    }
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onGetDefaultValue: defaultString<" + defaultString + ">");
    }
    return (defaultString);
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state)
  {
    super.onRestoreInstanceState(state);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onRestoreInstanceState()...");
    }
    // Check whether we saved the state in onSaveInstanceState
    if( state == null || !state.getClass().equals(SavedState.class) )
    {
      // Didn't save the state, so call superclass
      super.onRestoreInstanceState(state);
      return;
    }
    // Cast state to custom BaseSavedState and pass to superclass
    SavedState myState = ( SavedState ) state;
    super.onRestoreInstanceState(myState.getSuperState());
    // Set this Preference's widget to reflect the restored state
    makeValuesFromString(myState.value);
    try
    {
      o2Picker.setValue(gasParms.o2);
      hePicker.setValue(gasParms.he);
      n2Picker.setValue(gasParms.n2);
      d1Checkbox.setChecked(gasParms.d1);
      d2Checkbox.setChecked(gasParms.d2);
      bailoutCheckbox.setChecked(gasParms.bo);
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "onRestoreInstanceState: NumberPicker/Checkboxes was not initialized yet.");
    }
  }

  @Override
  protected Parcelable onSaveInstanceState()
  {
    super.onSaveInstanceState();
    final Parcelable superState = super.onSaveInstanceState();
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onSaveInstanceState()...");
    }
    // Check whether this Preference is persistent (continually saved)
    if( isPersistent() )
    {
      // No need to save instance state since it's persistent, use superclass state
      return superState;
    }
    // Create instance of custom BaseSavedState
    final SavedState myState = new SavedState(superState);
    // Set the state's value with the class member that holds current setting value
    myState.value = String.format("%d:%d:%d:%b:%b:%b", gasParms.o2, gasParms.he, gasParms.n2, gasParms.d1, gasParms.d2, gasParms.bo);
    return myState;
  }

  /**
   * Wenn der user am Helium rumdreht
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 01.01.2013
   */
  private void onSetHeValue(int newVal)
  {
    // ist der neue Heliumwert 0
    if( newVal == 0 )
    {
      gasParms.he = 0;
      gasParms.o2 = 100 - gasParms.n2;
    }
    // wenn der Wert gestiegen ist
    else if( newVal > gasParms.he )
    {
      // wenn noch Stickstoff zum verringern vorhanden ist
      if( gasParms.n2 > (newVal - gasParms.he) )
      {
        gasParms.he = newVal;
        gasParms.n2 = 100 - gasParms.he - gasParms.o2;
      }
      else
      {
        gasParms.n2 = 0;
        gasParms.he = 100 - gasParms.o2;
      }
    }
    // wenn der Wert gesunken ist (weniger Helium)
    else
    {
      // ich ersetzte das Helium mit Stickstoff
      gasParms.he = newVal;
      gasParms.n2 = 100 - gasParms.o2 - gasParms.he;
    }
    setPickerWoEvent();
  }

  @Override
  protected void onSetInitialValue(boolean restoreValue, Object def)
  {
    String defaultValueStr;
    //
    super.onSetInitialValue(restoreValue, def);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onSetInitialValue: restore:<" + restoreValue + ">");
    }
    if( restoreValue )
    {
      // es soll restored werden
      try
      {
        defaultValueStr = getPersistedString(defaultReturnValue);
      }
      catch( Exception ex )
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
   * Was passiert, wenn der Sauerstoff verändert wird
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 01.01.2013
   *
   * @param newVal
   */
  private void onSetO2Value(int newVal)
  {
    // wenn das bei 100 angekommen ist
    if( newVal >= 100 )
    {
      gasParms.he = 0;
      gasParms.n2 = 0;
      gasParms.o2 = 100;
    }
    // wenn es mehr O2 geworden ist
    else if( newVal > gasParms.o2 )
    {
      // wenn noch Stickstoff zum entfernen vorhanden ist
      if( gasParms.n2 >= (newVal - gasParms.o2) )
      {
        gasParms.o2 = newVal;
        gasParms.n2 = 100 - gasParms.o2 - gasParms.he;
      }
      else
      {
        gasParms.o2 = newVal;
        gasParms.n2 = 0;
        gasParms.he = 100 - gasParms.o2;
      }
    }
    else
    {
      // es ist also weniger O2 geworden
      if( newVal <= 1 )
      {
        gasParms.o2 = 1;
      }
      else
      {
        gasParms.o2 = newVal;
      }
      gasParms.n2 = 100 - gasParms.o2 - gasParms.he;
    }
    // setze die Werte in der Anzeige
    setPickerWoEvent();
  }

  @Override
  public void onValueChange(NumberPicker picker, int oldVal, int newVal)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onValueChange()...");
    }
    int id = picker.getId();
    if( noAction )
    {
      return;
    }
    //
    switch( id )
    {
      case R.id.o2NumberPicker:
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onValueChange: O2 oldVal: <" + oldVal + ">, newVal: <" + newVal + ">");
        }
        onSetO2Value(newVal);
        setO2PickerColor(gasParms.o2);
        setHePickerColor(gasParms.he);
        break;
      //
      case R.id.heNumberPicker:
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onValueChange: HE oldVal: <" + oldVal + ">, newVal: <" + newVal + ">");
        }
        onSetHeValue(newVal);
        setHePickerColor(gasParms.he);
        setO2PickerColor(gasParms.o2);
        break;
      //
      case R.id.n2NumberPicker:
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onValueChange: N2 oldVal: <" + oldVal + ">, newVal: <" + newVal + ">");
        }
        // eh, das stellen wir nicht um!
        gasParms.n2 = oldVal;
        setPickerWoEvent();
        break;
      //
      default:
        Log.e(TAG, "onValueChange: unknown event source! call programmer!");
        return;
    }
    setGasNameToTitle(gasParms.o2, gasParms.he);
  }

  /**
   * Gasnamen in titel setzen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 31.01.2013
   *
   * @param o2Current2
   * @param heCurrent2
   */
  private void setGasNameToTitle(int o2Current2, int heCurrent2)
  {
    if( gasNameTextView != null )
    {
      gasNameTextView.setText(String.format("%s <%s>", gasTitle, GasUtilitys.getNameForGas(o2Current2, heCurrent2)));
    }
  }

  /**
   * Setze die Farbe des Heliumpickers nach Heliumgehalt
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 02.01.2013
   *
   * @param he
   */
  private void setHePickerColor(int he)
  {
    int color;
    int rest;
    //
    // Na dann...
    //
    if( this.heTextView != null )
    {
      if( currentStyleId == R.style.AppDarkTheme )
      {
        // je mehr he desto grüner
        // also desto weniger blau and red
        // also he vom weiss abziehen
        rest = 200 - (he * 2);
        color = 0xff00c800 | (rest << 16) | 200 - he;
      }
      else
      {
        // anfangen mit schwarz, immer mehr grün...
        rest = (he * 2) + 54;
        color = 0xff300000 | (rest << 8) | he + 60;
      }
      heTextView.setTextColor(color);
      heTextView.setHintTextColor(color);
    }
  }

  /**
   * Setze die Farbe des Sauerstoffpickers nach Sauerstoffgehelt
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 02.01.2013
   *
   * @param o2
   */
  private void setO2PickerColor(int o2)
  {
    int color;
    int rest;
    //
    // Na dann...
    //
    if( this.o2TextView != null )
    {
      //
      // unterscheide noch nach Sauerstoff normoxisch/non-Normoxisch
      if( o2 > 20 )
      {
        // also NORMOXISCH
        if( currentStyleId == R.style.AppDarkTheme )
        {
          // je mehr O2 desto blauer
          // also desto weniger green and red
          // also o2 vom weiss abziehen
          rest = 144 - Math.round((o2 - 21) * 1.8F);
          color = 0xff0000ff | (rest << 16) | (rest << 8);
        }
        else
        {
          // anfangen mit schwarz, immer mehr blau...
          rest = o2 - 21;
          color = 0xff000000 | Math.round(rest * 2.11F) << 16 | Math.round(rest * 2.11F) << 16 | 156 + Math.round(rest * 0.86F);
        }
      }
      else
      {
        // also NICHT NORMOXISCH
        if( currentStyleId == R.style.AppDarkTheme )
        {
          // je weniger O2 desto roter
          // Bereich 0..20
          // Blau 00 ..0x90
          // rot 00 -- 0xff
          color = 0xff000000 | ((0xff - Math.round(o2 * 5.05F)) << 16) | Math.round(o2 * 7.2F) << 8 | Math.round(o2 * 12.7F);
        }
        else
        {
          // anfangen mit blau immer mehr rot
          color = 0xff000000 | (0xff - Math.round(o2 * 8.35F)) << 16 | Math.round(02 * 3.8F) << 8 | Math.round(o2 * 7.8F);
        }
      }
      o2TextView.setTextColor(color);
      o2TextView.setHintTextColor(color);
    }
  }

  /**
   * Setze die Spinner neu, aber ignoriere events währendessen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 01.01.2013
   */
  private void setPickerWoEvent()
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "setPickerWoEvent()...");
    }
    this.noAction = true;
    o2Picker.setValue(gasParms.o2);
    hePicker.setValue(gasParms.he);
    n2Picker.setValue(gasParms.n2);
    this.noAction = false;
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "setPickerWoEvent()...OK");
    }
  }

  /**
   * Setze den Wert für diese Preference
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   *
   * @param gasParms
   */
  public void setValue(final SPX42GasParms gasParms)
  {
    this.gasParms.o2 = gasParms.o2;
    this.gasParms.he = gasParms.he;
    this.gasParms.n2 = gasParms.n2;
    this.gasParms.d1 = gasParms.d1;
    this.gasParms.d2 = gasParms.d2;
    this.gasParms.bo = gasParms.bo;
  }

  /**
   * Gib die Gasparameter als Objekt zurück
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   *
   * @return SPX42GasParms
   */
  public SPX42GasParms getValue()
  {
    return (gasParms);
  }

  /**
   * Setze Wert für diese Preference als String
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   *
   * @param theValueStr
   */
  public void setValue(final String theValueStr)
  {
    makeValuesFromString(theValueStr);
  }

  /**
   * Private Klasse (nach Android Developers) zum sichern des aktuellen Status
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 29.12.2012
   */
  private static class SavedState extends BaseSavedState
  {
    // Standard creator object using an instance of this class
    @SuppressWarnings( "unused" )
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
        return new SavedState[ size ];
      }
    };
    // Member that holds the setting's value
    // Change this data type to match the type saved by your Preference
    String value;

    public SavedState(Parcel source)
    {
      super(source);
      // Get the current preference's value
      value = source.readString();
    }

    public SavedState(Parcelable superState)
    {
      super(superState);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
      super.writeToParcel(dest, flags);
      // Write the preference's value
      dest.writeString(value);
    }
  }

  /**
   * private Klase zur formatierung der Zahlen auf mindestens zweistellig
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 02.01.2013
   */
  @SuppressLint( "DefaultLocale" )
  private class TwoDigitFormatter implements Formatter
  {
    @Override
    public String format(int value)
    {
      return (String.format("%02d", value));
    }
  }
}
