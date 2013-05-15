package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import de.dmarcini.submatix.android4.BuildConfig;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.ArrayAdapterWithPics;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * A list fragment representing a list of areas. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon selection. This helps
 * indicate which item is currently being viewed in a {@link areaDetailFragment}.
 */
public class areaListFragment extends ListFragment
{
  private static final String TAG                      = areaListFragment.class.getSimpleName();
  private static final String STATE_ACTIVATED_POSITION = "activated_position";
  private int                 mActivatedPosition       = ListView.INVALID_POSITION;
  private boolean             whishedTheme             = true;

  /**
   * Der Konstruktor Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 04.11.2012
   */
  public areaListFragment()
  {}

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences( getActivity() );
    whishedTheme = sPref.getBoolean( "keyProgOthersThemeIsDark", false );
  }

  /**
   * 
   * Setze die Icons der Menüauswahl auf die Online/Offline Variante
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 15.05.2013
   * @param isOnline
   */
  public void setListAdapterForOnlinestatus( boolean isOnline )
  {
    Log.v( TAG, "setListAdapterForOnlinestatus()..." );
    setListAdapter( new ArrayAdapterWithPics( getActivity(), 0, isOnline, ContentSwitcher.getProgramItemsList(), ( whishedTheme ? R.style.AppDarkTheme : R.style.AppLightTheme ) ) );
    if( mActivatedPosition != ListView.INVALID_POSITION )
    {
      setActivatedPosition( mActivatedPosition );
    }
  }

  @Override
  public void onViewCreated( View view, Bundle savedInstanceState )
  {
    super.onViewCreated( view, savedInstanceState );
    Log.v( TAG, "onViewCreated..." );
    // Restore the previously serialized activated item position.
    if( savedInstanceState != null && savedInstanceState.containsKey( STATE_ACTIVATED_POSITION ) )
    {
      Log.v( TAG, "setActivadedPosition..." );
      setActivatedPosition( savedInstanceState.getInt( STATE_ACTIVATED_POSITION ) );
    }
  }

  @Override
  public void onSaveInstanceState( Bundle outState )
  {
    super.onSaveInstanceState( outState );
    if( mActivatedPosition != ListView.INVALID_POSITION )
    {
      // Serialize and persist the activated item position.
      outState.putInt( STATE_ACTIVATED_POSITION, mActivatedPosition );
    }
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    boolean isConnected = false;
    //
    super.onResume();
    Log.v( TAG, "onResume()..." );
    Log.v( TAG, "onResume(): setListAdapter...(" + ( whishedTheme ? "DARK" : "LIGHT" ) + ")" );
    if( ( ( FragmentCommonActivity )getActivity() ).getConnectionStatus() == ProjectConst.CONN_STATE_CONNECTED )
    {
      isConnected = true;
    }
    setListAdapterForOnlinestatus( isConnected );
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
  }

  @Override
  public void onListItemClick( ListView listView, View view, int position, long id )
  {
    super.onListItemClick( listView, view, position, id );
    Log.v( TAG, "onListItemClick()..." );
    //
    // delegiere die Bearbeitung an die aktive Activity
    //
    ( ( areaListActivity )getActivity() ).onListItemClick( listView, view, position, id );
    setActivatedPosition( position );
  }

  /**
   * Schaltet den Activate-On-click Mode an, damit erhalten angefasste Einträge den Activate Status Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 22.12.2012
   * @param activateOnItemClick
   */
  public void setActivateOnItemClick( boolean activateOnItemClick )
  {
    // When setting CHOICE_MODE_SINGLE, ListView will automatically
    // give items the 'activated' state when touched
    if( BuildConfig.DEBUG ) Log.d( TAG, "setActivateOnItemClick( " + activateOnItemClick + " )" );
    getListView().setChoiceMode( activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE );
  }

  /**
   * Kennzeichne die aktivierte Position Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de) Stand: 23.12.2012
   * @param position
   */
  private void setActivatedPosition( int position )
  {
    if( position == ListView.INVALID_POSITION )
    {
      getListView().setItemChecked( mActivatedPosition, false );
    }
    else
    {
      if( BuildConfig.DEBUG ) Log.d( TAG, "setActivatedPosition: checked Position was: <" + getListView().getCheckedItemPosition() + ">" );
      getListView().setItemChecked( position, true );
    }
    mActivatedPosition = position;
  }
}
