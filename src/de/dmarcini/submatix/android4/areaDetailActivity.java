package de.dmarcini.submatix.android4;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.android4.comm.BlueThoothCommThread;

/**
 * An activity representing a single area detail screen. This activity is only used on handset devices. On tablet-size devices, item details are presented side-by-side with a list
 * of items in a {@link areaListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link areaDetailFragment}.
 */
public class areaDetailActivity extends FragmentActivity
{
  private static final String TAG = areaDetailActivity.class.getSimpleName();

  @Override
  protected void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate:..." );
    setContentView( R.layout.activity_area_detail );
    // Show the Up button in the action bar.
    getActionBar().setDisplayHomeAsUpEnabled( true );
    // savedInstanceState is non-null when there is fragment state
    // saved from previous configurations of this activity
    // (e.g. when rotating the screen from portrait to landscape).
    // In this case, the fragment will automatically be re-added
    // to its container so we don't need to manually add it.
    // For more information, see the Fragments API guide at:
    //
    // http://developer.android.com/guide/components/fragments.html
    //
    if( savedInstanceState == null )
    {
      // Create the detail fragment and add it to the activity
      // using a fragment transaction.
      Bundle arguments = new Bundle();
      arguments.putString( areaDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra( areaDetailFragment.ARG_ITEM_ID ) );
      areaDetailFragment fragment = new areaDetailFragment();
      fragment.setArguments( arguments );
      getSupportFragmentManager().beginTransaction().add( R.id.area_detail_container, fragment ).commit();
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v( TAG, "onPause..." );
    if( areaListActivity.btThread != null )
    {
      // gib Bescheid
      Log.v( TAG, "onPause: prepare stop btThread..." );
      areaListActivity.btThread.prepareStopThread();
    }
  }

  @Override
  public void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume..." );
    Log.v( TAG, "resume btThread..." );
    //
    // der Thread wacht auf (sollte eigentlich nur im single-pane
    // modus passieren)
    if( ( areaListActivity.btThread != null ) && ( areaListActivity.btThread.getState() != Thread.State.TERMINATED ) )
    {
      // es ist ein Thread am ackern
      areaListActivity.btThread.threadNotSpopping();
    }
    else
    {
      // erzeuge einen Thread
      Log.v( TAG, "onResume: starting btThread..." );
      areaListActivity.btThread = new BlueThoothCommThread();
      areaListActivity.btThread.start();
      Log.v( TAG, "onResume: starting btThread...OK" );
    }
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        // This ID represents the Home or Up button. In the case of this
        // activity, the Up button is shown. Use NavUtils to allow users
        // to navigate up one level in the application structure. For
        // more details, see the Navigation pattern on Android Design:
        //
        // http://developer.android.com/design/patterns/navigation.html#up-vs-back
        //
        Log.v( TAG, "onOptionsItemSelected: HOME" );
        NavUtils.navigateUpTo( this, new Intent( this, areaListActivity.class ) );
        return true;
    }
    return super.onOptionsItemSelected( item );
  }
}
