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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.interfaces.INoticeDialogListener;

/**
 * Der Dialog fragt, ob der user SICHER ist, dass er loeschen möchte
 * <p>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p>
 *         Stand: 10.11.2013
 */
public class AreYouSureToDeleteFragment extends DialogFragment
{
  private static final String TAG = AreYouSureToDeleteFragment.class.getSimpleName();
  private View rootView;
  private String                msg       = "?";
  // Use this instance of the interface to deliver action events
  private INoticeDialogListener mListener = null;

  @SuppressWarnings( "unused" )
  private AreYouSureToDeleteFragment()
  {
  }

  /**
   * Konstruktor mit Message
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 21.01.2014
   *
   * @param msg
   */
  public AreYouSureToDeleteFragment(String msg)
  {
    super();
    this.msg = msg;
  }

  // Überschreibe onAttach für meine Zwecke mit dem Listener
  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    // Implementiert die Activity den Listener?
    try
    {
      // Instanziere den Listener, wenn möglich, ansonsten wirft das eine exception
      mListener = ( INoticeDialogListener ) activity;
    }
    catch( ClassCastException ex )
    {
      // Die activity implementiert den Listener nicht, werfe eine Exception
      throw new ClassCastException(activity.toString() + " must implement INoticeDialogListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    //
    // Benutze die Builderklasse zum erstellen des Dialogs
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    rootView = inflater.inflate(R.layout.delete_are_you_sure_dialog_fragment, null);
    //
    // die Message einbringen
    //
    TextView tv = ( TextView ) rootView.findViewById(R.id.AreYouSureToDeleteMsgTextView);
    tv.setText(msg);
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView(rootView);
    // Buttons erzeugen
    builder.setPositiveButton(R.string.dialog_delete_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Alles OK
        mListener.onDialogPositiveClick(AreYouSureToDeleteFragment.this);
      }
    });
    builder.setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Abbruch!
        mListener.onDialogNegativeClick(AreYouSureToDeleteFragment.this);
      }
    });
    // Create the AlertDialog object and return it
    return (builder.create());
  }

  /**
   * Nachricht in den Sind-Sie-Sicher Dialog
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 21.01.2014
   *
   * @param _msg
   */
  public void setMessage(String _msg)
  {
    msg = _msg;
  }

  // Überschreibe show fürs debugging
  @Override
  public void show(FragmentManager manager, String tag)
  {
    super.show(manager, tag);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "show(manager," + tag + ")...");
    }
  }
}
