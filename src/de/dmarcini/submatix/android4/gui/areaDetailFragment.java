/**
 * Dieses Fraqgment wird nur erzeugt udn aufgerufen, wenn die Hauptactivity keine spezielle Seite finden kann. Es ist also eine Fallback-Seite, die hoffentlich im Normalbetrieb
 * nicht angezeigt wird
 */
package de.dmarcini.submatix.android4.gui;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.dmarcini.submatix.android4.R;

/**
 * A fragment representing a single area detail screen. This fragment is either contained in a {@link areaListActivity} in two-pane mode (on tablets) or a
 * {@link areaDetailActivity} on handsets.
 */
public class areaDetailFragment extends Fragment
{
  private static final String TAG         = areaDetailFragment.class.getSimpleName();
  private View                rootView    = null;
  /**
   * The fragment argument representing the item ID that this fragment represents.
   */
  public static final String  ARG_ITEM_ID = "item_id";

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
   */
  public areaDetailFragment()
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
