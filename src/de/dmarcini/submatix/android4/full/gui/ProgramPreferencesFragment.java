﻿//@formatter:off
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
  public void onActivityCreated( Bundle savedInstanceState )
  {
    super.onActivityCreated( savedInstanceState );
    Bundle arguments = getArguments();
    if( arguments != null && arguments.containsKey( ProjectConst.ARG_ITEM_CONTENT ) )
    {
      ( ( MainActivity )getActivity() ).onSectionAttached( arguments.getString( ProjectConst.ARG_ITEM_CONTENT ) );
    }
    else
    {
      Log.w( TAG, "onActivityCreated: TITLE NOT SET!" );
    }
  };

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
        Intent intent = new Intent( getActivity(), MainActivity.class );
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
        MainActivity.localTimeFormatter = DateTimeFormat.forPattern( lP.getValue() );
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
        MainActivity.databaseDir = new File( tP.getText() );
        if( !MainActivity.databaseDir.exists() )
        {
          Log.i( TAG, "onCreate: create database root dir..." );
          if( !MainActivity.databaseDir.mkdirs() ) MainActivity.databaseDir = null;
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
        if( getActivity() instanceof MainActivity )
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
    //
    // Bescheid geben füer Restart
    //
    MainActivity.wasRestartForNewTheme = true;
    Log.i( TAG, "setThemeForApp: activity recreate..." );
    getActivity().recreate();
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
          if( MainActivity.getAppStyle() == R.style.AppDarkTheme )
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
