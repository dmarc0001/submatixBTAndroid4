package de.dmarcini.submatix.android4.full.gui;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher.ProgItem;
import de.dmarcini.submatix.android4.full.interfaces.INavigationDrawerCallbacks;

/**
 * Die Aktivität der Application
 *
 * Project: NaviTest Package: com.example.navitest
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *
 *         Stand: 06.11.2014
 */
public class MainActivity extends Activity implements INavigationDrawerCallbacks
{
  private static String      TAG                        = MainActivity.class.getSimpleName();
  private static int         currentStyleId             = R.style.AppDarkTheme;
  @SuppressWarnings( "javadoc" )
  public static final String ARG_DUMMY_ARGUMENT         = "dummy_arg";
  @SuppressWarnings( "javadoc" )
  public static final int    TYPE_MAIN_CONTENT_FRAGMENT = 0;
  @SuppressWarnings( "javadoc" )
  public static final int    TYPE_CONTENT1_FRAGMENT     = 1;
  /**
   * einzublendendes Navigator-Fragment für die Navigation in der App
   */
  private NavigatorFragment  mNavigationDrawerFragment;
  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence       mTitle;

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate:..." );
    initStaticContenSwitcher();
    setContentView( R.layout.activity_main );
    mNavigationDrawerFragment = ( NavigatorFragment )getFragmentManager().findFragmentById( R.id.navi_drawer );
    mTitle = getTitle();
    Log.v( TAG, "onCreate: set navigation drawer..." );
    // Initialisiere den Navigator
    mNavigationDrawerFragment.setUp( R.id.navi_drawer, ( DrawerLayout )findViewById( R.id.drawer_layout ) );
    Log.v( TAG, "onCreate:...OK" );
  }

  /**
   * 
   * Gib den Style der App zurück
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
   * 
   * Stand: 03.12.2013
   * 
   * @return Style der App
   */
  public static final int getAppStyle()
  {
    return( currentStyleId );
  }

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
    ContentSwitcher
            .addItem( new ProgItem( R.string.progitem_spx_status, R.drawable.spx_health_icon, R.drawable.spx_health_icon, getString( R.string.progitem_spx_status ), false ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_config, R.drawable.spx_toolbox_offline, R.drawable.spx_toolbox_online, getString( R.string.progitem_config ), false ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_gaslist, R.drawable.gasedit_offline, R.drawable.gasedit_online, getString( R.string.progitem_gaslist ), false ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_logging, R.drawable.logging_offline, R.drawable.logging_online, getString( R.string.progitem_logging ), false ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_loggraph, R.drawable.graphsbar_online, R.drawable.graphsbar_online, getString( R.string.progitem_loggraph ), true ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_export, R.drawable.export_offline, R.drawable.export_online, getString( R.string.progitem_export ), true ) );
    ContentSwitcher
            .addItem( new ProgItem( R.string.progitem_progpref, R.drawable.app_toolbox_offline, R.drawable.app_toolbox_online, getString( R.string.progitem_progpref ), true ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_about, R.drawable.yin_yang, R.drawable.yin_yang, getString( R.string.progitem_about ), true ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_exit, R.drawable.shutoff, R.drawable.shutoff, getString( R.string.progitem_exit ), true ) );
  }

  /**
   * Callback vom Navigator welcher Eintrag ausgewählt wurde
   */
  @Override
  public void onNavigationDrawerItemSelected( int position )
  {
    FragmentManager fragmentManager = getFragmentManager();
    Bundle arguments = new Bundle();
    Fragment fragment = null;
    //
    arguments.putInt( ARG_DUMMY_ARGUMENT, 1 );
    //
    // Den Inhalt in main_container UPDATEN
    //
    Log.v( TAG, "onNavigationDrawerItemSelected:...<" + position + ">" );
    // if( position == 0 )
    // {
    // fragment = new MainContentFragment();
    // fragment.setArguments( arguments );
    // fragmentManager.beginTransaction().replace( R.id.main_container, fragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
    // }
    // else
    // {
    // fragment = new Content1Fragment();
    // fragment.setArguments( arguments );
    // fragmentManager.beginTransaction().replace( R.id.main_container, fragment ).setTransition( FragmentTransaction.TRANSIT_FRAGMENT_FADE ).commit();
    // }
    Log.v( TAG, "onNavigationDrawerItemSelected:...OK" );
  }

  /**
   * Den Programmtitel entsprechend der Selektion setzen
   *
   * Project: NaviTest Package: com.example.navitest.gui
   * 
   * Stand: 06.11.2014
   * 
   * @param number
   */
  public void onSectionAttached( int number )
  {
    Log.v( TAG, String.format( Locale.getDefault(), "onSectionAttached: number <%d>...", number ) );
    mTitle = String.format( "Attached: %03d", number );
    // switch ( number )
    // {
    // case TYPE_MAIN_CONTENT_FRAGMENT:
    // mTitle = getString( R.string.title_main );
    // break;
    // case TYPE_CONTENT1_FRAGMENT:
    // mTitle = getString( R.string.title_content1 );
    // break;
    // default:
    // mTitle = getString( R.string.title_error );
    // break;
    // }
    Log.v( TAG, "onSectionAttached: OK" );
  }

  /**
   * ActionBar restaurieren bei onCreateOptionsMenu
   *
   * Project: NaviTest Package: com.example.navitest
   * 
   * Stand: 06.11.2014
   */
  public void restoreActionBar()
  {
    Log.v( TAG, "restoreActionBar:..." );
    ActionBar actionBar = getActionBar();
    // deprecated since API 21
    // actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_STANDARD );
    actionBar.setDisplayShowTitleEnabled( true );
    actionBar.setTitle( mTitle );
    Log.v( TAG, "restoreActionBar:...OK" );
  }

  @Override
  public boolean onCreateOptionsMenu( Menu menu )
  {
    if( !mNavigationDrawerFragment.isDrawerOpen() )
    {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      // getMenuInflater().inflate( R.menu.main, menu );
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu( menu );
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    // if( id == R.id.action_settings )
    // {
    // return true;
    // }
    return super.onOptionsItemSelected( item );
  }
}
