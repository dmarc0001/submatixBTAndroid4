/**
 * Code für Grundformular exportLog
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * 
 * Stand: 28.08.2013
 */
package de.dmarcini.submatix.android4.full.gui;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.full.exceptions.XMLFileCreatorException;
import de.dmarcini.submatix.android4.full.utils.CommToast;
import de.dmarcini.submatix.android4.full.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.full.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.full.utils.SPX42ReadLogListArrayAdapter;
import de.dmarcini.submatix.android4.full.utils.UDDFFileCreateClass;
import de.dmarcini.submatix.android4.full.utils.UserAlertDialogFragment;

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
  private static final String        TAG              = SPXExportLogFragment.class.getSimpleName();
  private Activity                   runningActivity  = null;
  private ListView                   mainListView     = null;
  private SPX42LogManager            logManager       = null;
  private int                        selectedDeviceId = -1;
  private Button                     changeDeviceButton;
  private Button                     exportLogsButton;
  private CommToast                  theToast         = null;
  private final boolean              isFileZipped     = false;
  private WaitProgressFragmentDialog pd               = null;

  /**
   * 
   * Exportiere die markierten Einträge, falls welche markiert sind
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 04.01.2014
   * 
   * @param markedItems
   * 
   *          TODO: Mach das mit Nachrichten, damit die Oberfläche nicht einfiert
   */
  private void exportSelectedLogItems( SPX42ReadLogListArrayAdapter rAdapter )
  {
    int itemIndex = 0;
    final Vector<ReadLogItemObj> lItems = new Vector<ReadLogItemObj>();
    File tempDir;
    UDDFFileCreateClass uddfClass = null;
    String xmlFileName;
    //
    if( rAdapter.getMarkedItems().isEmpty() )
    {
      Log.i( TAG, "exportSelectedLogItems: not selected items" );
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "exportSelectedLogItems: export %d selected items...", rAdapter.getMarkedItems().size() ) );
    // die LogObjekte in den Vector kopieren
    Iterator<Integer> it = rAdapter.getMarkedItems().iterator();
    while( it.hasNext() )
    {
      // den ersten index bitteschön!
      itemIndex = it.next();
      // das Objekt kopiern
      ReadLogItemObj rlo = rAdapter.getItem( itemIndex );
      // noch die Positon merken, damit ich dann die Markierung löschen kann
      rlo.tagId = itemIndex;
      lItems.add( rlo );
    }
    // gib mir mal einen Iterator für die Einträge
    Iterator<ReadLogItemObj> rloIt = lItems.iterator();
    //
    try
    {
      // erzeuge eine Klasse zum generieren der Exort-UDDF-Files
      uddfClass = new UDDFFileCreateClass( logManager );
    }
    catch( ParserConfigurationException ex )
    {
      theToast.showConnectionToastAlert( ex.getLocalizedMessage() );
      Log.e( TAG, ex.getLocalizedMessage() );
      return;
    }
    catch( TransformerException ex )
    {
      theToast.showConnectionToastAlert( ex.getLocalizedMessage() );
      Log.e( TAG, ex.getLocalizedMessage() );
      return;
    }
    catch( TransformerFactoryConfigurationError ex )
    {
      theToast.showConnectionToastAlert( ex.getLocalizedMessage() );
      Log.e( TAG, ex.getLocalizedMessage() );
      return;
    }
    catch( XMLFileCreatorException ex )
    {
      theToast.showConnectionToastAlert( ex.getLocalizedMessage() );
      Log.e( TAG, ex.getLocalizedMessage() );
      return;
    }
    //
    // temporaeres verzeichnis für die zu exportierenden Dateien
    //
    tempDir = new File( FragmentCommonActivity.databaseDir.getAbsolutePath() + File.separator + "temp" );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "temporary path: " + tempDir.getAbsolutePath() );
    //
    // stelle sicher, dass ein exportverzeichnis existiert
    //
    if( !tempDir.exists() || !tempDir.isDirectory() )
    {
      if( !tempDir.mkdirs() )
      {
        theToast.showConnectionToastAlert( String.format( getResources().getString( R.string.toast_export_cant_create_dir ), tempDir.getAbsolutePath() ) );
        return;
      }
    }
    openWaitDial( lItems.size(), "..." );
    while( rloIt.hasNext() )
    {
      // den ersten Index bitteschön!
      ReadLogItemObj rlo = rloIt.next();
      // erzeuge die XML...
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "exportThread: export dive %d db-id: %d...", rlo.numberOnSPX, rlo.dbId ) );
      if( pd != null )
      {
        pd.setSubMessage( String.format( "file nr %d", rlo.numberOnSPX ) );
        Thread.yield();
      }
      DateTime st = new DateTime( rlo.startTimeMilis );
      xmlFileName = String.format( Locale.ENGLISH, "%s%sdive_%07d_at_%04d%02d%02d%02d%02d%02d.uddf", tempDir.getAbsolutePath(), File.separator, rlo.numberOnSPX, st.getYear(),
              st.getMonthOfYear(), st.getDayOfMonth(), st.getHourOfDay(), st.getMinuteOfHour(), st.getSecondOfMinute() );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "create uddf-file: <" + xmlFileName + ">" );
      uddfClass.createXML( new File( xmlFileName ), rlo, isFileZipped );
      //
      // die Markierung umkehren
      // geht nicht im Thread!
      // rlo.isMarked = false;
      // int firstPos = mainListView.getFirstVisiblePosition();
      // mainListView.setAdapter( mainListView.getAdapter() );
      // View v = mainListView.getChildAt( rlo.tagId );
      // int top = ( v == null ) ? 0 : v.getTop();
      // mainListView.setSelectionFromTop( firstPos, top );
    }
    if( pd != null )
    {
      pd.dismiss();
      pd = null;
    }
    //
    // TODO: Mail versenden
    //
    {}
    //
    // Aufräumen
    //
    // deleteRecursive( tempDir );
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
    Vector<ReadLogItemObj> diveList;
    SPX42ReadLogListArrayAdapter logListAdapter;
    Resources res;
    //
    res = runningActivity.getResources();
    //
    // Creiere einen Adapter
    //
    logListAdapter = new SPX42ReadLogListArrayAdapter( runningActivity, R.layout.read_log_array_adapter_view, FragmentCommonActivity.getAppStyle() );
    logListAdapter.setShowSavedStatus( false );
    mainListView.setAdapter( logListAdapter );
    mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
    //
    // lese eine Liste der Tauchgänge ein
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "read divelist for dbId: <" + diveId + ">..." );
    diveList = logManager.getDiveListForDevice( diveId, res );
    Iterator<ReadLogItemObj> it = diveList.iterator();
    //
    // Die Liste in den Adapter implementieren
    //
    while( it.hasNext() )
    {
      ReadLogItemObj rlo = it.next();
      // Eintrag einbauen
      logListAdapter.add( rlo );
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
        exportSelectedLogItems( rAdapter );
        // rAdapter.clearMaredItems();
        // neu zeichnen der Elemente erzwingen
        // mainListView.setAdapter( rAdapter );
      }
    }
  }

  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView = null;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView..." );
    theToast = new CommToast( getActivity() );
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

  /**
   * 
   * vernichte rekursiv den Ordner mit allen Dateien darin
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 08.01.2014
   * 
   * @param fileOrDirectory
   */
  private void deleteRecursive( File fileOrDirectory )
  {
    if( fileOrDirectory.isDirectory() )
    {
      for( File child : fileOrDirectory.listFiles() )
      {
        deleteRecursive( child );
      }
      fileOrDirectory.delete();
    }
  }

  /**
   * 
   * Öffne einen wartedialog
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 08.01.2014
   * 
   * @param maxevents
   * @param msg
   */
  public void openWaitDial( int maxevents, String msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "openWaitDial()..." );
    //
    // wenn ein Dialog da ist, erst mal aus den Fragmenten entfernen
    //
    FragmentTransaction ft = runningActivity.getFragmentManager().beginTransaction();
    Fragment prev = runningActivity.getFragmentManager().findFragmentByTag( "dialog" );
    if( prev != null )
    {
      ft.remove( prev );
    }
    if( pd != null )
    {
      pd.dismiss();
    }
    pd = new WaitProgressFragmentDialog( runningActivity.getResources().getString( R.string.logread_please_patient ), msg );
    pd.setCancelable( true );
    pd.setMax( maxevents );
    pd.setProgress( 0 );
    ft.addToBackStack( null );
    pd.show( ft, "dialog" );
  }
}
