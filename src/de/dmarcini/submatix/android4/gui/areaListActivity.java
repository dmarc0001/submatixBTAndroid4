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
    Log.v(TAG, "initStaticContent...");
    // zuerst aufräumen
    ContentSwitcher.clearItems();
    //
    // irgendeine Kennung muss der String bekommen, also gibts halt die String-ID
    //
    ContentSwitcher.addItem(new ProgItem(R.string.progitem_connect, R.drawable.bluetooth_icon_color, getString(R.string.progitem_connect)));
    ContentSwitcher.addItem(new ProgItem(R.string.progitem_config, R.drawable.toolboxwhite, getString(R.string.progitem_config)));
    ContentSwitcher.addItem(new ProgItem(R.string.progitem_gaslist, R.drawable.pinion, getString(R.string.progitem_gaslist)));
    ContentSwitcher.addItem(new ProgItem(R.string.progitem_logging, R.drawable.logging, getString(R.string.progitem_logging)));
    ContentSwitcher.addItem(new ProgItem(R.string.progitem_exit, R.drawable.shutoff, getString(R.string.progitem_exit)));
  }

  /**
   * Wenn die Activity erzeugt wird, u.A. herausfinden ob ein- oder zwei-Flächen Mode
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   * @author Dirk Marciniak 28.12.2012
   * @param savedInstanceState
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "onCreate...");
    //
    // die Programmlisteneinträge initialisieren
    //
    Log.v(TAG, "onCreate: initStaticContentSwitcher()...");
    initStaticContenSwitcher();
    Log.v(TAG, "onCreate: initStaticContentSwitcher()...OK");
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v(TAG, "onPause...");
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Log.v(TAG, "onResume...");
  }
}
