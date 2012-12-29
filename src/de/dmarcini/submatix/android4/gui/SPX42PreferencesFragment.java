package de.dmarcini.submatix.android4.gui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;
import de.dmarcini.submatix.android4.R;

public class SPX42PreferencesFragment extends PreferenceFragment
{
  private static final String TAG = SPX42PreferencesFragment.class.getSimpleName();

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
    Log.v( TAG, "onCreate: add Resouce id <" + R.xml.config_spx42_preference + ">..." );
    addPreferencesFromResource( R.xml.config_spx42_preference );
    Log.v( TAG, "onCreate: add Resouce...OK" );
    // wenn gewünscht:
    //
    // PreferenceManager.setDefaultValues(this, R.xml.config_spx42_preference, false)
  }

  @Override
  public boolean onOptionsItemSelected( MenuItem item )
  {
    switch ( item.getItemId() )
    {
      case android.R.id.home:
        Log.v( TAG, "onOptionsItemSelected: HOME" );
        Intent intent = new Intent( getActivity(), areaListActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity( intent );
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
