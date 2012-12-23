/**
 * Activity für Details des Projektes
 * 
 * Wird ausgeführt, wenn bei kleinen Schirmen (Smartphones) eine Option des Menüs ausgewählt wurde. Bei grossen Schirmen wird diese Activity nicht ausgeführt Der Gemeisame Code für
 * beide Version ist im Parentobjekt combienedFragmentActivity
 */
package de.dmarcini.submatix.android4.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;
import de.dmarcini.submatix.android4.utils.ProjectConst;

/**
 * An activity representing a single area detail screen. This activity is only used on handset devices. On tablet-size devices, item details are presented side-by-side with a list
 * of items in a {@link areaListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link areaDetailFragment}.
 */
public class areaDetailActivity extends combinedFragmentActivity
{
  private static final String TAG = areaDetailActivity.class.getSimpleName();

  //
  //
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    String showId = null;
    ContentSwitcher.ProgItem mItem = null;
    Fragment fragment = null;
    int resourceId = 0;
    //
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate:..." );
    // Aktiviere Zurückfunktion via Actionbsar Home
    getActionBar().setDisplayHomeAsUpEnabled( true );
    //
    // was soll ich anzeigen?
    //
    showId = getIntent().getStringExtra( ProjectConst.ARG_ITEM_ID );
    // gibt es eine ID?
    if( showId != null )
    {
      Log.v( TAG, "SowId found: <" + showId + ">" );
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
          getActionBar().setTitle( R.string.connect_headline );
          getActionBar().setLogo( R.drawable.bluetooth_icon_color );
        }
        else if( mItem.nId == R.string.progitem_config )
        {
          Log.v( TAG, "create config fragment..." );
          configSPX42Fragment confFragment = new configSPX42Fragment();
          fragment = confFragment;
          resourceId = 0;
          setContentView( R.layout.fragment_spx42config );
          getActionBar().setTitle( R.string.config_headline );
        }
        else
        {
          Log.w( TAG, "Not programitem found for <" + showId + ">" );
          // Dann ist was faul, und ich zeig DUMMY
          areaDetailFragment dFragment = new areaDetailFragment();
          resourceId = R.id.area_detail_container;
          fragment = dFragment;
          setContentView( R.layout.activity_area_detail );
          getActionBar().setTitle( R.string.dummy_headline );
        }
      }
    }
    else
    {
      // Dann ist was faul, und ich zeig DUMMY
      Log.w( TAG, "Not showId found, show DUMMY !" );
      areaDetailFragment dFragment = new areaDetailFragment();
      resourceId = R.id.area_detail_container;
      fragment = dFragment;
      setContentView( R.layout.activity_area_detail );
      getActionBar().setTitle( R.string.dummy_headline );
    }
    //
    // und nun die Seite aufrufen, welche auch immer
    //
    // Log.v( TAG, "add transaction..." );
    Log.v( TAG, "getSupportFragmentManager..." );
    FragmentManager fm = getSupportFragmentManager();
    Log.v( TAG, "beginTransaction..." );
    FragmentTransaction ft = fm.beginTransaction();
    Log.v( TAG, "add(resourceId,fragment) ..." );
    ft.add( resourceId, fragment );
    Log.v( TAG, "commit..." );
    ft.commit();
    // getSupportFragmentManager().beginTransaction().add( resourceId, fragment ).commit();
    Log.v( TAG, "add transaction...OK" );
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
