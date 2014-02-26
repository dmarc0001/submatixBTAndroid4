package de.dmarcini.submatix.android4.full.gui;

import java.io.File;

import org.joda.time.format.DateTimeFormat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * 
 * Klasse zur Bearbeitung der Programmeinstellungen
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 * 
 */
public class ProgramPreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
  private static final String TAG = ProgramPreferencesFragment.class.getSimpleName();

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_program_preference + ">..." );
    addPreferencesFromResource( R.xml.config_program_preference );
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
  public void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume()" );
    PreferenceScreen ps = getPreferenceScreen();
    Log.v( TAG, "this preferencescreen has <" + ps.getPreferenceCount() + "> preferenes." );
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
  }

  @Override
  public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
  {
    ListPreference lP = null;
    EditTextPreference tP = null;
    Preference pref = null;
    Resources res = getResources();
    Log.v( TAG, "onSharedPreferenceChanged()...." );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onSharedPreferenceChanged: key = <" + key + ">" );
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
      // wen das globale Zeitformat geändert wurde
      //
      if( key.equals( "keyProgUnitsTimeFormat" ) )
      {
        FragmentCommonActivity.localTimeFormatter = DateTimeFormat.forPattern( lP.getValue() );
        lP.setSummary( String.format( res.getString( R.string.conf_prog_temp_units_summary ), lP.getEntry() ) );
      }
      //
      // was da so ist
      //
    }
    //
    // die EditTextPreferences abarbeiten...
    //
    else if( getPreferenceScreen().findPreference( key ) instanceof EditTextPreference )
    {
      tP = ( EditTextPreference )getPreferenceScreen().findPreference( key );
      if( tP == null )
      {
        Log.e( TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an EditTextPreference! abort!" );
        return;
      }
      //
      // Datenverzeichnis
      //
      if( key.equals( "keyProgDataDirectory" ) )
      {
        FragmentCommonActivity.databaseDir = new File( tP.getText() );
        if( !FragmentCommonActivity.databaseDir.exists() )
        {
          Log.i( TAG, "onCreate: create database root dir..." );
          if( !FragmentCommonActivity.databaseDir.mkdirs() ) FragmentCommonActivity.databaseDir = null;
        }
        tP.setSummary( String.format( res.getString( R.string.conf_prog_datadir_summary ), tP.getText() ) );
      }
      //
      // Hauptmailadresse
      //
      if( key.equals( "keyProgMailMain" ) )
      {
        tP.setSummary( String.format( res.getString( R.string.conf_prog_mail_main_summary ), tP.getText() ) );
      }
      //
      // kopiemailadresse
      //
      if( key.equals( "keyProgMail2nd" ) )
      {
        tP.setSummary( String.format( res.getString( R.string.conf_prog_mail_main_summary ), tP.getText() ) );
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
      if( key.equals( "keyProgOthersThemeIsDark" ) )
      {
        if( getActivity() instanceof FragmentCommonActivity )
        {
          setThemeForApp();
        }
      }
    }
  }

  /**
   * 
   * Starte die App neu mit dem neuen Theme und voreingestellt die Preferencen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * 
   * Stand: 07.01.2013
   */
  private void setThemeForApp()
  {
    if( ( ( FragmentCommonActivity )getActivity() ).istActivityTwoPane() )
    {
      //
      // Argumente für Intent zusammenbauen
      //
      Bundle arguments = new Bundle();
      arguments.putInt( ProjectConst.ARG_ITEM_ID, R.string.progitem_progpref );
      Intent parentActivityIntent = new Intent( getActivity(), AreaListActivity.class );
      parentActivityIntent.putExtras( arguments );
      parentActivityIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
      startActivity( parentActivityIntent );
      getActivity().finish();
    }
    else
    {
      Bundle arguments = new Bundle();
      arguments.putInt( ProjectConst.ARG_ITEM_ID, R.string.progitem_progpref );
      Intent parentActivityIntent = new Intent( getActivity(), AreaDetailActivity.class );
      parentActivityIntent.putExtras( arguments );
      parentActivityIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
      startActivity( parentActivityIntent );
      getActivity().finish();
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
  private void setAllSummarys()
  {
    ListPreference lP = null;
    EditTextPreference tP = null;
    String temp = null;
    PreferenceScreen pS = getPreferenceScreen();
    Resources res = getResources();
    // SharedPreferences shared = getPreferenceManager().getSharedPreferences();
    //
    //
    // Zeitformat
    //
    lP = ( ListPreference )pS.findPreference( "keyProgUnitsTimeFormat" );
    lP.setSummary( String.format( res.getString( R.string.conf_prog_temp_units_summary ), lP.getEntry() ) );
    //
    // Datenverzeichnis
    //
    tP = ( EditTextPreference )pS.findPreference( "keyProgDataDirectory" );
    temp = tP.getText();
    if( ( temp != null ) && ( !temp.isEmpty() ) )
    {
      tP.setSummary( String.format( res.getString( R.string.conf_prog_datadir_summary ), temp ) );
    }
    else
    {
      tP.setSummary( String.format( res.getString( R.string.conf_prog_datadir_summary ), "." ) );
    }
    //
    // Haupt Mailadresse
    //
    tP = ( EditTextPreference )pS.findPreference( "keyProgMailMain" );
    temp = tP.getText();
    if( ( temp != null ) && ( !temp.isEmpty() ) )
    {
      tP.setSummary( String.format( res.getString( R.string.conf_prog_mail_main_summary ), temp ) );
    }
    else
    {
      tP.setSummary( String.format( res.getString( R.string.conf_prog_mail_main_summary ), "." ) );
    }
    //
    // Kopie Mailadresse
    //
    tP = ( EditTextPreference )pS.findPreference( "keyProgMail2nd" );
    temp = tP.getText();
    if( ( temp != null ) && ( !temp.isEmpty() ) )
    {
      tP.setSummary( String.format( res.getString( R.string.conf_prog_mail_main_summary ), temp ) );
    }
    else
    {
      tP.setSummary( String.format( res.getString( R.string.conf_prog_mail_main_summary ), "." ) );
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
}
