package de.dmarcini.submatix.android4.utils;

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
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.gui.FragmentCommonActivity;
import de.dmarcini.submatix.android4.gui.WaitProgressFragmentDialog;

/**
 * 
 * Eine Klasse, um einen eigenen Toast bei Kommunikationen mit dem SPX anzuzeigern
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 18.07.2013
 */
public class CommToast
{
  private static final String        TAG      = CommToast.class.getSimpleName();
  private Activity                   act      = null;
  private Toast                      theToast = null;
  private WaitProgressFragmentDialog pd       = null;
  private View                       toastLayout;
  private TextView                   toastMessageTextView;

  //
  /**
   * 
   * Der Konstruktor, übergibt die referenz auf die Activity
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   * @param act
   */
  public CommToast( Activity act )
  {
    this.act = act;
    theToast = null;
    pd = null;
  }

  /**
   * 
   * gesperrter Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   */
  @SuppressWarnings( "unused" )
  private CommToast()
  {}

  /**
   * 
   * Das Grundgerüst des Toasts
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   * @param msg
   * @param isLong
   */
  private void makeToastObj( String msg, boolean isLong )
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
      theToast = new Toast( act );
      LayoutInflater inflater = act.getLayoutInflater();
      toastLayout = inflater.inflate( R.layout.comm_toast_layout, ( ViewGroup )act.findViewById( R.id.commToastLayout ) );
      toastMessageTextView = ( TextView )toastLayout.findViewById( R.id.toastTextView );
      toastMessageTextView.setText( msg );
      // der Toast ist unten, kleiner Abstand zum Boden, ganze Breite
      theToast.setGravity( Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 15 );
      theToast.setDuration( ( isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT ) );
      theToast.setView( toastLayout );
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "showConnectionToast: ups, there was not an pointer... Show none TOAST! (" + ex.getLocalizedMessage() + ")" );
    }
    catch( Exception ex )
    {
      Log.e( TAG, "showConnectionToast: ups, there was an exception: (" + ex.getLocalizedMessage() + ")" );
    }
  }

  /**
   * 
   * Erzeuge einen Toast um den User über Kommunikationen zu benachrichtigen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 15.07.2013
   * @param msg
   * @param isLong
   */
  public void showConnectionToast( String msg, Boolean isLong )
  {
    //
    // Ich mache einen eigenen Toast über die ganze Breite mit eigenem style
    //
    try
    {
      makeToastObj( msg, isLong );
      //
      // welcher Style ist angesagt?
      //
      if( FragmentCommonActivity.getAppStyle() == R.style.AppDarkTheme )
      {
        toastLayout.setBackgroundColor( act.getResources().getColor( R.color.connectToastDark_backgroundColor ) );
        toastMessageTextView.setTextAppearance( act.getApplicationContext(), R.style.commToastDark );
      }
      else
      {
        toastLayout.setBackgroundColor( act.getResources().getColor( R.color.connectToastLight_backgroundColor ) );
        toastMessageTextView.setTextAppearance( act.getApplicationContext(), R.style.commToastLight );
      }
      theToast.show();
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "showConnectionToast: ups, there was not an pointer... Show none TOAST! (" + ex.getLocalizedMessage() + ")" );
    }
    catch( Exception ex )
    {
      Log.e( TAG, "showConnectionToast: ups, there was an exception: (" + ex.getLocalizedMessage() + ")" );
    }
  }

  /**
   * 
   * Zeige einen Toast mit Alarmfarben
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.07.2013
   * @param msg
   */
  public void showConnectionToastAlert( String msg )
  {
    //
    // Ich mache einen eigenen Toast über die ganze Breite mit eigenem style
    //
    try
    {
      makeToastObj( msg, true );
      toastLayout.setBackgroundColor( act.getResources().getColor( R.color.connectToastAlert_backgroundColor ) );
      toastMessageTextView.setTextAppearance( act.getApplicationContext(), R.style.commToastAlert );
      theToast.show();
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "showConnectionToastAlert: ups, there was not an pointer... Show none TOAST! (" + ex.getLocalizedMessage() + ")" );
    }
    catch( Exception ex )
    {
      Log.e( TAG, "showConnectionToastAlert: ups, there was an exception: (" + ex.getLocalizedMessage() + ")" );
    }
  }

  /**
   * 
   * einen bite warten Dialog herzaubern
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   * @param maxevents
   * @param title
   * @param msg
   */
  public void openWaitDial( int maxevents, String title, String msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "openWaitDial()..." );
    //
    // wenn ein Dialog da ist, erst mal aus den Fragmenten entfernen
    //
    FragmentTransaction ft = act.getFragmentManager().beginTransaction();
    Fragment prev = act.getFragmentManager().findFragmentByTag( "dialog" );
    if( prev != null )
    {
      ft.remove( prev );
    }
    if( pd != null )
    {
      pd.dismiss();
    }
    pd = new WaitProgressFragmentDialog( title, msg );
    ;
    pd.setCancelable( true );
    pd.setMax( maxevents );
    pd.setProgress( 4 );
    ft.addToBackStack( null );
    pd.show( ft, "dialog" );
  }

  /**
   * 
   * den Bitte warten Dialog verschwinden lassen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 18.07.2013
   */
  public void dismissDial()
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "dismissDial()..." );
    FragmentTransaction ft = act.getFragmentManager().beginTransaction();
    Fragment prev = act.getFragmentManager().findFragmentByTag( "dialog" );
    if( prev != null )
    {
      ft.remove( prev );
    }
    if( pd != null )
    {
      pd.dismiss();
    }
  }
}
