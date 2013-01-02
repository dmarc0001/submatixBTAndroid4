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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.utils.GasPickerPreference;

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
  private static final String TAG            = SPX42GaslistPreferencesFragment.class.getSimpleName();
  private static final String gasKeyTemplate = "keyGaslistGas%02d";
  private boolean             isTrimix       = false;

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
    super();
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
    GasPickerPreference gP = null;
    String gasProperty, gasName;
    String[] fields;
    int o2, he;
    boolean d1 = false, d2 = false, bo = false;
    //
    Log.v( TAG, "onSharedPreferenceChanged()...." );
    Log.d( TAG, "onSharedPreferenceChanged: key = <" + key + ">" );
    //
    // Wenn das von der GasPickergeschichte kommt
    //
    if( getPreferenceScreen().findPreference( key ) instanceof GasPickerPreference )
    {
      gP = ( GasPickerPreference )getPreferenceScreen().findPreference( key );
      if( gP == null )
      {
        Log.e( TAG, "onSharedPreferenceChanged: Key <" + key + "> was not found an GradientPickerPreference! Abort!" );
        return;
      }
      if( !sharedPreferences.contains( key ) )
      {
        Log.e( TAG, "onSharedPreferenceChanged: for Key <" + key + "> was not found an preference value! Abort!" );
        return;
      }
      //
      // erst mal auf alle Fälle den String laden und aufarbeiten
      //
      gasProperty = sharedPreferences.getString( key, getResources().getString( R.string.conf_gaslist_default ) );
      fields = gasProperty.split( ":" );
      if( fields.length < 3 )
      {
        Log.e( TAG, "onSharedPreferenceChanged: for Key <" + key + "> the preference value was not correct (" + gasProperty + ") ! Abort!" );
        return;
      }
      //
      // konvertiere die Parameter nach Int zur weiteren Verwendung
      //
      try
      {
        o2 = Integer.parseInt( fields[0] );
        he = Integer.parseInt( fields[1] );
        if( fields.length >= 6 )
        {
          d1 = Boolean.parseBoolean( fields[3] );
          d2 = Boolean.parseBoolean( fields[4] );
          bo = Boolean.parseBoolean( fields[5] );
        }
        gasName = getNameForGas( o2, he );
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAG, String.format( "onSharedPreferenceChanged: for key <%s> raised an NumberFormatException (%s)", key, ex.getLocalizedMessage() ) );
        return;
      }
      catch( Exception ex )
      {
        Log.e( TAG, String.format( "onSharedPreferenceChanged: for key <%s> raised an Exception (%s)", key, ex.getLocalizedMessage() ) );
        return;
      }
      for( int idx = 1; idx < 9; idx++ )
      {
        if( key.equals( String.format( gasKeyTemplate, idx ) ) )
        {
          // frag mal die resource ab
          gP.setSummary( String.format( getResources().getString( R.string.conf_gaslist_summary ), idx, gasName, d1 ? "X" : " ", d2 ? "X" : " ", bo ? "X" : " " ) );
          break;
        }
      }
    }
    Log.v( TAG, "onSharedPreferenceChanged()....OK" );
  }

  /**
   * 
   * Gib den Namen des Gaases zurück
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 02.01.2013
   * @param o2
   * @param he
   * @return Gasname
   * 
   */
  @SuppressLint( "DefaultLocale" )
  public static String getNameForGas( final int o2, final int he )
  {
    //
    // Wieviel Stickstoff?
    //
    int n2 = 100 - o2 - he;
    //
    // Mal sondieren
    //
    if( n2 == 0 )
    {
      //
      // heliox oder O2
      //
      if( o2 == 100 )
      {
        return( "O2" );
      }
      // Es gibt Helium und O2.... == Heliox
      return( String.format( "HX%d/%d", o2, he ) );
    }
    if( he == 0 )
    {
      // eindeutig Nitrox
      if( o2 == 21 )
      {
        return( "AIR" );
      }
      return( String.format( "NX%02d", o2 ) );
    }
    else
    {
      // das ist dan wohl Trimix/Triox
      return( String.format( "TX%d/%d", o2, he ) );
    }
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
  @SuppressLint( "DefaultLocale" )
  private void setAllSummarys()
  {
    Resources res = getResources();
    SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
    //
    // alle Gase generisch durch (8 Gase sind im SPX42)
    //
    for( int idx = 1; idx < 9; idx++ )
    {
      String key = String.format( gasKeyTemplate, idx );
      GasPickerPreference gP = ( GasPickerPreference )getPreferenceScreen().findPreference( key );
      int o2, he;
      boolean d1 = false, d2 = false, bo = false;
      String gasName;
      if( gP == null )
      {
        Log.e( TAG, "setAllSummarys: Key <" + key + "> was not found an GradientPickerPreference! Abort!" );
        continue;
      }
      if( !sharedPreferences.contains( key ) )
      {
        Log.e( TAG, "setAllSummarys: for Key <" + key + "> was not found an preference value! Abort!" );
        gP.setSummary( String.format( res.getString( R.string.conf_gaslist_summary ), idx, res.getString( R.string.conf_gaslist_noname ), " ", " ", " " ) );
        continue;
      }
      //
      // erst mal auf alle Fälle den String laden und aufarbeiten
      //
      String gasProperty = sharedPreferences.getString( key, getResources().getString( R.string.conf_gaslist_default ) );
      String[] fields = gasProperty.split( ":" );
      if( fields.length < 3 )
      {
        Log.e( TAG, "setAllSummarys: for Key <" + key + "> the preference value was not correct (" + gasProperty + ") ! Abort!" );
        continue;
      }
      //
      // konvertiere die Parameter nach Int zur weiteren Verwendung
      //
      try
      {
        o2 = Integer.parseInt( fields[0] );
        he = Integer.parseInt( fields[1] );
        if( fields.length >= 6 )
        {
          d1 = Boolean.parseBoolean( fields[3] );
          d2 = Boolean.parseBoolean( fields[4] );
          bo = Boolean.parseBoolean( fields[5] );
        }
        gasName = getNameForGas( o2, he );
      }
      catch( NumberFormatException ex )
      {
        Log.e( TAG, String.format( "setAllSummarys: for key <%s> raised an NumberFormatException (%s)", key, ex.getLocalizedMessage() ) );
        continue;
      }
      catch( Exception ex )
      {
        Log.e( TAG, String.format( "setAllSummarys: for key <%s> raised an Exception (%s)", key, ex.getLocalizedMessage() ) );
        continue;
      }
      // schreib schön!
      gP.setSummary( String.format( res.getString( R.string.conf_gaslist_summary ), idx, gasName, d1 ? "X" : " ", d2 ? "X" : " ", bo ? "X" : " " ) );
    }
  }
}
