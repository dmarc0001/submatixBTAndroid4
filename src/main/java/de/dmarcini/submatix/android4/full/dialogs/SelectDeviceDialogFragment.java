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
package de.dmarcini.submatix.android4.full.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import java.util.Vector;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.interfaces.INoticeDialogListener;
import de.dmarcini.submatix.android4.full.utils.DeviceSelectArrayAdapterWithPics;

/**
 * Der Dialog zum Editieren von Geräte-Aliasen
 * <p>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * <p>
 * Stand: 10.11.2013
 */
public class SelectDeviceDialogFragment extends DialogFragment
{
  private static final String                            TAG              = SelectDeviceDialogFragment.class.getSimpleName();
  private              View                              rootView;
  private              Vector< Pair< Integer, String > > devices          = null;
  private              String                            selectedDevice   = null;
  private              int                               selectedDeviceId = - 1;
  // Use this instance of the interface to deliver action events
  private              INoticeDialogListener             mListener        = null;

  /**
   * Gib die selektiere Geräteid zurück
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * <p>
   * Stand: 01.12.2013
   *
   * @return ID des selektierten Gerätes
   */
  public int getSelectedDeviceId()
  {
    return ( selectedDeviceId );
  }

  /**
   * gib den Gerätenamen zurück
   * <p>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p>
   * <p>
   * Stand: 25.07.2013
   *
   * @return Gerätename
   */
  public String getSelectedDeviceName()
  {
    return ( selectedDevice );
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
      mListener = ( INoticeDialogListener ) activity;
    }
    catch( ClassCastException ex )
    {
      // Die activity implementiert den Listener nicht, werfe eine Exception
      throw new ClassCastException( activity.toString() + " must implement INoticeDialogListener" );
    }
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
    rootView = inflater.inflate( R.layout.device_select_dialog_fragment, null );
    //
    // die vorhandenen Devices einfügen, natürlich
    //
    Spinner                          deviceSpinner  = ( Spinner ) rootView.findViewById( R.id.deviceSelectSpinner );
    DeviceSelectArrayAdapterWithPics devicesAdapter = new DeviceSelectArrayAdapterWithPics( getActivity(), 0, devices );
    deviceSpinner.setAdapter( devicesAdapter );
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView( rootView );
    // Buttons erzeugen
    builder.setPositiveButton( R.string.dialog_device_select_button, new DialogInterface.OnClickListener()
    {
      @SuppressWarnings( "unchecked" )
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // was ist ausgewählt?
        Spinner deviceSpinner = ( Spinner ) rootView.findViewById( R.id.deviceSelectSpinner );
        selectedDevice = ( ( Pair< Integer, String > ) deviceSpinner.getSelectedItem() ).second;
        selectedDeviceId = ( ( Pair< Integer, String > ) deviceSpinner.getSelectedItem() ).first;
        mListener.onDialogPositiveClick( SelectDeviceDialogFragment.this );
      }
    } );
    builder.setNegativeButton( R.string.dialog_cancel_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // Abbruch!
        selectedDevice = null;
        selectedDeviceId = - 1;
        mListener.onDialogNegativeClick( SelectDeviceDialogFragment.this );
      }
    } );
    // Create the AlertDialog object and return it
    return ( builder.create() );
  }

  /**
   * Setzze die Liste
   * <p>
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * <p>
   * <p>
   * Stand: 01.12.2013
   *
   * @param devices
   */
  public void setDeviceList( Vector< Pair< Integer, String > > devices )
  {
    this.devices = devices;
  }

  // Überschreibe show fürs debugging
  @Override
  public void show( FragmentManager manager, String tag )
  {
    super.show( manager, tag );
    if( BuildConfig.DEBUG )
    {
      Log.v( TAG, "show(manager," + tag + ")..." );
    }
  }
}
