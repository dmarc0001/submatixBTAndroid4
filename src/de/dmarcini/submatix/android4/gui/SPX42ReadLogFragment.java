package de.dmarcini.submatix.android4.gui;

import java.io.File;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

public class SPX42ReadLogFragment extends Fragment implements IBtServiceListener, OnItemClickListener
{
  private static final String          TAG                 = SPX42ReadLogFragment.class.getSimpleName();
  private Activity                     runningActivity     = null;
  private ListView                     mainListView        = null;
  private SPX42LogManager              logManager          = null;
  private SPX42ReadLogListArrayAdapter logListAdapter      = null;
  private static final Pattern         fieldPatternDp      = Pattern.compile( ":" );
  private static final Pattern         fieldPatternUnderln = Pattern.compile( "[_.]" );
  private static String                timeFormatterString = "yyyy-MM-dd - hh:mm:ss";

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
        break;
      // ################################################################
      // DEFAULT
      // ################################################################
      default:
        if( BuildConfig.DEBUG ) Log.i( TAG, "unknown messsage with id <" + what + "> recived!" );
    }
  }

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
    // TODO: jetzt ist der Zeitpunkt, die Datenbank zu befragen, ob das Logteilchen schon gesichert ist
    //
    //
    // jetzt eintagen in die Anzeige
    //
    ReadLogItemObj rlio = new ReadLogItemObj( false, fileName, "DETAIL", -1, number );
    rlio.itemDetail = "DETAIL <" + number + "> " + tm.toString( fmt );
    // Eintrag an den Anfang stellen
    logListAdapter.insert( rlio, 0 );
    // return( String.format( "%d;%s;%s;%d;%d", number, fileName, tm.toString( fmt ), max, tm.getMillis() ) );
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    // mainListView = ( ListView )runningActivity.findViewById( R.id.readLogLinesListView );
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
      mainListView = ( ListView )runningActivity.findViewById( R.id.readLogLinesListView );
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
    mainListView.setChoiceMode( AbsListView.CHOICE_MODE_MULTIPLE );
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
    // Listener aktivieren
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
    mainListView.setOnItemClickListener( this );
  }

  @Override
  public void onItemClick( AdapterView<?> parent, View clickedView, int position, long id )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "Click on ListView! Pos: <" + position + ">" );
  }
}
