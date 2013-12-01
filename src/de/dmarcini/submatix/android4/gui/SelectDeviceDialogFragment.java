package de.dmarcini.submatix.android4.gui;

import java.util.ArrayList;

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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.utils.NoticeDialogListener;

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
public class SelectDeviceDialogFragment extends DialogFragment
{
  private static final String  TAG            = SelectDeviceDialogFragment.class.getSimpleName();
  private View                 rootView;
  private String[]             deviceNames    = null;
  private String               selectedDevice = null;
  // Use this instance of the interface to deliver action events
  private NoticeDialogListener mListener      = null;

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
  public SelectDeviceDialogFragment() throws Exception
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
   * Stand: 01.12.2013
   * @param devices 
   */
  public SelectDeviceDialogFragment( String[] devices )
  {
    super();
    this.deviceNames = devices;
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
    Spinner deviceSpinner = ( Spinner )rootView.findViewById( R.id.deviceSelectSpinner );
    final ArrayList<String> list = new ArrayList<String>();
    for( int i = 0; i < deviceNames.length; ++i )
    {
      list.add( deviceNames[i] );
    }
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_list_item_1, list );
    deviceSpinner.setAdapter( adapter );
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView( rootView );
    // Buttons erzeugen
    builder.setPositiveButton( R.string.dialog_save_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // was ist ausgewählt?
        Spinner deviceSpinner = ( Spinner )rootView.findViewById( R.id.deviceSelectSpinner );
        selectedDevice = ( String )deviceSpinner.getSelectedItem();
        mListener.onDialogPositiveClick( SelectDeviceDialogFragment.this );
      }
    } );
    builder.setNegativeButton( R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // Abbruch!
        selectedDevice = null;
        mListener.onDialogNegativeClick( SelectDeviceDialogFragment.this );
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
      mListener = ( NoticeDialogListener )activity;
    }
    catch( ClassCastException ex )
    {
      // Die activity implementiert den Listener nicht, werfe eine Exception
      throw new ClassCastException( activity.toString() + " must implement NoticeDialogListener" );
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
  public String getSelectedDeviceName()
  {
    return( selectedDevice );
  }
}
