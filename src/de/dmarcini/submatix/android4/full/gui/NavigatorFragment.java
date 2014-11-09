package de.dmarcini.submatix.android4.full.gui;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher;
import de.dmarcini.submatix.android4.full.content.ContentSwitcher.ProgItem;
import de.dmarcini.submatix.android4.full.interfaces.INavigationDrawerCallbacks;
import de.dmarcini.submatix.android4.full.utils.ArrayAdapterWithPics;
import de.dmarcini.submatix.android4.full.utils.NaviActionBarDrawerToggle;

/**
 * Fragment zur Interaktion mit dem user (Darstellung Menü)
 * 
 */
public class NavigatorFragment extends Fragment
{
  private static String              TAG                     = NavigatorFragment.class.getSimpleName();
  /**
   * Den selektieren Navigator-Eintrag hier merken
   */
  private static final String        STATE_SELECTED_POSITION = "selected_navigation_position";
  /**
   * Callback in die activity zur Benachrichtigung der erfolgreichen Initialisierung eines Frames
   */
  private INavigationDrawerCallbacks navigatorCallbacks;
  private ActionBarDrawerToggle      navigatorDrawerToggle;
  private DrawerLayout               navigatorLayout;
  private ListView                   menuListView;
  private View                       navigatorContainerView;
  private int                        currentSelectedPosition = 0;
  private boolean                    isFromSavedInstanceState;
  private boolean                    hasUserLearnedDrawer;

  /**
   * der Leerer, öffentliche Konstruktor
   *
   * Project: NaviTest Package: com.example.navitest.gui
   * 
   * Stand: 07.11.2014
   */
  public NavigatorFragment()
  {}

  /**
   * Gib (wenn vorhanden) das ListView des Menüs zurück
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 08.11.2014
   * 
   * @return ListView (das eigentliche Menü-View)
   */
  public ListView getMenuListView()
  {
    return( menuListView );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate:..." );
    // Read in the flag indicating whether or not the user has demonstrated awareness of the
    // drawer. See PREF_USER_LEARNED_DRAWER for details.
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( getActivity() );
    hasUserLearnedDrawer = sp.getBoolean( NaviActionBarDrawerToggle.PREF_USER_LEARNED_DRAWER, false );
    if( savedInstanceState != null )
    {
      currentSelectedPosition = savedInstanceState.getInt( STATE_SELECTED_POSITION );
      isFromSavedInstanceState = true;
    }
    // Select either the default item (0) or the last selected item.
    Log.v( TAG, String.format( Locale.getDefault(), "onCreate: select item <%d>...", currentSelectedPosition ) );
    Log.v( TAG, "onCreate:...OK" );
  }

  @Override
  public void onActivityCreated( Bundle savedInstanceState )
  {
    super.onActivityCreated( savedInstanceState );
    Log.v( TAG, "onActivityCreated:..." );
    // das Fragment hat ein Options Menü
    // damit reagiert es auf den Click auf den Titel mit dem Anzeigen des Navigators
    setHasOptionsMenu( true );
    Log.v( TAG, "onActivityCreated:...OK" );
  }

  /**
   * Setze en Adapter mit Online-Status
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 07.11.2014
   * 
   * @param isOnline
   */
  public void setListAdapterForOnlinestatus( boolean isOnline )
  {
    Log.v( TAG, "setListAdapterForOnlinestatus()..." );
    menuListView.setAdapter( new ArrayAdapterWithPics( getActivity(), 0, isOnline, ContentSwitcher.getProgramItemsList(),
            ( MainActivity.getAppStyle() == R.style.AppDarkTheme ? R.style.AppDarkTheme : R.style.AppLightTheme ) ) );
    menuListView.setItemChecked( currentSelectedPosition, true );
  }

  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreateView:..." );
    menuListView = ( ListView )inflater.inflate( R.layout.fragment_navigator, container, false );
    menuListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick( AdapterView<?> parent, View view, int position, long id )
      {
        selectItem( position );
      }
    } );
    setListAdapterForOnlinestatus( false );
    menuListView.setItemChecked( currentSelectedPosition, true );
    Log.v( TAG, "onCreateView:...OK" );
    return menuListView;
  }

  /**
   * Stellt fest, ob der Drawer geöffnet ist
   *
   * Project: NaviTest Package: com.example.navitest.gui
   * 
   * Stand: 07.11.2014
   * 
   * @return ist der Drawer geöffnet
   */
  public boolean isDrawerOpen()
  {
    return navigatorLayout != null && navigatorLayout.isDrawerOpen( navigatorContainerView );
  }

  /**
   * Den Navigator initialisieren
   *
   * @param fragmentId
   *          The android:id of this fragment in its activity's layout.
   * @param drawerLayout
   *          The DrawerLayout containing this fragment's UI.
   */
  public void setUp( int fragmentId, DrawerLayout drawerLayout )
  {
    Log.v( TAG, "setUp:..." );
    navigatorContainerView = getActivity().findViewById( fragmentId );
    navigatorLayout = drawerLayout;
    // set a custom shadow that overlays the main content when the drawer opens
    navigatorLayout.setDrawerShadow( R.drawable.drawer_shadow, GravityCompat.START );
    // set up the drawer's list view with items and click listener
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled( true );
    actionBar.setHomeButtonEnabled( true );
    // ActionBarDrawerToggle ties together the the proper interactions
    // between the navigation drawer and the action bar app icon.
    navigatorDrawerToggle = new NaviActionBarDrawerToggle( getActivity(), navigatorLayout, R.drawable.navigator_drawer, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close );
    // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
    // per the navigation drawer design guidelines.
    if( !hasUserLearnedDrawer && !isFromSavedInstanceState )
    {
      navigatorLayout.openDrawer( navigatorContainerView );
    }
    // Defer code dependent on restoration of previous instance state.
    navigatorLayout.post( new Runnable() {
      @Override
      public void run()
      {
        navigatorDrawerToggle.syncState();
      }
    } );
    navigatorLayout.setDrawerListener( navigatorDrawerToggle );
    Log.v( TAG, "setUp:...OK" );
  }

  /**
   * 
   * Wenn ein Navigationseintrag selektiert wurde
   *
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.gui
   * 
   * Stand: 07.11.2014
   * 
   * @param position
   *          welche Position war es?
   */
  private void selectItem( int position )
  {
    Log.v( TAG, "selectItem:..." );
    currentSelectedPosition = position;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "selectItem: Position:<" + position + ">..." );
    if( menuListView != null )
    {
      menuListView.setItemChecked( position, true );
    }
    if( navigatorLayout != null )
    {
      navigatorLayout.closeDrawer( navigatorContainerView );
    }
    if( navigatorCallbacks != null )
    {
      //
      // Übergib die ID des gewählten Eintrages
      //
      ProgItem pItem = ( ( ArrayAdapterWithPics )menuListView.getAdapter() ).getItem( position );
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, String.format( Locale.getDefault(), "selectItem: select item-Id: <%d>, String: <%s>", pItem.nId, pItem.content ) );
      navigatorCallbacks.onNavigationDrawerItemSelected( pItem );
    }
    Log.v( TAG, "selectItem:...OK" );
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    Log.v( TAG, "onAttach:..." );
    try
    {
      navigatorCallbacks = ( INavigationDrawerCallbacks )activity;
    }
    catch( ClassCastException e )
    {
      throw new ClassCastException( "Activity must implement INavigationDrawerCallbacks." );
    }
    Log.v( TAG, "onAttach:...OK" );
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    Log.v( TAG, "onDetach:..." );
    navigatorCallbacks = null;
    Log.v( TAG, "onDetach:...OK" );
  }

  @Override
  public void onSaveInstanceState( Bundle outState )
  {
    super.onSaveInstanceState( outState );
    outState.putInt( STATE_SELECTED_POSITION, currentSelectedPosition );
  }

  @Override
  public void onConfigurationChanged( Configuration newConfig )
  {
    super.onConfigurationChanged( newConfig );
    // Forward the new configuration the drawer toggle component.
    navigatorDrawerToggle.onConfigurationChanged( newConfig );
  }

  @Override
  public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
  {
    // If the drawer is open, show the global app actions in the action bar. See also
    // showGlobalContextActionBar, which controls the top-left area of the action bar.
    if( navigatorLayout != null && isDrawerOpen() )
    {
      // inflater.inflate( R.menu.global, menu );
      showGlobalContextActionBar();
    }
    super.onCreateOptionsMenu( menu, inflater );
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    Log.v( TAG, "onOptionsItemSelected:..." );
    if( navigatorDrawerToggle.onOptionsItemSelected( item ) )
    {
      Log.v( TAG, "onOptionsItemSelected:...OK" );
      return true;
    }
    Log.v( TAG, "onOptionsItemSelected:...OK" );
    return super.onOptionsItemSelected( item );
  }

  /**
   * Per the navigation drawer design guidelines, updates the action bar to show the global app 'context', rather than just what's in the current screen.
   */
  private void showGlobalContextActionBar()
  {
    Log.v( TAG, "showGlobalContextActionBar:..." );
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled( true );
    // deprecated since API 21
    // actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_STANDARD );
    Log.v( TAG, "showGlobalContextActionBar: show app_name" );
    actionBar.setTitle( R.string.app_name );
    Log.v( TAG, "showGlobalContextActionBar:...OK" );
  }

  private ActionBar getActionBar()
  {
    return getActivity().getActionBar();
  }
}
