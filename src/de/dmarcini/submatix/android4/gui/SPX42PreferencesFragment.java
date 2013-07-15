package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.utils.GradientPickerPreference;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * Ein Objekt zum bearbeiten der SPX42 Einstellungen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 31.12.2012
 */
public class SPX42PreferencesFragment extends PreferenceFragment implements IBtServiceListener, OnSharedPreferenceChangeListener
{
  private static final String    TAG                          = SPX42PreferencesFragment.class.getSimpleName();
  private static final int       maxEvents                    = 12;
  private Activity               runningActivity              = null;
  // Schlüsselwerte für Presets
  private static final String    setpointAuto                 = "keySetpointAutosetpointDepth";
  private static final String    setpointHigh                 = "keySetpointHighsetpointValue";
  private static final String    decoGradient                 = "keyDecoGradient";
  private static final String    decoGradientsPreset          = "keyDecoGradientPresets";
  private static final String    decoLastStop                 = "keyDecoLastStop";
  private static final String    decoDynGradients             = "keyDecoDynGradients";
  private static final String    decoDeepStops                = "keyDecoDeepStops";
  private static final String    displayLuminance             = "keyDisplayLuminance";
  private static final String    displayOrient                = "keyDisplayOrientation";
  private static final String    individualSensorsOn          = "keyIndividualSensorsOn";
  private static final String    individualPSCROn             = "keyindividualPSCROn";
  private static final String    individualCountSensorWarning = "keyIndividualCountSensorWarning";
  private static final String    individualAcousticWarnings   = "keyIndividualAcousticWarnings";
  private static final String    individualLoginterval        = "keyIndividualLoginterval";
  private static final String    unitsIsTempMetric            = "keyUnitsIsTempMetric";
  private static final String    unitsIsDepthImperial         = "keyUnitsIsDepthMetric";
  private static final String    unitsIsSaltwater             = "keyUnitsIsFreshwater";
  //
  private boolean                ignorePrefChange             = false;
  private FragmentProgressDialog pd                           = null;
  private String                 currFirmwareVersion          = null;

  /**
   * bitte-Warten Box verschwinden lassen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.06.2013
   */
  private void dismissDial()
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "dismissDial()..." );
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    Fragment prev = getFragmentManager().findFragmentByTag( "dialog" );
    if( prev != null )
    {
      ft.remove( prev );
    }
    if( pd != null )
    {
      pd.dismiss();
    }
  }

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
      // Seriennummer des ccomputers wurde gelesen
      // ################################################################
      case ProjectConst.MESSAGE_SERIAL_READ:
        msgRecivedSerial( smsg );
        break;
      // ################################################################
      // SPX sendet "ALIVE" und Ackuspannung
      // ################################################################
      case ProjectConst.MESSAGE_SPXALIVE:
        msgRecivedAlive( smsg );
        break;
      // ################################################################
      // SPX sendet Herstellerkennung
      // ################################################################
      case ProjectConst.MESSAGE_MANUFACTURER_READ:
        msgReciveManufacturer( smsg );
        break;
      // ################################################################
      // SPX sendet Firmwareversion
      // ################################################################
      case ProjectConst.MESSAGE_FWVERSION_READ:
        msgReciveFirmwareversion( smsg );
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
      // SPX Lizenz lesen
      // ################################################################
      case ProjectConst.MESSAGE_LICENSE_STATE_READ:
        msgReciveLicenseState( smsg );
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
      // Indioviduelle Einstellungen schreiben Bestätigung
      // ################################################################
      case ProjectConst.MESSAGE_INDIVID_ACK:
        msgReciveIndividualsAck( smsg );
        break;
      // ################################################################
      // Sonst....
      // ################################################################
      default:
        Log.w( TAG, "unhandled message with id <" + smsg.getId() + "> recived!" );
    }
  }

  /**
   * Feststellen, ob das die "kaputte" Firmware ist Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 14.07.2013
   * @return
   */
  private boolean isBuggyFirmware()
  {
    // Die Firmware gibt IMMER Fahrenheit zurück!
    if( currFirmwareVersion.equals( ProjectConst.FIRMWARE_2_6_7_7V ) )
    {
      return( true );
    }
    return false;
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
    Log.v( TAG, "msgConnected()...ask for SPX config..." );
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    if( BuildConfig.DEBUG ) Log.d( TAG, "msgConnected(): ask for SPX config..." );
    // Dialog schliesen, wenn geöffnet
    dismissDial();
    openWaitDial( maxEvents, getActivity().getResources().getString( R.string.dialog_please_wait_read_config ) );
    try
    {
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
    Intent intent = new Intent( getActivity(), areaListActivity.class );
    intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
    startActivity( intent );
    return;
  }

  @Override
  public void msgConnecting( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgDisconnected( BtServiceMessage msg )
  {
    // zum Menü zurück
    Intent intent = new Intent( getActivity(), areaListActivity.class );
    intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
    startActivity( intent );
    return;
  }

  @Override
  public void msgReciveAutosetpoint( BtServiceMessage msg )
  {
    ListPreference lP = null;
    // Preference pref = null;
    String[] setPoint;
    int autoSp, sP;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Autosetpoint recived" );
    //
    // versuche einmal, die beiden erwarteten Werter zu bekommen
    //
    if( msg.getContainer() instanceof String[] )
    {
      setPoint = ( String[] )msg.getContainer();
      if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Autosetpoint is <" + setPoint[0] + "," + setPoint[0] + ">" );
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
    if( getPreferenceScreen().findPreference( setpointAuto ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( setpointAuto );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + setpointAuto + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // Autosetpoint (off/tiefe) einstellen
      //
      // setze den Index auf den Wert, der ausgelesen wurde
      // empfangen werden kann 0..3, also kan ich das 1:1 übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set autosetpoint value to preference..." );
      lP.setValueIndex( autoSp );
    }
    else
    {
      Log.e( TAG, "can't set autosetpoint value to preference..." );
    }
    //
    if( getPreferenceScreen().findPreference( setpointHigh ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( setpointHigh );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + setpointHigh + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // Highsetpoint (partialdruck) einstellen
      //
      // setze den Index auf den Wert, der ausgelesen wurde
      // empfangen werden kann 0..4, also kan ich das 1:1 übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set highsetpoint value to preference..." );
      lP.setValueIndex( sP );
    }
    else
    {
      Log.e( TAG, "can't set highsetpoint value to preference..." );
    }
  }

  @Override
  public void msgReciveAutosetpointAck( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Autosetpoint successful set (preferences)" );
    ignorePrefChange = false;
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Alive <" + ( String )msg.getContainer() + "> recived" );
    dismissDial();
  }

  /*
   * Kommando DEC liefert zurück: ~34:LL:HH:D:Y:C LL=GF-Low, HH=GF-High, D=Deepstops (0/1) Y=Dynamische Gradienten (0/1) C=Last Decostop (0=3 Meter/1=6 Meter)
   */
  @Override
  public void msgReciveDeco( BtServiceMessage msg )
  {
    String[] decoParam;
    int[] presetCandidate =
    { 0, 0 };
    // String presetCandidate = "00:00";
    int lowG, highG, deepStops, dynGr, lastStop;
    //
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX deco settings recived" );
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
      deepStops = Integer.parseInt( decoParam[2] );
      dynGr = Integer.parseInt( decoParam[3] );
      lastStop = Integer.parseInt( decoParam[4] );
      // presetCandidate = String.format( "%02d:%02d", lowG, highG );
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
    if( BuildConfig.DEBUG )
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
    if( getPreferenceScreen().findPreference( decoLastStop ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( decoLastStop );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveDeco: Key <" + decoLastStop + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set deco last stop value to preference..." );
      sp.setChecked( ( lastStop > 0 ) );
    }
    else
    {
      Log.e( TAG, "can't set decoLastStop value to preference..." );
    }
    //
    // Dynamische Gradienten on/off übernehmen
    //
    if( getPreferenceScreen().findPreference( decoDynGradients ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( decoDynGradients );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveDeco: Key <" + decoDynGradients + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set dynGradients value to preference..." );
      sp.setChecked( ( dynGr > 0 ) );
    }
    else
    {
      Log.e( TAG, "can't set dynGradients value to preference..." );
    }
    //
    // Deep stops on/off übernehmen
    //
    if( getPreferenceScreen().findPreference( decoDeepStops ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( decoDeepStops );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveDeco: Key <" + decoDeepStops + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set deepStops value to preference..." );
      sp.setChecked( ( deepStops > 0 ) );
    }
    else
    {
      Log.e( TAG, "can't set dynGradients value to preference..." );
    }
    ignorePrefChange = false;
  }

  @Override
  public void msgReciveDecoAck( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX DECO propertys successful set (preferences)" );
    ignorePrefChange = false;
  }

  @Override
  public void msgReciveDisplay( BtServiceMessage msg )
  {
    String[] displayParm;
    int lumin = 0;
    int orient = 0;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX display settings recived" );
    // Kommando GET_SETUP_DISPLAYSETTINGS liefert
    // ~36:D:A
    // D= 0->10&, 1->50%, 2->100%
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
      if( lumin < 0 || lumin > 2 ) lumin = 1;
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
    if( getPreferenceScreen().findPreference( displayLuminance ) instanceof ListPreference )
    {
      // die Preferenz rausuchen
      ListPreference lP = ( ListPreference )getPreferenceScreen().findPreference( displayLuminance );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveDisplay: Key <" + displayLuminance + "> was not found an ListPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt den Preset übernehmen
      // Index sollte lumin sein....
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "msgReciveDisplay: set luminance value to preference..." );
      lP.setValueIndex( lumin );
    }
    else
    {
      Log.e( TAG, "msgReciveDisplay: can't set luminance preset value to preference..." );
    }
    //
    // jetzt Orientierung eintragen
    //
    if( getPreferenceScreen().findPreference( displayOrient ) instanceof ListPreference )
    {
      // die Preferenz rausuchen
      ListPreference lP = ( ListPreference )getPreferenceScreen().findPreference( displayOrient );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveDisplay: Key <" + displayOrient + "> was not found an ListPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt den Preset übernehmen
      // Index sollte lumin sein....
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "msgReciveDisplay: set display angle value to preference..." );
      lP.setValueIndex( orient );
    }
    else
    {
      Log.e( TAG, "msgReciveDisplay: can't set display angle preset value to preference..." );
    }
    ignorePrefChange = false;
  }

  @Override
  public void msgReciveDisplayAck( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX display settings ACK recived" );
    ignorePrefChange = false;
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedSerial( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgReciveFirmwareversion( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Firmware <" + ( String )msg.getContainer() + "> recived" );
    currFirmwareVersion = ( String )msg.getContainer();
  }

  @Override
  public void msgReciveIndividuals( BtServiceMessage msg )
  {
    String[] individualParm;
    int sensorsOff = 0, pscrOff = 0, sensorsCount = 3, soundOn = 1, logInterval = 2;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX individuals settings recived" );
    //
    // gibt es Parameter zu lesen?
    // Kommando GET_SETUP_INDIVIDUAL liefert
    // ~38:SE:PS:SC:SN:LI
    // SE: Sensors 0->ON 1->OFF
    // PS: PSCRMODE 0->OFF 1->ON
    // SC: SensorCount
    // SN: Sound 0->OFF 1->ON
    // LI: Loginterval 0->10sec 1->30Sec 2->60 Sec
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
      if( BuildConfig.DEBUG )
        Log.d( TAG, String.format( "SPX individuals settings <SE:%d, PS:%d, SC:%d, SN:%d, LI:%d>", sensorsOff, pscrOff, sensorsCount, soundOn, logInterval ) );
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
    if( getPreferenceScreen().findPreference( individualSensorsOn ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( individualSensorsOn );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveIndividuals: Key <" + individualSensorsOn + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für Sensoren an/aus
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set sensors on/off value to preference..." );
      sp.setChecked( ( sensorsOff == 0 ) );
    }
    else
    {
      Log.e( TAG, "can't set sensors on/off value to preference..." );
    }
    //
    // PSCR-Mode an/aus ...
    //
    if( getPreferenceScreen().findPreference( individualPSCROn ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( individualPSCROn );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveIndividuals: Key <" + individualPSCROn + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für PSCR an/aus
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set PSCR-Mode on/off value to preference..." );
      sp.setChecked( ( pscrOff == 1 ) );
    }
    else
    {
      Log.e( TAG, "can't set PSCR-Mode on/off value to preference..." );
    }
    //
    // Anzahl der Sensoren für die Berechnungen wählen
    //
    if( getPreferenceScreen().findPreference( individualCountSensorWarning ) instanceof ListPreference )
    {
      // die Preferenz rausuchen
      ListPreference lP = ( ListPreference )getPreferenceScreen().findPreference( individualCountSensorWarning );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveIndividuals: Key <" + individualCountSensorWarning + "> was not found an ListPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      if( BuildConfig.DEBUG ) Log.d( TAG, "msgReciveIndividuals: set sensors count value to preference..." );
      lP.setValueIndex( sensorsCount );
      lP.setSummary( String.format( getResources().getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
    }
    else
    {
      Log.e( TAG, "msgReciveIndividuals: can't set count sensors preset value to preference..." );
    }
    //
    // Akustische Warnungen an/aus ...
    //
    if( getPreferenceScreen().findPreference( individualAcousticWarnings ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( individualAcousticWarnings );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveIndividuals: Key <" + individualAcousticWarnings + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für Akustische Warnungen an/aus
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set acoustic warnings on/off value to preference..." );
      sp.setChecked( ( soundOn == 1 ) );
    }
    else
    {
      Log.e( TAG, "can't set acoustic warnings on/off value to preference..." );
    }
    //
    // Loginterval in Oberfläche einbauen
    //
    if( getPreferenceScreen().findPreference( individualLoginterval ) instanceof ListPreference )
    {
      // die Preferenz rausuchen
      ListPreference lP = ( ListPreference )getPreferenceScreen().findPreference( individualLoginterval );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveIndividuals: Key <" + individualLoginterval + "> was not found an ListPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      if( BuildConfig.DEBUG ) Log.d( TAG, "msgReciveIndividuals: set loginterval value to preference..." );
      lP.setValueIndex( logInterval );
      lP.setSummary( String.format( getResources().getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
    }
    else
    {
      Log.e( TAG, "msgReciveIndividuals: can't set log interval preset value to preference..." );
    }
    ignorePrefChange = false;
  }

  @Override
  public void msgReciveIndividualsAck( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX INDIVIDUALS settings ACK recived" );
    ignorePrefChange = false;
  }

  @Override
  public void msgReciveLicenseState( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgReciveManufacturer( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Manufacturer <" + ( String )msg.getContainer() + "> recived" );
  }

  @Override
  public void msgReciveUnits( BtServiceMessage msg )
  {
    // Kommando SPX_GET_SETUP_UNITS
    // ~37:UD:UL:UW
    // UD= 1=Fahrenheit/0=Celsius => immer 0 in der aktuellen Firmware 2.6.7.7_U
    // UL= 0=metrisch 1=imperial
    // UW= 0->Salzwasser 1->Süßwasser
    int isTempImperial = 0, isDepthImperial = 0, isFreshwater = 0;
    String[] unitsParm;
    //
    if( msg.getContainer() instanceof String[] )
    {
      unitsParm = ( String[] )msg.getContainer();
      if( BuildConfig.DEBUG )
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
    if( isBuggyFirmware() )
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
      if( BuildConfig.DEBUG ) Log.w( TAG, "msgReciveUnits: SPX firmware is buggy version: switch all to " + ( isDepthImperial == 0 ? "metric" : "imperial" ) );
    }
    ignorePrefChange = true;
    //
    // Temperatur...
    //
    if( getPreferenceScreen().findPreference( unitsIsTempMetric ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( unitsIsTempMetric );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveUnits: Key <" + unitsIsTempMetric + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für Temperatureinheit übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set temp unit value to preference..." );
      sp.setChecked( ( isTempImperial == 0 ) );
    }
    else
    {
      Log.e( TAG, "can't set temp unit value to preference..." );
    }
    //
    // Tiefeneinheit...
    //
    if( getPreferenceScreen().findPreference( unitsIsDepthImperial ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( unitsIsDepthImperial );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveUnits: Key <" + unitsIsDepthImperial + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für Tiefgeneinheit übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set depth unit value to preference..." );
      sp.setChecked( ( isDepthImperial == 0 ) );
    }
    else
    {
      Log.e( TAG, "can't set temp unit value to preference..." );
    }
    //
    // Süsswasser...
    //
    if( getPreferenceScreen().findPreference( unitsIsSaltwater ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( unitsIsSaltwater );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveUnits: Key <" + unitsIsSaltwater + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt Wert für Süß oder Salzwasser eintragen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "set salnity value to preference..." );
      sp.setChecked( ( isFreshwater > 0 ) );
    }
    else
    {
      Log.e( TAG, "can't set salnity unit value to preference..." );
    }
    ignorePrefChange = false;
  }

  @Override
  public void msgReciveUnitsAck( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX units settings ACK recived" );
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
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_preference_individual + ">..." );
    if( FragmentCommonActivity.isIndividual )
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "Preferences in INDIVIDUAL Mode" );
      addPreferencesFromResource( R.xml.config_spx42_preference_individual );
    }
    else
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "Preferences in STANDART Mode" );
      addPreferencesFromResource( R.xml.config_spx42_preference_std );
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
        Intent intent = new Intent( getActivity(), areaListActivity.class );
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
    if( BuildConfig.DEBUG ) Log.d( TAG, "onPause(): clear service listener for preferences fragment..." );
    ( ( FragmentCommonActivity )runningActivity ).removeServiceListener( this );
  }

  @Override
  public synchronized void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume()..." );
    currFirmwareVersion = null;
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
    ignorePrefChange = true;
    // Service Listener setzen
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    if( BuildConfig.DEBUG ) Log.d( TAG, "onResume(): set service listener for preferences fragment..." );
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
      if( BuildConfig.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged(): ignore Change Event" );
      return;
    }
    if( BuildConfig.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged(): key = <" + key + ">" );
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
        lP.setSummary( String.format( getResources().getString( R.string.conf_luminance_header_summary ), lP.getEntry() ) );
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
      else if( key.equals( unitsIsSaltwater ) )
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
    if( BuildConfig.DEBUG ) Log.d( TAG, "this preferencescreen has <" + ps.getPreferenceCount() + "> preferenes." );
    for( int groupIdx = 0; groupIdx < ps.getPreferenceCount(); groupIdx++ )
    {
      PreferenceGroup pg = ( PreferenceGroup )ps.getPreference( groupIdx );
      if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "The Group <%s> has %d preferences", pg.getTitle(), pg.getPreferenceCount() ) );
      for( int prefIdx = 0; prefIdx < pg.getPreferenceCount(); prefIdx++ )
      {
        Preference pref = pg.getPreference( prefIdx );
        if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "The Preference <%s> is number %d", pref.getTitle(), prefIdx ) );
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
   * den Bitte-warten Dialog anzeigen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.06.2013
   * @param maxevents
   *          maximal zu erwartende ereignisse
   * @param msg
   *          Nachricht
   */
  private void openWaitDial( int maxevents, String msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "openWaitDial()..." );
    //
    // wenn ein Dialog da ist, erst mal aus den Fragmenten entfernen
    //
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    Fragment prev = getFragmentManager().findFragmentByTag( "dialog" );
    if( prev != null )
    {
      ft.remove( prev );
    }
    if( pd != null )
    {
      pd.dismiss();
    }
    pd = new FragmentProgressDialog( msg );
    pd.setCancelable( true );
    pd.setMax( maxevents );
    pd.setProgress( 4 );
    ft.addToBackStack( null );
    pd.show( ft, "dialog" );
  }

  /**
   * Wenn der User den Setpoint verändert hat, dann schicke das an den SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 02.06.2013
   */
  private void sendAutoSetpoint()
  {
    ListPreference lP = null;
    // Preference pref = null;
    int autoSp = 0, sP = 0;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendAutoSetpoint()..." );
    //
    // aus den Voreinstellungen holen
    //
    if( getPreferenceScreen().findPreference( setpointAuto ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( setpointAuto );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + setpointAuto + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // Autosetpoint (off/tiefe) holen
      //
      // setze den Index auf den Wert, der ausgelesen wurde
      // empfangen werden kann 0..3, also kan ich das 1:1 übernehmen
      //
      autoSp = lP.findIndexOfValue( lP.getValue() );
    }
    if( getPreferenceScreen().findPreference( setpointHigh ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( setpointHigh );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + setpointHigh + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // Highsetpoint (partialdruck) einstellen
      //
      // setze den Index auf den Wert, der ausgelesen wurde
      // empfangen werden kann 0..4, also kan ich das 1:1 übernehmen
      //
      sP = lP.findIndexOfValue( lP.getValue() );
    }
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    fActivity.writeAutoSetpoint( autoSp, sP );
  }

  /**
   * Sende DECO Preferenzen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.07.2013
   */
  private void sendDecoPrefs()
  {
    int lowG, highG, deepStops, dynGr, lastStop;
    //
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendDecoPrefs..." );
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
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendDecoPrefs: get Gradients value from preference..." );
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
    if( getPreferenceScreen().findPreference( decoLastStop ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( decoLastStop );
      if( sp == null )
      {
        Log.e( TAG, "sendDecoPrefs: Key <" + decoLastStop + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendDecoPrefs: get deco last stop value from preference..." );
      if( sp.isChecked() )
      {
        lastStop = 1;
      }
      else
      {
        lastStop = 0;
      }
    }
    else
    {
      Log.e( TAG, "sendDecoPrefs: can't set decoLastStop value to preference..." );
      return;
    }
    //
    // Dynamische Gradienten on/off lesen
    //
    if( getPreferenceScreen().findPreference( decoDynGradients ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( decoDynGradients );
      if( sp == null )
      {
        Log.e( TAG, "sendDecoPrefs: Key <" + decoDynGradients + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendDecoPrefs: get dynGradients value from preference..." );
      if( sp.isChecked() )
      {
        dynGr = 1;
      }
      else
      {
        dynGr = 0;
      }
    }
    else
    {
      Log.e( TAG, "sendDecoPrefs: can't set dynGradients value to preference..." );
      return;
    }
    //
    // Deep stops on/off lesen
    //
    if( getPreferenceScreen().findPreference( decoDeepStops ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( decoDeepStops );
      if( sp == null )
      {
        Log.e( TAG, "sendDecoPrefs: Key <" + decoDeepStops + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendDecoPrefs: get deepStops value from preference..." );
      if( sp.isChecked() )
      {
        deepStops = 1;
      }
      else
      {
        deepStops = 0;
      }
    }
    else
    {
      Log.e( TAG, "sendDecoPrefs: can't set dynGradients value to preference..." );
      return;
    }
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendDecoPrefs: write deco prefs via runningActivity..." );
    fActivity.writeDecoPrefs( lowG, highG, deepStops, dynGr, lastStop );
  }

  /**
   * Sende Display Einstellungen zum SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 14.07.2013
   */
  private void sendDisplayPrefs()
  {
    ListPreference lP = null;
    int lumin = 1, orient = 0;
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendDisplayPrefs()..." );
    //
    // Helligkeit erfragen
    //
    if( getPreferenceScreen().findPreference( displayLuminance ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( displayLuminance );
      if( lP == null )
      {
        Log.e( TAG, "sendDisplayPrefs: Key <" + displayLuminance + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Helligkeit übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "get display luminance value from preference..." );
      lumin = lP.findIndexOfValue( lP.getValue() );
      if( lumin == -1 ) lumin = 2;
    }
    else
    {
      Log.e( TAG, "can't get value for luminance from preference.." );
      return;
    }
    //
    // Display Ausrichtung erfragen
    //
    if( getPreferenceScreen().findPreference( displayOrient ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( displayOrient );
      if( lP == null )
      {
        Log.e( TAG, "sendDisplayPrefs: Key <" + displayOrient + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Ausrichtung übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "get display orientation value from preference..." );
      orient = lP.findIndexOfValue( lP.getValue() );
      if( orient == -1 ) orient = 0;
    }
    else
    {
      Log.e( TAG, "sendDisplayPrefs: can't get value for display orientation from preference.." );
      return;
    }
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "sendDisplayPrefs: write display prefs via runningActivity lum:%d, orient:%d...", lumin, orient ) );
    fActivity.writeDisplayPrefs( lumin, orient );
  }

  /**
   * sende die individualeinstellungen zum SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 14.07.2013
   */
  private void sendIndividualPrefs()
  {
    int sensorsOff = 0, pscrOff = 0, sensorsCount = 2, soundOn = 1, logInterval = 2;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendIndividualPrefs()..." );
    // ~38:SE:PS:SC:SN:LI
    // SE: Sensors 0->ON 1->OFF
    // PS: PSCRMODE 0->OFF 1->ON
    // SC: SensorCount
    // SN: Sound 0->OFF 1->ON
    // LI: Loginterval 0->10sec 1->30Sec 2->60 Sec
    //
    // Sensoren an/aus ...
    //
    if( getPreferenceScreen().findPreference( individualSensorsOn ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( individualSensorsOn );
      if( sp == null )
      {
        Log.e( TAG, "sendIndividualPrefs: Key <" + individualSensorsOn + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Sensoren an/aus
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read sensors on/off value from preference..." );
      if( sp.isChecked() )
      {
        sensorsOff = 0;
      }
      else
      {
        sensorsOff = 1;
      }
    }
    else
    {
      Log.e( TAG, "sendIndividualPrefs: can't read sensors on/off value from preference..." );
      return;
    }
    //
    // PSCR-Mode an/aus ...
    //
    if( getPreferenceScreen().findPreference( individualPSCROn ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( individualPSCROn );
      if( sp == null )
      {
        Log.e( TAG, "sendIndividualPrefs: Key <" + individualPSCROn + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für PSCR an/aus
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendIndividualPrefs read PSCR-Mode on/off value from preference..." );
      if( sp.isChecked() )
      {
        pscrOff = 1;
      }
      else
      {
        pscrOff = 0;
      }
    }
    else
    {
      Log.e( TAG, "sendIndividualPrefs: can't read PSCR-Mode on/off value from preference..." );
    }
    //
    // Anzahl der Sensoren für die Berechnungen
    //
    if( getPreferenceScreen().findPreference( individualCountSensorWarning ) instanceof ListPreference )
    {
      // die Preferenz rausuchen
      ListPreference lP = ( ListPreference )getPreferenceScreen().findPreference( individualCountSensorWarning );
      if( lP == null )
      {
        Log.e( TAG, "sendIndividualPrefs: Key <" + individualCountSensorWarning + "> was not found an ListPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read sensors count value from preference..." );
      sensorsCount = lP.findIndexOfValue( lP.getValue() );
      if( sensorsCount == -1 ) sensorsCount = 2;
    }
    else
    {
      Log.e( TAG, "sendIndividualPrefs: can't read count sensors preset value from preference..." );
    }
    //
    // Akustische Warnungen an/aus ...
    //
    if( getPreferenceScreen().findPreference( individualAcousticWarnings ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( individualAcousticWarnings );
      if( sp == null )
      {
        Log.e( TAG, "msgReciveIndividuals: Key <" + individualAcousticWarnings + "> was not found an SwitchPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      //
      // jetzt die Werte für Akustische Warnungen an/aus
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read acoustic warnings on/off value from preference..." );
      if( sp.isChecked() )
      {
        soundOn = 1;
      }
      else
      {
        soundOn = 0;
      }
    }
    else
    {
      Log.e( TAG, "sendIndividualPrefs: can't read acoustic warnings on/off value from preference..." );
    }
    //
    // Loginterval in Oberfläche einbauen
    //
    if( getPreferenceScreen().findPreference( individualLoginterval ) instanceof ListPreference )
    {
      // die Preferenz rausuchen
      ListPreference lP = ( ListPreference )getPreferenceScreen().findPreference( individualLoginterval );
      if( lP == null )
      {
        Log.e( TAG, "sendIndividualPrefs: Key <" + individualLoginterval + "> was not found an ListPreference! abort!" );
        ignorePrefChange = false;
        return;
      }
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendIndividualPrefs: read loginterval value from preference..." );
      logInterval = lP.findIndexOfValue( lP.getValue() );
      if( logInterval == -1 ) logInterval = 2;
    }
    else
    {
      Log.e( TAG, "sendIndividualPrefs: can't read log interval preset value from preference..." );
    }
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( BuildConfig.DEBUG )
      Log.d( TAG, String.format( "sendIndividualPrefs: write individual prefs via runningActivity :<SE:%d, PS:%d, SC:%d, SN:%d, LI:%d>...", sensorsOff, pscrOff, sensorsCount,
              soundOn, logInterval ) );
    fActivity.writeIndividualPrefs( sensorsOff, pscrOff, sensorsCount, soundOn, logInterval );
  }

  /**
   * Sende geänderte Einstellungen der Masseinheiten an den SPX Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 14.07.2013
   */
  private void sendUnitPrefs()
  {
    // UD= 1=Fahrenheit/0=Celsius => immer 0 in der aktuellen Firmware 2.6.7.7_U
    // UL= 0=metrisch 1=imperial
    // UW= 0->Salzwasser 1->Süßwasser
    int isTempImperial = 0, isDepthImperial = 0, isFreshwater = 1;
    SwitchPreference sP = null;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendUnitPrefs()..." );
    //
    // Temperatur Einheit Celsius oder Imperial
    //
    if( getPreferenceScreen().findPreference( unitsIsTempMetric ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( unitsIsTempMetric );
      if( sp == null )
      {
        Log.e( TAG, "sendUnitPrefs: Key <" + unitsIsTempMetric + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Temperatureinheit übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendUnitPrefs: get temp unit value from preference..." );
      if( sp.isChecked() )
      {
        // Celsius!
        isTempImperial = 0;
      }
      else
      {
        isTempImperial = 1;
      }
    }
    else
    {
      Log.e( TAG, "sendUnitPrefs: can't read tempterature unit value to preference..." );
      return;
    }
    //
    // Tiefeneinheit imperial oder metrisch
    //
    if( getPreferenceScreen().findPreference( unitsIsDepthImperial ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( unitsIsDepthImperial );
      if( sp == null )
      {
        Log.e( TAG, "sendUnitPrefs: Key <" + unitsIsDepthImperial + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Tiefeneinheit übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendUnitPrefs: get depth unit value from preference..." );
      if( sp.isChecked() )
      {
        // metrisch
        isDepthImperial = 0;
      }
      else
      {
        isDepthImperial = 1;
      }
    }
    else
    {
      Log.e( TAG, "sendUnitPrefs: can't read depth unit value to preference..." );
      return;
    }
    //
    // Süß oder Salzwasser
    //
    if( getPreferenceScreen().findPreference( unitsIsSaltwater ) instanceof SwitchPreference )
    {
      SwitchPreference sp = ( SwitchPreference )getPreferenceScreen().findPreference( unitsIsSaltwater );
      if( sp == null )
      {
        Log.e( TAG, "sendUnitPrefs: Key <" + unitsIsSaltwater + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Süß oder Salzwasser übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "sendUnitPrefs: get salnity value from preference..." );
      if( sp.isChecked() )
      {
        // süßwasser
        isFreshwater = 1;
      }
      else
      {
        isFreshwater = 0;
      }
    }
    else
    {
      Log.e( TAG, "sendUnitPrefs: can't read salnity value to preference..." );
      return;
    }
    //
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( BuildConfig.DEBUG )
      Log.d( TAG, String.format( "sendUnitPrefs: write display prefs via runningActivity temp:%d, depth:%d, freshwater:%d...", isTempImperial, isDepthImperial, isFreshwater ) );
    fActivity.writeUnitPrefs( isTempImperial, isDepthImperial, isFreshwater );
  }

  /**
   * Setze alle Summarys auf ihren aktuellen Wert (weil das die Activity nicht selber macht) Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 31.12.2012
   */
  private void setAllSummarys()
  {
    ListPreference lP = null;
    PreferenceScreen pS = getPreferenceScreen();
    Resources res = getResources();
    // SharedPreferences shared = getPreferenceManager().getSharedPreferences();
    //
    // Autoset
    //
    lP = ( ListPreference )pS.findPreference( setpointAuto );
    lP.setSummary( String.format( res.getString( R.string.conf_autoset_summary ), lP.getEntry() ) );
    //
    // High Setpoint
    //
    lP = ( ListPreference )pS.findPreference( setpointHigh );
    lP.setSummary( String.format( res.getString( R.string.conf_highset_summary ), lP.getEntry() ) );
    //
    // Deco gradienten Preset
    //
    lP = ( ListPreference )pS.findPreference( decoGradientsPreset );
    lP.setSummary( String.format( res.getString( R.string.conf_deco_presets_summary ), lP.getEntry() ) );
    //
    // Deco gradienten
    //
    setDecoGradientsSummary();
    //
    // Displayhelligkeit
    //
    lP = ( ListPreference )pS.findPreference( displayLuminance );
    lP.setSummary( String.format( res.getString( R.string.conf_luminance_header_summary ), lP.getEntry() ) );
    //
    // Display Orientierung
    //
    lP = ( ListPreference )pS.findPreference( displayOrient );
    lP.setSummary( String.format( res.getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
    //
    // das nur bei Individuallizenz
    //
    if( FragmentCommonActivity.isIndividual )
    {
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
    }
  }

  /**
   * Gradienten in der Preferenz setzen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.07.2013
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
      if( BuildConfig.DEBUG )
      {
        presetCandidateStr = String.format( "%02d:%02d", presetCandidate[0], presetCandidate[1] );
        Log.d( TAG, "set Gradients value to preference (" + presetCandidateStr + ")..." );
      }
      dgp.setValue( presetCandidate );
      setDecoGradientsSummary();
      if( BuildConfig.DEBUG ) Log.d( TAG, "set Gradients value to preference (" + presetCandidateStr + ")...OK" );
      return( true );
    }
    else
    {
      Log.e( TAG, "can't set gradient value to preference..." );
      return( false );
    }
  }

  private boolean setDecoGradients( String presetCandidate )
  {
    int[] vals =
    { 0, 0 };
    if( BuildConfig.DEBUG ) Log.d( TAG, "setDecoGradients(STRING): String to split <" + presetCandidate + ">" );
    String fields[] = presetCandidate.split( ":" );
    if( ( fields != null ) && ( fields.length >= 2 ) )
    {
      Log.d( TAG, String.format( "makeValuesFromString: <%s> <%s>", fields[0], fields[1] ) );
      Log.d( TAG, "makeValuesFromString: successful split default value!" );
      try
      {
        vals[0] = Integer.parseInt( fields[0] );
        vals[1] = Integer.parseInt( fields[1] );
        if( BuildConfig.DEBUG ) Log.d( TAG, "setDecoGradients(STRING): successful set Values" );
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
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 14.07.2013
   * @param presetCandidate
   */
  private void setDecoGradientsPreset( int[] presetCandidate )
  {
    String presetCandidateStr = String.format( "%02d:%02d", presetCandidate[0], presetCandidate[1] );
    int i;
    //
    // in die Voreinstellungen übertragen, wenn es ein Preset ist
    //
    if( getPreferenceScreen().findPreference( decoGradientsPreset ) instanceof ListPreference )
    {
      // zum vergleich, ob ein Preset da ist
      String[] gradientPresetsVals = getResources().getStringArray( R.array.gradientPresetValuesArray );
      // die Preferenz rausuchen
      ListPreference lP = ( ListPreference )getPreferenceScreen().findPreference( decoGradientsPreset );
      if( lP == null )
      {
        Log.e( TAG, "setDecoGradientsPreset: Key <" + decoGradientsPreset + "> was not found an ListPreference! abort!" );
        return;
      }
      // jetzt gucken ob es passt, wenn nichts passt -> "CUSTOM"
      for( i = 0; i < gradientPresetsVals.length; i++ )
      {
        if( presetCandidateStr.equals( gradientPresetsVals[i] ) )
        {
          if( BuildConfig.DEBUG )
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
      if( BuildConfig.DEBUG ) Log.d( TAG, "setDecoGradientsPreset: set preset value to preference..." );
      lP.setValueIndex( i );
    }
    else
    {
      Log.e( TAG, "setDecoGradientsPreset: can't set gradient preset value to preference..." );
    }
  }

  /**
   * zusammenstellen der Summary für Gradienten Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 13.07.2013
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
    if( BuildConfig.DEBUG ) Log.d( TAG, String.format( "setDecoGradientsSummary: write " + getResources().getString( R.string.conf_deco_gradient_summary ), low, high ) );
    getPreferenceScreen().findPreference( decoGradient ).setSummary( String.format( getResources().getString( R.string.conf_deco_gradient_summary ), low, high ) );
  }
}
