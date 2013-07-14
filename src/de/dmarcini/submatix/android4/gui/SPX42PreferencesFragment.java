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
 * 
 */
public class SPX42PreferencesFragment extends PreferenceFragment implements IBtServiceListener, OnSharedPreferenceChangeListener
{
  private static final String    TAG              = SPX42PreferencesFragment.class.getSimpleName();
  private static final int       maxEvents        = 12;
  private Activity               runningActivity  = null;
  private boolean                ignorePrefChange = false;
  private FragmentProgressDialog pd               = null;

  /**
   * 
   * bitte-Warten Box verschwinden lassen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.06.2013
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
    if( getPreferenceScreen().findPreference( "keyDecoGradient" ) instanceof GradientPickerPreference )
    {
      GradientPickerPreference pr = ( GradientPickerPreference )getPreferenceScreen().findPreference( "keyDecoGradient" );
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
      // Sonst....
      // ################################################################
      default:
        Log.w( TAG, "unhandled message with id <" + smsg.getId() + "> recived!" );
    }
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
    String autoSetpointKey = "keySetpointAutosetpointDepth";
    String highSetpointKey = "keySetpointHighsetpointValue";
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
    if( getPreferenceScreen().findPreference( autoSetpointKey ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( autoSetpointKey );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + autoSetpointKey + "> was not found an ListPreference! abort!" );
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
    if( getPreferenceScreen().findPreference( highSetpointKey ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( highSetpointKey );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + highSetpointKey + "> was not found an ListPreference! abort!" );
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
    String decoLastStop = "keyDecoLastStop";
    String decoDynGradients = "keyDecoDynGradients";
    String decoDeepStops = "keyDecoDeepStops";
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
    String displayLuminance = "keyDisplayLuminance";
    String displayOrient = "keyDisplayOrientation";
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
      addPreferencesFromResource( R.xml.config_spx42_preference_individual );
    }
    else
    {
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
      if( key.equals( "keySetpointAutosetpointDepth" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_autoset_summary ), lP.getEntry() ) );
        sendAutoSetpoint();
      }
      //
      // Highsetpoint (wenn on)
      //
      else if( key.equals( "keySetpointHighsetpointValue" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_highset_summary ), lP.getEntry() ) );
        sendAutoSetpoint();
      }
      //
      // DECO-Preset, wenn ON
      //
      else if( key.equals( "keyDecoGradientPresets" ) )
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
      else if( key.equals( "keyDisplayLuminance" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_luminance_header_summary ), lP.getEntry() ) );
        sendDisplayPrefs();
      }
      //
      // Orientierung Display
      //
      else if( key.equals( "keyDisplayOrientation" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
        sendDisplayPrefs();
      }
      //
      // Sensors Count Warning
      //
      else if( key.equals( "keyIndividualCountSensorWarning" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
      }
      //
      // Intervall zwischen zwei Logeinträgen
      //
      else if( key.equals( "keyIndividualLoginterval" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
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
      if( key.equals( "keyDecoGradient" ) )
      {
        setDecoGradientsSummary();
        int[] val = getDecoGradients();
        setDecoGradientsPreset( val );
        sendDecoPrefs();
      }
      //
      // DECO Last Stop verändert
      //
      else if( key.equals( "keyDecoLastStop" ) )
      {
        sendDecoPrefs();
      }
      //
      // DECO dyn gradients
      //
      else if( key.equals( "keyDecoDynGradients" ) )
      {
        sendDecoPrefs();
      }
      //
      // DECO deep stops
      //
      else if( key.equals( "keyDecoDeepStops" ) )
      {
        sendDecoPrefs();
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
   * 
   * den Bitte-warten Dialog anzeigen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.06.2013
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
   * 
   * Wenn der User den Setpoint verändert hat, dann schicke das an den SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.06.2013
   */
  private void sendAutoSetpoint()
  {
    String autoSetpointKey = "keySetpointAutosetpointDepth";
    String highSetpointKey = "keySetpointHighsetpointValue";
    ListPreference lP = null;
    // Preference pref = null;
    int autoSp = 0, sP = 0;
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendAutoSetpoint()..." );
    //
    // aus den Voreinstellungen holen
    //
    if( getPreferenceScreen().findPreference( autoSetpointKey ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( autoSetpointKey );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + autoSetpointKey + "> was not found an ListPreference! abort!" );
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
    if( getPreferenceScreen().findPreference( highSetpointKey ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( highSetpointKey );
      if( lP == null )
      {
        Log.e( TAG, "msgReciveAutosetpoint: Key <" + highSetpointKey + "> was not found an ListPreference! abort!" );
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
   * 
   * Sende DECO Preferenzen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.07.2013
   */
  private void sendDecoPrefs()
  {
    String decoGradient = "keyDecoGradient";
    String decoLastStop = "keyDecoLastStop";
    String decoDynGradients = "keyDecoDynGradients";
    String decoDeepStops = "keyDecoDeepStops";
    int lowG, highG, deepStops, dynGr, lastStop;
    //
    //
    if( BuildConfig.DEBUG ) Log.d( TAG, "sendDecoPrefs()..." );
    //
    // Low/High Gradient erfragen
    //
    if( getPreferenceScreen().findPreference( decoGradient ) instanceof GradientPickerPreference )
    {
      GradientPickerPreference dgp = ( GradientPickerPreference )getPreferenceScreen().findPreference( decoGradient );
      if( dgp == null )
      {
        Log.e( TAG, "msgReciveDeco: Key <" + decoGradient + "> was not found an GradientPickerPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für Gradienten übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "get Gradients value from preference..." );
      int[] val = dgp.getValue();
      lowG = val[0];
      highG = val[1];
    }
    else
    {
      Log.e( TAG, "can't set gradient value to preference..." );
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
        Log.e( TAG, "msgReciveDeco: Key <" + decoLastStop + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "get deco last stop value from preference..." );
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
      Log.e( TAG, "can't set decoLastStop value to preference..." );
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
        Log.e( TAG, "msgReciveDeco: Key <" + decoDynGradients + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "get dynGradients value from preference..." );
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
      Log.e( TAG, "can't set dynGradients value to preference..." );
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
        Log.e( TAG, "msgReciveDeco: Key <" + decoDeepStops + "> was not found an SwitchPreference! abort!" );
        return;
      }
      //
      // jetzt die Werte für LastStop übernehmen
      //
      if( BuildConfig.DEBUG ) Log.d( TAG, "get deepStops value from preference..." );
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
      Log.e( TAG, "can't set dynGradients value to preference..." );
      return;
    }
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = true;
    if( BuildConfig.DEBUG ) Log.d( TAG, "write deco prefs via runningActivity..." );
    fActivity.writeDecoPrefs( lowG, highG, deepStops, dynGr, lastStop );
  }

  /**
   * 
   * Sende Display Einstellungen zum SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   */
  private void sendDisplayPrefs()
  {
    String displayLuminance = "keyDisplayLuminance";
    String displayOrientation = "keyDisplayOrientation";
    ListPreference lP = null;
  }

  /**
   * Setze alle Summarys auf ihren aktuellen Wert (wi das die Activity nichzt selber macht) Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 31.12.2012
   */
  private void setAllSummarys()
  {
    ListPreference lP = null;
    PreferenceScreen pS = getPreferenceScreen();
    Resources res = getResources();
    SharedPreferences shared = getPreferenceManager().getSharedPreferences();
    //
    // Autoset
    //
    lP = ( ListPreference )pS.findPreference( "keySetpointAutosetpointDepth" );
    lP.setSummary( String.format( res.getString( R.string.conf_autoset_summary ), lP.getEntry() ) );
    //
    // High Setpoint
    //
    lP = ( ListPreference )pS.findPreference( "keySetpointHighsetpointValue" );
    lP.setSummary( String.format( res.getString( R.string.conf_highset_summary ), lP.getEntry() ) );
    //
    // Deco gradienten Preset
    //
    lP = ( ListPreference )pS.findPreference( "keyDecoGradientPresets" );
    lP.setSummary( String.format( res.getString( R.string.conf_deco_presets_summary ), lP.getEntry() ) );
    //
    // Deco gradienten
    //
    setDecoGradientsSummary();
    //
    // Displayhelligkeit
    //
    lP = ( ListPreference )pS.findPreference( "keyDisplayLuminance" );
    lP.setSummary( String.format( res.getString( R.string.conf_luminance_header_summary ), lP.getEntry() ) );
    //
    // Display Orientierung
    //
    lP = ( ListPreference )pS.findPreference( "keyDisplayOrientation" );
    lP.setSummary( String.format( res.getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
    //
    // das nur bei Individuallizenz
    //
    if( FragmentCommonActivity.isIndividual )
    {
      //
      // Sensors Count for Warning
      //
      lP = ( ListPreference )pS.findPreference( "keyIndividualCountSensorWarning" );
      lP.setSummary( String.format( res.getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
      //
      // Logintervall
      //
      lP = ( ListPreference )pS.findPreference( "keyIndividualLoginterval" );
      lP.setSummary( String.format( res.getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
    }
  }

  /**
   * 
   * Gradienten in der Preferenz setzen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.07.2013
   * @param presetCandidateStr
   * @return
   */
  private boolean setDecoGradients( int[] presetCandidate )
  {
    String decoGradient = "keyDecoGradient";
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
   * 
   * Setze das Preset auf einen definierten Wert oder auf CUSTOM
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 14.07.2013
   * @param presetCandidate
   */
  private void setDecoGradientsPreset( int[] presetCandidate )
  {
    String decoGradientsPreset = "keyDecoGradientPresets";
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
   * 
   * zusammenstellen der Summary für Gradienten
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 13.07.2013
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
    getPreferenceScreen().findPreference( "keyDecoGradient" ).setSummary( String.format( getResources().getString( R.string.conf_deco_gradient_summary ), low, high ) );
  }
}
