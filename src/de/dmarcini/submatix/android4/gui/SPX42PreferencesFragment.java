package de.dmarcini.submatix.android4.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.R;

/**
 * 
 * Ein Objekt zum bearbeiten der SPX42 Einstellungen
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 31.12.2012 TODO Abhängigkeit bei Gradienten zwischen Voreinstellungen/custom und Presets berücksichtigen
 */
public class SPX42PreferencesFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnSharedPreferenceChangeListener
{
  private static final String TAG = SPX42PreferencesFragment.class.getSimpleName();

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_preference + ">..." );
    addPreferencesFromResource( R.xml.config_spx42_preference );
    //
    // initiiere die notwendigen summarys
    //
    setAllSummarys();
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
    Log.v( TAG, "onCreate: add Resouce...OK" );
  }

  /**
   * 
   * Setze alle Summarys auf ihren aktuellen Wert (wi das die Activity nichzt selber macht)
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 31.12.2012 TODO
   */
  private void setAllSummarys()
  {
    ListPreference lP = null;
    PreferenceScreen pS = getPreferenceScreen();
    SharedPreferences shared = getPreferenceManager().getSharedPreferences();
    //
    // Autoset
    //
    pS.findPreference( "keySetpointAutosetpointDepth" ).setSummary(
            String.format( getResources().getString( R.string.conf_autoset_summary ),
                    shared.getString( "keySetpointAutosetpointDepth", getResources().getString( R.string.conf_autoset_default ) ) ) );
    //
    // High Setpoint
    //
    pS.findPreference( "keySetpointHighsetpointValue" ).setSummary(
            String.format( getResources().getString( R.string.conf_highset_summary ),
                    shared.getString( "keySetpointHighsetpointValue", getResources().getString( R.string.conf_highset_default ) ) ) );
    //
    // Deco gradienten Preset
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( "keyDecoGradientPresets" );
    lP.setSummary( String.format( getResources().getString( R.string.conf_deco_presets_summary ), lP.getEntry() ) );
    //
    // Deco gradienten LOW
    //
    pS.findPreference( "keyDecoGradientLow" ).setSummary(
            String.format( getResources().getString( R.string.conf_deco_gradient_low_summary ),
                    shared.getInt( "keyDecoGradientLow", getIntegerFromStringResource( R.string.conf_deco_gradient_low_default ) ) ) );
    //
    // Deco gradienten High
    //
    pS.findPreference( "keyDecoGradientHigh" ).setSummary(
            String.format( getResources().getString( R.string.conf_deco_gradient_high_summary ),
                    shared.getInt( "keyDecoGradientHigh", getIntegerFromStringResource( R.string.conf_deco_gradient_high_default ) ) ) );
    //
    // Displayhelligkeit
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( "keyDisplayLuminance" );
    lP.setSummary( String.format( getResources().getString( R.string.conf_luminance_header_summary ), lP.getEntry() ) );
    //
    // Display Orientierung
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( "keyDisplayOrientation" );
    lP.setSummary( String.format( getResources().getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
    //
    // Sensors Count for Warning
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( "keyIndividualCountSensorWarning" );
    lP.setSummary( String.format( getResources().getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
    //
    // Logintervall
    //
    lP = ( ListPreference )getPreferenceScreen().findPreference( "keyIndividualLoginterval" );
    lP.setSummary( String.format( getResources().getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
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
    Log.v( TAG, "onPause..." );
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume..." );
  }

  @Override
  public boolean onPreferenceChange( Preference preference, Object newValue )
  {
    Log.v( TAG, "onPreferenceChange()..." );
    return false;
  }

  @Override
  public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
  {
    ListPreference lP = null;
    Log.v( TAG, "onSharedPreferenceChanged()...." );
    Log.d( TAG, "onSharedPreferenceChanged: key = <" + key + ">" );
    //
    // Autosetpoint (off/tiefe
    //
    if( key.equals( "keySetpointAutosetpointDepth" ) )
    {
      getPreferenceScreen().findPreference( key ).setSummary(
              String.format( getResources().getString( R.string.conf_autoset_summary ),
                      sharedPreferences.getString( key, getResources().getString( R.string.conf_autoset_default ) ) ) );
    }
    //
    // Highsetpoint (wenn on)
    //
    else if( key.equals( "keySetpointHighsetpointValue" ) )
    {
      getPreferenceScreen().findPreference( key ).setSummary(
              String.format( getResources().getString( R.string.conf_highset_summary ),
                      sharedPreferences.getString( key, getResources().getString( R.string.conf_highset_default ) ) ) );
    }
    //
    // DECO-Preset, wenn ON
    //
    else if( key.equals( "keyDecoGradientPresets" ) )
    {
      if( getPreferenceScreen().findPreference( key ) instanceof ListPreference )
      {
        lP = ( ListPreference )getPreferenceScreen().findPreference( key );
        lP.setSummary( String.format( getResources().getString( R.string.conf_deco_presets_summary ), lP.getEntry() ) );
      }
    }
    //
    // DECO-Gradient LOW
    //
    else if( key.equals( "keyDecoGradientLow" ) )
    {
      getPreferenceScreen().findPreference( key ).setSummary(
              String.format( getResources().getString( R.string.conf_deco_gradient_low_summary ),
                      sharedPreferences.getInt( key, getIntegerFromStringResource( R.string.conf_deco_gradient_low_default ) ) ) );
    }
    //
    // DECO-Gradient HIGH
    //
    else if( key.equals( "keyDecoGradientHigh" ) )
    {
      getPreferenceScreen().findPreference( key ).setSummary(
              String.format( getResources().getString( R.string.conf_deco_gradient_high_summary ),
                      sharedPreferences.getInt( key, getIntegerFromStringResource( R.string.conf_deco_gradient_high_default ) ) ) );
    }
    //
    // Helligkeit Display
    //
    else if( key.equals( "keyDisplayLuminance" ) )
    {
      if( getPreferenceScreen().findPreference( key ) instanceof ListPreference )
      {
        lP = ( ListPreference )getPreferenceScreen().findPreference( key );
        lP.setSummary( String.format( getResources().getString( R.string.conf_luminance_header_summary ), lP.getEntry() ) );
      }
    }
    //
    // Orientierung Display
    //
    else if( key.equals( "keyDisplayOrientation" ) )
    {
      if( getPreferenceScreen().findPreference( key ) instanceof ListPreference )
      {
        lP = ( ListPreference )getPreferenceScreen().findPreference( key );
        lP.setSummary( String.format( getResources().getString( R.string.conf_display_orientation_header_summary ), lP.getEntry() ) );
      }
    }
    //
    // Sensors Count Warning
    //
    else if( key.equals( "keyIndividualCountSensorWarning" ) )
    {
      if( getPreferenceScreen().findPreference( key ) instanceof ListPreference )
      {
        lP = ( ListPreference )getPreferenceScreen().findPreference( key );
        lP.setSummary( String.format( getResources().getString( R.string.conf_ind_count_sensorwarning_header_summary ), lP.getEntry() ) );
      }
    }
    //
    // Intervall zwischen zwei Logeinträgen
    //
    else if( key.equals( "keyIndividualLoginterval" ) )
    {
      if( getPreferenceScreen().findPreference( key ) instanceof ListPreference )
      {
        lP = ( ListPreference )getPreferenceScreen().findPreference( key );
        lP.setSummary( String.format( getResources().getString( R.string.conf_ind_interval_header_summary ), lP.getEntry() ) );
      }
    }
  }

  /**
   * 
   * Den Integerwert oder Null zurückgeben für eine Stringresource
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 31.12.2012
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
    finally
    {
      return( retVal );
    }
  }
}
