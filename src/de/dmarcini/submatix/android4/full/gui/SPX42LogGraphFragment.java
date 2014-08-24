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
import java.util.Vector;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.dialogs.UserAlertDialogFragment;
import de.dmarcini.submatix.android4.full.exceptions.NoDatabaseException;
import de.dmarcini.submatix.android4.full.exceptions.NoXMLDataFileFoundException;
import de.dmarcini.submatix.android4.full.utils.DataSQLHelper;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.ReadLogItemObj;
import de.dmarcini.submatix.android4.full.utils.SPX42DiveSampleClass;
import de.dmarcini.submatix.android4.full.utils.SPX42LogGraphView;
import de.dmarcini.submatix.android4.full.utils.SPX42LogManager;

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
public class SPX42LogGraphFragment extends Fragment implements IBtServiceListener
{
  @SuppressWarnings( "javadoc" )
  public static final String TAG               = SPX42LogGraphFragment.class.getSimpleName();
  protected ProgressDialog   progressDialog    = null;
  // private CommToast theToast = null;
  private SPX42LogManager    logManager        = null;
  private Activity           runningActivity   = null;
  private int                dbId              = -1;
  private SPX42LogGraphView  sPX42LogGraphView = null;

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
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "handleMessages: unhandled message message with id <" + smsg.getId() + "> recived!" );
    }
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
    //
    // die Datenbank öffnen
    //
    try
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: create SQLite helper..." );
      DataSQLHelper sqlHelper = new DataSQLHelper( getActivity().getApplicationContext(), FragmentCommonActivity.databaseDir.getAbsolutePath() + File.separator
              + ProjectConst.DATABASE_NAME );
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

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate..." );
    // theToast = new CommToast( getActivity() );
    dbId = getArguments().getInt( ProjectConst.ARG_ITEM_DBID, -1 );
    if( ApplicationDEBUG.DEBUG ) Log.e( TAG, "onCreate... DBID=<" + dbId + ">" );
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView;
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
    sPX42LogGraphView = new SPX42LogGraphView( getActivity().getApplication().getApplicationContext() );
    sPX42LogGraphView.setTheme( FragmentCommonActivity.getAppStyle() );
    rootView = sPX42LogGraphView;
    return rootView;
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    // handler loeschen
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
    //
    // Service Listener setzen
    //
    ( ( FragmentCommonActivity )runningActivity ).addServiceListener( this );
    makeGraphForDive();
  }

  /**
   * 
   * Erzeuge nun die grafik für den Tauchgang mit dem objekt SPX42LogGraphView
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 02.02.2014
   */
  private void makeGraphForDive()
  {
    Vector<float[]> sampleVector;
    //
    ReadLogItemObj rlo = logManager.getLogObjForDbId( dbId, getActivity().getResources() );
    if( rlo != null )
    {
      //
      // Wenn die Überschrift nicht ausgeblendet ist,
      // Tauchgangsdaten einblenden
      //
      if( runningActivity.getActionBar() != null )
      {
        String headerStr = String.format( "%s <%s>", getResources().getString( R.string.graphlog_header ), rlo.itemName );
        runningActivity.getActionBar().setTitle( headerStr );
      }
      try
      {
        sampleVector = SPX42DiveSampleClass.makeSamples( rlo );
        sPX42LogGraphView.setDiveData( sampleVector );
        sPX42LogGraphView.invalidate();
      }
      catch( NoXMLDataFileFoundException ex )
      {
        Log.e( TAG, "can't create diveLog samples : <" + ex.getLocalizedMessage() + ">" );
        // TODO: User benachrichtigen
        return;
      }
    }
  }
}
