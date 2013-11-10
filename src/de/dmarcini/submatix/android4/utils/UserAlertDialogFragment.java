package de.dmarcini.submatix.android4.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.gui.EditAliasDialogFragment;

/**
 * 
 * Benutzer Alarmdiialog
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class UserAlertDialogFragment extends DialogFragment
{
  @SuppressWarnings( "unused" )
  private static final String  TAG       = EditAliasDialogFragment.class.getSimpleName();
  private View                 rootView;
  private String               headLine;
  private String               msg;
  // Use this instance of the interface to deliver action events
  private NoticeDialogListener mListener = null;

  /**
   * 
   * Der Default-Konstruktor ist gesperrt
   * 
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 02.11.2012
   */
  @SuppressWarnings( "unused" )
  private UserAlertDialogFragment()
  {}

  /**
   * 
   * Konstruktor mit Überschrift
   * 
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 02.11.2012
   * 
   * @param headLine
   * @param msg
   */
  public UserAlertDialogFragment( String headLine, String msg )
  {
    super();
    this.headLine = headLine;
    this.msg = msg;
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
    rootView = inflater.inflate( R.layout.fragment_alert_dialog, null );
    //
    // die Texte einfügen, natürlich
    //
    TextView tv = ( TextView )rootView.findViewById( R.id.alertHeadlineTextView );
    tv.setText( headLine );
    // das wird ein editierbarer Text!
    tv = ( TextView )rootView.findViewById( R.id.alertMessageTextView );
    tv.setText( msg, TextView.BufferType.EDITABLE );
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView( rootView );
    // Buttons erzeugen
    builder.setPositiveButton( R.string.dialog_exit_button, new DialogInterface.OnClickListener() {
      @Override
      public void onClick( DialogInterface dialog, int id )
      {
        // Gib in der App bescheid, ich will es so!
        mListener.onDialogPositiveClick( UserAlertDialogFragment.this );
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
}
