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
import android.os.Handler;
import android.os.Message;
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
  private static final String          TAG              = SPXExportLogFragment.class.getSimpleName();
  private Activity                     runningActivity  = null;
  private ListView                     mainListView     = null;
  private SPX42LogManager              logManager       = null;
  private int                          selectedDeviceId = -1;
  private Button                       changeDeviceButton;
  private Button                       exportLogsButton;
  private CommToast                    theToast         = null;
  private final boolean                isFileZipped     = false;
  private final Vector<ReadLogItemObj> lItems           = new Vector<ReadLogItemObj>();
  private WaitProgressFragmentDialog   pd               = null;
  private File                         tempDir          = null;
  private final Handler                mHandler         = new Handler() {
                                                          @Override
                                                          public void handleMessage( Message msg )
                                                          {
                                                            if( !( msg.obj instanceof BtServiceMessage ) )
                                                            {
                                                              Log.e( TAG, "Handler::handleMessage: Recived Message is NOT type of BtServiceMessage!" );
                                                              return;
                                                            }
                                                            BtServiceMessage smsg = ( BtServiceMessage )msg.obj;
                                                            handleMessages( msg.what, smsg );
                                                          }
                                                        };

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
   * Exportiere einen Eintrag in eigenem Thread
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 09.01.2014
   * 
   * @param rlo
   */
  private void exportOneLogItem( ReadLogItemObj _rlo, File _tempDir )
  {
    final ReadLogItemObj rlo = _rlo;
    final File tempDir = _tempDir;
    Thread exportThread = null;
    //
    exportThread = new Thread() {
      @Override
      public void run()
      {
        UDDFFileCreateClass uddfClass = null;
        String uddfFileName = null;
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
          mHandler.obtainMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, rlo ) ).sendToTarget();
          return;
        }
        catch( TransformerException ex )
        {
          theToast.showConnectionToastAlert( ex.getLocalizedMessage() );
          Log.e( TAG, ex.getLocalizedMessage() );
          mHandler.obtainMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, rlo ) ).sendToTarget();
          return;
        }
        catch( TransformerFactoryConfigurationError ex )
        {
          theToast.showConnectionToastAlert( ex.getLocalizedMessage() );
          Log.e( TAG, ex.getLocalizedMessage() );
          mHandler.obtainMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, rlo ) ).sendToTarget();
          return;
        }
        catch( XMLFileCreatorException ex )
        {
          theToast.showConnectionToastAlert( ex.getLocalizedMessage() );
          Log.e( TAG, ex.getLocalizedMessage() );
          mHandler.obtainMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, new BtServiceMessage( ProjectConst.MESSAGE_LOCAL_EXPORTERR, rlo ) ).sendToTarget();
          return;
        }
        // erzeuge die XML...
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "exportThread: export dive %d db-id: %d...", rlo.numberOnSPX, rlo.dbId ) );
        DateTime st = new DateTime( rlo.startTimeMilis );
        uddfFileName = String.format( Locale.ENGLISH, "%s%sdive_%07d_at_%04d%02d%02d%02d%02d%02d.uddf", tempDir.getAbsolutePath(), File.separator, rlo.numberOnSPX, st.getYear(),
                st.getMonthOfYear(), st.getDayOfMonth(), st.getHourOfDay(), st.getMinuteOfHour(), st.getSecondOfMinute() );
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "create uddf-file: <" + uddfFileName + ">" );
        uddfClass.createXML( new File( uddfFileName ), rlo, isFileZipped );
        mHandler.obtainMessage( ProjectConst.MESSAGE_LOCAL_LOGEXPORTED, new BtServiceMessage( ProjectConst.MESSAGE_LOCAL_LOGEXPORTED, rlo ) ).sendToTarget();
      }
    };
    exportThread.setName( "log_export_thread" );
    exportThread.start();
  }

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
   */
  private void exportSelectedLogItems( SPX42ReadLogListArrayAdapter rAdapter )
  {
    int itemIndex = 0;
    //
    if( rAdapter.getMarkedItems().isEmpty() )
    {
      Log.i( TAG, "exportSelectedLogItems: not selected items" );
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "exportSelectedLogItems: export %d selected items...", rAdapter.getMarkedItems().size() ) );
    // die LogObjekte in den Vector kopieren
    lItems.clear();
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
    //
    // so, jetzt hab ich die infrage kommenden Einträge
    // jetz starte ich den Thread für den ersten Eintrag
    //
    if( !lItems.isEmpty() )
    {
      // das erste Element entfernen und exportieren
      ReadLogItemObj rlo = lItems.remove( 0 );
      //
      // die Markierung umkehren
      //
      rlo.isMarked = false;
      int firstPos = mainListView.getFirstVisiblePosition();
      mainListView.setAdapter( mainListView.getAdapter() );
      View v = mainListView.getChildAt( rlo.tagId );
      int top = ( v == null ) ? 0 : v.getTop();
      mainListView.setSelectionFromTop( firstPos, top );
      //
      // temporaeres Verzeichnis für die zu exportierenden Dateien
      //
      tempDir = new File( FragmentCommonActivity.databaseDir.getAbsolutePath() + File.separator + "temp" );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "temporary path: " + tempDir.getAbsolutePath() );
      // alte Elemente vernichten, falls vorhanden
      if( tempDir.exists() ) deleteRecursive( tempDir );
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
      openWaitDial( lItems.size(), String.format( "file nr %d", rlo.numberOnSPX ) );
      exportOneLogItem( rlo, tempDir );
    }
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
      // Logeintrag erfolgreich exportiert
      // ################################################################
      case ProjectConst.MESSAGE_LOCAL_LOGEXPORTED:
        msgExportOk( msg );
        break;
      // ################################################################
      // Logeintrag erfolgreich exportiert
      // ################################################################
      case ProjectConst.MESSAGE_LOCAL_EXPORTERR:
        msgExportError( msg );
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

  /**
   * 
   * Wenn der Export schief ging, Ende und aufräumen
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 09.01.2014
   * 
   * @param msg
   */
  private void msgExportError( BtServiceMessage msg )
  {
    // Messagebox verschwinden lassen
    if( pd != null )
    {
      pd.dismiss();
      pd = null;
    }
    //
    // Aufräumen
    //
    if( tempDir != null )
    {
      deleteRecursive( tempDir );
      tempDir = null;
    }
  }

  /**
   * 
   * Export Ok, nächster Export oder Mail senden?
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 09.01.2014
   * 
   * @param msg
   */
  private void msgExportOk( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "export ok, check next entry..." );
    //
    if( !lItems.isEmpty() )
    {
      //
      // es sind noch Elemente zu exportieren
      // das erste Element entfernen und exportieren
      //
      ReadLogItemObj rlo = lItems.remove( 0 );
      //
      // die Markierung umkehren
      //
      rlo.isMarked = false;
      int firstPos = mainListView.getFirstVisiblePosition();
      mainListView.setAdapter( mainListView.getAdapter() );
      View v = mainListView.getChildAt( rlo.tagId );
      int top = ( v == null ) ? 0 : v.getTop();
      mainListView.setSelectionFromTop( firstPos, top );
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
      if( pd != null )
      {
        pd.setSubMessage( String.format( "file nr %d", rlo.numberOnSPX ) );
      }
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "next entry..." );
      exportOneLogItem( rlo, tempDir );
    }
    else
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "send message..." );
      //
      // alle Elemente exortiert, jetzt bitte Mail fertig machen und versenden
      // TODO: Mail machen
      //
      // Aufräumen
      //
      if( pd != null )
      {
        pd.dismiss();
        pd = null;
      }
      if( tempDir != null )
      {
        deleteRecursive( tempDir );
        tempDir = null;
      }
    }
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
