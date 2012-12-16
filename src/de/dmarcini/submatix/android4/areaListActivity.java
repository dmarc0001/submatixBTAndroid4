package de.dmarcini.submatix.android4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import de.dmarcini.android4.comm.BlueThoothCommThread;

/**
 * An activity representing a list of areas. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a list of items,
 * which when touched, lead to a {@link areaDetailActivity} representing item details. On tablets, the activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link areaListFragment} and the item details (if present) is a {@link areaDetailFragment}.
 * <p>
 * This activity also implements the required {@link areaListFragment.Callbacks} interface to listen for item selections.
 */
public class areaListActivity extends FragmentActivity implements areaListFragment.Callbacks
{
  protected static BlueThoothCommThread btThread = null;
  private static final String           TAG      = areaListActivity.class.getSimpleName();
  /**
   * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
   */
  protected boolean                     mTwoPane = false;

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.activity_area_list );
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
    if( btThread == null )
    {
      Log.v( TAG, "starting btThread..." );
      btThread = new BlueThoothCommThread();
      btThread.start();
      Log.v( TAG, "starting btThread...OK" );
    }
    // TODO: If exposing deep links into your app, handle intents here.
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause..." );
    if( ( btThread != null ) && mTwoPane )
    {
      Log.v( TAG, "onDestroy: stop btThread..." );
      btThread.stopThread();
      btThread = null;
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume..." );
  }

  @Override
  public void onRestart()
  {
    super.onResume();
    Log.v( TAG, "onRestart..." );
  }

  @Override
  public void finishFromChild( Activity child )
  {
    Log.i( TAG, "child process called finish()..." );
    if( btThread != null )
    {
      Log.v( TAG, "onDestroy: stop btThread..." );
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
    Log.v( TAG, "onItemSelected: <" + id + ">" );
    if( id.equals( "3" ) )
    {
      Log.i( TAG, "Program exit!" );
      if( btThread != null )
      {
        Log.v( TAG, "onItemSelected: stop btThread..." );
        btThread.stopThread();
      }
      this.finish();
      return;
    }
    if( mTwoPane )
    {
      // In two-pane mode, show the detail view in this activity by
      // adding or replacing the detail fragment using a
      // fragment transaction.
      Bundle arguments = new Bundle();
      arguments.putString( areaDetailFragment.ARG_ITEM_ID, id );
      areaDetailFragment fragment = new areaDetailFragment();
      fragment.setArguments( arguments );
      getSupportFragmentManager().beginTransaction().replace( R.id.area_detail_container, fragment ).commit();
    }
    else
    {
      // In single-pane mode, simply start the detail activity
      // for the selected item ID.
      Intent detailIntent = new Intent( this, areaDetailActivity.class );
      detailIntent.putExtra( areaDetailFragment.ARG_ITEM_ID, id );
      startActivity( detailIntent );
    }
  }
}
