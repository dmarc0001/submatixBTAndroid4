/**
 * Hauptaktivity des Projektes
 * 
 * Wird als erstes ausgeführt. Bei kleinen Schirmen wird bei Auswahl einer Option an die detailActivity übergeben (eine Liste der Optionen angezeigt), bei großen Schirmen wird
 * direkt in dieser Activity der Detailschirm aufgebaut und bedient. Der Gemeisame Code für beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.content.ContentSwitcher.ProgItem;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * Activity mit welcher die App startet. Bei Tablett die Main-Activity, bei kleinen screens die Menü-Activity
 * 
 * @author dmarc
 */
public class areaListActivity extends FragmentCommonActivity
{
  private static final String TAG = areaListActivity.class.getSimpleName();

  /**
   * Erzeuge die Menüeinträge Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 17.12.2012
   */
  private void initStaticContenSwitcher()
  {
    Log.v( TAG, "initStaticContent..." );
    // zuerst aufräumen
    ContentSwitcher.clearItems();
    //
    // irgendeine Kennung muss der String bekommen, also gibts halt die String-ID
    //
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_connect, R.drawable.bluetooth_icon_color, getString( R.string.progitem_connect ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_config, R.drawable.toolboxwhite, getString( R.string.progitem_config ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_gaslist, R.drawable.pinion, getString( R.string.progitem_gaslist ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_logging, R.drawable.logging, getString( R.string.progitem_logging ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_progpref, R.drawable.toolboxred, getString( R.string.progitem_progpref ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_exit, R.drawable.shutoff, getString( R.string.progitem_exit ) ) );
    if( BuildConfig.DEBUG )
    {
      ContentSwitcher.addItem( new ProgItem( R.string.progitem_null, R.drawable.placeholder, getString( R.string.progitem_null ) ) );
      ContentSwitcher.addItem( new ProgItem( R.string.progitem_set_defaults, R.drawable.radiation, getString( R.string.progitem_set_defaults ) ) );
      ContentSwitcher.addItem( new ProgItem( R.string.progitem_log_propertys, R.drawable.radiation, getString( R.string.progitem_log_propertys ) ) );
    }
  }

  /**
   * Wenn die Activity erzeugt wird, u.A. herausfinden ob ein- oder zwei-Flächen Mode
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   * @author Dirk Marciniak 28.12.2012
   * @param savedInstanceState
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate..." );
    //
    // wurden jemals Preferences gesetzt?
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( this );
    if( !sPref.contains( "firstTimeInitiated" ) )
    {
      Log.w( TAG, "onCreate: make first time preferences..." );
      if( !sPref.getBoolean( "firstTimeInitiated", false ) )
      {
        setDefaultPreferences();
      }
    }
    //
    // sind die Preferenzen in der richtigen version?
    //
    if( sPref.contains( "PREF_VERSION" ) )
    {
      if( ProjectConst.PREF_VERSION != sPref.getInt( "PREF_VERSION", 0 ) )
      {
        setDefaultPreferences();
      }
    }
    else
    {
      setDefaultPreferences();
    }
    //
    // guck mal. ob das ein grosses Display ist,
    // dann ist da nämlich auch der Detailcontainer vorhanden
    //
    if( findViewById( R.id.area_detail_container ) != null )
    {
      Log.v( TAG, "onCreate: twoPane-mode" );
      // Der Detailcontainer ist nur vorhanden, wenn die App
      // (durch das System) festgestellt hat, dass es auf einem
      // Gerät mirt grossem Display (res/values-large and
      // res/values-sw600dp) läuft.
      mTwoPane = true;
      // Im twoPane Modus soll der aktivierte Eintrag immer gekennzeichnet sein!
      Log.v( TAG, "onCreate: set \"activate on item click\"..." );
      ( ( areaListFragment )getFragmentManager().findFragmentById( R.id.area_list ) ).setActivateOnItemClick( true );
    }
    else
    {
      Log.v( TAG, "onCreate: onePane-mode" );
    }
    //
    // die Programmlisteneinträge initialisieren
    //
    Log.v( TAG, "onCreate: initStaticContentSwitcher()..." );
    initStaticContenSwitcher();
    Log.v( TAG, "onCreate: initStaticContentSwitcher()...OK" );
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

  /**
   * 
   * Erzeuge Preferenzen für den SPX42
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 29.12.2012
   */
  public void setDefaultPreferences()
  {
    Log.i( TAG, "setDefaultPreferences: make default preferences..." );
    PreferenceManager.getDefaultSharedPreferences( this );
    PreferenceManager.setDefaultValues( this, R.xml.config_spx42_preference_individual, true );
    PreferenceManager.setDefaultValues( this, R.xml.config_program_preference, true );
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( this );
    //
    // workarround um defaults zu setzen
    //
    sPref.edit().putBoolean( "firstTimeInitiated", true );
    sPref.edit().commit();
    sPref.edit().putInt( "PREF_VERSION", ProjectConst.PREF_VERSION );
    sPref.edit().commit();
  }
}
