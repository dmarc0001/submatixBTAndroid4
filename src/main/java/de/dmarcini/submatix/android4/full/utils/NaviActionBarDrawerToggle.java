package de.dmarcini.submatix.android4.full.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

/**
 * Objekt zum binden des Navigationsframes mit der ActionBar
 * <p/>
 * Project: NaviTest Package: com.example.navitest.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 07.11.2014
 */
@SuppressWarnings( "deprecation" )
public class NaviActionBarDrawerToggle extends ActionBarDrawerToggle
{
  /**
   * Merken, ob der User das zur Kenntnis genommen hat
   */
  public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
  private final Activity parentActivity;
  private       boolean  userHasLearnedDrawer;

  /**
   * HEADLINE
   * <p/>
   * Project: NaviTest Package: com.example.navitest.utils
   * <p/>
   * Stand: 07.11.2014
   *
   * @param activity                  Die Activity zu der dieses Objekt gehört
   * @param drawerLayout              Das Layout in dem der Navi gezeigt wird
   * @param drawerImageRes            Das "zurück" Icon
   * @param openDrawerContentDescRes  String für Beschreibung OPEN
   * @param closeDrawerContentDescRes String für Beschreibung CLOSE
   */
  public NaviActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes)
  {
    super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
    parentActivity = activity;
    // Read in the flag indicating whether or not the user has demonstrated awareness of the
    // drawer. See PREF_USER_LEARNED_DRAWER for details.
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
    userHasLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
  }

  @Override
  public void onDrawerClosed(View drawerView)
  {
    super.onDrawerClosed(drawerView);
    // if( !isAdded() )
    // {
    // return;
    // }
    parentActivity.invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
  }

  @Override
  public void onDrawerOpened(View drawerView)
  {
    super.onDrawerOpened(drawerView);
    // if( !isAdded() )
    // {
    // return;
    // }
    if( !userHasLearnedDrawer )
    {
      // The user manually opened the drawer; store this flag to prevent auto-showing
      // the navigation drawer automatically in the future.
      userHasLearnedDrawer = true;
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(parentActivity);
      sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
    }
    parentActivity.invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
  }
}
