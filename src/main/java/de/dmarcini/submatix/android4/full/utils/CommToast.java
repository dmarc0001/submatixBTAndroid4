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
package de.dmarcini.submatix.android4.full.utils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.dialogs.WaitProgressFragmentDialog;
import de.dmarcini.submatix.android4.full.gui.MainActivity;

/**
 * Eine Klasse, um einen eigenen Toast bei Kommunikationen mit dem SPX anzuzeigern
 * <p/>
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 18.07.2013
 */
public class CommToast
{
  private static final String                     TAG      = CommToast.class.getSimpleName();
  private              Activity                   act      = null;
  private              Toast                      theToast = null;
  private              WaitProgressFragmentDialog pd       = null;
  private View     toastLayout;
  private TextView toastMessageTextView;

  //

  /**
   * Der Konstruktor, übergibt die referenz auf die Activity
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   *
   * @param act
   */
  public CommToast(Activity act)
  {
    this.act = act;
    theToast = null;
    pd = null;
  }

  /**
   * gesperrter Konstruktor
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   */
  @SuppressWarnings( "unused" )
  private CommToast()
  {
  }

  /**
   * Das Grundgerüst des Toasts
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   *
   * @param msg
   * @param isLong
   */
  private void makeToastObj(String msg, boolean isLong)
  {
    try
    {
      //
      // einen alten Toast entfernen
      //
      if( theToast != null )
      {
        theToast.cancel();
        theToast = null;
      }
      theToast = new Toast(act);
      LayoutInflater inflater = act.getLayoutInflater();
      toastLayout = inflater.inflate(R.layout.comm_toast_layout, ( ViewGroup ) act.findViewById(R.id.commToastLayout));
      toastMessageTextView = ( TextView ) toastLayout.findViewById(R.id.toastTextView);
      toastMessageTextView.setText(msg);
      // der Toast ist unten, kleiner Abstand zum Boden, ganze Breite
      theToast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 15);
      theToast.setDuration((isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT));
      theToast.setView(toastLayout);
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "showConnectionToast: ups, there was not an pointer... Show none TOAST! (" + ex.getLocalizedMessage() + ")");
    }
    catch( Exception ex )
    {
      Log.e(TAG, "showConnectionToast: ups, there was an exception: (" + ex.getLocalizedMessage() + ")");
    }
  }

  /**
   * Erzeuge einen Toast um den User über Kommunikationen zu benachrichtigen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 15.07.2013
   *
   * @param msg
   * @param isLong
   */
  public void showConnectionToast(String msg, Boolean isLong)
  {
    //
    // Ich mache einen eigenen Toast über die ganze Breite mit eigenem style
    //
    try
    {
      makeToastObj(msg, isLong);
      //
      // welcher Style ist angesagt?
      //
      if( MainActivity.getAppStyle() == R.style.AppDarkTheme )
      {
        toastLayout.setBackgroundColor(act.getResources().getColor(R.color.connectToastDark_backgroundColor));
        toastMessageTextView.setTextAppearance(act.getApplicationContext(), R.style.commToastDark);
      }
      else
      {
        toastLayout.setBackgroundColor(act.getResources().getColor(R.color.connectToastLight_backgroundColor));
        toastMessageTextView.setTextAppearance(act.getApplicationContext(), R.style.commToastLight);
      }
      theToast.show();
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "showConnectionToast: ups, there was not an pointer... Show none TOAST! (" + ex.getLocalizedMessage() + ")");
    }
    catch( Exception ex )
    {
      Log.e(TAG, "showConnectionToast: ups, there was an exception: (" + ex.getLocalizedMessage() + ")");
    }
  }

  /**
   * Zeige einen Toast mit Alarmfarben
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 17.07.2013
   *
   * @param msg
   */
  public void showConnectionToastAlert(String msg)
  {
    //
    // Ich mache einen eigenen Toast über die ganze Breite mit eigenem style
    //
    try
    {
      makeToastObj(msg, true);
      toastLayout.setBackgroundColor(act.getResources().getColor(R.color.connectToastAlert_backgroundColor));
      toastMessageTextView.setTextAppearance(act.getApplicationContext(), R.style.commToastAlert);
      theToast.show();
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "showConnectionToastAlert: ups, there was not an pointer... Show none TOAST! (" + ex.getLocalizedMessage() + ")");
    }
    catch( Exception ex )
    {
      Log.e(TAG, "showConnectionToastAlert: ups, there was an exception: (" + ex.getLocalizedMessage() + ")");
    }
  }

  /**
   * einen bitte warten Dialog herzaubern
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   *
   * @param maxevents
   * @param title
   * @param msg
   */
  public void openWaitDial(int maxevents, String title, String msg)
  {
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "openWaitDial()...");
    }
    //
    // wenn ein Dialog da ist, erst mal aus den Fragmenten entfernen
    //
    FragmentTransaction ft   = act.getFragmentManager().beginTransaction();
    Fragment            prev = act.getFragmentManager().findFragmentByTag("dialog");
    if( prev != null )
    {
      ft.remove(prev);
    }
    if( pd != null )
    {
      pd.dismiss();
    }
    pd = new WaitProgressFragmentDialog();
    pd.setTitle(title);
    pd.setMessage(msg);
    pd.setCancelable(true);
    pd.setMax(maxevents);
    pd.setProgress(4);
    ft.addToBackStack(null);
    pd.show(ft, "dialog");
  }

  /**
   * den Bitte warten Dialog verschwinden lassen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 18.07.2013
   */
  public void dismissDial()
  {
    if( ApplicationDEBUG.DEBUG )
    {
      Log.d(TAG, "dismissDial()...");
    }
    FragmentTransaction ft = act.getFragmentManager().beginTransaction();
    ft.commit();
    Fragment prev = act.getFragmentManager().findFragmentByTag("dialog");
    if( prev != null )
    {
      ft.remove(prev);
    }
    if( pd != null )
    {
      pd.dismiss();
    }
  }
}
