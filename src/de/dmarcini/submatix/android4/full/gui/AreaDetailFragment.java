package de.dmarcini.submatix.android4.full.gui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.dmarcini.submatix.android4.full.R;

/**
 * 
 * Ein Fallback-Fragment, wenn die Activity keine spezielle Seite finden kann
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class AreaDetailFragment extends Fragment
{
  private static final String TAG         = AreaDetailFragment.class.getSimpleName();
  private View                rootView    = null;
  /**
   * The fragment argument representing the item ID that this fragment represents.
   */
  public static final String  ARG_ITEM_ID = "item_id";

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
   */
  public AreaDetailFragment()
  {}

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreate()..." );
    super.onCreate( savedInstanceState );
  }

  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreateView()..." );
    rootView = inflater.inflate( R.layout.activity_area_detail, container, false );
    // rootView = inflater.inflate( R.layout.fragment_dummy, container, false );
    return rootView;
  }
}
