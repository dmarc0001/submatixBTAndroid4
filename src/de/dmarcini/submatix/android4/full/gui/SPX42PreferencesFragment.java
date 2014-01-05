package de.dmarcini.submatix.android4.full.gui;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.exceptions.FirmwareNotSupportetException;
import de.dmarcini.submatix.android4.full.utils.CommToast;
import de.dmarcini.submatix.android4.full.utils.GradientPickerPreference;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * 
 * Ein Objekt zum bearbeiten der SPX42 Einstellungen
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class SPX42PreferencesFragment extends PreferenceFragment implements IBtServiceListener, OnSharedPreferenceChangeListener
{
  private static final String  TAG                          = SPX42PreferencesFragment.class.getSimpleName();
  private static final int     maxEvents                    = 12;
  private Activity             runningActivity              = null;
  private static final Pattern fieldPatternKdo              = Pattern.compile( "~\\d+" );
  // Schlüsselwerte für Presets
  private static final String  setpointAuto                 = "keySetpointAutosetpointDepth";
  private static final String  setpointHigh                 = "keySetpointHighsetpointValue";
  private static final String  decoGradient                 = "keyDecoGradient";
  private static final String  decoGradientsPreset          = "keyDecoGradientPresets";
  private static final String  decoLastStop                 = "keyDecoLastStop";
  private static final String  decoDynGradients             = "keyDecoDynGradients";
  private static final String  decoDeepStops                = "keyDecoDeepStops";
  private static final String  displayCategory              = "keyDisplay";
  private static final String  displayLuminance             = "keyDisplayLuminance";
  private static final String  displayOrient                = "keyDisplayOrientation";
  private static final String  individualSensorsOn          = "keyIndividualSensorsOn";
  private static final String  individualPSCROn             = "keyIndividualPSCROn";
  private static final String  individualCountSensorWarning = "keyIndividualCountSensorWarning";
  private static final String  individualAcousticWarnings   = "keyIndividualAcousticWarnings";
  private static final String  individualLoginterval        = "keyIndividualLoginterval";
  private static final String  individualTempStick          = "keyIndividualTempStick";
  private static final String  unitsIsTempMetric            = "keyUnitsIsTempMetric";
  private static final String  unitsIsDepthImperial         = "keyUnitsIsDepthMetric";
  private static final String  unitsIsFreshwater            = "keyUnitsIsFreshwater";
  // Ende Schlüsselwerte
  private boolean              ignorePrefChange             = false;
  private CommToast            theToast                     = null;

  /**
   * 
   * Erfrage deco Gradieneten aus Preferenz
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 15.07.2013
   * 
   * @return
   */
  private int[] getDecoGradients()
  {
    int[] res =
    { 0, 0 };
    //
    if( getPreferenceScreen().findPreference( decoGradient ) instanceof GradientPickerPreference )
    {
      GradientPickerPreference pr = ( GradientPickerPreference )getPreferenceScreen().findPreference( decoGradient );
      if( pr == null )
      {
        Log.e( TAG, "setDecoGradients: not preference found (preference is NULL)" );
        return( res );
      }
      // OK, frag mal nach
      res = pr.getValue();
      return( res );
    }
    else
    {
      Log.e( TAG, "setDecoGradients: not preference found" );
      return( res );
    }
  }

  /**
   * 
   * Hilfsfunktion zum erfragen einer Referenz auf ListPreference
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 15.07.2013
   * 
   * @param prefKey
   * @return
   */
  private ListPreference getListPreference( String prefKey )
  {
    ListPreference lP = null;
    if( getPreferenceScreen().findPreference( prefKey ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( prefKey );
      if( lP != null )
      {
        return( lP );
      }
    }
    Log.e( TAG, "getListPreference: Key <" + prefKey + "> was not found an ListPreference! abort!" );
    return( null );
  }

  /**
   * 
   * Finde eine Preferenz Kategorie
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param prefKey
   * @return die gesuchte Kategorie oder null
   */
  private PreferenceCategory getPreferenceCategory( String prefKey )
  {
    PreferenceCategory pC = null;
    if( getPreferenceScreen().findPreference( prefKey ) instanceof PreferenceCategory )
    {
      pC = ( PreferenceCategory )getPreferenceScreen().findPreference( prefKey );
      if( pC != null )
      {
        return( pC );
      }
    }
    Log.e( TAG, "getPreferenceCategory: Key <" + prefKey + "> was not found an PreferenceCategory! abort!" );
    return( null );
  }

  /**
   * 
   * Hilfsfunktion zum Erfragen einer SwitchReference
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 15.07.2013
   * 
   * @param prefKey
   * @return
   */
  private SwitchPreference getSwitchPreference( String prefKey )
  {
    SwitchPreference lP = null;
    if( getPreferenceScreen().findPreference( prefKey ) instanceof SwitchPreference )
    {
      lP = ( SwitchPreference )getPreferenceScreen().findPreference( prefKey );
      if( lP != null )
      {
        return( lP );
      }
    }
    Log.e( TAG, "getSwitchPreference: Key <" + prefKey + "> was not found an SwitchPreference! abort!" );
    return( null );
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
      // SPX sendet Setpoint
      // ################################################################
      case ProjectConst.MESSAGE_SETPOINT_READ:
        msgReciveAutosetpoint( smsg );
        break;
      // ################################################################
      // SPX Setpoint setzen bestätigt
      // ################################################################
      case ProjectConst.MESSAGE_SETPOINT_ACK:
        msgReciveAutosetpointAck( smsg );
        break;
      // ################################################################
      // Deko einstellungen empfangen
      // ################################################################
      case ProjectConst.MESSAGE_DECO_READ:
        msgReciveDeco( smsg );
        break;
      // ################################################################
      // Deko setzen erfolgreich
      // ################################################################
      case ProjectConst.MESSAGE_DECO_ACK:
        msgReciveDecoAck( smsg );
        break;
      // ################################################################
      // Display Einstellungen empfangen
      // ################################################################
      case ProjectConst.MESSAGE_DISPLAY_READ:
        msgReciveDisplay( smsg );
        break;
      // ################################################################
      // Deko setzen erfolgreich
      // ################################################################
      case ProjectConst.MESSAGE_DISPLAY_ACK:
        msgReciveDisplayAck( smsg );
        break;
      // ################################################################
      // Units vom SPX emnpfangen
      // ################################################################
      case ProjectConst.MESSAGE_UNITS_READ:
        msgReciveUnits( smsg );
        break;
      // ################################################################
      // UNITS setzen erfolgreich
      // ################################################################
      case ProjectConst.MESSAGE_UNITS_ACK:
        msgReciveUnitsAck( smsg );
        break;
      // ################################################################
      // Individuelle Einstellungen lesen
      // ################################################################
      case ProjectConst.MESSAGE_INDIVID_READ:
        msgReciveIndividuals( smsg );
        break;
      // ################################################################
      // Individuelle Einstellungen schreiben Bestätigung
      // ################################################################
      case ProjectConst.MESSAGE_INDIVID_ACK:
        msgReciveIndividualsAck( smsg );
        break;
      // ################################################################
      // ein Timeout beim Schreiben eines Kommandos trat auf!
      // ################################################################
      case ProjectConst.MESSAGE_COMMTIMEOUT:
        msgReciveWriteTmeout( smsg );
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
    fActivity.askForConfigFromSPX42();
    ignorePrefChange = false;
  }

  @Override
  public void msgConnectError( BtServiceMessage msg )
  {
    // zum Menü zurück
    Intent intent = new Intent( getActivity(), AreaListActivity.class );
    intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
    startActivity( intent );
    return;
  }

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {}

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    // zum Menü zurück
    Intent intent = new Intent( getActivity(), AreaListActivity.class );
    intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
    startActivity( intent );
  }

  /**
   * 
   * Empfange Nachricht über (Auto)setpoint
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveAutosetpoint( BtServiceMessage msg )
  {
    ListPreference lP = null;
    // Preference pref = null;
    String[] setPoint;
    int autoSp, sP;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX Autosetpoint recived" );
    //
    // versuche einmal, die beiden erwarteten Werter zu bekommen
    //
    if( msg.getContainer() instanceof String[] )
    {
      setPoint = ( String[] )msg.getContainer();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX Autosetpoint is <" + setPoint[0] + "," + setPoint[0] + ">" );
    }
    else
    {
      Log.e( TAG, "msgReciveAutosetpoint(): message object not an String[] !" );
      return;
    }
    //
    // versuche die Parameter als Integer zu wandeln
    //
    try
    {
      autoSp = Integer.parseInt( setPoint[0] );
      sP = Integer.parseInt( setPoint[1] );
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveAutosetpoint(): Setpoint Object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "msgReciveAutosetpoint(): Setpoint Object is not an correct integer! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    //
    // in die Voreinstellungen übertragen
    //
    lP = getListPreference( setpointAuto );
    if( lP == null ) return;
    ignorePrefChange = true;
    //
    // Autosetpoint (off/tiefe) einstellen
    //
    // setze den Index auf den Wert, der ausgelesen wurde
    // empfangen werden kann 0..3, also kan ich das 1:1 übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set autosetpoint value to preference..." );
    lP.setValueIndex( autoSp );
    //
    lP = getListPreference( setpointHigh );
    if( lP == null ) return;
    //
    // Highsetpoint (partialdruck) einstellen
    //
    // setze den Index auf den Wert, der ausgelesen wurde
    // empfangen werden kann 0..4, also kan ich das 1:1 übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set highsetpoint value to preference..." );
    lP.setValueIndex( sP );
    setSetpointSummarys();
    ignorePrefChange = false;
  }

  /**
   * 
   * Empfange Bestätigung für Setzen des (Auto)setpoint
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveAutosetpointAck( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX Autosetpoint successful set (preferences)" );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_autosetpoint_ok ), false );
    ignorePrefChange = false;
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX Alive <" + ( String )msg.getContainer() + "> recived" );
    theToast.dismissDial();
  }

  /**
   * 
   * Empfange Nachricht über Deko-Einstellungen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveDeco( BtServiceMessage msg )
  {
    String[] decoParam;
    int[] presetCandidate =
    { 0, 0 };
    SwitchPreference sp;
    int lowG, highG, deepStops, dynGr, lastStop;
    //
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX deco settings recived" );
    // Kommando SPX_GET_SETUP_DEKO liefert zurück:
    // ~34:LL:HH:D:Y:C
    // LL=GF-Low, HH=GF-High,
    // D=Deepstops (0/1)
    // Y=Dynamische Gradienten (0/1)
    // C=Last Decostop (0=3 Meter/1=6 Meter)
    //
    // versuche einmal, die fünf erwarteten Werte zu bekommen
    //
    if( msg.getContainer() instanceof String[] )
    {
      decoParam = ( String[] )msg.getContainer();
    }
    else
    {
      Log.e( TAG, "msgReciveDeco(): message object not an String[] !" );
      return;
    }
    //
    // versuche die Parameter als Integer zu wandeln
    //
    try
    {
      lowG = Integer.parseInt( decoParam[0], 16 );
      highG = Integer.parseInt( decoParam[1], 16 );
      deepStops = Integer.parseInt( decoParam[2], 16 );
      dynGr = Integer.parseInt( decoParam[3], 16 );
      lastStop = Integer.parseInt( decoParam[4], 16 );
      presetCandidate[0] = lowG;
      presetCandidate[1] = highG;
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveDeco(): Setpoint Object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "msgReciveDeco(): Setpoint Object is not an correct integer! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    ignorePrefChange = true;
    if( ApplicationDEBUG.DEBUG )
      Log.d( TAG, String.format( "SPX deco settings are low:%d, high:%d, deepstops:%d, dyn gradients:%d, last deco:%d", lowG, highG, deepStops, dynGr, lastStop ) );
    //
    // hier hab ich alle Werte (hoffentlich)
    // ab hier in die Oberfläche einmassieren.
    //
    setDecoGradientsPreset( presetCandidate );
    //
    // Low/High Gradient in die Voreinstellungen übertragen
    //
    setDecoGradients( presetCandidate );
    //
    // LastDeco Stop on/off übernehmen
    //
    sp = getSwitchPreference( decoLastStop );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für LastStop übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set deco last stop value to preference..." );
    sp.setChecked( ( lastStop == 0 ) );
    //
    // Dynamische Gradienten on/off übernehmen
    //
    sp = getSwitchPreference( decoDynGradients );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für LastStop übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set dynGradients value to preference..." );
    sp.setChecked( ( dynGr > 0 ) );
    //
    // Deep stops on/off übernehmen
    //
    sp = getSwitchPreference( decoDeepStops );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für LastStop übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set deepStops value to preference..." );
    sp.setChecked( ( deepStops > 0 ) );
    setDecoSummary();
    ignorePrefChange = false;
  }

  /**
   * 
   * Empfange Bestätigung über Setzen der Deco-Einstellungen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveDecoAck( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX DECO propertys successful set (preferences)" );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_deco_ok ), false );
    ignorePrefChange = false;
  }

  /**
   * 
   * Empfange Nachricht über Einstellungen zum Display
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveDisplay( BtServiceMessage msg )
  {
    String[] displayParm;
    int lumin = 0;
    int orient = 0;
    ListPreference lP;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX display settings recived" );
    // Kommando GET_SETUP_DISPLAYSETTINGS liefert
    // ~36:D:A
    // D= 0->10&, 1->50%, 2->100%
    // ODER 0=20%, 1=40%,2=60%,3=80%,4=100%
    // A= 0->Landscape 1->180Grad
    //
    // gibt es Parameter zu lesen?
    //
    if( msg.getContainer() instanceof String[] )
    {
      displayParm = ( String[] )msg.getContainer();
    }
    else
    {
      Log.e( TAG, "msgReciveDisplay: message object not an String[] !" );
      return;
    }
    //
    // versuche die Parameter als Integer zu wandeln, gültige Werte erzeugen
    //
    try
    {
      lumin = Integer.parseInt( displayParm[0], 16 );
      if( FragmentCommonActivity.spxConfig.isNewerDisplayBrigthness() )
      {
        if( lumin < 0 || lumin > 4 ) lumin = 1;
      }
      else
      {
        if( lumin < 0 || lumin > 2 ) lumin = 1;
      }
      orient = Integer.parseInt( displayParm[1], 16 );
      if( orient < 0 || orient > 1 ) orient = 0;
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveDisplay: Setpoint Object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "msgReciveDisplay: Setpoint Object is not an correct integer! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    ignorePrefChange = true;
    //
    // jetzt Helligkeit eintragen
    //
    lP = getListPreference( displayLuminance );
    if( lP == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt den Preset übernehmen
    // Index sollte lumin sein....
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveDisplay: set luminance value to preference..." );
    lP.setValueIndex( lumin );
    //
    // jetzt Orientierung eintragen
    //
    lP = getListPreference( displayOrient );
    if( lP == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt den Preset übernehmen
    // Index sollte lumin sein....
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveDisplay: set display angle value to preference..." );
    lP.setValueIndex( orient );
    setDisplaySummary();
    ignorePrefChange = false;
  }

  /**
   * 
   * Empfange Bestätigung Setzen der Displayeinstellungen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveDisplayAck( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX display settings ACK recived" );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_display_ok ), false );
    ignorePrefChange = false;
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {}

  /**
   * 
   * Empfange Nachricht über individuelle Einstellungen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveIndividuals( BtServiceMessage msg )
  {
    String[] individualParm;
    int sensorsOff = 0, pscrOff = 0, sensorsCount = 3, soundOn = 1, logInterval = 2, tempStick = 0;
    SwitchPreference sp;
    ListPreference lP;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX individuals settings recived" );
    //
    // gibt es Parameter zu lesen?
    // Kommando GET_SETUP_INDIVIDUAL liefert
    // ~38:SE:PS:SC:SN:LI
    // SE: Sensors 0->ON 1->OFF
    // PS: PSCRMODE 0->OFF 1->ON
    // SC: SensorCount
    // SN: Sound 0->OFF 1->ON
    // LI: Loginterval 0->10sec 1->30Sec 2->60 Sec
    // TS: TempStick Typ (bei neuerer Firmware)
    //
    if( msg.getContainer() instanceof String[] )
    {
      individualParm = ( String[] )msg.getContainer();
    }
    else
    {
      Log.e( TAG, "msgReciveDisplay: message object not an String[] !" );
      return;
    }
    //
    // versuche die Parameter als Integer zu wandeln, gültige Werte erzeugen
    //
    try
    {
      sensorsOff = Integer.parseInt( individualParm[0], 16 );
      pscrOff = Integer.parseInt( individualParm[1], 16 );
      sensorsCount = Integer.parseInt( individualParm[2], 16 );
      soundOn = Integer.parseInt( individualParm[3], 16 );
      logInterval = Integer.parseInt( individualParm[4], 16 );
      if( individualParm.length == 6 )
      {
        tempStick = Integer.parseInt( individualParm[5], 16 );
      }
      if( ApplicationDEBUG.DEBUG )
        Log.d( TAG, String.format( "SPX individuals settings <SE:%d, PS:%d, SC:%d, SN:%d, LI:%d, TS:%d>", sensorsOff, pscrOff, sensorsCount, soundOn, logInterval, tempStick ) );
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e( TAG, "msgReciveIndividuals: Individuals Object has not enough elements! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "msgReciveIndividuals: Individuals Object is not an correct integer! (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    ignorePrefChange = true;
    //
    // Sensoren an/aus ...
    //
    sp = getSwitchPreference( individualSensorsOn );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für Sensoren an/aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set sensors on/off value to preference..." );
    sp.setChecked( ( sensorsOff == 0 ) );
    //
    // PSCR-Mode an/aus ...
    //
    sp = getSwitchPreference( individualPSCROn );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für PSCR an/aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set PSCR-Mode on/off value to preference..." );
    sp.setChecked( ( pscrOff == 1 ) );
    //
    // Anzahl der Sensoren für die Berechnungen wählen
    //
    lP = getListPreference( individualCountSensorWarning );
    if( lP == null )
    {
      ignorePrefChange = false;
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveIndividuals: set sensors count value to preference..." );
    lP.setValueIndex( sensorsCount );
    lP.setSummary( String.format( getResources().getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
    //
    // Akustische Warnungen an/aus ...
    //
    sp = getSwitchPreference( individualAcousticWarnings );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für Akustische Warnungen an/aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set acoustic warnings on/off value to preference..." );
    sp.setChecked( ( soundOn == 1 ) );
    //
    // Loginterval in Oberfläche einbauen
    //
    lP = getListPreference( individualLoginterval );
    if( lP == null )
    {
      ignorePrefChange = false;
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveIndividuals: set loginterval value to preference..." );
    lP.setValueIndex( logInterval );
    lP.setSummary( String.format( getResources().getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
    //
    // und den Tempstick einstellen, wenn vorhanden
    //
    if( FragmentCommonActivity.spxConfig.hasSixValuesIndividual() )
    {
      lP = getListPreference( individualTempStick );
      if( lP == null )
      {
        ignorePrefChange = false;
        return;
      }
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "msgReciveIndividuals: set tempstick value to preference..." );
      lP.setValueIndex( tempStick );
      lP.setSummary( String.format( getResources().getString( R.string.conf_ind_tempstick_header_summary ), lP.getEntry() ) );
    }
    setIndividualsSummary();
    ignorePrefChange = false;
  }

  /**
   * 
   * Empfange Bestätigung über Setzen der individuellen Einstellungen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveIndividualsAck( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX INDIVIDUALS settings ACK recived" );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_individuals_ok ), false );
    ignorePrefChange = false;
  }

  /**
   * 
   * Empfange Nachrichten mit den Einstellungen der Masseinheiten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
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
    int isTempImperial = 0, isDepthImperial = 0, isFreshwater = 0;
    String[] unitsParm;
    SwitchPreference sp;
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
      isTempImperial = Integer.parseInt( unitsParm[0], 16 );
      isDepthImperial = Integer.parseInt( unitsParm[1], 16 );
      isFreshwater = Integer.parseInt( unitsParm[2], 16 );
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
    //
    // ist es die fehhlerhafte firmware?
    //
    if( FragmentCommonActivity.spxConfig.hasFahrenheidBug() )
    {
      // ist es die Fehlerhafte Firmware, IMMER alles gemeinsam auf METRISCH/Imperial setzen
      if( isDepthImperial == 0 )
      {
        isTempImperial = 0;
      }
      else
      {
        isTempImperial = 1;
      }
      if( ApplicationDEBUG.DEBUG ) Log.w( TAG, "msgReciveUnits: SPX firmware is buggy version: switch all to " + ( isDepthImperial == 0 ? "metric" : "imperial" ) );
    }
    ignorePrefChange = true;
    //
    // Temperatur...
    //
    sp = getSwitchPreference( unitsIsTempMetric );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für Temperatureinheit übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set temp unit value to preference..." );
    sp.setChecked( ( isTempImperial == 0 ) );
    //
    // Tiefeneinheit...
    //
    sp = getSwitchPreference( unitsIsDepthImperial );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für Tiefgeneinheit übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set depth unit value to preference..." );
    sp.setChecked( ( isDepthImperial == 0 ) );
    //
    // Süsswasser...
    //
    sp = getSwitchPreference( unitsIsFreshwater );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt Wert für Süß oder Salzwasser eintragen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set salnity value to preference..." );
    sp.setChecked( ( isFreshwater > 0 ) );
    ignorePrefChange = false;
  }

  /**
   * 
   * Empfange Nachricht über Setzen der Masseinheiten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 17.07.2013
   * 
   * @param msg
   */
  private void msgReciveUnitsAck( BtServiceMessage msg )
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX units settings ACK recived" );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_units_ok ), false );
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
      case ProjectConst.SPX_SET_SETUP_SETPOINT:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX O2 setpoint was not correct send!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_set_autosetpoint_alert ) );
        break;
      case ProjectConst.SPX_SET_SETUP_DEKO:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX Deco settings was not correct send!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_set_deco_alert ) );
        break;
      case ProjectConst.SPX_SET_SETUP_DISPLAYSETTINGS:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX Display settings was not correct send!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_set_display_alert ) );
        break;
      case ProjectConst.SPX_SET_SETUP_INDIVIDUAL:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX individual settings was not correct send!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_set_individuals_alert ) );
        break;
      case ProjectConst.SPX_GET_SETUP_UNITS:
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "SPX unit settings was not correct send!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_set_units_alert ) );
        break;
      default:
        Log.e( TAG, "SPX Write timeout" + ( ( toSendMsg == null ) ? "(NULL Message)" : toSendMsg ) + "!" );
        theToast.showConnectionToastAlert( getResources().getString( R.string.toast_comm_set_autosetpoint_alert ) );
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
    ListPreference autoSetPref = null;
    //
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate()..." );
    theToast = new CommToast( getActivity() );
    if( FragmentCommonActivity.spxConfig.getCustomEnabled() == 1 )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "Preferences in INDIVIDUAL Mode" );
      if( FragmentCommonActivity.spxConfig.hasSixValuesIndividual() )
      {
        addPreferencesFromResource( R.xml.config_spx42_preference_individual_six );
        if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_preference_individual_six + ">..." );
      }
      else
      {
        addPreferencesFromResource( R.xml.config_spx42_preference_individual );
        if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_preference_individual + ">..." );
      }
    }
    else
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "Preferences in STANDART Mode" );
      addPreferencesFromResource( R.xml.config_spx42_preference_std );
      if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_preference_std + ">..." );
    }
    if( FragmentCommonActivity.spxConfig.isSixMetersAutoSetpoint() )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate: isSixMetersAutoSetpoint..." );
      autoSetPref = getListPreference( setpointAuto );
      if( autoSetPref != null )
      {
        if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate: isSixMetersAutoSetpoint...found resource" );
        autoSetPref.setEntries( R.array.highsetpointSixDepthNamesArray );
        autoSetPref.setEntryValues( R.array.highsetpointSixDepthValuesArray );
        if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate: isSixMetersAutoSetpoint...set resource" );
      }
    }
    else
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate: NOT isSixMetersAutoSetpoint..." );
    }
    //
    // die richtigen Helligkeitsstufen setzen
    //
    if( FragmentCommonActivity.spxConfig.isInitialized() )
    {
      setNewLuminancePropertys( FragmentCommonActivity.spxConfig.isNewerDisplayBrigthness() );
    }
    //
    // initiiere die notwendigen summarys
    //
    setAllSummarys();
    Log.v( TAG, "onCreate: add Resouce...OK" );
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
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
    Log.v( TAG, "onPause()..." );
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
    Log.v( TAG, "onResume()..." );
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
    ignorePrefChange = true;
    // Service Listener setzen
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume(): set service listener for preferences fragment..." );
    fActivity.addServiceListener( this );
  }

  @Override
  public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
  {
    ListPreference lP = null;
    Preference pref = null;
    Log.v( TAG, "onSharedPreferenceChanged()...." );
    if( ignorePrefChange )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged(): ignore Change Event" );
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged(): key = <" + key + ">" );
    //
    // zuerst mal die ListPreferenzen abklappern
    //
    if( getPreferenceScreen().findPreference( key ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( key );
      if( lP == null )
      {
        Log.e( TAG, "onSharedPreferenceChanged(): for Key <" + key + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // Autosetpoint (off/tiefe)
      //
      if( key.equals( setpointAuto ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_autoset_summary ), lP.getEntry() ) );
        sendAutoSetpoint();
      }
      //
      // Highsetpoint (wenn on)
      //
      else if( key.equals( setpointHigh ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_highset_summary ), lP.getEntry() ) );
        sendAutoSetpoint();
      }
      //
      // DECO-Preset, wenn ON
      //
      else if( key.equals( decoGradientsPreset ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_deco_presets_summary ), lP.getEntry() ) );
        //
        // welche Voreinstellung wurde gewählt?
        //
        if( setDecoGradients( lP.getValue() ) )
        {
          sendDecoPrefs();
        }
      }
      //
      // Helligkeit Display
      //
      else if( key.equals( displayLuminance ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_luminance_header_summary ), lP.getValue() ) );
        sendDisplayPrefs();
      }
      //
      // Orientierung Display
      //
      else if( key.equals( displayOrient ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
        sendDisplayPrefs();
      }
      //
      // Sensors Count Warning
      //
      else if( key.equals( individualCountSensorWarning ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
        sendIndividualPrefs();
      }
      //
      // Intervall zwischen zwei Logeinträgen
      //
      else if( key.equals( individualLoginterval ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
        sendIndividualPrefs();
      }
      else if( key.equals( individualTempStick ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_ind_tempstick_header_summary ), lP.getEntry() ) );
        sendIndividualPrefs();
      }
    }
    else
    {
      pref = getPreferenceScreen().findPreference( key );
      if( pref == null )
      {
        Log.e( TAG, "onSharedPreferenceChanged(): for Key <" + key + "> was not found an Preference! abort!" );
        return;
      }
      //
      // DECO-Gradient verändert
      //
      if( key.equals( decoGradient ) )
      {
        setDecoGradientsSummary();
        int[] val = getDecoGradients();
        setDecoGradientsPreset( val );
        sendDecoPrefs();
      }
      //
      // DECO Last Stop verändert
      //
      else if( key.equals( decoLastStop ) )
      {
        sendDecoPrefs();
      }
      //
      // DECO dyn gradients
      //
      else if( key.equals( decoDynGradients ) )
      {
        sendDecoPrefs();
      }
      //
      // DECO deep stops
      //
      else if( key.equals( decoDeepStops ) )
      {
        sendDecoPrefs();
      }
      //
      // Temperatureinheit
      //
      else if( key.equals( unitsIsTempMetric ) )
      {
        sendUnitPrefs();
      }
      //
      // Tiefeneinheit
      //
      else if( key.equals( unitsIsDepthImperial ) )
      {
        sendUnitPrefs();
      }
      //
      // Salz/Süßwasser
      //
      else if( key.equals( unitsIsFreshwater ) )
      {
        sendUnitPrefs();
      }
      //
      // wie viele Sensoren
      //
      else if( key.equals( individualSensorsOn ) )
      {
        sendIndividualPrefs();
      }
      //
      // Salz/Süßwasser
      //
      else if( key.equals( individualPSCROn ) )
      {
        sendIndividualPrefs();
      }
      //
      // Salz/Süßwasser
      //
      else if( key.equals( individualAcousticWarnings ) )
      {
        sendIndividualPrefs();
      }
    }
  }

  @Override
  public void onViewCreated( View view, Bundle savedInstanceState )
  {
    super.onViewCreated( view, savedInstanceState );
    Log.v( TAG, "onViewCreated..." );
    PreferenceScreen ps = getPreferenceScreen();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "this preferencescreen has <" + ps.getPreferenceCount() + "> preferenes." );
    for( int groupIdx = 0; groupIdx < ps.getPreferenceCount(); groupIdx++ )
    {
      PreferenceGroup pg = ( PreferenceGroup )ps.getPreference( groupIdx );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "The Group <%s> has %d preferences", pg.getTitle(), pg.getPreferenceCount() ) );
      for( int prefIdx = 0; prefIdx < pg.getPreferenceCount(); prefIdx++ )
      {
        Preference pref = pg.getPreference( prefIdx );
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "The Preference <%s> is number %d", pref.getTitle(), prefIdx ) );
        // jede ungerade Zeile färben
        if( prefIdx % 2 > 0 )
        {
          if( FragmentCommonActivity.getAppStyle() == R.style.AppDarkTheme )
          {
            // dunkles Thema
            pref.setLayoutResource( R.layout.preference_dark );
          }
          else
          {
            // helles Thema
            pref.setLayoutResource( R.layout.preference_light );
          }
        }
        else
        {
          pref.setLayoutResource( R.layout.preference );
        }
      }
    }
  }

  /**
   * Wenn der User den Setpoint verändert hat, dann schicke das an den SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void sendAutoSetpoint()
  {
    ListPreference lP = null;
    // Preference pref = null;
    int autoSp = 0, sP = 0;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendAutoSetpoint()..." );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_autosetpoint ), true );
    //
    // aus den Voreinstellungen holen
    //
    lP = getListPreference( setpointAuto );
    if( lP == null )
    {
      return;
    }
    //
    // Autosetpoint (off/tiefe) holen
    //
    // setze den Index auf den Wert, der ausgelesen wurde
    // empfangen werden kann 0..3, also kan ich das 1:1 übernehmen
    //
    autoSp = lP.findIndexOfValue( lP.getValue() );
    //
    lP = getListPreference( setpointHigh );
    if( lP == null )
    {
      return;
    }
    //
    // Highsetpoint (partialdruck) einstellen
    //
    // setze den Index auf den Wert, der ausgelesen wurde
    // empfangen werden kann 0..4, also kan ich das 1:1 übernehmen
    //
    sP = lP.findIndexOfValue( lP.getValue() );
    //
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    try
    {
      fActivity.writeAutoSetpoint( autoSp, sP );
    }
    catch( FirmwareNotSupportetException ex )
    {
      // TODO Erzeuge Warnung an Benutzer!
      ex.printStackTrace();
    }
  }

  /**
   * Sende DECO Preferenzen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void sendDecoPrefs()
  {
    int lowG, highG, deepStops, dynGr, lastStop;
    SwitchPreference sp;
    //
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendDecoPrefs..." );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_deco ), true );
    //
    // Low/High Gradient erfragen
    //
    if( getPreferenceScreen().findPreference( decoGradient ) instanceof GradientPickerPreference )
    {
      GradientPickerPreference dgp = ( GradientPickerPreference )getPreferenceScreen().findPreference( decoGradient );
      if( dgp == null )
      {
        Log.e( TAG, "sendDecoPrefs: Key <" + decoGradient + "> was not found an GradientPickerPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Gradienten übernehmen
      //
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendDecoPrefs: get Gradients value from preference..." );
      int[] val = dgp.getValue();
      lowG = val[0];
      highG = val[1];
    }
    else
    {
      Log.e( TAG, "sendDecoPrefs: can't set gradient value to preference..." );
      return;
    }
    //
    // LastDeco Stop on/off übernehmen
    //
    sp = getSwitchPreference( decoLastStop );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für LastStop übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendDecoPrefs: get deco last stop value from preference..." );
    if( sp.isChecked() )
    {
      lastStop = 0;
    }
    else
    {
      lastStop = 1;
    }
    //
    // Dynamische Gradienten on/off lesen
    //
    sp = getSwitchPreference( decoDynGradients );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für LastStop übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendDecoPrefs: get dynGradients value from preference..." );
    if( sp.isChecked() )
    {
      dynGr = 1;
    }
    else
    {
      dynGr = 0;
    }
    //
    // Deep stops on/off lesen
    //
    sp = getSwitchPreference( decoDeepStops );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für LastStop übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendDecoPrefs: get deepStops value from preference..." );
    if( sp.isChecked() )
    {
      deepStops = 1;
    }
    else
    {
      deepStops = 0;
    }
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendDecoPrefs: write deco prefs via runningActivity..." );
    try
    {
      fActivity.writeDecoPrefs( lowG, highG, deepStops, dynGr, lastStop );
    }
    catch( FirmwareNotSupportetException ex )
    {
      // TODO Fehklermeldung an User generieren
      ex.printStackTrace();
    }
  }

  /**
   * Sende Display Einstellungen zum SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void sendDisplayPrefs()
  {
    ListPreference lP = null;
    int lumin = 1, orient = 0;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendDisplayPrefs()..." );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_display ), true );
    //
    // Helligkeit erfragen
    //
    lP = getListPreference( displayLuminance );
    if( lP == null )
    {
      return;
    }
    //
    // jetzt die Werte für Helligkeit übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "get display luminance value from preference..." );
    lumin = lP.findIndexOfValue( lP.getValue() );
    if( lumin == -1 ) lumin = 2;
    //
    // Display Ausrichtung erfragen
    //
    lP = getListPreference( displayOrient );
    if( lP == null )
    {
      return;
    }
    //
    // jetzt die Werte für Ausrichtung übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "get display orientation value from preference..." );
    orient = lP.findIndexOfValue( lP.getValue() );
    if( orient == -1 ) orient = 0;
    //
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( "sendDisplayPrefs: write display prefs via runningActivity lum:%d, orient:%d...", lumin, orient ) );
    try
    {
      fActivity.writeDisplayPrefs( lumin, orient );
    }
    catch( FirmwareNotSupportetException ex )
    {
      // TODO FEhlermeldung für User generieren
      ex.printStackTrace();
    }
  }

  /**
   * sende die individualeinstellungen zum SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void sendIndividualPrefs()
  {
    int sensorsOff = 0, pscrOff = 0, sensorsCount = 2, soundOn = 1, logInterval = 2, tempStick = 0;
    SwitchPreference sp;
    ListPreference lP;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendIndividualPrefs()..." );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_individuals ), true );
    // ~38:SE:PS:SC:SN:LI
    // SE: Sensors 0->ON 1->OFF
    // PS: PSCRMODE 0->OFF 1->ON
    // SC: SensorCount
    // SN: Sound 0->OFF 1->ON
    // LI: Loginterval 0->10sec 1->30Sec 2->60 Sec
    // TS: Temppstick 1,2 oder 3 (bei neueren Versionen)
    //
    // Sensoren an/aus ...
    //
    sp = getSwitchPreference( individualSensorsOn );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für Sensoren an/aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read sensors on/off value from preference..." );
    if( sp.isChecked() )
    {
      sensorsOff = 0;
    }
    else
    {
      sensorsOff = 1;
    }
    //
    // PSCR-Mode an/aus ...
    //
    sp = getSwitchPreference( individualPSCROn );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für PSCR an/aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendIndividualPrefs read PSCR-Mode on/off value from preference..." );
    if( sp.isChecked() )
    {
      pscrOff = 1;
    }
    else
    {
      pscrOff = 0;
    }
    //
    // Anzahl der Sensoren für die Berechnungen
    //
    lP = getListPreference( individualCountSensorWarning );
    if( lP == null )
    {
      ignorePrefChange = false;
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read sensors count value from preference..." );
    sensorsCount = lP.findIndexOfValue( lP.getValue() );
    if( sensorsCount == -1 ) sensorsCount = 2;
    //
    // Akustische Warnungen an/aus ...
    //
    sp = getSwitchPreference( individualAcousticWarnings );
    if( sp == null )
    {
      ignorePrefChange = false;
      return;
    }
    //
    // jetzt die Werte für Akustische Warnungen an/aus
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read acoustic warnings on/off value from preference..." );
    if( sp.isChecked() )
    {
      soundOn = 1;
    }
    else
    {
      soundOn = 0;
    }
    //
    // Loginterval in Oberfläche einbauen
    //
    lP = getListPreference( individualLoginterval );
    if( lP == null )
    {
      ignorePrefChange = false;
      return;
    }
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read loginterval value from preference..." );
    logInterval = lP.findIndexOfValue( lP.getValue() );
    if( logInterval == -1 ) logInterval = 2;
    //
    // Tempstick auslesen
    //
    if( FragmentCommonActivity.spxConfig.hasSixValuesIndividual() )
    {
      lP = getListPreference( individualTempStick );
      if( lP == null )
      {
        ignorePrefChange = false;
        return;
      }
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read tempstick typ value from preference..." );
      tempStick = lP.findIndexOfValue( lP.getValue() );
    }
    //
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( ApplicationDEBUG.DEBUG )
      Log.d( TAG, String.format( "sendIndividualPrefs: write individual prefs via runningActivity :<SE:%d, PS:%d, SC:%d, SN:%d, LI:%d, TS:%d>...", sensorsOff, pscrOff,
              sensorsCount, soundOn, logInterval, tempStick ) );
    try
    {
      fActivity.writeIndividualPrefs( sensorsOff, pscrOff, sensorsCount, soundOn, logInterval, tempStick );
    }
    catch( FirmwareNotSupportetException ex )
    {
      // Fehlermeldung für User generieren
      ex.printStackTrace();
    }
  }

  /**
   * Sende geänderte Einstellungen der Masseinheiten an den SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void sendUnitPrefs()
  {
    // UD= 1=Fahrenheit/0=Celsius => immer 0 in der aktuellen Firmware 2.6.7.7_U
    // UL= 0=metrisch 1=imperial
    // UW= 0->Salzwasser 1->Süßwasser
    int isTempImperial = 0, isDepthImperial = 0, isFreshwater = 1;
    SwitchPreference sp;
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendUnitPrefs()..." );
    theToast.showConnectionToast( getResources().getString( R.string.toast_comm_set_units ), true );
    //
    // Temperatur Einheit Celsius oder Imperial
    //
    sp = getSwitchPreference( unitsIsTempMetric );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für Temperatureinheit übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendUnitPrefs: get temp unit value from preference..." );
    if( sp.isChecked() )
    {
      // Celsius!
      isTempImperial = 0;
    }
    else
    {
      isTempImperial = 1;
    }
    //
    // Tiefeneinheit imperial oder metrisch
    //
    sp = getSwitchPreference( unitsIsDepthImperial );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für Tiefeneinheit übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendUnitPrefs: get depth unit value from preference..." );
    if( sp.isChecked() )
    {
      // metrisch
      isDepthImperial = 0;
    }
    else
    {
      isDepthImperial = 1;
    }
    //
    // Süß oder Salzwasser
    //
    sp = getSwitchPreference( unitsIsFreshwater );
    if( sp == null )
    {
      return;
    }
    //
    // jetzt die Werte für Süß oder Salzwasser übernehmen
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "sendUnitPrefs: get salnity value from preference..." );
    if( sp.isChecked() )
    {
      // süßwasser
      isFreshwater = 1;
    }
    else
    {
      isFreshwater = 0;
    }
    //
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( ApplicationDEBUG.DEBUG )
      Log.d( TAG, String.format( "sendUnitPrefs: write display prefs via runningActivity temp:%d, depth:%d, freshwater:%d...", isTempImperial, isDepthImperial, isFreshwater ) );
    try
    {
      fActivity.writeUnitPrefs( isTempImperial, isDepthImperial, isFreshwater );
    }
    catch( FirmwareNotSupportetException ex )
    {
      // TODO FEhlermeldung für User generieren
      ex.printStackTrace();
    }
  }

  /**
   * Setze alle Summarys auf ihren aktuellen Wert (weil das die Activity nicht selber macht) Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void setAllSummarys()
  {
    // SharedPreferences shared = getPreferenceManager().getSharedPreferences();
    //
    // Autosetpoint
    // High Setpoint
    //
    setSetpointSummarys();
    //
    // Deco gradienten Preset
    //
    setDecoSummary();
    //
    // Deco gradienten
    //
    setDecoGradientsSummary();
    //
    // Displayhelligkeit
    // Display Orientierung
    //
    setDisplaySummary();
    //
    // das nur bei Individuallizenz
    //
    if( FragmentCommonActivity.spxConfig.getCustomEnabled() == 1 )
    {
      setIndividualsSummary();
    }
  }

  /**
   * 
   * Setze Summary für Autosetpoint
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 02.01.2014
   */
  private void setSetpointSummarys()
  {
    ListPreference lP = null;
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( setpointAuto );
    lP.setSummary( String.format( getResources().getString( R.string.conf_autoset_summary ), lP.getEntry() ) );
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( setpointHigh );
    lP.setSummary( String.format( getResources().getString( R.string.conf_highset_summary ), lP.getEntry() ) );
  }

  /**
   * Gradienten in der Preferenz setzen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @param presetCandidateStr
   * @return
   */
  private boolean setDecoGradients( int[] presetCandidate )
  {
    String presetCandidateStr;
    //
    // wenn das ohne Voreinstelung kommt, einfach die Werte stehen lassen
    //
    if( presetCandidate[0] == 0 && presetCandidate[1] == 0 )
    {
      return( false );
    }
    //
    if( getPreferenceScreen().findPreference( decoGradient ) instanceof GradientPickerPreference )
    {
      GradientPickerPreference dgp = ( GradientPickerPreference )getPreferenceScreen().findPreference( decoGradient );
      if( dgp == null )
      {
        Log.e( TAG, "msgReciveDeco: Key <" + decoGradient + "> was not found an GradientPickerPreference! abort!" );
        return( false );
      }
      //
      // jetzt die Werte für Gradienten übernehmen
      //
      if( ApplicationDEBUG.DEBUG )
      {
        presetCandidateStr = String.format( Locale.getDefault(), "%02d:%02d", presetCandidate[0], presetCandidate[1] );
        Log.d( TAG, "set Gradients value to preference (" + presetCandidateStr + ")..." );
      }
      dgp.setValue( presetCandidate );
      setDecoGradientsSummary();
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "set Gradients value to preference (" + presetCandidateStr + ")...OK" );
      return( true );
    }
    else
    {
      Log.e( TAG, "can't set gradient value to preference..." );
      return( false );
    }
  }

  /**
   * 
   * Schjreibe DECO Gradienetne zum Gerät
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 10.11.2013
   * 
   * @param presetCandidate
   * @return Wahr, wenn alles OK
   */
  private boolean setDecoGradients( String presetCandidate )
  {
    int[] vals =
    { 0, 0 };
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setDecoGradients(STRING): String to split <" + presetCandidate + ">" );
    String fields[] = presetCandidate.split( ":" );
    if( ( fields != null ) && ( fields.length >= 2 ) )
    {
      Log.d( TAG, String.format( "makeValuesFromString: <%s> <%s>", fields[0], fields[1] ) );
      Log.d( TAG, "makeValuesFromString: successful split default value!" );
      try
      {
        vals[0] = Integer.parseInt( fields[0] );
        vals[1] = Integer.parseInt( fields[1] );
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setDecoGradients(STRING): successful set Values" );
        return( setDecoGradients( vals ) );
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAG, "setDecoGradients(STRING): <" + ex.getLocalizedMessage() + ">" );
        return( false );
      }
    }
    else
    {
      Log.w( TAG, "setDecoGradients(STRING): not correct default Value (" + presetCandidate + ")" );
    }
    return( false );
  }

  /**
   * Setze das Preset auf einen definierten Wert oder auf CUSTOM Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @param presetCandidate
   */
  private void setDecoGradientsPreset( int[] presetCandidate )
  {
    ListPreference lP;
    String presetCandidateStr = String.format( "%02d:%02d", presetCandidate[0], presetCandidate[1] );
    int i;
    //
    // in die Voreinstellungen übertragen, wenn es ein Preset ist
    //
    lP = getListPreference( decoGradientsPreset );
    if( lP == null )
    {
      return;
    }
    // zum vergleich, ob ein Preset da ist
    String[] gradientPresetsVals = getResources().getStringArray( R.array.gradientPresetValuesArray );
    // die Preferenz rausuchen
    lP = ( ListPreference )getPreferenceScreen().findPreference( decoGradientsPreset );
    // jetzt gucken ob es passt, wenn nichts passt -> "CUSTOM"
    for( i = 0; i < gradientPresetsVals.length; i++ )
    {
      if( presetCandidateStr.equals( gradientPresetsVals[i] ) )
      {
        if( ApplicationDEBUG.DEBUG )
        {
          String[] gradientPresetsNames = getResources().getStringArray( R.array.gradientPresetNamesArray );
          Log.d( TAG, "setDecoGradientsPreset: deco preset (" + presetCandidateStr + " = " + gradientPresetsNames[i] + ") found!" );
        }
        break;
      }
    }
    // wenn nicht gefunden, Preset auf CUSTOM!
    if( i >= gradientPresetsVals.length )
    {
      i = gradientPresetsVals.length - 1;
    }
    //
    // jetzt den Preset übernehmen
    // Index sollte i sein....
    //
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setDecoGradientsPreset: set preset value to preference..." );
    lP.setValueIndex( i );
  }

  /**
   * zusammenstellen der Summary für Gradienten Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void setDecoGradientsSummary()
  {
    int low = 0;
    int high = 0;
    //
    int[] val = getDecoGradients();
    try
    {
      low = val[0];
      high = val[1];
    }
    catch( Exception ex )
    {
      Log.e( TAG, "setDecoGradientsSummary: exception while gt value from deco gradients (" + ex.getLocalizedMessage() + ")" );
      return;
    }
    // die Summary Geschichte schreiben
    if( ApplicationDEBUG.DEBUG )
      Log.d( TAG, String.format( "setDecoGradientsSummary: write \"" + getResources().getString( R.string.conf_deco_gradient_summary ), low, high ) + "\"" );
    getPreferenceScreen().findPreference( decoGradient ).setSummary( String.format( getResources().getString( R.string.conf_deco_gradient_summary ), low, high ) );
  }

  /**
   * 
   * Setze Summary für DECO
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 02.01.2014
   */
  private void setDecoSummary()
  {
    ListPreference lP = null;
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( decoGradientsPreset );
    lP.setSummary( String.format( getResources().getString( R.string.conf_deco_presets_summary ), lP.getEntry() ) );
  }

  /**
   * 
   * Zeige Display Einstellungen als Summary
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 02.01.2014
   */
  private void setDisplaySummary()
  {
    ListPreference lP = null;
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( displayLuminance );
    lP.setSummary( String.format( getResources().getString( R.string.conf_luminance_header_summary ), lP.getValue() ) );
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( displayOrient );
    lP.setSummary( String.format( getResources().getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
  }

  /**
   * 
   * Setze Summary für Individuals
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 02.01.2014
   */
  private void setIndividualsSummary()
  {
    ListPreference lP = null;
    PreferenceScreen pS = getPreferenceScreen();
    Resources res = getResources();
    //
    // Sensors Count for Warning
    //
    lP = ( ListPreference )pS.findPreference( individualCountSensorWarning );
    lP.setSummary( String.format( res.getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
    //
    // Logintervall
    //
    lP = ( ListPreference )pS.findPreference( individualLoginterval );
    lP.setSummary( String.format( res.getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
    //
    // Tempstick
    //
    if( FragmentCommonActivity.spxConfig.hasSixValuesIndividual() )
    {
      lP = ( ListPreference )pS.findPreference( individualTempStick );
      lP.setSummary( String.format( res.getString( R.string.conf_ind_tempstick_header_summary ), lP.getEntry() ) );
    }
  }

  /**
   * 
   * Stelle die Preferenz entsprechend des Type der Helligkeit
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 16.11.2013
   * 
   * @param isNew
   * @return True ist in Ordnung, false ist FEHLER
   */
  private boolean setNewLuminancePropertys( boolean isNew )
  {
    PreferenceCategory pC = getPreferenceCategory( displayCategory );
    ListPreference lPL = getListPreference( displayLuminance );
    if( pC == null || lPL == null )
    {
      Log.e( TAG, "can't find Preference category <" + displayCategory + "> or ListPreference <" + displayLuminance + ">" );
      // TODO: Fehlermeldung generieren
      return( false );
    }
    if( isNew )
    {
      pC.setTitle( R.string.conf_display_header_new );
      lPL.setEntries( R.array.displayNewLuminanceNamesArray );
      lPL.setEntryValues( R.array.displayNewLuminanceValuesArray );
    }
    else
    {
      pC.setTitle( R.string.conf_display_header );
      lPL.setEntries( R.array.displayLuminanceNamesArray );
      lPL.setEntryValues( R.array.displayLuminanceValuesArray );
    }
    return( true );
  }
}
