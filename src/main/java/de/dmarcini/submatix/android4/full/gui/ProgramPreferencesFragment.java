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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.joda.time.format.DateTimeFormat;

import java.io.File;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.comm.BtServiceMessage;
import de.dmarcini.submatix.android4.full.dialogs.DatabaseFileDialog;
import de.dmarcini.submatix.android4.full.interfaces.IBtServiceListener;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * Klasse zur Bearbeitung der Programmeinstellungen
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 10.11.2013
 */
public class ProgramPreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceClickListener, IBtServiceListener
{
  private static final String            TAG           = ProgramPreferencesFragment.class.getSimpleName();
  private static final String            DATA_DIR_KEY  = "keyProgDataDirectory";
  private              SharedPreferences sPref         = null;
  private              String            fragmentTitle = "unknown";

  @Override
  public void handleMessages(int what, BtServiceMessage smsg)
  {
    // was war denn los? Welche Nachricht kam rein?
    switch( what )
    {
      //
      // ################################################################
      // Dialog Abgebrochen
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_NEGATIVE:
        if( smsg.getContainer() instanceof DatabaseFileDialog )
        {
          DatabaseFileDialog dl = ( DatabaseFileDialog ) smsg.getContainer();
          dl.dismiss();
        }
        break;
      // ################################################################
      // Dialog Abgebrochen
      // ################################################################
      case ProjectConst.MESSAGE_DIALOG_POSITIVE:
        if( smsg.getContainer() instanceof DatabaseFileDialog )
        {
          //
          // Alle Aktionen in MainActivity abgearbeitet, jetzt noch Zusätzlich
          // die Summary machen
          //
          Preference       pP;
          PreferenceScreen pS = getPreferenceScreen();
          // auf dem Screen die Voreinstellung finden
          pP = pS.findPreference(DATA_DIR_KEY);
          // Die Summary schreiben
          pP.setSummary(String.format(getResources().getString(R.string.conf_prog_datadir_summary), MainActivity.databaseDir.getAbsolutePath()));
        }
        break;
    }
  }

  @Override
  public void msgConnected(BtServiceMessage msg)
  {
  }

  @Override
  public void msgConnectError(BtServiceMessage msg)
  {
  }

  @Override
  public void msgConnecting(BtServiceMessage msg)
  {
  }

  @Override
  public void msgDisconnected(BtServiceMessage msg)
  {
  }

  @Override
  public void msgRecivedAlive(BtServiceMessage msg)
  {
  }

  @Override
  public void msgRecivedTick(BtServiceMessage msg)
  {
  }

  @Override
  public void msgReciveWriteTmeout(BtServiceMessage msg)
  {
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    //
    // den Titel in der Actionbar setzten
    // Aufruf via create
    //
    Bundle arguments = getArguments();
    if( arguments != null && arguments.containsKey(ProjectConst.ARG_ITEM_CONTENT) )
    {
      fragmentTitle = arguments.getString(ProjectConst.ARG_ITEM_CONTENT);
      (( MainActivity ) getActivity()).onSectionAttached(fragmentTitle);
    }
    else
    {
      Log.w(TAG, "onActivityCreated: TITLE NOT SET!");
    }
    //
    // im Falle eines restaurierten Frames
    //
    if( savedInstanceState != null && savedInstanceState.containsKey(ProjectConst.ARG_ITEM_CONTENT) )
    {
      fragmentTitle = savedInstanceState.getString(ProjectConst.ARG_ITEM_CONTENT);
      (( MainActivity ) getActivity()).onSectionAttached(fragmentTitle);
    }
    //
    // Callback, wenn das Verzeichnis angeklickt werden soll
    //
    Preference pref = getPreferenceScreen().findPreference(DATA_DIR_KEY);
    if( pref != null )
    {
      pref.setOnPreferenceClickListener(this);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreate()...");
      Log.v(TAG, "onCreate: add Resouce id <" + R.xml.config_program_preference + ">...");
    }
    addPreferencesFromResource(R.xml.config_program_preference);
    //
    // initiiere die notwendigen summarys
    //
    setAllSummarys();
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    sPref = getPreferenceManager().getSharedPreferences();
    sPref.registerOnSharedPreferenceChangeListener(this);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreate: add Resouce...OK");
    }
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    //
    // den Change-Listener abbestellen ;-)
    //
    sPref.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch( item.getItemId() )
    {
      case android.R.id.home:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "onOptionsItemSelected: HOME");
        }
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onPause...");
    }
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onPause: clear service listener for preferences fragment...");
    }
    (( MainActivity ) getActivity()).removeServiceListener(this);
  }

  /**
   * Wenn eine Preference angeklickt wird
   * <p/>
   * Project: SubmatixBTAndroid4 Package: de.dmarcini.submatix.android4.full.gui
   * <p/>
   * Stand: 07.12.2014
   *
   * @param preference
   * @return true, wenn der Klick bearbeitet wurde
   */
  @Override
  public boolean onPreferenceClick(Preference preference)
  {
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onPreferenceClick: Klicked");
    }
    //
    // ist das die Datenverzeichnis-Preferenz?
    //
    if( preference.getKey().equals(DATA_DIR_KEY) )
    {
      DatabaseFileDialog dl = new DatabaseFileDialog(new File(sPref.getString(DATA_DIR_KEY, Environment.getExternalStorageDirectory().getAbsolutePath())));
      dl.show(getFragmentManager(), "set_database_directory_pref");
      return (true);
    }
    return (false);
  }

  @Override
  public void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onResume()");
    }
    PreferenceScreen ps = getPreferenceScreen();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "this preferencescreen has <" + ps.getPreferenceCount() + "> preferenes.");
    }
    (( MainActivity ) getActivity()).addServiceListener(this);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
    super.onSaveInstanceState(savedInstanceState);
    fragmentTitle = savedInstanceState.getString(ProjectConst.ARG_ITEM_CONTENT);
    savedInstanceState.putString(ProjectConst.ARG_ITEM_CONTENT, fragmentTitle);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
  {
    ListPreference     lP   = null;
    EditTextPreference tP   = null;
    Preference         pref = null;
    Resources          res  = getResources();
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onSharedPreferenceChanged()....");
      Log.d(TAG, "onSharedPreferenceChanged: key = <" + key + ">");
    }
    //
    // zuerst mal die ListPreferenzen abklappern
    //
    if( getPreferenceScreen().findPreference(key) instanceof ListPreference )
    {
      lP = ( ListPreference ) getPreferenceScreen().findPreference(key);
      if( lP == null )
      {
        Log.e(TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an ListPreference! abort!");
        return;
      }
      //
      // wen das globale Zeitformat geändert wurde
      //
      if( key.equals("keyProgUnitsTimeFormat") )
      {
        MainActivity.localTimeFormatter = DateTimeFormat.forPattern(lP.getValue());
        lP.setSummary(String.format(res.getString(R.string.conf_prog_temp_units_summary), lP.getEntry()));
      }
      //
      // was da so ist
      //
    }
    //
    // die EditTextPreferences abarbeiten...
    //
    else if( getPreferenceScreen().findPreference(key) instanceof EditTextPreference )
    {
      tP = ( EditTextPreference ) getPreferenceScreen().findPreference(key);
      if( tP == null )
      {
        Log.e(TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an EditTextPreference! abort!");
        return;
      }
      //
      // Hauptmailadresse
      //
      if( key.equals("keyProgMailMain") )
      {
        tP.setSummary(String.format(res.getString(R.string.conf_prog_mail_main_summary), tP.getText()));
      }
      //
      // kopiemailadresse
      //
      if( key.equals("keyProgMail2nd") )
      {
        tP.setSummary(String.format(res.getString(R.string.conf_prog_mail_main_summary), tP.getText()));
      }
    }
    else
    {
      pref = getPreferenceScreen().findPreference(key);
      if( pref == null )
      {
        Log.e(TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an Preference! abort!");
        return;
      }
      if( key.equals("keyProgOthersThemeIsDark") )
      {
        if( getActivity() instanceof MainActivity )
        {
          setThemeForApp();
        }
      }
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    PreferenceScreen ps = getPreferenceScreen();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onViewCreated...");
      Log.v(TAG, "this preferencescreen has <" + ps.getPreferenceCount() + "> preferenes.");
    }

    for( int groupIdx = 0; groupIdx < ps.getPreferenceCount(); groupIdx++ )
    {
      PreferenceGroup pg = ( PreferenceGroup ) ps.getPreference(groupIdx);
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, String.format("The Group <%s> has %d preferences", pg.getTitle(), pg.getPreferenceCount()));
      }
      for( int prefIdx = 0; prefIdx < pg.getPreferenceCount(); prefIdx++ )
      {
        Preference pref = pg.getPreference(prefIdx);
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format("The Preference <%s> is number %d", pref.getTitle(), prefIdx));
        }
        // jede ungerade Zeile färben
        if( prefIdx % 2 > 0 )
        {
          if( MainActivity.getAppStyle() == R.style.AppDarkTheme )
          {
            // dunkles Thema
            pref.setLayoutResource(R.layout.preference_dark);
          }
          else
          {
            // helles Thema
            pref.setLayoutResource(R.layout.preference_light);
          }
        }
        else
        {
          pref.setLayoutResource(R.layout.preference);
        }
      }
    }
  }

  /**
   * Setze alle summarys
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 01.01.2013
   */
  private void setAllSummarys()
  {
    ListPreference     lP   = null;
    EditTextPreference tP   = null;
    Preference         pP   = null;
    String             temp = null;
    PreferenceScreen   pS   = getPreferenceScreen();
    Resources          res  = getResources();
    //
    //
    // Zeitformat
    //
    sPref = getPreferenceManager().getSharedPreferences();
    lP = ( ListPreference ) pS.findPreference("keyProgUnitsTimeFormat");
    lP.setSummary(String.format(res.getString(R.string.conf_prog_temp_units_summary), lP.getEntry()));
    //
    // Datenverzeichnis
    // zunächst die Voreinstellungen finden
    //
    if( sPref.contains(DATA_DIR_KEY) )
    {
      // auf dem Screen die Voreinstellung finden
      pP = pS.findPreference(DATA_DIR_KEY);
      // Die Voreinstellung lesen
      temp = sPref.getString(DATA_DIR_KEY, "/");
      if( (temp != null) && (!temp.isEmpty()) )
      {
        pP.setSummary(String.format(res.getString(R.string.conf_prog_datadir_summary), temp));
      }
      else
      {
        pP.setSummary(String.format(res.getString(R.string.conf_prog_datadir_summary), "."));
      }
    }
    //
    // Haupt Mailadresse
    //
    tP = ( EditTextPreference ) pS.findPreference("keyProgMailMain");
    temp = tP.getText();
    if( (temp != null) && (!temp.isEmpty()) )
    {
      tP.setSummary(String.format(res.getString(R.string.conf_prog_mail_main_summary), temp));
    }
    else
    {
      tP.setSummary(String.format(res.getString(R.string.conf_prog_mail_main_summary), "."));
    }
    //
    // Kopie Mailadresse
    //
    tP = ( EditTextPreference ) pS.findPreference("keyProgMail2nd");
    temp = tP.getText();
    if( (temp != null) && (!temp.isEmpty()) )
    {
      tP.setSummary(String.format(res.getString(R.string.conf_prog_mail_main_summary), temp));
    }
    else
    {
      tP.setSummary(String.format(res.getString(R.string.conf_prog_mail_main_summary), "."));
    }
  }

  /**
   * Starte die App neu mit dem neuen Theme und voreingestellt die Preferencen
   * <p/>
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * <p/>
   * <p/>
   * Stand: 07.01.2013
   */
  private void setThemeForApp()
  {
    //
    // Bescheid geben füer Restart
    //
    MainActivity.wasRestartForNewTheme = true;
    Log.i(TAG, "setThemeForApp: activity recreate...");
    getActivity().recreate();
  }
}
