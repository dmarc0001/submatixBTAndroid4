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

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.interfaces.INoticeDialogListener;

/**
 * Ein Fragment für die anzeige der Frage, ob der user sicher ist
 * <p>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p>
 *         Stand: 10.11.2013
 */
public class AreYouSureDialogFragment extends DialogFragment
{
  private static final String                TAG       = AreYouSureDialogFragment.class.getSimpleName();
  private              String                msg       = null;
  private              Dialog                alDial    = null;
  // Use this instance of the interface to deliver action events
  private              INoticeDialogListener mListener = null;

  /**
   * Der Default-Konstruktor ist gesperrt
   * <p>
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * <p>
   * <p>
   * Stand: 02.11.2012
   */
  @SuppressWarnings( "unused" )
  private AreYouSureDialogFragment()
  {
  }

  /**
   * Konstruktor mit Überschrift
   * <p>
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * <p>
   * <p>
   * Stand: 02.11.2012
   *
   * @param msg
   */
  public AreYouSureDialogFragment(String msg)
  {
    super();
    this.msg = msg;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    // Benutze die Builderklasse zum erstellen des Dialogs
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(msg);
    builder.setPositiveButton(R.string.dialog_exit_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Gib in der App bescheid, ich will es so!
        mListener.onDialogPositiveClick(AreYouSureDialogFragment.this);
      }
    });
    builder.setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Abbruch!
        mListener.onDialogNegativeClick(AreYouSureDialogFragment.this);
      }
    });
    // Create the AlertDialog object and return it
    alDial = builder.create();
    return (alDial);
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

  // Überschreibe show fürs debugging
  @Override
  public void show(FragmentManager manager, String tag)
  {
    super.show(manager, tag);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "show(manager,tag)...");
    }
  }
}
