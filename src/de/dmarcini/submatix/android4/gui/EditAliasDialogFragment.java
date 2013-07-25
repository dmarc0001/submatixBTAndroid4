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
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.utils.NoticeDialogListener;

public class EditAliasDialogFragment extends DialogFragment
{
  private static final String  TAG        = EditAliasDialogFragment.class.getSimpleName();
  private String               msg        = null;
  private String               deviceName = null;
  private String               aliasName  = null;
  private Dialog               alDial     = null;
  // Use this instance of the interface to deliver action events
  private NoticeDialogListener mListener  = null;

  /**
   * 
   * Der Default-Konstruktor ist gesperrt
   * 
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.11.2012
   */
  @SuppressWarnings( "unused" )
  private EditAliasDialogFragment()
  {}

  /**
   * 
   * Konstruktor mit Überschrift
   * 
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.11.2012
   * @param msg
   */
  public EditAliasDialogFragment( String msg, String device, String alias )
  {
    super();
    this.msg = msg;
    this.aliasName = alias;
    this.deviceName = device;
  }

  @Override
  public Dialog onCreateDialog( Bundle savedInstanceState )
  {
    // Benutze die Builderklasse zum erstellen des Dialogs
    AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    builder.setView( inflater.inflate( R.layout.alias_edit_dialog_fragment, null ) );
    // Buttons erzeugen
    builder.setPositiveButton( R.string.dialog_exit_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // Gib in der App bescheid, ich will es so!
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
    alDial = builder.create();
    return( alDial );
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
}
