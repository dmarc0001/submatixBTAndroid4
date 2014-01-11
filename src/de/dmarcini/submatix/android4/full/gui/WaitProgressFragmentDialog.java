/**
 * Ein Dialog zum WARTEN
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * 
 * Stand: 13.06.2013
 */
package de.dmarcini.submatix.android4.full.gui;

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
   * 
   * Ein eigener Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 03.12.2013
   * 
   * @param title
   * @param msg
   */
  // @SuppressLint( "ValidFragment" )
  // public WaitProgressFragmentDialog( final String title, final String msg )
  // {
  // this.vMessage = msg;
  // this.messageTitle = title;
  // }
  /**
   * der Konstruktor als Default
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 11.01.2014
   */
  public WaitProgressFragmentDialog()
  {}

  @Override
  public void onStart()
  {
    // Keinen Bildschirmschoner zulassen
    getActivity().getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
  }

  @Override
  public void onStop()
  {
    // Bildschirmschoner wieder zulassen
    getActivity().getWindow().clearFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
  }

  //
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView = inflater.inflate( R.layout.fragment_dialog_please_wait, container, false );
    msgView = ( TextView )rootView.findViewById( R.id.dialogMessageString );
    subMsgView = ( TextView )rootView.findViewById( R.id.dialogSubMessageTextView );
    pBar = ( ProgressBar )rootView.findViewById( R.id.dialogProgressBar );
    if( ( messageTitle != null ) && ( !messageTitle.isEmpty() ) )
    {
      getDialog().setTitle( messageTitle );
    }
    if( ( vMessage != null ) && ( !vMessage.isEmpty() ) )
    {
      msgView.setText( vMessage );
    }
    if( subMessage != null )
    {
      subMsgView.setVisibility( View.VISIBLE );
      subMsgView.setText( subMessage );
    }
    pBar.setMax( maxEvents );
    pBar.setProgress( progress );
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
  public void setMax( int maxevents )
  {
    maxEvents = maxevents;
    if( pBar != null )
    {
      pBar.setMax( maxevents );
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
  public void setProgress( int progress )
  {
    this.progress = progress;
    if( pBar != null )
    {
      pBar.setProgress( progress );
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
  public void setTitle( String title )
  {
    this.messageTitle = title;
    if( getDialog() != null )
    {
      getDialog().setTitle( title );
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
  public void setMessage( String msg )
  {
    vMessage = msg;
    if( msgView != null )
    {
      msgView.setText( msg );
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
  public void setSubMessage( String msg )
  {
    subMessage = msg;
    if( subMsgView != null )
    {
      if( subMsgView.getVisibility() != View.VISIBLE )
      {
        subMsgView.setVisibility( View.VISIBLE );
      }
      subMsgView.setText( subMessage );
    }
  }
}
