//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.submatix.android4.full.gui;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

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
import de.dmarcini.submatix.android4.full.dialogs.AreYouSureToDeleteFragment;
import de.dmarcini.submatix.android4.full.dialogs.SelectDeviceDialogFragment;
import de.dmarcini.submatix.android4.full.dialogs.UserAlertDialogFragment;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.full.interfaces.IBtServiceListener;
import de.dmarcini.submatix.android4.full.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.full.utils.SPX42LogManager;
import de.dmarcini.submatix.android4.full.utils.SPX42ReadLogListArrayAdapter;

/**
 * 
 * Ein Detsailfragment, welches die Verbindung mit dem SPX Managed
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.02.2014
 */
public class SPX42LogGraphSelectFragment extends Fragment implements IBtServiceListener, OnClickListener, OnItemClickListener
{
  @SuppressWarnings( "javadoc" )
  public static final String TAG                     = SPX42LogGraphSelectFragment.class.getSimpleName();
  // private CommToast theToast = null;
  private SPX42LogManager    logManager              = null;
  private int                selectedDeviceId        = -1;
  private String             selectedDeviceAlias     = null;
  private MainActivity       runningActivity;
  private Button             changeGraphDeviceButton = null;
  private Button             graphLogsButton         = null;
  private ListView           graphLogsListView       = null;
  private int                dbId                    = -1;
  private int                diveId                  = -1;
  private String             fragmentTitle           = "unknown";

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
    logListAdapter = new SPX42ReadLogListArrayAdapter( runningActivity, R.layout.read_log_array_adapter_view, MainActivity.getAppStyle() );
    logListAdapter.setShowSavedStatus( false );
    graphLogsListView.setAdapter( logListAdapter );
    graphLogsListView.setChoiceMode( AbsListView.CHOICE_MODE_SINGLE );
    //
    // lese eine Liste der Tauchgänge ein
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "read divelist for dbId: <" + diveId + ">..." );
    diveList = logManager.getDiveListForDevice( diveId, res, true );
    if( diveList != null && diveList.size() > 0 )
    {
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
    }
    return( true );
  }

  @Override
  public void handleMessages( int what, BtServiceMessage smsg )
  {
    // was war denn los? Welche Nachricht kam rein?
    switch ( what )
    {
    //
    // ################################################################
    // Service TICK empfangen
    // ################################################################
      case ProjectConst.MESSAGE_TICK:
        msgRecivedTick( smsg );
        break;
      // ################################################################
      // JA, Positive Antwort
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_POSITIVE:
        onDialogPositive( ( DialogFragment )smsg.getContainer() );
        break;
      // ################################################################
      // NEIN, negative antwort
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_NEGATIVE:
        onDialogNegative( ( DialogFragment )smsg.getContainer() );
        break;
      // ################################################################
      // Computer wird gerade verbunden
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTING:
        msgConnecting( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTED:
        msgConnected( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_DISCONNECTED:
        msgDisconnected( smsg );
        break;
      // ################################################################
      // Computer wurde getrennt
      // ################################################################
      case ProjectConst.MESSAGE_CONNECTERROR:
        msgConnectError( smsg );
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "handleMessages: unhadled message message with id <" + smsg.getId() + "> recived!" );
    }
  }

  /**
   * 
   * Die Anzeige der Seite
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 04.11.2012
   * 
   * @param inflater
   * @param container
   * @return
   */
  private View makeGraphSelectionView( LayoutInflater inflater, ViewGroup container )
  {
    View rootView;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "makeGraphSelectionView..." );
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_log_protocol, container, false );
    //
    // Objekte lokalisieren
    //
    changeGraphDeviceButton = ( Button )rootView.findViewById( R.id.changeGraphDeviceButton );
    graphLogsButton = ( Button )rootView.findViewById( R.id.graphLogsButton );
    graphLogsListView = ( ListView )rootView.findViewById( R.id.graphLogsListView );
    if( changeGraphDeviceButton == null || graphLogsButton == null || graphLogsListView == null )
    {
      throw new NullPointerException( "makeConnectionView: can't init GUI (not found an Element)" );
    }
    return( rootView );
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgConnected..." );
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {}

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {}

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {}

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {}

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "recived Tick <%x08x>", msg.getTimeStamp() ) );
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    //
  }

  @Override
  public void onActivityCreated( Bundle savedInstanceState )
  {
    super.onActivityCreated( savedInstanceState );
    dbId = -1;
    diveId = -1;
    runningActivity = ( MainActivity )getActivity();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: ACTIVITY ATTACH" );
    try
    {
      //
      // auch die Objekte lokalisieren
      //
      changeGraphDeviceButton = ( Button )runningActivity.findViewById( R.id.changeGraphDeviceButton );
      graphLogsButton = ( Button )runningActivity.findViewById( R.id.graphLogsButton );
      graphLogsListView = ( ListView )runningActivity.findViewById( R.id.graphLogsListView );
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "onActivityCreated: gui objects not allocated!" );
    }
    //
    // Aufruf mittels create
    //
    Bundle arguments = getArguments();
    if( arguments != null && arguments.containsKey( ProjectConst.ARG_ITEM_CONTENT ) )
    {
      fragmentTitle = arguments.getString( ProjectConst.ARG_ITEM_CONTENT );
      runningActivity.onSectionAttached( fragmentTitle );
      if( arguments.containsKey( ProjectConst.ARG_SELECTED_DEVICE ) )
      {
        // Gerät vorselektieren
        dbId = arguments.getInt( ProjectConst.ARG_SELECTED_DEVICE );
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: marked device <" + arguments.getInt( ProjectConst.ARG_SELECTED_DEVICE ) + ">" );
        fillListAdapter( dbId );
        if( arguments.containsKey( ProjectConst.ARG_SELECTED_DIVE ) )
        {
          // Dive vorselektieren
          diveId = arguments.getInt( ProjectConst.ARG_SELECTED_DIVE );
          setDiveAdapterForDiveId( diveId );
        }
      }
    }
    else
    {
      Log.w( TAG, "onActivityCreated: TITLE NOT SET!" );
    }
    //
    // im Falle eines restaurierten Frames
    //
    if( savedInstanceState != null && savedInstanceState.containsKey( ProjectConst.ARG_ITEM_CONTENT ) )
    {
      fragmentTitle = savedInstanceState.getString( ProjectConst.ARG_ITEM_CONTENT );
      runningActivity.onSectionAttached( fragmentTitle );
    }
    //
    // Fragment vom Stack geholt?
    //
    if( savedInstanceState != null && savedInstanceState.containsKey( ProjectConst.ARG_SELECTED_DEVICE ) )
    {
      dbId = savedInstanceState.getInt( ProjectConst.ARG_SELECTED_DEVICE );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: marked device <" + savedInstanceState.getInt( ProjectConst.ARG_SELECTED_DEVICE ) + ">" );
      fillListAdapter( dbId );
      if( savedInstanceState.containsKey( ProjectConst.ARG_SELECTED_DIVE ) )
      {
        // Dive vorselektieren
        diveId = savedInstanceState.getInt( ProjectConst.ARG_SELECTED_DIVE );
        setDiveAdapterForDiveId( savedInstanceState.getInt( ProjectConst.ARG_SELECTED_DIVE ) );
      }
    }
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = ( MainActivity )activity;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: ATTACH" );
    //
    // die Datenbank öffnen
    //
    try
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: create SQLite helper..." );
      DataSQLHelper sqlHelper = new DataSQLHelper( getActivity().getApplicationContext(), MainActivity.databaseDir.getAbsolutePath() + File.separator + ProjectConst.DATABASE_NAME );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: create logManager helper..." );
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

  @Override
  public void onClick( View v )
  {
    SPX42ReadLogListArrayAdapter rAdapter = null;
    Button button = null;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "Click" );
    //
    // Wurde ein Button geklickt?
    //
    if( v instanceof Button )
    {
      button = ( Button )v;
      //
      // sollen Daten visualisiert werden?
      //
      if( button == graphLogsButton )
      {
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: VIEW selected Item" );
        //
        // Zeige markiertes Log an
        //
        rAdapter = ( SPX42ReadLogListArrayAdapter )graphLogsListView.getAdapter();
        if( rAdapter != null && !rAdapter.getMarkedItems().isEmpty() )
        {
          int idx = rAdapter.getMarkedItems().firstElement();
          ReadLogItemObj rlo = rAdapter.getItem( idx );
          viewSelectedLogItem( rlo.dbId );
        }
        else
        {
          // Nee, da ist nix ausgewählt,
          Log.i( TAG, "onClick: not device selected!" );
          UserAlertDialogFragment wDial = new UserAlertDialogFragment( getString( R.string.dialog_not_selected_items_header ), getString( R.string.dialog_not_selected_items_graph ) );
          wDial.show( getFragmentManager(), "notingSelectedWarning" );
          return;
        }
      }
      //
      // soll das angezeigte Gerät gewechselt werden?
      //
      else if( button == changeGraphDeviceButton )
      {
        // Hier wird dann ein Dialog gebraucht!
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onClick: call changeGraphDeviceButton!" );
        SelectDeviceDialogFragment dialog = new SelectDeviceDialogFragment();
        dialog.setDeviceList( logManager.getDeviceNameIdList() );
        dialog.show( getFragmentManager(), "SelectDeviceDialogFragment" );
      }
    }
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    dbId = -1;
    diveId = -1;
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
    rootView = makeGraphSelectionView( inflater, container );
    return rootView;
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    runningActivity.removeServiceListener( this );
  }

  /**
   * 
   * Wenn der Dialog Positiv abgeschlossen wurde (OK oder ähnlich)
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
    dialog.dismiss();
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
    Bundle arguments;
    //
    if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "Positive dialog click!" );
    if( dialog instanceof SelectDeviceDialogFragment )
    {
      arguments = getArguments();
      SelectDeviceDialogFragment deviceDialog = ( SelectDeviceDialogFragment )dialog;
      selectedDeviceId = deviceDialog.getSelectedDeviceId();
      selectedDeviceAlias = logManager.getAliasForId( selectedDeviceId );
      if( ApplicationDEBUG.DEBUG )
        Log.i( TAG, "onDialogNegative: selected Device Alias: <" + deviceDialog.getSelectedDeviceName() + "> Device-ID <" + deviceDialog.getSelectedDeviceId() + ">" );
      getActivity().getActionBar().setTitle( String.format( getResources().getString( R.string.graphlog_header_device ), deviceDialog.getSelectedDeviceName() ) );
      //
      // ist eigentlich ein Gerät ausgewählt?
      //
      if( selectedDeviceId > 0 )
      {
        //
        // wenn ein gerät gewählt wurde, speicher das gleich in den Argumenten
        //
        if( arguments != null )
        {
          arguments.putInt( ProjectConst.ARG_SELECTED_DEVICE, selectedDeviceId );
        }
        fillListAdapter( selectedDeviceId );
      }
      else
      {
        //
        // Tja, Einstellunge entfernen
        //
        if( arguments != null )
        {
          arguments.remove( ProjectConst.ARG_SELECTED_DEVICE );
        }
        // Das wird nix, kein Gerät ausgewählt
        if( ApplicationDEBUG.DEBUG ) Log.w( TAG, "no device selected -> note to user" );
        UserAlertDialogFragment uad = new UserAlertDialogFragment( runningActivity.getResources().getString( R.string.dialog_no_device_selected_header ), runningActivity
                .getResources().getString( R.string.dialog_no_device_selected ) );
        uad.show( getFragmentManager(), "noSelectedDevice" );
        return;
      }
      //
      // Wenn keine Einträge in der Datenbank waren
      //
      if( graphLogsListView.getAdapter().isEmpty() )
      {
        //
        // Tja, Einstellunge entfernen
        //
        if( arguments != null )
        {
          arguments.remove( ProjectConst.ARG_SELECTED_DEVICE );
        }
        // Das wird auch nix, Keine Einträge in der Datenbank
        if( ApplicationDEBUG.DEBUG ) Log.w( TAG, "no logs foir device -> note to user" );
        UserAlertDialogFragment uad = new UserAlertDialogFragment( runningActivity.getResources().getString( R.string.dialog_not_log_entrys_header ), runningActivity
                .getResources().getString( R.string.dialog_not_log_entrys ) );
        uad.show( getFragmentManager(), "noLogsForDevice" );
        return;
      }
    }
    //
    // ist das der Bist-Du-Sicher Dialog gewesen?
    //
    else if( dialog instanceof AreYouSureToDeleteFragment )
    {
      if( dialog.getTag().matches( "sureToDeleteDeviceData" ) )
      {
        //
        // ALLE Daten des Gerätes (einschliesslich ALIAS und Datendateien) löschen
        //
        if( ApplicationDEBUG.DEBUG ) Log.w( TAG, "DELETE ALL Data for device: <" + selectedDeviceAlias + "..." );
        logManager.deleteAllDataForDevice( selectedDeviceId );
        //
        // Alle Daten verschwinden lassen
        //
        selectedDeviceAlias = null;
        selectedDeviceId = -1;
        graphLogsListView.setAdapter( null );
      }
    }
    else
    {
      Log.i( TAG, "onDialogNegative: UNKNOWN dialog type" );
    }
  }

  @Override
  public void onItemClick( AdapterView<?> parent, View clickedView, int position, long id )
  {
    Bundle arguments;
    SPX42ReadLogListArrayAdapter rlAdapter = null;
    //
    // Die Liste der Logs
    //
    if( parent.equals( graphLogsListView ) )
    {
      arguments = getArguments();
      rlAdapter = ( SPX42ReadLogListArrayAdapter )graphLogsListView.getAdapter();
      //
      // mache die Markierung auch im View (das wird ja sonst nicht automatisch aktualisiert)
      //
      Vector<Integer> markedItems = rlAdapter.getMarkedItems();
      //
      // Sollen alle anderen Markierungen gelöscht werden (wenn vorhanden)
      //
      if( !markedItems.isEmpty() )
      {
        // Falls vorhanxen, löschen
        Iterator<Integer> it = markedItems.iterator();
        int idx = it.next();
        rlAdapter.setMarked( idx, false );
        // View erfraqgen
        ImageView ivMarked = ( ImageView )graphLogsListView.getChildAt( idx ).findViewById( R.id.readLogMarkedIconView );
        if( ivMarked != null )
        {
          // ist ein View vorhanden, geht es schon los
          ivMarked.setImageResource( R.drawable.circle_empty_yellow );
        }
      }
      //
      // setze die Markierung im Adapter
      //
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set item <" + position + "> as marked" );
      rlAdapter.setMarked( position, true );
      ImageView ivMarked = ( ImageView )clickedView.findViewById( R.id.readLogMarkedIconView );
      ivMarked.setImageResource( R.drawable.circle_full_green );
      //
      // Tja, Einstellung merken
      //
      if( ( arguments != null ) && ( !rlAdapter.getMarkedItems().isEmpty() ) )
      {
        int idx = rlAdapter.getMarkedItems().firstElement();
        ReadLogItemObj rlo = rlAdapter.getItem( idx );
        arguments.putInt( ProjectConst.ARG_SELECTED_DIVE, rlo.dbId );
      }
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    // handler loeschen
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause: clear service listener for preferences fragment..." );
    runningActivity.removeServiceListener( this );
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume..." );
    //
    // Objekte initialisieren
    //
    changeGraphDeviceButton.setOnClickListener( this );
    graphLogsButton = ( Button )runningActivity.findViewById( R.id.graphLogsButton );
    graphLogsButton.setOnClickListener( this );
    graphLogsListView = ( ListView )runningActivity.findViewById( R.id.graphLogsListView );
    graphLogsListView.setOnItemClickListener( this );
    //
    //
    // Liste füllen
    //
    if( selectedDeviceId > 0 )
    {
      fillListAdapter( selectedDeviceId );
    }
    else
    {
      selectedDeviceAlias = null;
    }
    //
    // Service Listener setzen
    //
    runningActivity.addServiceListener( this );
  }

  @Override
  public void onSaveInstanceState( Bundle savedInstanceState )
  {
    super.onSaveInstanceState( savedInstanceState );
    fragmentTitle = savedInstanceState.getString( ProjectConst.ARG_ITEM_CONTENT );
    savedInstanceState.putString( ProjectConst.ARG_ITEM_CONTENT, fragmentTitle );
    savedInstanceState.putInt( ProjectConst.ARG_DBID, dbId );
    savedInstanceState.putInt( ProjectConst.ARG_SELECTED_DIVE, diveId );
  }

  /**
   * 
   * Suche und selektiere ggf. den vorgemerkten Dive
   *
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 25.11.2014
   * 
   * @param diveId
   */
  private void setDiveAdapterForDiveId( int diveId )
  {
    SPX42ReadLogListArrayAdapter rAdapter;
    ReadLogItemObj rlo;
    int idx;
    //
    rAdapter = ( SPX42ReadLogListArrayAdapter )graphLogsListView.getAdapter();
    rAdapter.clearMarkedItems();
    // alle Einträge nach DBID durchsuchen
    for( idx = 0; idx < rAdapter.getCount(); idx++ )
    {
      rlo = rAdapter.getItem( idx );
      if( rlo.dbId == diveId )
      {
        rAdapter.setMarked( idx, true );
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setDiveAdapterForDiveId: marked IDX <" + idx + ">" );
        break;
      }
    }
  }

  /**
   * 
   * Zeige das selektierte View an!
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 01.02.2014
   * 
   * @param dbId
   */
  private void viewSelectedLogItem( int dbId )
  {
    //
    // Logs grafisch darstellen, dazu neues Frame aufrufen
    //
    Bundle arguments = new Bundle();
    arguments.putString( ProjectConst.ARG_ITEM_CONTENT, getString( R.string.progitem_loggraph_detail ) );
    arguments.putInt( ProjectConst.ARG_ITEM_ID, R.string.progitem_loggraph_detail );
    arguments.putInt( ProjectConst.ARG_DBID, dbId );
    Log.i( TAG, "viewSelectedLogItem: create SPX42LogGraphDetailFragment..." );
    //
    // Logs detailiert darstellen
    //
    SPX42LogGraphDetailFragment lgdf = new SPX42LogGraphDetailFragment();
    lgdf.setArguments( arguments );
    FragmentTransaction fTrans = getFragmentManager().beginTransaction();
    fTrans.replace( R.id.main_container, lgdf );
    fTrans.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN | FragmentTransaction.TRANSIT_FRAGMENT_FADE );
    fTrans.addToBackStack( "SPX42LogGraphDetailFragment" );
    fTrans.commit();
  }
}
