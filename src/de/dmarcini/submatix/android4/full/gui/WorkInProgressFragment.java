package de.dmarcini.submatix.android4.full.gui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;

/**
 * 
 * Ein Fragment zur Anzeige, daß die Funktion och in Arbeit ist
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class WorkInProgressFragment extends Fragment implements IBtServiceListener
{
  @SuppressWarnings( "javadoc" )
  public static final String TAG             = WorkInProgressFragment.class.getSimpleName();
  private Activity           runningActivity = null;

  @Override
  public void onActivityCreated( Bundle bundle )
  {
    super.onActivityCreated( bundle );
    runningActivity = getActivity();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: ACTIVITY ATTACH" );
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = activity;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: ATTACH" );
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate..." );
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView..." );
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e( TAG, "onCreateView: container is NULL ..." );
      return( null );
    }
    //
    // wenn die laufende Activity eine AreaDetailActivity ist, dann gibts das View schon
    //
    if( runningActivity instanceof AreaDetailActivity )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView: running from AreaDetailActivity ..." );
      return( null );
    }
    //
    // Verbindungsseite via twoPane ausgewählt
    //
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "makeConnectionView..." );
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_in_progress, container, false );
    return( rootView );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    //
    // die abgeleiteten Objekte führen das auch aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause: clear service listener for preferences fragment..." );
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume..." );
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
    // wenn zu diesem Zeitpunkt das Array noch nicht gefüllt ist, dann mach das nun
  }

  @Override
  public void handleMessages( int what, BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }
}
