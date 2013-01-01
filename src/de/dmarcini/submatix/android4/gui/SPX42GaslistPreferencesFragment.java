/**
 * Gaslisten im SPX42 editieren
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.01.2013
 */
package de.dmarcini.submatix.android4.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.R;

/**
 * Editor für die Gaslisten
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 01.01.2013 TODO
 */
public class SPX42GaslistPreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
  private static final String TAG      = SPX42GaslistPreferencesFragment.class.getSimpleName();
  private boolean             isTrimix = false;

  /**
   * 
   * Gesperrter Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   */
  @SuppressWarnings( "unused" )
  private SPX42GaslistPreferencesFragment()
  {};

  /**
   * 
   * Konstruktor mit Lizenzinfo für Trimix
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 01.01.2013
   * @param isTrimix
   */
  public SPX42GaslistPreferencesFragment( boolean isTrimix )
  {
    this.isTrimix = isTrimix;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_gaslist_preference + ">..." );
    addPreferencesFromResource( R.xml.config_spx42_gaslist_preference );
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
    Log.v( TAG, "onSharedPreferenceChanged()...." );
    Log.d( TAG, "onSharedPreferenceChanged: key = <" + key + ">" );
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
    PreferenceScreen pS = getPreferenceScreen();
    Resources res = getResources();
    // SharedPreferences shared = getPreferenceManager().getSharedPreferences();
  }
}
