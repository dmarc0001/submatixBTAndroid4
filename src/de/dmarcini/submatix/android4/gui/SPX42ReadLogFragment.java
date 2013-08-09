package de.dmarcini.submatix.android4.gui;

import java.io.File;
import java.util.Vector;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.utils.CommToast;
import de.dmarcini.submatix.android4.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.utils.ProjectConst;
import de.dmarcini.submatix.android4.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.utils.SPX42ReadLogListArrayAdapter;
import de.dmarcini.submatix.android4.utils.UserAlertDialogFragment;

public class SPX42ReadLogFragment extends Fragment implements IBtServiceListener, OnItemClickListener, OnClickListener
{
  private static final String          TAG                 = SPX42ReadLogFragment.class.getSimpleName();
  private Activity                     runningActivity     = null;
  private ListView                     mainListView        = null;
  private Button                       readDirButton       = null;
  private SPX42LogManager              logManager          = null;
  private SPX42ReadLogListArrayAdapter logListAdapter      = null;
  private WaitProgressFragmentDialog   pd                  = null;
  private int                          logLineCount        = 0;
  private Vector<Integer>              items               = null;
  private CommToast                    theToast            = null;
  // private static final Pattern fieldPatternDp = Pattern.compile( ":" );
  private static final Pattern         fieldPatternUnderln = Pattern.compile( "[_.]" );
  private static String                timeFormatterString = "yyyy-MM-dd - hh:mm:ss";

  /**
   * 
   * Bearbeite die Nachricht über einen Logeintrag
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
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
    int number, max;
    int day, month, year, hour, minute, second;
    boolean isSaved = false;
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
    // da sein, wio der SPX auch ist... (schwieriges Thema)
    DateTime tm = new DateTime( year, month, day, hour, minute, second );
    DateTimeFormatter fmt = DateTimeFormat.forPattern( timeFormatterString );
    //
    // Jetzt ist der Zeitpunkt, die Datenbank zu befragen, ob das Logteilchen schon gesichert ist
    //
    isSaved = logManager.isLogInDatabase( FragmentCommonActivity.serialNumber, fileName );
    //
    // jetzt eintagen in die Anzeige
    //
    ReadLogItemObj rlio = new ReadLogItemObj( isSaved, String.format( "#%03d: %s", number, tm.toString( fmt ) ), fileName, runningActivity.getResources().getString(
            R.string.logread_not_saved_yet_msg ), -1, number );
    // Eintrag an den Anfang stellen
    logListAdapter.insert( rlio, 0 );
  }

  /**
   * 
   * Bearbeite ein array mit Loginfos
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.08.2013
   * @param msg
   */
  private void computeLogLine( BtServiceMessage msg )
  {
    if( msg.getContainer() instanceof String[] )
    {
      logLineCount++;
      if( pd != null )
      {
        pd.setSubMessage( String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_count_items ), logLineCount ) );
      }
    }
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
          Log.i( TAG, "start logentry number <" + num + "> on SPX" );
          if( pd != null )
          {
            // versuche den Titel zu ändern
            int number = 0;
            try
            {
              number = Integer.parseInt( num.trim(), 16 );
            }
            catch( NumberFormatException ex )
            {
              Log.w( TAG, "Can't read number of log: <" + ex.getLocalizedMessage() + ">" );
            }
            pd.setMessage( String.format( runningActivity.getResources().getString( R.string.logread_please_wait_dialog_header ), number ) );
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
          Log.i( TAG, "stop logentry number <" + ( String )msg.getContainer() + "> on SPX" );
          if( items != null && !items.isEmpty() )
          {
            int position = items.remove( 0 );
            ReadLogItemObj dirItem = logListAdapter.getItem( position );
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
        if( BuildConfig.DEBUG ) Log.i( TAG, "unknown messsage with id <" + what + "> recived!" );
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
    ( ( FragmentCommonActivity )runningActivity ).askForLogDirectoryFromSPX();
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
      mainListView = ( ListView )runningActivity.findViewById( R.id.readLogDirListView );
      logListAdapter = new SPX42ReadLogListArrayAdapter( runningActivity, R.layout.read_log_array_adapter_view, FragmentCommonActivity.getAppStyle() );
      //
      // FOR DEBUG:
      //
      // ReadLogItemObj rlio = new ReadLogItemObj( false, "PROGRAM1", "PROGRAMDETAIL1" );
      // logListAdapter.add( rlio );
      // rlio = new ReadLogItemObj( false, "PROGRAM2", "PROGRAMDETAIL2" );
      // logListAdapter.add( rlio );
      // rlio = new ReadLogItemObj( false, "PROGRAM3", "PROGRAMDETAIL3" );
      // logListAdapter.add( rlio );
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

  /**
   * Für den Klick auf den LESEN-Button
   */
  @Override
  public void onClick( View view )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "Click..." );
    if( ( view instanceof Button ) && view.equals( readDirButton ) )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "Click on READ DIR BUTTON..." );
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
      int position = items.remove( 0 );
      ReadLogItemObj dirItem = logListAdapter.getItem( position );
      ( ( FragmentCommonActivity )runningActivity ).askForLogDetail( dirItem.numberOnSPX );
    }
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    // Layout View
    super.onCreate( savedInstanceState );
    if( BuildConfig.DEBUG ) Log.v( TAG, "onCreate..." );
    theToast = new CommToast( getActivity() );
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
      mainListView = ( ListView )runningActivity.findViewById( R.id.readLogDirListView );
      mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
      readDirButton = ( Button )runningActivity.findViewById( R.id.readLogDirButton );
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
    return( rootView );
  }

  /**
   * Für das Klicken auf einen Directory Eintrag
   */
  @Override
  public void onItemClick( AdapterView<?> parent, View clickedView, int position, long id )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "Click on ListView! Pos: <" + position + ">" );
    // invertiere die Markierung im Adapter
    logListAdapter.setMarked( position, !logListAdapter.getMarked( position ) );
    // mache die Markierung auch im View (das wird ja sonst nicht automatisch aktualisiert)
    ImageView ivMarked = ( ImageView )clickedView.findViewById( R.id.readLogMarkedIconView );
    if( ivMarked != null )
    {
      if( logListAdapter.getMarked( position ) )
      {
        ivMarked.setImageResource( R.drawable.star_full_yellow );
      }
      else
      {
        ivMarked.setImageResource( R.drawable.star_empty_yellow );
      }
    }
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.08.2013
   * @param maxevents
   * @param msg
   */
  public void openWaitDial( int maxevents, String msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "openWaitDial()..." );
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
   * Wenn das lesen des Verzeichnisses zuende ist, Funktionen freigeben
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.08.2013
   */
  private void setEventsEnabled( boolean enabled )
  {
    readDirButton.setEnabled( enabled );
    mainListView.setEnabled( enabled );
  }
}
