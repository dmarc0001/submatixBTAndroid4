package de.dmarcini.submatix.android4.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * 
 * Klasse zur Bearbeitung der Programmeinstellungen
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.01.2013
 */
public class ProgramPreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
  private static final String TAG = ProgramPreferencesFragment.class.getSimpleName();

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_preference_individual + ">..." );
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
  public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
  {
    ListPreference lP = null;
    EditTextPreference tP = null;
    Preference pref = null;
    Resources res = getResources();
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
      // was da so ist
      //
    }
    //
    // die EditTRextPreferences abarbeiten...
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
        setThemeForApp();
      }
    }
  }

  /**
   * 
   * Starte die App neu mit dem neuen Theme und voreingestellt die Preferencen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 07.01.2013
   */
  private void setThemeForApp()
  {
    //
    // Argumente für Intent zusammenbauen
    //
    Bundle arguments = new Bundle();
    arguments.putString( ProjectConst.ARG_ITEM_ID, getResources().getString( R.string.progitem_progpref ) );
    Intent parentActivityIntent = new Intent( getActivity(), areaListActivity.class );
    parentActivityIntent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
    getActivity().finish();
    startActivity( parentActivityIntent );
  }

  /**
   * 
   * Setze alle summarys
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   */
  private void setAllSummarys()
  {
    // ListPreference lP = null;
    EditTextPreference tP = null;
    String temp = null;
    PreferenceScreen pS = getPreferenceScreen();
    Resources res = getResources();
    // SharedPreferences shared = getPreferenceManager().getSharedPreferences();
    //
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
}
