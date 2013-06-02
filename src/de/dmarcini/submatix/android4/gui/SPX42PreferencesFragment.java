package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.comm.BtServiceMessage;

/**
 * Ein Objekt zum bearbeiten der SPX42 Einstellungen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 31.12.2012
 * 
 *         TODO Abhängigkeit bei Gradienten zwischen Voreinstellungen/custom und Presets berücksichtigen
 */
public class SPX42PreferencesFragment extends PreferenceFragment implements IBtServiceListener, OnSharedPreferenceChangeListener
{
  private static final String TAG              = SPX42PreferencesFragment.class.getSimpleName();
  private boolean             isIndividual     = false;
  private Activity            runningActivity  = null;
  private String              serialNumber     = null;
  private String              firmwareVersion  = null;
  private boolean             ignorePrefChange = false;

  /**
   * Sperre den Standartkonstruktor!
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 01.01.2013
   */
  @SuppressWarnings( "unused" )
  private SPX42PreferencesFragment()
  {}

  /**
   * Bei der Konstruktion soll angegeben sein, welcher Lizenzstatus vorhanden ist Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 01.01.2013
   * @param isIndividual
   */
  public SPX42PreferencesFragment( boolean isIndividual )
  {
    super();
    this.isIndividual = isIndividual;
    if( isIndividual )
    {
      Log.i( TAG, "SPX42 preferences starts with \"individual\" license..." );
    }
    else
    {
      Log.i( TAG, "SPX42 preferences starts without \"individual\" license..." );
    }
  }

  /**
   * Den Integerwert oder Null zurückgeben für eine Stringresource Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 31.12.2012
   * @param id
   * @return
   */
  private int getIntegerFromStringResource( int id )
  {
    int retVal = 0;
    String strVal = getResources().getString( id );
    try
    {
      retVal = Integer.parseInt( strVal );
    }
    catch( NumberFormatException ex )
    {
      Log.e( TAG, "getIntegerFromStringResource(): String <" + strVal + "> is not an correct integer" );
    }
    return( retVal );
  }

  @Override
  public void msgConnected( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
    Log.v( TAG, "msgConnected()..." );
    FragmentCommonActivity fActivity = ( FragmentCommonActivity )runningActivity;
    ignorePrefChange = false;
    fActivity.askForSerialNumber();
    fActivity.askForFirmwareVersion();
    fActivity.askForConfigFromSPX42();
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
    serialNumber = null;
    firmwareVersion = null;
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
    Preference pref = null;
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
      lP.setValueIndex( autoSp );
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
      lP.setValueIndex( sP );
    }
  }

  @Override
  public void msgReciveAutosetpointAck( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Autosetpoint successful set" );
    ignorePrefChange = false;
  }

  @Override
  public void msgRecivedAlive( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgRecivedSerial( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
    serialNumber = ( String )msg.getContainer();
  }

  @Override
  public void msgRecivedTick( BtServiceMessage msg )
  {
    // TODO Automatisch generierter Methodenstub
  }

  @Override
  public void msgReciveFirmwareversion( BtServiceMessage msg )
  {
    firmwareVersion = ( String )msg.getContainer();
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Firmware <" + firmwareVersion + "> recived" );
  }

  @Override
  public void msgReciveManufacturer( BtServiceMessage msg )
  {
    if( BuildConfig.DEBUG ) Log.d( TAG, "SPX Manufacturer <" + firmwareVersion + "> recived" );
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
    if( isIndividual )
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
    ( ( FragmentCommonActivity )runningActivity ).clearServiceListener();
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
    fActivity.setServiceListener( this );
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
      }
      //
      // Helligkeit Display
      //
      else if( key.equals( "keyDisplayLuminance" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_luminance_header_summary ), lP.getEntry() ) );
      }
      //
      // Orientierung Display
      //
      else if( key.equals( "keyDisplayOrientation" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
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
      // DECO-Gradient
      //
      if( key.equals( "keyDecoGradient" ) )
      {
        // frag mal die resource ab
        String gradientProperty = sharedPreferences.getString( key, getResources().getString( R.string.conf_deco_gradient_default ) );
        String[] fields = gradientProperty.split( ":" );
        int low = 0;
        int high = 0;
        if( fields.length >= 2 )
        {
          // wernnes zwei Felder gibt die Werte lesen, sonst bleibt es bei 0
          try
          {
            low = Integer.parseInt( fields[0] );
            high = Integer.parseInt( fields[1] );
          }
          catch( NumberFormatException ex )
          {
            Log.e( TAG, "onSharedPreferenceChanged(): String <" + gradientProperty + "> contains not valis values!?: " + ex.getLocalizedMessage() );
          }
          catch( Exception ex )
          {
            Log.e( TAG, "onSharedPreferenceChanged(): String <" + gradientProperty + "> contains not valis values!?: " + ex.getLocalizedMessage() );
          }
        }
        // die Summary Geschichte schreiben
        pref.setSummary( String.format( getResources().getString( R.string.conf_deco_gradient_summary ), low, high ) );
      }
    }
  }

  @Override
  public void onViewCreated( View view, Bundle savedInstanceState )
  {
    super.onViewCreated( view, savedInstanceState );
    Log.v( TAG, "onViewCreated..." );
    PreferenceScreen ps = getPreferenceScreen();
    Log.v( TAG, "this preferencescreen has <" + ps.getPreferenceCount() + "> preferenes." );
    for( int groupIdx = 0; groupIdx < ps.getPreferenceCount(); groupIdx++ )
    {
      PreferenceGroup pg = ( PreferenceGroup )ps.getPreference( groupIdx );
      Log.v( TAG, String.format( "The Group <%s> has %d preferences", pg.getTitle(), pg.getPreferenceCount() ) );
      for( int prefIdx = 0; prefIdx < pg.getPreferenceCount(); prefIdx++ )
      {
        Preference pref = pg.getPreference( prefIdx );
        Log.v( TAG, String.format( "The Preference <%s> is number %d", pref.getTitle(), prefIdx ) );
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
    Preference pref = null;
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
    // frag mal die resource ab
    String gradientProperty = shared.getString( "keyDecoGradient", getResources().getString( R.string.conf_deco_gradient_default ) );
    String[] fields = gradientProperty.split( ":" );
    int low = 0;
    int high = 0;
    if( fields.length >= 2 )
    {
      // wernnes zwei Felder gibt die Werte lesen, sonst bleibt es bei 0
      try
      {
        low = Integer.parseInt( fields[0] );
        high = Integer.parseInt( fields[1] );
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAG, "String <" + gradientProperty + "> contains not valis values!?: " + ex.getLocalizedMessage() );
      }
      catch( Exception ex )
      {
        Log.e( TAG, "String <" + gradientProperty + "> contains not valis values!?: " + ex.getLocalizedMessage() );
      }
    }
    // die Summary Geschichte schreiben
    pS.findPreference( "keyDecoGradient" ).setSummary( String.format( res.getString( R.string.conf_deco_gradient_summary ), low, high ) );
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
    if( isIndividual )
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
}
