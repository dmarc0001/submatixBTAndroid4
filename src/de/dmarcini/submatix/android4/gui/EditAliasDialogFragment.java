package de.dmarcini.submatix.android4.gui;

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
public class EditAliasDialogFragment extends DialogFragment
{
  private static final String  TAG        = EditAliasDialogFragment.class.getSimpleName();
  private View                 rootView;
  private String               deviceName = null;
  private String               aliasName  = null;
  private String               macAddr    = null;
  // Use this instance of the interface to deliver action events
  private NoticeDialogListener mListener  = null;

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
   * @param alias
   * @param mac
   */
  public EditAliasDialogFragment( String device, String alias, String mac )
  {
    super();
    this.aliasName = alias;
    this.deviceName = device;
    this.macAddr = mac;
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
    rootView = inflater.inflate( R.layout.alias_edit_dialog_fragment, null );
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
  public String getDeviceName()
  {
    return( deviceName );
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
