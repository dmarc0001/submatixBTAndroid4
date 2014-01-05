/**
 * Hauptaktivity des Projektes
 * 
 * Wird als erstes ausgeführt. Bei kleinen Schirmen wird bei Auswahl einer Option an die detailActivity übergeben (eine Liste der Optionen angezeigt), bei großen Schirmen wird
 * direkt in dieser Activity der Detailschirm aufgebaut und bedient. Der Gemeisame Code für beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.full.gui;

import java.io.File;

import org.joda.time.format.DateTimeFormat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher.ProgItem;
import de.dmarcini.submatix.android4.full.utils.BuildVersion;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;
import de.jockels.open.Environment2;
import de.jockels.open.NoSecondaryStorageException;

/**
 * 
 * Activity mit welcher die App startet. Bei Tablett die Main-Activity, bei kleinen screens die Menü-Activity
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class AreaListActivity extends FragmentCommonActivity
{
  private static final String TAG         = AreaListActivity.class.getSimpleName();
  private static final String FIRSTTIME   = "keyFirstTimeInitiated";
  private static final String PREFVERSION = "keyPreferencesVersion";

  /**
   * Erzeuge die Menüeinträge Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  private void initStaticContenSwitcher()
  {
    Log.v( TAG, "initStaticContent..." );
    // zuerst aufräumen
    ContentSwitcher.clearItems();
    //
    // irgendeine Kennung muss der String bekommen, also gibts halt die String-ID
    //
    ContentSwitcher
            .addItem( new ProgItem( R.string.progitem_connect, R.drawable.bluetooth_icon_bw, R.drawable.bluetooth_icon_color, getString( R.string.progitem_connect ), true ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_config, R.drawable.spx_toolbox_offline, R.drawable.spx_toolbox_online, getString( R.string.progitem_config ), false ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_gaslist, R.drawable.gasedit_offline, R.drawable.gasedit_online, getString( R.string.progitem_gaslist ), false ) );
    if( !BuildVersion.isLightVersion )
    {
      ContentSwitcher.addItem( new ProgItem( R.string.progitem_logging, R.drawable.logging_offline, R.drawable.logging_online, getString( R.string.progitem_logging ), false ) );
      ContentSwitcher
              .addItem( new ProgItem( R.string.progitem_loggraph, R.drawable.graphsbar_offline, R.drawable.graphsbar_online, getString( R.string.progitem_loggraph ), true ) );
      ContentSwitcher.addItem( new ProgItem( R.string.progitem_export, R.drawable.export_offline, R.drawable.export_online, getString( R.string.progitem_export ), true ) );
    }
    ContentSwitcher
            .addItem( new ProgItem( R.string.progitem_progpref, R.drawable.app_toolbox_offline, R.drawable.app_toolbox_online, getString( R.string.progitem_progpref ), true ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_about, R.drawable.yin_yang, R.drawable.yin_yang, getString( R.string.progitem_about ), true ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_exit, R.drawable.shutoff, R.drawable.shutoff, getString( R.string.progitem_exit ), true ) );
  }

  /**
   * Wenn die Activity erzeugt wird, u.A. herausfinden ob ein- oder zwei-Flächen Mode
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
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
    if( !sPref.contains( FIRSTTIME ) )
    {
      Log.w( TAG, "onCreate: not found firsttime key == make first time preferences..." );
      if( !sPref.getBoolean( FIRSTTIME, false ) )
      {
        setDefaultPreferences();
      }
    }
    //
    // sind die Preferenzen in der richtigen version?
    //
    if( sPref.contains( PREFVERSION ) )
    {
      if( ProjectConst.PREF_VERSION != sPref.getInt( PREFVERSION, 0 ) )
      {
        Log.w( TAG, "onCreate: pref version to old == make first time preferences..." );
        setDefaultPreferences();
      }
    }
    else
    {
      Log.w( TAG, "onCreate: pref version not found == make first time preferences..." );
      setDefaultPreferences();
    }
    //
    // Verzeichnis für Datenbanken etc
    //
    databaseDir = new File( sPref.getString( "keyProgDataDirectory", getdatabaseDir().getAbsolutePath() ) );
    if( databaseDir != null )
    {
      if( !databaseDir.exists() )
      {
        Log.i( TAG, "onCreate: create database root dir..." );
        if( !databaseDir.mkdirs() ) databaseDir = null;
      }
    }
    if( sPref.contains( "keyProgUnitsTimeFormat" ) )
    {
      FragmentCommonActivity.localTimeFormatter = DateTimeFormat.forPattern( sPref.getString( "keyProgUnitsTimeFormat", "yyyy/dd/MM - hh:mm:ss a" ) );
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
      // Gerät mit grossem Display (res/values-large and
      // res/values-sw600dp) läuft.
      mTwoPane = true;
      // Im twoPane Modus soll der aktivierte Eintrag immer gekennzeichnet sein!
      Log.v( TAG, "onCreate: set \"activate on item click\"..." );
      ( ( AreaListFragment )getFragmentManager().findFragmentById( R.id.area_list ) ).setActivateOnItemClick( true );
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
    Log.v( TAG, "onPause..." );
    super.onPause();
  }

  @Override
  public void onResume()
  {
    Log.v( TAG, "onResume..." );
    super.onResume();
  }

  @Override
  public void onDestroy()
  {
    Log.v( TAG, "onDestroy..." );
    super.onDestroy();
  }

  @Override
  public void onStop()
  {
    Log.v( TAG, "onStop..." );
    super.onStop();
  }

  /**
   * Erzeuge Preferenzen für den SPX42 Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   */
  public void setDefaultPreferences()
  {
    String gasKeyTemplate = getResources().getString( R.string.conf_gaslist_gas_key_template );
    String gasListDefault = getResources().getString( R.string.conf_gaslist_default );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setDefaultPreferences: make default preferences..." );
    PreferenceManager.setDefaultValues( this, R.xml.config_spx42_preference_individual, true );
    PreferenceManager.setDefaultValues( this, R.xml.config_program_preference, true );
    //
    // workarround um defaults zu setzen
    //
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( this );
    SharedPreferences.Editor editor = sPref.edit();
    editor.remove( FIRSTTIME );
    editor.putBoolean( FIRSTTIME, true );
    editor.remove( PREFVERSION );
    editor.putInt( PREFVERSION, ProjectConst.PREF_VERSION );
    //
    // Gaslistenpresets eintragen
    //
    for( int i = 1; i < 9; i++ )
    {
      editor.putString( String.format( gasKeyTemplate, i ), gasListDefault );
    }
    //
    // external Storage eintragen
    //
    databaseDir = getdatabaseDir();
    editor.putString( "keyProgDataDirectory", databaseDir.getAbsolutePath() );
    //
    // alles in die Propertys
    //
    if( editor.commit() )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "setDefaultPreferences: wrote preferences to storeage." );
    }
    else
    {
      Log.e( TAG, "setDefaultPreferences: CAN'T wrote preferences to storage." );
    }
  }

  /**
   * 
   * das Database Directory finden
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * TODO: Android 4.3 funktioniert hier nicht :( Bibliothek kann das nicht
   * 
   * Stand: 25.07.2013
   * 
   * @return Das Datenbankverzeichnis
   */
  private File getdatabaseDir()
  {
    File extSdCard;
    File dataBaseRoot;
    //
    try
    {
      if( Environment2.isSecondaryExternalStorageAvailable() )
      {
        extSdCard = Environment2.getSecondaryExternalStorageDirectory();
      }
      else
      {
        Log.w( TAG, "extern storage not found! fallbsack to internal store!" );
        extSdCard = Environment.getExternalStorageDirectory();
        // extSdCard = Environment2.getCardDirectory();
      }
    }
    catch( NoSecondaryStorageException ex )
    {
      return( null );
    }
    if( extSdCard.exists() && extSdCard.isDirectory() && extSdCard.canWrite() )
    {
      Log.i( TAG, "datastore Directory is: <" + extSdCard + ">" );
      dataBaseRoot = new File( extSdCard + File.separator + ProjectConst.APPROOTDIR );
      return( dataBaseRoot );
    }
    return( null );
  }
}
