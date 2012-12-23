/**
 * Hauptaktivity des Projektes
 * 
 * Wird als erstes ausgeführt. Bei kleinen Schirmen wird bei Auswahl einer Option an die detailActivity übergeben (eine Liste der Optionen angezeigt), bei großen Schirmen wird
 * direkt in dieser Activity der Detailschirm aufgebaut und bedient. Der Gemeisame Code für beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.gui;

import android.os.Bundle;
import android.util.Log;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.content.ContentSwitcher.ProgItem;

public class areaListActivity extends combinedFragmentActivity
{
  private static final String TAG = areaListActivity.class.getSimpleName();

  /**
   * 
   * Erzeuge die Menüeinträge
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 17.12.2012
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
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_exit, R.drawable.shutoff, getString( R.string.progitem_exit ) ) );
  }

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate..." );
    Log.v( TAG, "onCreate: setContentView..." );
    setContentView( R.layout.activity_area_list );
    Log.v( TAG, "onCreate: initStaticContentSwitcher()..." );
    initStaticContenSwitcher();
    Log.v( TAG, "onCreate: initStaticContentSwitcher()...OK" );
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
      ( ( areaListFragment )getSupportFragmentManager().findFragmentById( R.id.area_list ) ).setActivateOnItemClick( true );
    }
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
}
