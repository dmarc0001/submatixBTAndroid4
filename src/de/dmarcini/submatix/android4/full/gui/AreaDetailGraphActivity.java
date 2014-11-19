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
/**
 * Activity für grafische Datstellung eines Logs bei kleinen screens
 * 
 * Wird ausgeführt, wenn bei kleinen Schirmen (Smartphones) eine Option des Menüs ausgewählt wurde. Bei grossen Schirmen wird diese Activity nicht ausgeführt Der Gemeisame Code für
 * beide Version ist im Parentobjekt FragmentCommonActivity
 */
package de.dmarcini.submatix.android4.full.gui;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * 
 * Diese Activity kommt nur bei kleinen Screens zum Einsatz, wenn vom Menü un der araeListActivity ein Eintrag gewählt wurde
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.02.2014
 */
public class AreaDetailGraphActivity extends MainActivity
{
  private static final String TAG              = AreaDetailGraphActivity.class.getSimpleName();
  private static String       KEY_SHOWHEADLINE = "keyProgLogShowHeadline";
  private static Fragment     currFragment     = null;

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    SharedPreferences sPref = null;
    //
    if( ApplicationDEBUG.DEBUG ) Log.v( TAG, "onCreate:..." );
    //
    // bevor es losgeht, gucken, ob die headline gezeigt werden soll
    //
    sPref = PreferenceManager.getDefaultSharedPreferences( this );
    if( sPref.contains( KEY_SHOWHEADLINE ) )
    {
      //
      // wenn alle Voraussetzungen für die Grafik erfüllt sind, und FULSCREEN sein soll
      //
      if( !sPref.getBoolean( KEY_SHOWHEADLINE, true ) && getIntent().getBooleanExtra( ProjectConst.ARG_ITEM_GRAPHEXTRA, false )
              && ( getIntent().getIntExtra( ProjectConst.ARG_ITEM_DBID, -1 ) > 0 ) )
      {
        // Jepp, Überschrift verschwinden lassen
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate: hide app headline" );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
      }
      else
      {
        // Nee, soll headline ausblenden
        if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate: show app headline" );
      }
    }
    //
    super.onCreate( savedInstanceState );
    // Aktiviere Zurückfunktion via Actionbar Home, wenn Headline vorhanden
    if( getActionBar() != null )
    {
      getActionBar().setHomeButtonEnabled( true );
      getActionBar().setDisplayHomeAsUpEnabled( true );
    }
    getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
    //
    // was soll ich anzeigen?
    //
    if( getIntent().getBooleanExtra( ProjectConst.ARG_ITEM_GRAPHEXTRA, false ) && ( getIntent().getIntExtra( ProjectConst.ARG_ITEM_DBID, -1 ) > 0 ) )
    {
      //
      // Ist als EXTRA die Logid und das Flag für das EXTRA gesetzt
      // übergib die Extras gleich wieder an das neue Element
      //
      Log.i( TAG, "onCreate: start SPX42LogGraphFragment..." );
      currFragment = new SPX42LogGraphFragment();
      currFragment.setArguments( getIntent().getExtras() );
      setContentView( R.layout.fragment_log_protocol_graph );
      // Überschrift und Icon festlegen, wenn _Headline vorhanden ist
      if( getActionBar() != null )
      {
        getActionBar().setTitle( R.string.graphlog_header );
        getActionBar().setLogo( R.drawable.graphsbar_online );
      }
      getFragmentManager().beginTransaction().replace( R.id.logGraphOuterLayout, currFragment ).commit();
    }
    else
    {
      //
      // Seite zum selektieren zeigen
      //
      Log.i( TAG, "onCreate: start SPX42LogGraphSelectFragment..." );
      currFragment = ( new SPX42LogGraphSelectFragment() );
      setContentView( R.layout.fragment_log_protocol );
      if( getActionBar() != null )
      {
        getActionBar().setTitle( R.string.graphlog_header );
        getActionBar().setLogo( R.drawable.graphsbar_online );
      }
      getFragmentManager().beginTransaction().replace( R.id.logGraphOuterLayout, currFragment ).commit();
    }
  }

  @Override
  public void onPause()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onPause..." );
    super.onPause();
  }

  @Override
  public void onResume()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onResume..." );
    super.onResume();
  }

  @Override
  public void onDestroy()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onDestroy..." );
    super.onDestroy();
  }

  @Override
  public void onStop()
  {
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onStop..." );
    super.onStop();
  }
}
