package de.dmarcini.submatix.android4.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.comm.BlueThoothCommThread;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.ProjectConst;

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
    String showId = null;
    ContentSwitcher.ProgItem mItem = null;
    Fragment fragment = null;
    int resourceId = R.id.area_detail_container;
    //
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate:..." );
    //
    // was soll ich anzeigen?
    //
    showId = getIntent().getStringExtra( ProjectConst.ARG_ITEM_ID );
    // gibt es eine ID?
    if( showId != null )
    {
      // argumente basteln
      Bundle arguments = new Bundle();
      arguments.putString( ProjectConst.ARG_ITEM_ID, getIntent().getStringExtra( ProjectConst.ARG_ITEM_ID ) );
      // Welcher Programmmenüpunkt war denn das?
      mItem = ContentSwitcher.progItemsMap.get( showId );
      // hab ich einen Eintrag vorrätig?
      if( mItem != null )
      {
        // ok, ich hab eine Id, jetzt guck mal wo das hinführt
        if( mItem.nId == R.string.progitem_connect )
        {
          Log.v( TAG, "create connect fragment..." );
          connectFragment conFragment = new connectFragment();
          fragment = conFragment;
          resourceId = 0;
          setContentView( R.layout.fragment_connect );
        }
        else if( mItem.nId == R.string.progitem_config )
        {
          Log.v( TAG, "create config fragment..." );
          configSPX42Fragment confFragment = new configSPX42Fragment();
          fragment = confFragment;
          resourceId = 0;
          setContentView( R.layout.fragment_spx42config );
        }
        else
        {
          Log.w( TAG, "Not programitem found for <" + showId + ">" );
          // Dann ist was faul, und ich zeig DUMMY
          areaDetailFragment dFragment = new areaDetailFragment();
          fragment = dFragment;
          setContentView( R.layout.activity_area_detail );
        }
      }
    }
    else
    {
      // Dann ist was faul, und ich zeig DUMMY
      Log.w( TAG, "Not showId found !" );
      areaDetailFragment dFragment = new areaDetailFragment();
      fragment = dFragment;
      setContentView( R.layout.activity_area_detail );
    }
    //
    // und nun die Seite aufrufen, welche auch immer
    //
    Log.v( TAG, "add transaction..." );
    getSupportFragmentManager().beginTransaction().add( resourceId, fragment ).commit();
    Log.v( TAG, "add transaction...OK" );
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
