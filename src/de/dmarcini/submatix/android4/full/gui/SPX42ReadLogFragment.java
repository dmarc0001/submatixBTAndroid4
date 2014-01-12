package de.dmarcini.submatix.android4.full.gui;

import java.io.File;
import java.util.Vector;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import de.dmarcini.submatix.android4.full.utils.LogXMLCreator;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.full.utils.SPX42DiveHeadData;
import de.dmarcini.submatix.android4.full.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.full.utils.SPX42ReadLogListArrayAdapter;
import de.dmarcini.submatix.android4.full.utils.UserAlertDialogFragment;

/**
 * 
 * Fragment zur Anzeige der Liste der Logeinträge im SPX
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class SPX42ReadLogFragment extends Fragment implements IBtServiceListener, OnItemClickListener, OnClickListener
{
  private static final String          TAG                 = SPX42ReadLogFragment.class.getSimpleName();
  private Activity                     runningActivity     = null;
  private ListView                     mainListView        = null;
  private Button                       readDirButton       = null;
  private SPX42LogManager              logManager          = null;
  private SPX42ReadLogListArrayAdapter logListAdapter      = null;
  private WaitProgressFragmentDialog   pd                  = null;
  // aktuelles Log START
  private int                          logLineCount        = 0;
  private int                          logNumberOnSPX      = -1;
  private int                          currPositionOnItems = -1;
  private SPX42DiveHeadData            diveHeader          = null;
  private LogXMLCreator                xmlCreator          = null;
  // aktuelles Log END
  private Vector<Integer>              items               = null;
  private CommToast                    theToast            = null;
  private static final Pattern         fieldPatternUnderln = Pattern.compile( "[_.]" );
  private boolean                      isUnitImperial      = false;
  private boolean                      showAllLogEntrys    = true;
  private int                          countDirEntrys      = 0;

  /**
   * 
   * Bearbeite die Nachricht über einen Logeintrag
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 06.08.2013
   * 
   * @param msg
   */
  private void computeDirentry( BtServiceMessage msg )
  {
    // Fields
    // 0 => Nummer
    // 1 => Filename
    // 2 => Max Nummer
    String[] fields;
    String fileName;
    int dbId = -1;
    String detailText;
    int number, max;
    int day, month, year, hour, minute, second;
    boolean isSaved = false;
    Resources res = runningActivity.getResources();
    //
    if( !( msg.getContainer() instanceof String[] ) )
    {
      Log.e( TAG, "Message container not an String ARRAY!" );
      return;
    }
    fields = ( String[] )msg.getContainer();
    if( fields.length < 3 )
    {
      Log.e( TAG, "recived message for logdir has lower than 3 fields. It is wrong! Abort!" );
      return;
    }
    // Wandel die Nummerierung in Integer um
    try
    {
      number = Integer.parseInt( fields[0], 16 );
      max = Integer.parseInt( fields[2], 16 );
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "Fail to convert Hex to int: " + ex.getLocalizedMessage() );
      return;
    }
    // Alles gelesen: Fertich
    if( number == max )
    {
      countDirEntrys = 0;
      if( pd != null )
      {
        pd.dismiss();
      }
      return;
    }
    fileName = fields[1];
    // verwandle die Dateiangabe in eine lesbare Datumsangabe
    // Format des Strings ist ja
    // TAG_MONAT_JAHR_STUNDE_MINUTE_SEKUNDE
    // des Beginns der Aufzeichnung
    fields = fieldPatternUnderln.split( fields[1] );
    try
    {
      day = Integer.parseInt( fields[0] );
      month = Integer.parseInt( fields[1] );
      year = Integer.parseInt( fields[2] ) + 2000;
      hour = Integer.parseInt( fields[3] );
      minute = Integer.parseInt( fields[4] );
      second = Integer.parseInt( fields[5] );
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "Fail to convert Hex to int: " + ex.getLocalizedMessage() );
      return;
    }
    // So, die Angaben des SPX sind immer im Localtime-Format
    // daher werde ich die auch so interpretieren
    // Die Funktion macht das in der default-Lokalzone, sollte also
    // da sein, wo der SPX auch ist... (schwieriges Thema)
    DateTime tm = new DateTime( year, month, day, hour, minute, second );
    //
    // Jetzt ist der Zeitpunkt, die Datenbank zu befragen, ob das Logteilchen schon gesichert ist
    //
    isSaved = logManager.isLogInDatabase( FragmentCommonActivity.spxConfig.getSerial(), fileName );
    if( isSaved )
    {
      if( showAllLogEntrys )
      {
        SPX42DiveHeadData diveHead = logManager.getDiveHeader( FragmentCommonActivity.spxConfig.getSerial(), fileName );
        detailText = makeDetailText( diveHead );
        dbId = diveHead.diveId;
        //
        // jetzt eintagen in die Anzeige
        //
        ReadLogItemObj rlio = new ReadLogItemObj( isSaved, String.format( "#%03d: %s", number, tm.toString( FragmentCommonActivity.localTimeFormatter ) ), fileName, detailText,
                dbId, number, tm.getMillis() );
        // Eintrag an den Anfang stellen
        logListAdapter.insert( rlio, 0 );
      }
      else
      {
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "ignore saved log while \"show all logitems\" is not set..." );
      }
    }
    else
    {
      detailText = res.getString( R.string.logread_not_saved_yet_msg );
      //
      // jetzt eintagen in die Anzeige
      //
      ReadLogItemObj rlio = new ReadLogItemObj( isSaved, String.format( "#%03d: %s", number, tm.toString( FragmentCommonActivity.localTimeFormatter ) ), fileName, detailText,
              dbId, number, tm.getMillis() );
      // Eintrag an den Anfang stellen
      logListAdapter.insert( rlio, 0 );
    }
    if( pd != null )
    {
      pd.setMessage( String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_directory ), ++countDirEntrys ) );
    }
  }

  /**
   * 
   * Bearbeite ein Array mit Loginfos
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 07.08.2013
   * 
   * @param msg
   */
  private void computeLogLine( BtServiceMessage msg )
  {
    if( msg.getContainer() instanceof String[] )
    {
      logLineCount++;
      xmlCreator.appendLogLine( ( String[] )msg.getContainer() );
      if( pd != null )
      {
        pd.setSubMessage( String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_count_items ), logLineCount ) );
      }
    }
  }

  /**
   * 
   * Aus dem Preferenzen lesen, ob alle Logeinträge gezeigt werden sollen
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 12.01.2014
   * 
   * @return
   */
  private boolean getShowAllEntrysFromPrefs()
  {
    boolean showAll = true;
    //
    // Sollen alle Einträge angezeigt werden oder nur bisher unbekannte?
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( runningActivity );
    if( !sPref.contains( "keyProgShowAllLogentrys" ) )
    {
      // Das wird nix, Voreinstellunge ist ALLE ANZEIGEN
      showAll = true;
      Log.w( TAG, "there is not preference key for showAllLogEntrys!" );
      return( showAll );
    }
    // der Schlüssel ist da, ist da auch eine Mailadresse hinterlegt?
    showAll = sPref.getBoolean( "keyProgShowAllLogentrys", true );
    if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "show all logentrys is <" + showAll + ">" );
    return( showAll );
  }

  @Override
  public void handleMessages( int what, BtServiceMessage msg )
  {
    switch ( what )
    {
    //
    // ################################################################
    // Computer wurde verbunden
    // ################################################################
      case ProjectConst.MESSAGE_CONNECTED:
        msgConnected( msg );
        break;
      // ################################################################
      // Units vom SPX emnpfangen
      // ################################################################
      case ProjectConst.MESSAGE_UNITS_READ:
        msgReciveUnits( msg );
        break;
      // ################################################################
      // Verzeichniseintrag gefunden
      // ################################################################
      case ProjectConst.MESSAGE_DIRENTRY_READ:
        computeDirentry( msg );
        break;
      // ################################################################
      // Verzeichnis zuende
      // ################################################################
      case ProjectConst.MESSAGE_DIRENTRY_END:
        Log.i( TAG, "end of logdir recived" );
        setEventsEnabled( true );
        break;
      // ################################################################
      // Logfile START
      // ################################################################
      case ProjectConst.MESSAGE_LOGENTRY_START:
        if( msg.getContainer() instanceof String )
        {
          logLineCount = 0;
          String num = ( String )msg.getContainer();
          Log.i( TAG, "start logentry logNumberOnSPX <" + num + "> on SPX" );
          if( pd != null )
          {
            //
            // versuche den Titel zu ändern
            // und vermerke die aktuelle Nummer des Eintrages
            //
            try
            {
              // ich muss ja später wiedererkennen, welche Logdaten nun kommen
              logNumberOnSPX = Integer.parseInt( num.trim(), 16 );
              startLogWriting();
              pd.setMessage( String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_header ), logNumberOnSPX ) );
            }
            catch( NumberFormatException ex )
            {
              Log.w( TAG, "Can't read logNumberOnSPX of log: <" + ex.getLocalizedMessage() + ">" );
            }
          }
        }
        break;
      // ################################################################
      // Logfile LINE
      // ################################################################
      case ProjectConst.MESSAGE_LOGENTRY_LINE:
        computeLogLine( msg );
        break;
      // ################################################################
      // Logfile END
      // ################################################################
      case ProjectConst.MESSAGE_LOGENTRY_STOP:
        if( msg.getContainer() instanceof String )
        {
          Log.i( TAG, "stop logentry logNumberOnSPX <" + ( String )msg.getContainer() + "> on SPX" );
          //
          // XML-Datei schliessen
          //
          stopLogWriting();
          //
          // und nun noch weitere lesen?
          //
          if( items != null && !items.isEmpty() )
          {
            currPositionOnItems = items.remove( 0 );
            ReadLogItemObj dirItem = logListAdapter.getItem( currPositionOnItems );
            ( ( FragmentCommonActivity )runningActivity ).askForLogDetail( dirItem.numberOnSPX );
          }
          else
          {
            if( pd != null )
            {
              pd.dismiss();
              pd = null;
            }
          }
        }
        break;
      // ################################################################
      // DEFAULT
      // ################################################################
      default:
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "unknown messsage with id <" + what + "> recived!" );
    }
  }

  /**
   * 
   * Erzeuge den Text für die Details wenn ein Tauchgang gespeichert war
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 19.08.2013
   * 
   * @param maxDepth
   * @param diveLength
   * @return
   */
  private String makeDetailText( SPX42DiveHeadData diveHead )
  {
    Resources res = runningActivity.getResources();
    String detailText = String.format( res.getString( R.string.logread_saved_format ), diveHead.maxDepth / 10.0, res.getString( R.string.app_unit_depth_metric ),
            diveHead.diveLength / 60, diveHead.diveLength % 60 );
    return( detailText );
  }

  /**
   * 
   * Lese Voreinstellung aus Preference und setzte bei BEdarf einen Header in die Liste
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 12.01.2014
   */
  private void makeShowEntryPreferences()
  {
    //
    // Einstellung(en) lesen
    //
    showAllLogEntrys = getShowAllEntrysFromPrefs();
    //
    // Anpassen der ListView, wenn erforderlich)
    //
    if( !showAllLogEntrys )
    {
      // TextView tv = new TextView( getActivity().getApplicationContext() );
      LayoutInflater mInflater = ( LayoutInflater )getActivity().getApplicationContext().getSystemService( Activity.LAYOUT_INFLATER_SERVICE );
      View headerView = mInflater.inflate( R.layout.read_log_show_not_all_view_header, null, false );
      View footerView = mInflater.inflate( R.layout.read_log_show_not_all_view_footer, null, false );
      mainListView.addHeaderView( headerView );
      mainListView.addFooterView( footerView );
    }
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    //
    // den Adapter leeren
    //
    logListAdapter.clear();
    // Logdirectory lesen
    ( ( FragmentCommonActivity )runningActivity ).aksForUnitsFromSPX42();
    ( ( FragmentCommonActivity )runningActivity ).askForLogDirectoryFromSPX();
    openWaitDial( 0, String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_header ), 1 ) );
    countDirEntrys = 0;
    if( pd != null )
    {
      pd.setMessage( String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_directory ), 0 ) );
    }
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {}

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {}

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    Log.v( TAG, "msgDisconnected" );
    Intent intent = new Intent( getActivity(), AreaListActivity.class );
    intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
    startActivity( intent );
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {}

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {}

  /**
   * 
   * Empfange Masseinheiten vom SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 15.08.2013
   * 
   * @param msg
   */
  private void msgReciveUnits( BtServiceMessage msg )
  {
    // Kommando SPX_GET_SETUP_UNITS
    // ~37:UD:UL:UW
    // UD= 1=Fahrenheit/0=Celsius => immer 0 in der aktuellen Firmware 2.6.7.7_U
    // UL= 0=metrisch 1=imperial
    // UW= 0->Salzwasser 1->Süßwasser
    int isImperial = 0;
    String[] unitsParm;
    //
    if( msg.getContainer() instanceof String[] )
    {
      unitsParm = ( String[] )msg.getContainer();
      if( ApplicationDEBUG.DEBUG )
      {
        try
        {
          Log.d( TAG, "SPX units settings <" + unitsParm[0] + "," + unitsParm[1] + "," + unitsParm[2] + "> recived" );
          Log.d( TAG, "temperature unit: " + ( unitsParm[0].equals( "0" ) ? "celsius" : "fahrenheit" ) );
          Log.d( TAG, "depth unit: " + ( unitsParm[1].equals( "0" ) ? "metric" : "imperial" ) );
          Log.d( TAG, "salnity: " + ( unitsParm[2].equals( "0" ) ? "salt water" : "fresh water" ) );
        }
        catch( IndexOutOfBoundsException ex )
        {
          Log.e( TAG, "msgReciveUnits: Units Object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
          return;
        }
      }
    }
    else
    {
      Log.e( TAG, "msgReciveUnits: message object not an String[] !" );
      return;
    }
    //
    // versuche die Parameter als Integer zu wandeln, gültige Werte erzeugen
    //
    try
    {
      isImperial = Integer.parseInt( unitsParm[1], 16 );
      if( isImperial > 0 )
      {
        isUnitImperial = true;
      }
      else
      {
        isUnitImperial = false;
      }
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveUnits: Units Object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "msgReciveUnits: Units Object is not an correct integer! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {}

  @Override
  public void onActivityCreated( Bundle bundle )
  {
    super.onActivityCreated( bundle );
    runningActivity = getActivity();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: ACTIVITY ATTACH" );
    try
    {
      mainListView = ( ListView )runningActivity.findViewById( R.id.readLogDirListView );
      logListAdapter = new SPX42ReadLogListArrayAdapter( runningActivity, R.layout.read_log_array_adapter_view, FragmentCommonActivity.getAppStyle() );
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
      uad.show( getFragmentManager(), "abortProgram" );
    }
  }

  /**
   * Für den Klick auf den LESEN-Button
   */
  @Override
  public void onClick( View view )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "Click..." );
    if( ( view instanceof Button ) && view.equals( readDirButton ) )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "Click on READ DIR BUTTON..." );
      items = logListAdapter.getMarkedItems();
      // wenn nix markiert ist, wech
      if( items.size() == 0 )
      {
        theToast.showConnectionToast( runningActivity.getResources().getString( R.string.toast_read_logdir_no_selected_entrys ), false );
        return;
      }
      openWaitDial( items.size(), String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_header ), 1 ) );
      if( pd != null )
      {
        pd.setSubMessage( String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_count_items ), 0 ) );
      }
      //
      // so, ab hier wird dann feste gelesen!
      //
      currPositionOnItems = items.remove( 0 );
      ReadLogItemObj dirItem = logListAdapter.getItem( currPositionOnItems );
      ( ( FragmentCommonActivity )runningActivity ).askForLogDetail( dirItem.numberOnSPX );
    }
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    // Layout View
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate..." );
    theToast = new CommToast( getActivity() );
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
      mainListView = ( ListView )runningActivity.findViewById( R.id.readLogDirListView );
      mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
      readDirButton = ( Button )runningActivity.findViewById( R.id.readLogDirButton );
      //
      // Einstellung(en) lesen und Oberfläche einstellen
      //
      makeShowEntryPreferences();
      showAllLogEntrys = getShowAllEntrysFromPrefs();
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
    mainListView = ( ListView )rootView.findViewById( R.id.readLogDirListView );
    mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
    readDirButton = ( Button )rootView.findViewById( R.id.readLogDirButton );
    //
    // Einstellung(en) lesen und Oberfläche einstellen
    //
    makeShowEntryPreferences();
    return( rootView );
  }

  /**
   * Für das Klicken auf einen Directory Eintrag
   */
  @Override
  public void onItemClick( AdapterView<?> parent, View clickedView, int position, long id )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "Click on ListView! Pos: <" + position + ">" );
    // invertiere die Markierung im Adapter
    logListAdapter.setMarked( position, !logListAdapter.getMarked( position ) );
    // mache die Markierung auch im View (das wird ja sonst nicht automatisch aktualisiert)
    ImageView ivMarked = ( ImageView )clickedView.findViewById( R.id.readLogMarkedIconView );
    if( ivMarked != null )
    {
      if( logListAdapter.getMarked( position ) )
      {
        ivMarked.setImageResource( R.drawable.circle_full_yellow );
      }
      else
      {
        ivMarked.setImageResource( R.drawable.circle_empty_yellow );
      }
    }
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
    // Listener aktivieren
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
    mainListView.setOnItemClickListener( this );
    readDirButton = ( Button )runningActivity.findViewById( R.id.readLogDirButton );
    readDirButton.setOnClickListener( this );
    setEventsEnabled( false );
  }

  /**
   * 
   * Öffne einen wartedialog
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 07.08.2013
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
    pd = new WaitProgressFragmentDialog();
    pd.setTitle( runningActivity.getResources().getString( R.string.logread_please_patient ) );
    pd.setMessage( msg );
    pd.setCancelable( true );
    pd.setMax( maxevents );
    pd.setProgress( 0 );
    ft.addToBackStack( null );
    pd.show( ft, "dialog" );
  }

  /**
   * 
   * Wenn das lesen des Verzeichnisses zuende ist, Funktionen freigeben
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 07.08.2013
   */
  private void setEventsEnabled( boolean enabled )
  {
    readDirButton.setEnabled( enabled );
    mainListView.setEnabled( enabled );
  }

  /**
   * 
   * Beginne mit dem Einlesen eines Logeintrages
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 19.08.2013
   */
  private void startLogWriting()
  {
    if( logNumberOnSPX == -1 ) return;
    if( currPositionOnItems == -1 ) return;
    ReadLogItemObj rlio = logListAdapter.getItem( currPositionOnItems );
    //
    // erst mal ein neues Headerobjekt erzeugen
    //
    diveHeader = new SPX42DiveHeadData();
    //
    // erzeuge eine XML-Datei
    //
    diveHeader.xmlFile = new File( String.format( "%s%s%s-%04d-%s.xml", FragmentCommonActivity.databaseDir.getAbsolutePath(), File.separator,
            FragmentCommonActivity.spxConfig.getSerial(), logNumberOnSPX, FragmentCommonActivity.mBtAdapter.getAddress().replaceAll( ":", "_" ) ) );
    diveHeader.fileNameOnSpx = rlio.itemNameOnSPX;
    diveHeader.startTime = rlio.startTimeMilis;
    diveHeader.diveNumberOnSPX = rlio.numberOnSPX;
    diveHeader.deviceSerialNumber = FragmentCommonActivity.spxConfig.getSerial();
    diveHeader.units = ( isUnitImperial ? "i" : "m" );
    diveHeader.deviceId = logManager.getIdForDeviceFromSerial( FragmentCommonActivity.spxConfig.getSerial() );
    //
    // und einen neuen XML-Dateicreator
    //
    try
    {
      xmlCreator = new LogXMLCreator( diveHeader );
    }
    catch( XMLFileCreatorException ex )
    {
      Log.e( TAG, "XML Creator Exception <" + ex.getLocalizedMessage() + ">" );
    }
  }

  /**
   * 
   * Wenn das Einlesen eines Logs beendet ist
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 19.08.2013
   */
  private void stopLogWriting()
  {
    String detailText;
    //
    if( xmlCreator != null )
    {
      try
      {
        xmlCreator.closeLog();
        logManager.saveDive( diveHeader );
        ReadLogItemObj rlo = logListAdapter.getItem( currPositionOnItems );
        detailText = makeDetailText( diveHeader );
        rlo.itemDetail = detailText;
        rlo.dbId = diveHeader.diveId;
        rlo.isMarked = false;
        rlo.isSaved = true;
        //
        // optisch mitarbeiten
        //
        int firstPos = mainListView.getFirstVisiblePosition();
        mainListView.setAdapter( logListAdapter );
        View v = mainListView.getChildAt( currPositionOnItems );
        int top = ( v == null ) ? 0 : v.getTop();
        mainListView.setSelectionFromTop( firstPos, top );
      }
      catch( NullPointerException ex )
      {
        Log.e( TAG, "Nullpointer Exception <" + ex.getLocalizedMessage() + ">" );
      }
      catch( XMLFileCreatorException ex )
      {
        Log.e( TAG, "XML Creator Exception <" + ex.getLocalizedMessage() + ">" );
      }
    }
    //
    // aufräumen
    //
    logNumberOnSPX = -1;
    currPositionOnItems = -1;
    diveHeader = null;
    xmlCreator = null;
  }
}
