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
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.interfaces.INoticeDialogListener;

/**
 * Benutzer Alarmdiialog
 * <p>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p>
 *         Stand: 10.11.2013
 */
public class PinErrorDialog extends DialogFragment
{
  @SuppressWarnings( "unused" )
  private static final String TAG = PinErrorDialog.class.getSimpleName();
  private View   rootView;
  private String headLine;
  private String msg;
  private String pin = null;
  private BluetoothDevice device;
  private boolean         isEditable;
  // Use this instance of the interface to deliver action events
  private INoticeDialogListener mListener = null;

  /**
   * Der Default-Konstruktor ist gesperrt
   * <p>
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * <p>
   * <p>
   * Stand: 02.11.2012
   */
  @SuppressWarnings( "unused" )
  private PinErrorDialog()
  {
  }

  /**
   * Fehlermeldung mit Überschrift
   * <p>
   * Project: Android_4_BlueThoothTest Package: de.dmarcini.submatix.android4.utils
   * <p>
   * <p>
   * Stand: 02.11.2012
   *
   * @param headLine Die Dialogüberschrift
   * @param msg      die Nachricht
   */
  public PinErrorDialog(String headLine, String msg)
  {
    super();
    this.headLine = headLine;
    this.msg = msg;
    this.isEditable = false;
    this.device = null;
  }

  /**
   * Fehlermeldung mit Eingabemöglichkeit
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 18.11.2014
   *
   * @param headLine Die Überschrift der Dialogbox
   * @param msg      Nachricht an den User
   * @param device   für welches Gerät bitteschön
   */
  public PinErrorDialog(String headLine, String msg, final BluetoothDevice device)
  {
    super();
    this.headLine = headLine;
    this.msg = msg;
    this.isEditable = true;
    this.device = device;
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
    rootView = inflater.inflate(R.layout.fragment_dialog_pin_error, ( ViewGroup ) null);
    //
    // die Texte einfügen, natürlich
    //
    // zuerst Headline
    TextView tv = ( TextView ) rootView.findViewById(R.id.pinErrorHeadlineTextView);
    tv.setText(headLine);
    // finde den normalen Text
    tv = ( TextView ) rootView.findViewById(R.id.pinErrorMessageTextView);
    // finde die Eingabezeile
    EditText et = ( EditText ) rootView.findViewById(R.id.pinErrorMessageEditText);
    if( this.isEditable )
    {
      // Text ausblenden, Eingabezeile einblenden
      tv.setVisibility(View.VISIBLE);
      et.setVisibility(View.VISIBLE);
      // Vorgabetext setzen
      et.setText("");
      tv.setText(msg);
    }
    else
    {
      // Eingabezeile ausblenden, Text einblenden
      et.setVisibility(View.INVISIBLE);
      tv.setVisibility(View.VISIBLE);
      // Vorgabetext setzen
      tv.setText(msg);
    }
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView(rootView);
    //
    // entscheide, welche Art der Dialog ist
    //
    if( this.isEditable )
    {
      // Buttons für editierbar erzeugen
      builder.setPositiveButton(R.string.dialog_save_button, new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
          //
          // die PIN zwischenspeichern, vorher Lerzeichen vorn und hinten entfernen
          //
          EditText et = ( EditText ) rootView.findViewById(R.id.pinErrorMessageEditText);
          pin = et.getText().toString();
          pin.trim();
          // Gib in der App bescheid, ich will es so!
          mListener.onDialogPositiveClick(PinErrorDialog.this);
        }
      });
      builder.setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
          // Gib in der App bescheid, ich will es so!
          mListener.onDialogNegativeClick(PinErrorDialog.this);
        }
      });
    }
    else
    {
      // Button erzeugen für einfach, nicht editierbar
      builder.setPositiveButton(R.string.dialog_understand_button, new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int id)
        {
          // Gib in der App bescheid, ich will es so!
          mListener.onDialogPositiveClick(PinErrorDialog.this);
        }
      });
    }
    // Create the AlertDialog object and return it
    return (builder.create());
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

  /**
   * Wenn nach der PIN gefragt wurde, welches Gerät ist gemeint?
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 18.11.2014
   *
   * @return Das Geräteobjekt für welches die PIN angefordert wurde
   */
  public BluetoothDevice getDevice()
  {
    return (device);
  }

  /**
   * Gib die eingegebene PIN zurück
   * <p>
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.dialogs
   * <p>
   * Stand: 18.11.2014
   *
   * @return die PIN vom Benutzer editiert
   */
  public String getPin()
  {
    return (pin);
  }
}
