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
import android.widget.TextView;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.utils.NoticeDialogListener;

/**
 * 
 * Der Dialog fragt, ob der user SICHER ist, dass er loeschen möchte
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class AreYouuSureToDelete extends DialogFragment
{
  private static final String  TAG       = AreYouuSureToDelete.class.getSimpleName();
  private View                 rootView;
  private String               msg       = "?";
  // Use this instance of the interface to deliver action events
  private NoticeDialogListener mListener = null;

  @SuppressWarnings( "unused" )
  private AreYouuSureToDelete()
  {}

  /**
   * 
   * Konstruktor mit Message
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * 
   * Stand: 21.01.2014
   * 
   * @param msg
   */
  public AreYouuSureToDelete( String msg )
  {
    super();
    this.msg = msg;
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
    rootView = inflater.inflate( R.layout.delete_are_you_sure_dialog_fragment, null );
    //
    // die Message einbringen
    //
    TextView tv = ( TextView )rootView.findViewById( R.id.AreYouSureToDeleteMsgTextView );
    tv.setText( msg );
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView( rootView );
    // Buttons erzeugen
    builder.setPositiveButton( R.string.dialog_save_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // Alles OK
        mListener.onDialogPositiveClick( AreYouuSureToDelete.this );
      }
    } );
    builder.setNegativeButton( R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // Abbruch!
        mListener.onDialogNegativeClick( AreYouuSureToDelete.this );
      }
    } );
    // Create the AlertDialog object and return it
    return( builder.create() );
  }

  /**
   * 
   * Nachricht in den Sind-Sie-Sicher Dialog
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * 
   * Stand: 21.01.2014
   * 
   * @param _msg
   */
  public void setMessage( String _msg )
  {
    msg = _msg;
  }

  // Überschreibe show fürs debugging
  @Override
  public void show( FragmentManager manager, String tag )
  {
    super.show( manager, tag );
    Log.v( TAG, "show(manager," + tag + ")..." );
  }
}
