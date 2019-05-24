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
/**
 * Ein Dialog zum WARTEN
 * <p>
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * <p>
 * <p>
 * Stand: 13.06.2013
 */
package de.dmarcini.submatix.android4.full.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import de.dmarcini.submatix.android4.full.R;

/**
 *
 * Klasse für den Wartedialog
 *
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *
 *         Stand: 10.11.2013
 */
public class WaitProgressFragmentDialog extends DialogFragment
{
  private TextView    msgView      = null;
  private TextView    subMsgView   = null;
  private ProgressBar pBar         = null;
  private String      vMessage     = "none";
  private String      subMessage   = null;
  private String      messageTitle = null;
  private int         maxEvents    = 10;
  private int         progress     = 0;

  /**
   * der Konstruktor als Default
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   *
   * Stand: 11.01.2014
   */
  public WaitProgressFragmentDialog()
  {
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    // Keinen Bildschirmschoner zulassen
    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    // Bildschirmschoner wieder zulassen
    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  // @Override
  // public void dismiss()
  // {
  // super.dismiss();
  // // Bildschirmschoner wieder zulassen
  // getActivity().getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
  // }
  //
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_dialog_please_wait, container, false);
    msgView = ( TextView ) rootView.findViewById(R.id.dialogMessageString);
    subMsgView = ( TextView ) rootView.findViewById(R.id.dialogSubMessageTextView);
    pBar = ( ProgressBar ) rootView.findViewById(R.id.dialogProgressBar);
    if( (messageTitle != null) && (!messageTitle.isEmpty()) )
    {
      getDialog().setTitle(messageTitle);
    }
    if( (vMessage != null) && (!vMessage.isEmpty()) )
    {
      msgView.setText(vMessage);
    }
    if( subMessage != null )
    {
      subMsgView.setVisibility(View.VISIBLE);
      subMsgView.setText(subMessage);
    }
    pBar.setMax(maxEvents);
    pBar.setProgress(progress);
    // gleich neu zeichnen!
    rootView.invalidate();
    return rootView;
  }

  /**
   *
   * Wir gross ist der Wertebereich maximal
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   *
   * Stand: 03.12.2013
   *
   * @param maxevents
   */
  public void setMax(int maxevents)
  {
    maxEvents = maxevents;
    if( pBar != null )
    {
      pBar.setMax(maxevents);
    }
  }

  /**
   *
   * Setze aktuelle Position
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   *
   * Stand: 03.12.2013
   *
   * @param progress
   */
  public void setProgress(int progress)
  {
    this.progress = progress;
    if( pBar != null )
    {
      pBar.setProgress(progress);
    }
  }

  /**
   *
   * Setze den Fenstertitel
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   *
   * Stand: 03.12.2013
   *
   * @param title
   */
  public void setTitle(String title)
  {
    this.messageTitle = title;
    if( getDialog() != null )
    {
      getDialog().setTitle(title);
    }
  }

  /**
   *
   * Setze die Nachricht im Fenster
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   *
   * Stand: 03.12.2013
   *
   * @param msg
   */
  public void setMessage(String msg)
  {
    vMessage = msg;
    if( msgView != null )
    {
      msgView.setText(msg);
    }
  }

  /**
   *
   * Submessage setzen
   *
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   *
   *
   * Stand: 07.08.2013
   *
   * @param msg
   */
  public void setSubMessage(String msg)
  {
    subMessage = msg;
    if( subMsgView != null )
    {
      if( subMsgView.getVisibility() != View.VISIBLE )
      {
        subMsgView.setVisibility(View.VISIBLE);
      }
      subMsgView.setText(subMessage);
    }
  }
}
