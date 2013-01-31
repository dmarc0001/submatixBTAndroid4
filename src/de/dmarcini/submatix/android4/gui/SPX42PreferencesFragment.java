package de.dmarcini.submatix.android4.gui;

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
import de.dmarcini.submatix.android4.R;

/**
 * Ein Objekt zum bearbeiten der SPX42 Einstellungen Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 31.12.2012
 * 
 *         TODO Abhängigkeit bei Gradienten zwischen Voreinstellungen/custom und Presets berücksichtigen
 */
public class SPX42PreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
  private static final String TAG          = SPX42PreferencesFragment.class.getSimpleName();
  private boolean             isIndividual = false;

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
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
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
          pref.setLayoutResource( R.layout.preference_dark );
        }
      }
    }
  }

  @Override
  public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
  {
    ListPreference lP = null;
    Preference pref = null;
    Log.v( TAG, "onSharedPreferenceChanged()...." );
    Log.d( TAG, "onSharedPreferenceChanged: key = <" + key + ">" );
    //
    // zuerst mal die ListPreferenzen abklappern
    //
    if( getPreferenceScreen().findPreference( key ) instanceof ListPreference )
    {
      lP = ( ListPreference )getPreferenceScreen().findPreference( key );
      if( lP == null )
      {
        Log.e( TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an ListPreference! abort!" );
        return;
      }
      //
      // Autosetpoint (off/tiefe)
      //
      if( key.equals( "keySetpointAutosetpointDepth" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_autoset_summary ), lP.getEntry() ) );
      }
      //
      // Highsetpoint (wenn on)
      //
      else if( key.equals( "keySetpointHighsetpointValue" ) )
      {
        lP.setSummary( String.format( getResources().getString( R.string.conf_highset_summary ), lP.getEntry() ) );
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
        Log.e( TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an Preference! abort!" );
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
            Log.e( TAG, "String <" + gradientProperty + "> contains not valis values!?: " + ex.getLocalizedMessage() );
          }
          catch( Exception ex )
          {
            Log.e( TAG, "String <" + gradientProperty + "> contains not valis values!?: " + ex.getLocalizedMessage() );
          }
        }
        // die Summary Geschichte schreiben
        pref.setSummary( String.format( getResources().getString( R.string.conf_deco_gradient_summary ), low, high ) );
      }
    }
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
