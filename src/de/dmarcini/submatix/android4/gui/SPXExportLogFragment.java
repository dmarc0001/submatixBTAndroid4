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
import java.util.Iterator;
import java.util.Vector;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.res.Resources;
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
import android.widget.ImageView;
import android.widget.ListView;
import de.dmarcini.submatix.android4.ApplicationDEBUG;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.utils.ProjectConst;
import de.dmarcini.submatix.android4.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.utils.SPX42ReadLogListArrayAdapter;
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

  /**
   * 
   * Exportiere die markierten Einträge, falls welche markiert sind
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 04.01.2014
   * 
   * @param markedItems
   */
  private void exportSelectedLogItems( Vector<Integer> _markedItems )
  {
    Thread exportThread;
    final Vector<Integer> markedItems = _markedItems;
    //
    if( _markedItems.isEmpty() )
    {
      Log.i( TAG, "exportSelectedLogItems: not selected items" );
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "exportSelectedLogItems: export %d selected items...", _markedItems.size() ) );
    exportThread = new Thread() {
      @Override
      public void run()
      {
        Iterator<Integer> it = markedItems.iterator();
        int itemIndex;
        while( it.hasNext() )
        {
          itemIndex = it.next();
          if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "exportThread: export index %d ...", itemIndex ) );
        }
      }
    };
    exportThread.setName( "export_logs_to_xml_thread" );
    exportThread.start();
  }

  /**
   * 
   * Fülle den ListAdapter mit den Einträgen für Tauchgänge aus der Datenbank
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 04.01.2014
   * 
   * @param diveId
   * @return
   */
  private boolean fillListAdapter( int diveId )
  {
    Vector<Long[]> diveList;
    int diveNr = 0;
    DateTime startDateTime;
    SPX42ReadLogListArrayAdapter logListAdapter;
    Resources res;
    String detailText;
    //
    res = runningActivity.getResources();
    //
    // Creiere einen Adapter
    //
    logListAdapter = new SPX42ReadLogListArrayAdapter( runningActivity, R.layout.read_log_array_adapter_view, FragmentCommonActivity.getAppStyle() );
    mainListView.setAdapter( logListAdapter );
    mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
    //
    // lese eine Liste der Tauchgänge ein
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "read divelist for deviceId: <" + diveId + ">..." );
    diveList = logManager.getDiveListForDevice( diveId );
    Iterator<Long[]> it = diveList.iterator();
    //
    // Die Liste in den Adapter implementieren
    //
    while( it.hasNext() )
    {
      Long[] set = it.next(); // 0: diveID, 1: startTime, 2: diveLength, 3: SPX-DiveNr, 4: MAx Tiefe
      diveNr = ( int )( 0xffffffff & set[3] );
      startDateTime = new DateTime( set[1] * 1000 );
      detailText = String.format( res.getString( R.string.logread_saved_format ), set[4] / 10.0, res.getString( R.string.app_unit_depth_metric ), set[2] / 60, set[2] % 60 );
      //
      ReadLogItemObj rlio = new ReadLogItemObj( true, String.format( "#%03d: %s", diveNr, startDateTime.toString( FragmentCommonActivity.localTimeFormatter ) ), "file", detailText );
      // Eintrag an den Anfang stellen
      logListAdapter.add( rlio );
    }
    return( true );
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see de.dmarcini.submatix.android4.gui.IBtServiceListener#handleMessages(int, de.dmarcini.submatix.android4.comm.BtServiceMessage)
   */
  @Override
  public void handleMessages( int what, BtServiceMessage msg )
  {
    switch ( what )
    {
      case ProjectConst.MESSAGE_TICK:
        break;
      // ################################################################
      // JA, Positive Antwort
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_POSITIVE:
        onDialogPositive( ( DialogFragment )msg.getContainer() );
        break;
      // ################################################################
      // NEIN, negative antwort
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_NEGATIVE:
        onDialogNegative( ( DialogFragment )msg.getContainer() );
        break;
      // ################################################################
      // DEFAULT
      // ################################################################
      default:
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "unknown messsage with id <" + what + "> recived!" );
    }
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
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: ACTIVITY ATTACH" );
    try
    {
      mainListView = ( ListView )runningActivity.findViewById( R.id.exportLogsListView );
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
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: ATTACH" );
    //
    // die Datenbank öffnen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: create SQLite helper..." );
    DataSQLHelper sqlHelper = new DataSQLHelper( getActivity().getApplicationContext(), FragmentCommonActivity.databaseDir.getAbsolutePath() + File.separator
            + ProjectConst.DATABASE_NAME );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: open Database..." );
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
    SPX42ReadLogListArrayAdapter rAdapter;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "Click" );
    if( v instanceof Button )
    {
      if( ( Button )v == changeDeviceButton )
      {
        // Hier wird dann ein Dialog gebraucht!
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: call changeDeviceDialog!" );
        SelectDeviceDialogFragment dialog = new SelectDeviceDialogFragment();
        dialog.setDeviceList( logManager.getDeviceNameIdList() );
        dialog.show( getFragmentManager(), "SelectDeviceDialogFragment" );
      }
      else if( ( Button )v == exportLogsButton )
      {
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: EXPORT selected Items" );
        //
        // exportiere alle markierten Elemente
        //
        rAdapter = ( SPX42ReadLogListArrayAdapter )mainListView.getAdapter();
        exportSelectedLogItems( rAdapter.getMarkedItems() );
        rAdapter.clearMaredItems();
        // neu zeichnen der Elemente erzwingen
        mainListView.setAdapter( rAdapter );
      }
    }
  }

  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView = null;
    //
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
      //
      // Objekte lokalisieren, Verbindungsseite ist von onePane Mode
      //
      mainListView = ( ListView )runningActivity.findViewById( R.id.exportLogsListView );
      mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
      changeDeviceButton = ( Button )runningActivity.findViewById( R.id.changeDeviceButton );
      exportLogsButton = ( Button )runningActivity.findViewById( R.id.exportLogsButton );
      //
      // TextView tv = new TextView( runningActivity );
      // tv.setText( "TEXTHEADER" );
      // mainListView.addHeaderView( tv );
      //
      return( null );
    }
    //
    // Verbindungsseite via twoPane ausgewählt
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView: running two pane mode ..." );
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_export_log, container, false );
    setTitleString( "?" );
    //
    // Objekte lokalisieren
    //
    mainListView = ( ListView )rootView.findViewById( R.id.exportLogsListView );
    mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
    changeDeviceButton = ( Button )rootView.findViewById( R.id.changeDeviceButton );
    exportLogsButton = ( Button )rootView.findViewById( R.id.exportLogsButton );
    //
    // TextView tv = new TextView( runningActivity );
    // tv.setText( "TEXTHEADER2" );
    // mainListView.addHeaderView( tv );
    //
    return( rootView );
  }

  /**
   * 
   * Wenn der Dialog Positiv abgeschlossen wurde (OKO oder ähnlich)
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 01.12.2013
   * 
   * @param dialog
   */
  public void onDialogNegative( DialogFragment dialog )
  {
    if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "Negative dialog click!" );
  }

  /**
   * 
   * Wenn der Dialog negativ abgeschlossen wurde (Abbruchg o.ä.)
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 01.12.2013
   * 
   * @param dialog
   */
  public void onDialogPositive( DialogFragment dialog )
  {
    if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "Positive dialog click!" );
    if( dialog instanceof SelectDeviceDialogFragment )
    {
      SelectDeviceDialogFragment deviceDialog = ( SelectDeviceDialogFragment )dialog;
      selectedDeviceId = deviceDialog.getSelectedDeviceId();
      if( ApplicationDEBUG.DEBUG )
        Log.i( TAG, "onDialogNegative: selected Device Alias: <" + deviceDialog.getSelectedDeviceName() + "> Device-ID <" + deviceDialog.getSelectedDeviceId() + ">" );
      getActivity().getActionBar().setTitle( String.format( getResources().getString( R.string.export_header_device ), deviceDialog.getSelectedDeviceName() ) );
      if( selectedDeviceId > 0 )
      {
        // Vector<Long[]> diveHeadList = logManager.getDiveListForDevice( selectedDeviceId );
        fillListAdapter( selectedDeviceId );
      }
    }
    else
    {
      Log.i( TAG, "onDialogNegative: UNKNOWN dialog type" );
    }
  }

  /*
   * (nicht-Javadoc)
   * 
   * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
   */
  @Override
  public void onItemClick( AdapterView<?> parent, View clickedView, int position, long id )
  {
    SPX42ReadLogListArrayAdapter rlAdapter = null;
    //
    if( parent.equals( mainListView ) )
    {
      rlAdapter = ( SPX42ReadLogListArrayAdapter )mainListView.getAdapter();
      if( ApplicationDEBUG.DEBUG )
      {
        Log.d( TAG, "Click on mainListView! Pos: <" + position + ">" );
        if( rlAdapter.getMarked( position ) )
          Log.d( TAG, "View was SELECTED" );
        else
          Log.d( TAG, "View was UNSELECTED" );
      }
      //
      // invertiere die Markierung im Adapter
      //
      rlAdapter.setMarked( position, !rlAdapter.getMarked( position ) );
      //
      // mache die Markierung auch im View (das wird ja sonst nicht automatisch aktualisiert)
      //
      ImageView ivMarked = ( ImageView )clickedView.findViewById( R.id.readLogMarkedIconView );
      if( ivMarked != null )
      {
        if( rlAdapter.getMarked( position ) )
        {
          ivMarked.setImageResource( R.drawable.circle_full_yellow );
        }
        else
        {
          ivMarked.setImageResource( R.drawable.circle_empty_yellow );
        }
      }
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    // Listener abmelden
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume..." );
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
    if( selectedDeviceId > 0 )
    {
      fillListAdapter( selectedDeviceId );
    }
    // Listener aktivieren
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
  }

  /**
   * 
   * Setze den Titel in der Action Bar mit Test und Name des gelisteten Gerätes
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 01.12.2013
   * 
   * @param string
   */
  private void setTitleString( String devName )
  {
    String titleString;
    //
    titleString = String.format( getResources().getString( R.string.export_header_device ), devName );
    runningActivity.getActionBar().setTitle( titleString );
  }
}
