package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import de.dmarcini.submatix.android4.comm.BlueThoothCommThread;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.content.ContentSwitcher.ProgItem;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * An activity representing a list of areas. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a list of items,
 * which when touched, lead to a {@link areaDetailActivity} representing item details. On tablets, the activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link areaListFragment} and the item details (if present) is a {@link areaDetailFragment}.
 * <p>
 * This activity also implements the required {@link areaListFragment.Callbacks} interface to listen for item selections.
 */
public class areaListActivity extends FragmentActivity implements areaListFragment.Callbacks, AreYouSureDialogFragment.NoticeDialogListener
{
  protected static BlueThoothCommThread btThread = null;
  private static final String           TAG      = areaListActivity.class.getSimpleName();
  /**
   * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
   */
  protected static boolean              mTwoPane = false;

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate..." );
    setContentView( R.layout.activity_area_list );
    Log.v( TAG, "onCreate: initStaticContentSwitcher()..." );
    initStaticContenSwitcher();
    Log.v( TAG, "onCreate: initStaticContentSwitcher()...OK" );
    if( findViewById( R.id.area_detail_container ) != null )
    {
      // The detail container view will be present only in the
      // large-screen layouts (res/values-large and
      // res/values-sw600dp). If this view is present, then the
      // activity should be in two-pane mode.
      mTwoPane = true;
      // In two-pane mode, list items should be given the
      // 'activated' state when touched.
      ( ( areaListFragment )getSupportFragmentManager().findFragmentById( R.id.area_list ) ).setActivateOnItemClick( true );
    }
  }

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
    // zuerst aufräumen
    ContentSwitcher.clearItems();
    //
    // irgendeine Kennung muss der String bekommen, also gibts halt die String-ID
    //
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_connect, getString( R.string.progitem_connect ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_config, getString( R.string.progitem_config ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_gaslist, getString( R.string.progitem_gaslist ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_logging, getString( R.string.progitem_logging ) ) );
    ContentSwitcher.addItem( new ProgItem( R.string.progitem_exit, getString( R.string.progitem_exit ) ) );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause..." );
    //
    // ist der Tablettmodus aktiv und ein Thread am ackern
    // App im Hintergrund oder beendet
    // beende den Thread
    //
    if( ( btThread != null ) && mTwoPane )
    {
      if( mTwoPane )
      {
        Log.v( TAG, "onPause: stop btThread..." );
        btThread.stopThread();
        btThread = null;
      }
      else
      {
        // gib Bescheid
        Log.v( TAG, "onPause: prepare stop btThread..." );
        btThread.prepareStopThread();
      }
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume..." );
    //
    // ist ein Thread schon vorhanden
    //
    if( ( btThread != null ) && ( btThread.getState() != Thread.State.TERMINATED ) )
    {
      // gib bescheid, daß kein ende erforderlich war
      Log.v( TAG, "resume btThread..." );
      btThread.threadNotSpopping();
    }
    else
    {
      // erzeuge einen Thread
      Log.v( TAG, "onResume: starting btThread..." );
      btThread = new BlueThoothCommThread();
      btThread.start();
      Log.v( TAG, "onResume: starting btThread...OK" );
    }
  }

  @Override
  public void finishFromChild( Activity child )
  {
    Log.i( TAG, "child process called finish()..." );
    //
    // wenn eine Clientactivity mit finish() beendet
    // wurde, ist hier auch schluss
    //
    if( btThread != null )
    {
      Log.v( TAG, "finishFromChild: stop btThread..." );
      btThread.stopThread();
    }
    finish();
  }

  /**
   * Callback method from {@link areaListFragment.Callbacks} indicating that the item with the given ID was selected.
   */
  @Override
  public void onItemSelected( String id )
  {
    ContentSwitcher.ProgItem mItem = null;
    android.support.v4.app.Fragment fragment = null;
    //
    // zunächst will ich mal wissen, was das werden soll!
    //
    Log.v( TAG, "onItemSelected(): ID was: <" + id + ">" );
    mItem = ContentSwitcher.progItemsMap.get( id );
    if( mItem == null )
    {
      Log.e( TAG, "program menu item was NOT explored!" );
      return;
    }
    Log.v( TAG, "onItemSelected(): item was: " + mItem.content );
    //
    // jetzt noch zwischen Tablett mit Schirmsplitt und Smartphone unterscheiden
    //
    if( mTwoPane )
    {
      //
      // zweischirmbetrieb
      //
      Bundle arguments = new Bundle();
      arguments.putString( ProjectConst.ARG_ITEM_ID, id );
      if( mItem.nId == R.string.progitem_connect )
      {
        connectFragment connFragment = new connectFragment();
        fragment = connFragment;
      }
      else if( mItem.nId == R.string.progitem_config )
      {
        configSPX42Fragment connFragment = new configSPX42Fragment();
        fragment = connFragment;
      }
      else
      {
        Log.e( TAG, "detail fragment for id <" + id + "> not found!" );
        areaListFragment dFragment = new areaListFragment();
        fragment = dFragment;
      }
      fragment.setArguments( arguments );
      getSupportFragmentManager().beginTransaction().replace( R.id.area_detail_container, fragment ).commit();
    }
    else
    {
      //
      // kleiner Schirm
      //
      Intent detailIntent = new Intent( this, areaListActivity.class );
      detailIntent.putExtra( ProjectConst.ARG_ITEM_ID, id );
      startActivity( detailIntent );
    }
  }

  @Override
  public void onDialogPositiveClick( DialogFragment dialog )
  {
    Log.v( TAG, "Positive dialog click!" );
    //
    // war es ein AreYouSureDialogFragment Dialog?
    //
    if( dialog instanceof AreYouSureDialogFragment )
    {
      AreYouSureDialogFragment aDial = ( AreYouSureDialogFragment )dialog;
      //
      // War der Tag für den Dialog zum Exit des Programmes?
      //
      if( aDial.getTag().equals( "programexit" ) )
      {
        Log.i( TAG, "User close app..." );
        Toast.makeText( this, R.string.toast_exit, Toast.LENGTH_SHORT ).show();
        if( btThread != null )
        {
          Log.v( TAG, "finishFromChild: stop btThread..." );
          btThread.stopThread();
        }
        finish();
      }
    }
  }

  @Override
  public void onDialogNegativeClick( DialogFragment dialog )
  {
    Log.v( TAG, "Negative dialog click!" );
  }
}
