/**
 * Code für Grundformular exportLog
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * 
 * Stand: 28.08.2013
 */
package de.dmarcini.submatix.android4.gui;

import java.io.File;
import java.util.Vector;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.utils.ProjectConst;
import de.dmarcini.submatix.android4.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.utils.UserAlertDialogFragment;

/**
 * 
 * Klasse für das Grundformular zum exportieren der Logs
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class SPXExportLogFragment extends Fragment implements IBtServiceListener, OnItemClickListener, OnClickListener
{
  private static final String TAG              = SPXExportLogFragment.class.getSimpleName();
  private Activity            runningActivity  = null;
  private ListView            mainListView     = null;
  private SPX42LogManager     logManager       = null;
  private int                 selectedDeviceId = -1;
  private Button              changeDeviceButton;
  private Button              exportLogsButton;

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#handleMessages(int, de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void handleMessages( int what, BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgConnected(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgConnectError(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgConnecting(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgDisconnected(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgRecivedAlive(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgRecivedTick(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#msgReciveWriteTmeout(de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
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
      mainListView = ( ListView )runningActivity.findViewById( R.id.exportLogsListView );
      // logListAdapter = new SPX42ReadLogListArrayAdapter( runningActivity, R.layout.read_log_array_adapter_view, FragmentCommonActivity.getAppStyle() );
      // mainListView.setAdapter( logListAdapter );
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

  /*
   * (nicht-Javadoc)
   * 
   * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
   */
  @Override
  public void onClick( View v )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "Click" );
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
      //
      // Objekte lokalisieren, Verbindungsseite ist von onePane Mode
      //
      mainListView = ( ListView )runningActivity.findViewById( R.id.exportLogsListView );
      mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
      changeDeviceButton = ( Button )runningActivity.findViewById( R.id.changeDeviceButton );
      exportLogsButton = ( Button )runningActivity.findViewById( R.id.exportLogsButton );
      //
      TextView tv = new TextView( runningActivity );
      tv.setText( "TEXTHEADER" );
      mainListView.addHeaderView( tv );
      //
      return( null );
    }
    //
    // Verbindungsseite via twoPane ausgewählt
    //
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_export_log, container, false );
    //
    // Objekte lokalisieren
    //
    mainListView = ( ListView )rootView.findViewById( R.id.exportLogsListView );
    mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
    changeDeviceButton = ( Button )rootView.findViewById( R.id.changeDeviceButton );
    exportLogsButton = ( Button )rootView.findViewById( R.id.exportLogsButton );
    //
    TextView tv = new TextView( runningActivity );
    tv.setText( "TEXTHEADER" );
    mainListView.addHeaderView( tv );
    //
    return( rootView );
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public void onItemClick( AdapterView<?> parent, View clickedView, int position, long id )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "Click on ListView! Pos: <" + position + ">" );
    // // invertiere die Markierung im Adapter
    // logListAdapter.setMarked( position, !logListAdapter.getMarked( position ) );
    // // mache die Markierung auch im View (das wird ja sonst nicht automatisch aktualisiert)
    // ImageView ivMarked = ( ImageView )clickedView.findViewById( R.id.readLogMarkedIconView );
    // if( ivMarked != null )
    // {
    // if( logListAdapter.getMarked( position ) )
    // {
    // ivMarked.setImageResource( R.drawable.circle_full_yellow );
    // }
    // else
    // {
    // ivMarked.setImageResource( R.drawable.circle_empty_yellow );
    // }
    // }
  }

  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG ) Log.d( TAG, "onResume..." );
    // Listener aktivieren
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
    mainListView.setOnItemClickListener( this );
    changeDeviceButton = ( Button )runningActivity.findViewById( R.id.changeDeviceButton );
    changeDeviceButton.setOnClickListener( this );
    exportLogsButton = ( Button )runningActivity.findViewById( R.id.exportLogsButton );
    exportLogsButton.setOnClickListener( this );
    //
    // Liste füllen
    //
    selectedDeviceId = -1;
    Vector<Long[]> diveHeadList = logManager.getDiveListForDevice( selectedDeviceId );
    Log.d( TAG, "read a list of <" + diveHeadList + "> dive entrys..." );
    // TODO: hier geht es weiter
  }
}
