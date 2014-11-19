﻿//@formatter:off
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
package de.dmarcini.submatix.android4.full.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.interfaces.INoticeDialogListener;

/**
 * 
 * Der Dialog zum Editieren von Geräte-Aliasen
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class EditAliasDialogFragment extends DialogFragment
{
  private static final String   TAG        = EditAliasDialogFragment.class.getSimpleName();
  private View                  rootView;
  private String                deviceName = null;
  private String                aliasName  = null;
  private String                macAddr    = null;
  private String                oldPin     = null;
  // Use this instance of the interface to deliver action events
  private INoticeDialogListener mListener  = null;

  /**
   * 
   * Der Default-Konstruktor ist gesperrt
   * 
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 02.11.2012
   * 
   * @throws Exception
   */
  public EditAliasDialogFragment() throws Exception
  {
    throw new Exception( "use NOT this constructor!" );
  }

  /**
   * 
   * Konstruktor mit Überschrift
   * 
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 02.11.2012
   * 
   * @param device
   *          Welches Gerät
   * @param alias
   *          Welcher Gerätealias (alt)
   * @param mac
   *          Welche Geräte-MAC
   */
  public EditAliasDialogFragment( String device, String alias, String mac )
  {
    super();
    this.aliasName = alias;
    this.deviceName = device;
    this.macAddr = mac;
    this.oldPin = null;
  }

  /**
   * 
   * Konstruktor mit Überschrift und Parametern
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * 
   * Stand: 19.11.2014
   * 
   * @param device
   *          Welches Gerät
   * @param alias
   *          Welcher Gerätealias (alt)
   * @param mac
   *          Welche Geräte-MAC
   * @param oldPin
   *          Welche alte PIN war da?
   */
  public EditAliasDialogFragment( String device, String alias, String mac, String oldPin )
  {
    super();
    this.aliasName = alias;
    this.deviceName = device;
    this.macAddr = mac;
    this.oldPin = oldPin;
  }

  @Override
  public Dialog onCreateDialog( Bundle savedInstanceState )
  {
    //
    // Benutze die Builderklasse zum erstellen des Dialogs
    AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    rootView = inflater.inflate( R.layout.fragment_dialog_alias_edit, null );
    //
    // die Texte einfügen, natürlich
    //
    TextView tv = ( TextView )rootView.findViewById( R.id.aliasEditDialogDeviceTextView );
    tv.setText( deviceName );
    // das wird ein editierbarer Text!
    EditText ed = ( EditText )rootView.findViewById( R.id.aliasEditDialogAliasEditTextView );
    ed.setText( aliasName, TextView.BufferType.EDITABLE );
    ed.selectAll();
    //
    // ud, wenn Android ab 4.4 läuft
    //
    if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
    {
      EditText edPin = ( EditText )rootView.findViewById( R.id.aliasEditDialogPINEditTextView );
      edPin.setVisibility( View.VISIBLE );
      edPin.setText( this.oldPin );
    }
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView( rootView );
    // Buttons erzeugen
    builder.setPositiveButton( R.string.dialog_save_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        EditText ed;
        // Gib in der App bescheid, ich will es so!
        ed = ( EditText )rootView.findViewById( R.id.aliasEditDialogAliasEditTextView );
        aliasName = ed.getText().toString();
        if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT )
        {
          // bei Android ab 4.4
          EditText edPin = ( EditText )rootView.findViewById( R.id.aliasEditDialogPINEditTextView );
          oldPin = edPin.getText().toString();
        }
        mListener.onDialogPositiveClick( EditAliasDialogFragment.this );
      }
    } );
    builder.setNegativeButton( R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // Abbruch!
        mListener.onDialogNegativeClick( EditAliasDialogFragment.this );
      }
    } );
    // Create the AlertDialog object and return it
    return( builder.create() );
  }

  // Überschreibe onAttach für meine Zwecke mit dem Listener
  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    // Implementiert die Activity den Listener?
    try
    {
      // Instanziere den Listener, wenn möglich, ansonsten wirft das eine exception
      mListener = ( INoticeDialogListener )activity;
    }
    catch( ClassCastException ex )
    {
      // Die activity implementiert den Listener nicht, werfe eine Exception
      throw new ClassCastException( activity.toString() + " must implement INoticeDialogListener" );
    }
  }

  // Überschreibe show fürs debugging
  @Override
  public void show( FragmentManager manager, String tag )
  {
    super.show( manager, tag );
    Log.v( TAG, "show(manager,tag)..." );
  }

  /**
   * 
   * gib den Gerätenamen zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 25.07.2013
   * 
   * @return Gerätename
   */
  public String getDeviceName()
  {
    return( deviceName );
  }

  /**
   * 
   * Gib die (editierte) PIN zurück
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * 
   * Stand: 19.11.2014
   * 
   * @return
   */
  public String getPin()
  {
    return( oldPin );
  }

  /**
   * 
   * Gib den (editierten) Aliasnamen zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 25.07.2013
   * 
   * @return Alias des Gerätes
   */
  public String getAliasName()
  {
    return( aliasName );
  }

  /**
   * 
   * Gib die MAC-Adresse des editierten Eintrages zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 26.07.2013
   * 
   * @return MAC Adresse des Gerätes
   */
  public String getMac()
  {
    return( macAddr );
  }
}
