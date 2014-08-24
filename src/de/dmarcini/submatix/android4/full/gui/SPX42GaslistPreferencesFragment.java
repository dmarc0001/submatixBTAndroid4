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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.exceptions.FirmwareNotSupportetException;
import de.dmarcini.submatix.android4.full.utils.CommToast;
import de.dmarcini.submatix.android4.full.utils.GasPickerPreference;
import de.dmarcini.submatix.android4.full.utils.GasUpdateEntity;
import de.dmarcini.submatix.android4.full.utils.GasUtilitys;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.dmarcini.submatix.android4.full.utils.SPX42GasParms;

/**
 * 
 * Editor für die Gaslisten
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class SPX42GaslistPreferencesFragment extends PreferenceFragment implements IBtServiceListener, OnSharedPreferenceChangeListener
{
  private static final String                  TAG               = SPX42GaslistPreferencesFragment.class.getSimpleName();
  private static final int                     maxEvents         = 8;
  private static final Pattern                 fieldPatternKdo   = Pattern.compile( "~\\d+" );
  private String                               gasKeyTemplate    = null;
  private String                               gasKeyStub        = null;
  private Activity                             runningActivity   = null;
  private boolean                              ignorePrefChange  = false;
  private CommToast                            theToast          = null;
  private String                               diluent1String, diluent2String, noDiluent, bailoutString;
  private int                                  waitForGasNumber  = 0;
  private int                                  waitForGasOkCount = 0;
  private final ArrayList<GasPickerPreference> gasPrefs          = new ArrayList<GasPickerPreference>();

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
      // SPX sendet "ALIVE" und Ackuspannung
      // ################################################################
      case ProjectConst.MESSAGE_SPXALIVE:
        msgRecivedAlive( smsg );
        break;
      // ################################################################
      // ein Timeout beim Schreiben eines Kommandos trat auf!
      // ################################################################
      case ProjectConst.MESSAGE_COMMTIMEOUT:
        msgReciveWriteTmeout( smsg );
        break;
      // ################################################################
      // ein Timeout beim Schreiben eines Kommandos trat auf!
      // ################################################################
      case ProjectConst.MESSAGE_GAS_READ:
        msgReciveGasSetup( smsg );
        break;
      // ################################################################
      // das Schreiben eines Gassetups bestätigt
      // ################################################################
      case ProjectConst.MESSAGE_GAS_ACK:
        msgReciveGasSetupAck( smsg );
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        if( ApplicationDEBUG.DEBUG ) Log.i( TAG, "unhandled message with id <" + smsg.getId() + "> recived!" );
    }
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    Log.v( TAG, "msgConnected()...ask for SPX config..." );
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgConnected(): ask for SPX config..." );
    // Dialog schliesen, wenn geöffnet
    theToast.dismissDial();
    theToast.openWaitDial( maxEvents, getActivity().getResources().getString( R.string.dialog_please_wait_title ),
            getActivity().getResources().getString( R.string.dialog_please_wait_read_config ) );
    try
    {
      Thread.yield();
      Thread.sleep( 100 );
      Thread.yield();
      Thread.sleep( 100 );
      Thread.yield();
    }
    catch( InterruptedException ex )
    {}
    fActivity.askForGasFromSPX();
    waitForGasOkCount = 0;
    waitForGasNumber = 0;
    ignorePrefChange = false;
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
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX Alive <" + ( String )msg.getContainer() + "> recived" );
    theToast.dismissDial();
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {}

  /**
   * 
   * Empfange eine Nachricht über eine Gaseinstellung (0..7)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 18.07.2013
   * 
   * @param smsg
   */
  private void msgReciveGasSetup( BtServiceMessage msg )
  {
    String[] gasParm;
    int gasNr = 0, dil = 0, cg = 0;
    SPX42GasParms gasParms = new SPX42GasParms();
    //
    // GET_SETUP_GASLIST
    // NR:ST:HE:BA:AA:CG
    // NR: Numer des Gases
    // ST Stickstoff in Prozent (hex)
    // HELIUM
    // Bailout
    // AA Diluent 1 oder 2 oder keins
    // CG curent Gas
    if( msg.getContainer() instanceof String[] )
    {
      gasParm = ( String[] )msg.getContainer();
    }
    else
    {
      Log.e( TAG, "msgReciveGasSetup: message object not an String[] !" );
      return;
    }
    try
    {
      gasNr = Integer.parseInt( gasParm[0], 16 ); // Gasnr
      dil = Integer.parseInt( gasParm[4], 16 ); // diluent
      gasParms.n2 = Integer.parseInt( gasParm[1], 16 ); // n2
      gasParms.he = Integer.parseInt( gasParm[2], 16 ); // he
      gasParms.o2 = 100 - gasParms.he - gasParms.n2; // O2
      gasParms.d1 = ( dil == 1 ) ? true : false; // Diluent 1
      gasParms.d2 = ( dil == 2 ) ? true : false; // diluent 2?
      gasParms.bo = ( ( Integer.parseInt( gasParm[3], 16 ) > 0 ) ? true : false ); // bailout?
      cg = Integer.parseInt( gasParm[5], 16 ); // current gas
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveGasSetup: gas setup object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "msgReciveGasSetup: gas setup object is not an correct integer! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    if( ApplicationDEBUG.DEBUG )
      Log.d( TAG, String.format( "msgReciveGasSetup: gas: %d, n2:%02d%%, he:%02d%%, bailout:%b, dil: %d, currentGas: %d", gasNr, gasParms.n2, gasParms.he, gasParms.bo, dil, cg ) );
    // Jetz in die Preference und damit in die GUI meisseln
    try
    {
      GasPickerPreference gpp = gasPrefs.get( gasNr );
      ignorePrefChange = true;
      //
      // jetzt die Werte für Gas übernehmen
      //
      gpp.setValue( gasParms );
      setGasSummary( gasNr, gasParms );
      ignorePrefChange = false;
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveGasSetup: gas <" + gasNr + "> was not found an GasPickerPreference! <" + ex.getLocalizedMessage() + ">" );
      return;
    }
  }

  /**
   * 
   * Wenn eine Bestätigung für ein Gassetzuip kommt
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 18.07.2013
   * 
   * @param smsg
   */
  private void msgReciveGasSetupAck( BtServiceMessage smsg )
  {
    int gasNr;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX SET GAS settings ACK recived" );
    if( waitForGasOkCount > 1 )
    {
      // zähle die zu erwartenden ACK herunter...
      waitForGasOkCount--;
      gasNr = waitForGasNumber - waitForGasOkCount;
      theToast.showConnectionToast( String.format( getResources().getString( R.string.toast_comm_set_gas_count ), gasNr, waitForGasNumber ), false );
    }
    else
    {
      // alle zu erwartenden ACK angekommen, BIN FERTIG!
      theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_gas_ok ), false );
    }
    ignorePrefChange = false;
  }

  @Override
  public void msgReciveWriteTmeout( BtServiceMessage msg )
  {
    int kdo = 0;
    String toSendMsg = null;
    //
    // ich versuche rauszubekommen, welches Kommando das war
    //
    if( ( msg.getContainer() != null ) && ( msg.getContainer() instanceof String ) )
    {
      toSendMsg = ( String )msg.getContainer();
      Matcher m = fieldPatternKdo.matcher( toSendMsg );
      if( m.find() )
      {
        // das Erste will ich haben!
        String erg = m.group();
        erg = erg.replace( "~", "" );
        try
        {
          // wenn ich das umwandeln in INT kann....
          kdo = Integer.parseInt( erg, 16 );
        }
        catch( NumberFormatException ex )
        {
          Log.e( TAG, "msgReciveWriteTmeout: NumberFormatException: <" + ex.getLocalizedMessage() + ">" );
        }
      }
    }
    //
    // so, nun gucken, von wem das kam
    //
    switch ( kdo )
    {
    //
    // artig die richtige Meldung absetzen
    //
      case ProjectConst.SPX_SET_SETUP_GASLIST:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX gas setup was not correct set!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_set_gas_alert ) );
        break;
      default:
        Log.e( TAG, "SPX Write timeout" + ( ( toSendMsg == null ) ? "(NULL Message)" : toSendMsg ) + "!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_timeout ) );
    }
    //
    // wieder mitarbeiten
    //
    ignorePrefChange = false;
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = activity;
    Log.w( TAG, "ATTACH" );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    Resources res;
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    theToast = new CommToast( getActivity() );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_gaslist_preference + ">..." );
    res = getResources();
    gasKeyTemplate = res.getString( R.string.conf_gaslist_gas_key_template );
    gasKeyStub = res.getString( R.string.conf_gaslist_gas_key );
    diluent1String = res.getString( R.string.conf_gaslist_summary_diluent1 );
    diluent2String = res.getString( R.string.conf_gaslist_summary_diluent2 );
    noDiluent = res.getString( R.string.conf_gaslist_summary_no_diluent );
    bailoutString = res.getString( R.string.conf_gaslist_summary_bailout );
    addPreferencesFromResource( R.xml.config_spx42_gaslist_preference );
    //
    // initiiere die notwendigen summarys
    //
    gasPrefs.clear();
    for( int idx = 0; idx < 8; idx++ )
    {
      String key = String.format( gasKeyTemplate, idx );
      GasPickerPreference gP = ( GasPickerPreference )getPreferenceScreen().findPreference( key );
      gasPrefs.add( gP );
    }
    // setAllSummarys();
    Log.v( TAG, "onCreate: add Resouce...OK" );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    //
    // den Change-Listener abbestellen ;-)
    //
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        Log.v( TAG, "onOptionsItemSelected: HOME" );
        Intent intent = new Intent( getActivity(), AreaListActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
        return true;
    }
    return super.onOptionsItemSelected( item );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause..." );
    //
    // lösche Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause(): clear service listener for preferences fragment..." );
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  @Override
  public synchronized void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume..." );
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
    ignorePrefChange = true;
    waitForGasOkCount = 0;
    waitForGasNumber = 0;
    // Service Listener setzen
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume(): set service listener for preferences fragment..." );
    fActivity.addServiceListener( this );
  }

  @Override
  public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
  {
    GasPickerPreference gpp = null;
    SPX42GasParms gasParms;
    Vector<GasUpdateEntity> gasUpdates;
    int gasNr;
    //
    Log.v( TAG, "onSharedPreferenceChanged...." );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged: key = <" + key + ">" );
    if( ignorePrefChange )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged: ignore change..." );
      return;
    }
    //
    // Wenn das von der GasPickergeschichte kommt
    //
    try
    {
      gasNr = Integer.parseInt( key.replaceAll( gasKeyStub, "" ) );
      gpp = gasPrefs.get( gasNr );
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "can't inspect gasnumber (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "gas preference not in List (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    if( gpp == null )
    {
      Log.e( TAG, "onSharedPreferenceChanged: Key <" + key + "> was not found an GasPickerPreference! Abort!" );
      return;
    }
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_gas ), true );
    //
    // hole mal die Gaseinstellungen
    //
    // gucke mal, ob die Diluents stimmen
    Vector<Integer> changeSets = checkDiluentSets( gasNr );
    gasParms = gpp.getValue();
    // das aktuelle Gas anzeigen
    setGasSummary( gasNr, gasParms );
    // Den Vector für die Gasupdates anlegen
    gasUpdates = new Vector<GasUpdateEntity>();
    // das aktuelle Gas schon mal eintragen
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged: gas to update to list: <" + gasNr + ">" );
    gasUpdates.add( new GasUpdateEntity( gasNr, gasParms ) );
    Iterator<Integer> it = changeSets.iterator();
    // für alle zu ändernden Gase Parameter festlegen und in einen Vector schreiben
    while( it.hasNext() )
    {
      gasNr = it.next();
      gpp = gasPrefs.get( gasNr );
      gasParms = gpp.getValue();
      setGasSummary( gasNr, gasParms );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged: gas to update to list: <" + gasNr + ">" );
      gasUpdates.add( new GasUpdateEntity( gasNr, gasParms ) );
    }
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    // wie viele ACK muss ich abwarten?
    waitForGasOkCount = waitForGasNumber = gasUpdates.size();
    try
    {
      fActivity.writeGasSetup( gasUpdates );
    }
    catch( FirmwareNotSupportetException ex )
    {
      // TODO GEneriere Fehlermeldung an User
      ex.printStackTrace();
    }
    Log.v( TAG, "onSharedPreferenceChanged....OK" );
  }

  @Override
  public void onViewCreated( View view, Bundle savedInstanceState )
  {
    super.onViewCreated( view, savedInstanceState );
    Log.v( TAG, "onViewCreated..." );
    //
    // alle Gase generisch durch (8 Gase sind im SPX42)
    //
    for( int idx = 0; idx < 8; idx++ )
    {
      GasPickerPreference gP = gasPrefs.get( idx );
      gasPrefs.add( gP );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "The Preference <%s> is number %d", gP.getTitle(), idx ) );
      if( idx % 2 > 0 )
      {
        if( FragmentCommonActivity.getAppStyle() == R.style.AppDarkTheme )
        {
          // dunkles Thema
          gP.setLayoutResource( R.layout.preference_dark );
        }
        else
        {
          // helles Thema
          gP.setLayoutResource( R.layout.preference_light );
        }
      }
      else
      {
        gP.setLayoutResource( R.layout.preference );
      }
    }
  }

  /**
   * 
   * Setze alle summarys
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 01.01.2013
   */
  @SuppressLint( "DefaultLocale" )
  private void setAllSummarys()
  {
    //
    // alle Gase generisch durch (8 Gase sind im SPX42)
    //
    for( int idx = 0; idx < 8; idx++ )
    {
      setGasSummary( idx );
    }
  }

  /**
   * 
   * setze eine Gas-summary (Beschreibung in der Preference)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 18.07.2013
   * 
   * @param gasNr
   * @param gpp
   *          GasPickerPreference Referenz
   * @param gasParms
   */
  private void setGasSummary( int gasNr )
  {
    SPX42GasParms gasParms;
    //
    // hole mal die Gaseinstellungen
    //
    gasParms = gasPrefs.get( gasNr ).getValue();
    setGasSummary( gasNr, gasParms );
  }

  /**
   * 
   * setze eine Gas-summary (Beschreibung in der Preference
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 23.07.2013
   * 
   * @param gasNr
   * @param gpp
   * @param gasParms
   */
  private void setGasSummary( int gasNr, SPX42GasParms gasParms )
  {
    String gasName, gasExt;
    GasPickerPreference gpp;
    gpp = gasPrefs.get( gasNr );
    //
    // baue den String für das Summary zusammen
    //
    gasExt = noDiluent;
    if( gasParms.d1 ) gasExt = diluent1String;
    if( gasParms.d2 ) gasExt = diluent2String;
    if( gasParms.bo ) gasExt += bailoutString;
    gasName = GasUtilitys.getNameForGas( gasParms.o2, gasParms.he ) + gasExt;
    // schreib schön!
    if( ApplicationDEBUG.DEBUG )
      Log.d( TAG, String.format( "setGasSummary: gas number <%02d> has O2 <%02d%%>, MOD: %f ", gasNr, gasParms.o2, GasUtilitys.getMODForGasMetric( gasParms.o2 ) ) );
    gpp.setSummary( String.format( getResources().getString( R.string.conf_gaslist_summary_first ), gasNr + 1, gasName, Math.round( GasUtilitys.getMODForGasMetric( gasParms.o2 ) ) ) );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "setGasSummary: gas number <%02d> has now summary <%s>", gasNr, gasName ) );
  }

  /**
   * 
   * Diluent setzen in einer Preference. Immer nur einmal D1 und einmal D2 möglich!
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 23.07.2013
   * 
   * @param idx
   * @return
   */
  private Vector<Integer> checkDiluentSets( int gasNr )
  {
    SPX42GasParms gasParms;
    Boolean prefD1, prefD2;
    // Boolean wasD1, wasD2;
    Boolean wasChanged;
    GasPickerPreference gpp;
    Vector<Integer> toChange = new Vector<Integer>();
    //
    // Werte vom aktuellen Gas merken und Vorbereitungen
    //
    gpp = gasPrefs.get( gasNr );
    gasParms = gpp.getValue();
    prefD1 = gasParms.d1;
    prefD2 = gasParms.d2;
    //
    // Alle Gase durchackern
    //
    for( int idx = 0; idx < 8; idx++ )
    {
      wasChanged = false;
      try
      {
        // mein gas brauch ich nicht beackern
        if( idx == gasNr ) continue;
        // Preferenzen lesen
        gpp = gasPrefs.get( idx );
        gasParms = gpp.getValue();
        // Diluent 1 checken
        if( gasParms.d1 )
        {
          // wenn beide true sind, ist was schief
          if( prefD1 && gasParms.d1 )
          {
            // ups, das muss korrigiert werden!
            if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "checkDiluentSets: gas number <%d> was diluent 1 changed, write to Preference", idx ) );
            wasChanged = true;
            gasParms.d1 = false;
          }
        }
        // Diluent 2 checken
        if( gasParms.d2 )
        {
          // wenn beide true sind, ist was schief
          if( prefD2 && gasParms.d2 )
          {
            // ups, das muss korrigiert werden!
            if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "checkDiluentSets: gas number <%d> was diluent 2 changed, write to Preference", idx ) );
            wasChanged = true;
            gasParms.d2 = false;
          }
        }
        //
        // wenn was verändert wurde, Values setzen
        //
        if( wasChanged )
        {
          gpp.setValue( gasParms );
          setGasSummary( idx );
          toChange.add( idx );
        }
        // setGasSummary( idx );
      }
      catch( IndexOutOfBoundsException ex )
      {
        Log.e( TAG, "setDiluent: no gasPreference for index! <" + ex.getLocalizedMessage() + ">" );
        return( null );
      }
    }
    return( toChange );
  }
}
