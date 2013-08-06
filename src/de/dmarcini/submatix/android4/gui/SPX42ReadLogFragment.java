package de.dmarcini.submatix.android4.gui;

import java.io.File;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.utils.NoDatabaseException;
import de.dmarcini.submatix.android4.utils.ProjectConst;
import de.dmarcini.submatix.android4.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.utils.SPX42ReadLogListArrayAdapter;
import de.dmarcini.submatix.android4.utils.UserAlertDialogFragment;

public class SPX42ReadLogFragment extends Fragment implements IBtServiceListener
{
  private static final String          TAG             = SPX42ReadLogFragment.class.getSimpleName();
  private Activity                     runningActivity = null;
  private ListView                     mainListView    = null;
  private SPX42LogManager              logManager      = null;
  private SPX42ReadLogListArrayAdapter logListAdapter  = null;

  @Override
  public void handleMessages( int what, BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void onActivityCreated( Bundle bundle )
  {
    super.onActivityCreated( bundle );
    runningActivity = getActivity();
    if( BuildConfig.DEBUG ) Log.d( TAG, "onActivityCreated: ACTIVITY ATTACH" );
    try
    {
      mainListView = ( ListView )runningActivity.findViewById( R.id.readLogLinesListView );
      logListAdapter = new SPX42ReadLogListArrayAdapter( runningActivity, R.layout.read_log_array_adapter_view, FragmentCommonActivity.getAppStyle() );
      //
      // FOR DEBUG:
      //
      ReadLogItemObj rlio = new ReadLogItemObj( false, "PROGRAM1", "PROGRAMDETAIL1" );
      logListAdapter.add( rlio );
      rlio = new ReadLogItemObj( false, "PROGRAM2", "PROGRAMDETAIL2" );
      logListAdapter.add( rlio );
      rlio = new ReadLogItemObj( false, "PROGRAM3", "PROGRAMDETAIL3" );
      logListAdapter.add( rlio );
      //
      // FOR DEBUG:
      //
      mainListView.setAdapter( logListAdapter );
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "onActivityCreated: gui objects not allocated!" );
    }
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = activity;
    if( BuildConfig.DEBUG ) Log.d( TAG, "onAttach: ATTACH" );
    //
    // die Datenbank öffnen
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "onAttach: create SQLite helper..." );
    DataSQLHelper sqlHelper = new DataSQLHelper( getActivity().getApplicationContext(), FragmentCommonActivity.databaseDir.getAbsolutePath() + File.separator
            + ProjectConst.DATABASE_NAME );
    if( BuildConfig.DEBUG ) Log.d( TAG, "onAttach: open Database..." );
    try
    {
      logManager = new SPX42LogManager( sqlHelper.getWritableDatabase() );
    }
    catch( NoDatabaseException ex )
    {
      Log.e( TAG, "NoDatabaseException: <" + ex.getLocalizedMessage() + ">" );
      UserAlertDialogFragment uad = new UserAlertDialogFragment( runningActivity.getResources().getString( R.string.dialog_sqlite_error_header ), runningActivity.getResources()
              .getString( R.string.dialog_sqlite_nodatabase_error ) );
      uad.show( getFragmentManager(), "UserAlertDialogFragment" );
    }
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    // Layout View
    super.onCreate( savedInstanceState );
    if( BuildConfig.DEBUG ) Log.v( TAG, "onCreate..." );
  }

  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView = null;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "onCreateView..." );
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
      if( BuildConfig.DEBUG ) Log.d( TAG, "onCreateView: running from AreaDetailActivity ..." );
      return( null );
    }
    //
    // Verbindungsseite via twoPane ausgewählt
    //
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_read_log, container, false );
    //
    // Objekte lokalisieren
    //
    mainListView = ( ListView )runningActivity.findViewById( R.id.readLogLinesListView );
    return( rootView );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( BuildConfig.DEBUG ) Log.d( TAG, "onPause..." );
    //
    // die abgeleiteten Objekte führen das auch aus
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "onPause: clear service listener for preferences fragment..." );
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG ) Log.d( TAG, "onResume..." );
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
  }
}
