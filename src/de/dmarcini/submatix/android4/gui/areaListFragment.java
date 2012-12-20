package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.dmarcini.submatix.android4.content.ContentSwitcher;

/**
 * A list fragment representing a list of areas. This fragment also supports tablet devices by allowing list items to be given an 'activated' state upon selection. This helps
 * indicate which item is currently being viewed in a {@link areaDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class areaListFragment extends ListFragment
{
  private static final String TAG                      = areaListFragment.class.getSimpleName();
  private static final String STATE_ACTIVATED_POSITION = "activated_position";
  private Callbacks           mCallbacks               = sDummyCallbacks;
  private int                 mActivatedPosition       = ListView.INVALID_POSITION;

  public interface Callbacks
  {
    /**
     * Callback for when an item has been selected.
     * 
     * @param id
     */
    public void onItemSelected( String id );
  }

  /**
   * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when this fragment is not attached to an activity.
   */
  private static Callbacks sDummyCallbacks = new Callbacks() {
                                             @Override
                                             public void onItemSelected( String id )
                                             {}
                                           };

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
   */
  public areaListFragment()
  {}

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    //
    // TODO:
    // hier läßt sich am Listadapter mit Sicherheit noch was schickeres anfangen
    //
    setListAdapter( new ArrayAdapter<ContentSwitcher.ProgItem>( getActivity(), android.R.layout.simple_list_item_activated_1, android.R.id.text1, ContentSwitcher.progItems ) );
  }

  @Override
  public void onViewCreated( View view, Bundle savedInstanceState )
  {
    super.onViewCreated( view, savedInstanceState );
    // Restore the previously serialized activated item position.
    if( savedInstanceState != null && savedInstanceState.containsKey( STATE_ACTIVATED_POSITION ) )
    {
      setActivatedPosition( savedInstanceState.getInt( STATE_ACTIVATED_POSITION ) );
    }
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    // Activities containing this fragment must implement its callbacks.
    if( !( activity instanceof Callbacks ) )
    {
      throw new IllegalStateException( "Activity must implement fragment's callbacks." );
    }
    mCallbacks = ( Callbacks )activity;
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    // Reset the active callbacks interface to the dummy implementation.
    mCallbacks = sDummyCallbacks;
  }

  @Override
  public void onListItemClick( ListView listView, View view, int position, long id )
  {
    super.onListItemClick( listView, view, position, id );
    Log.v( TAG, "onListItemClick()..." );
    if( ContentSwitcher.progItems.get( position ).nId == R.string.progitem_exit )
    {
      Log.v( TAG, "make dialog for USER..." );
      AreYouSureDialogFragment sureDial = new AreYouSureDialogFragment( getString( R.string.dialog_sure_exit ) );
      sureDial.show( getActivity().getFragmentManager(), "programexit" );
      return;
    }
    mCallbacks.onItemSelected( ContentSwitcher.progItems.get( position ).sId );
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
   * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when touched.
   */
  public void setActivateOnItemClick( boolean activateOnItemClick )
  {
    // When setting CHOICE_MODE_SINGLE, ListView will automatically
    // give items the 'activated' state when touched.
    getListView().setChoiceMode( activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE );
  }

  private void setActivatedPosition( int position )
  {
    if( position == ListView.INVALID_POSITION )
    {
      getListView().setItemChecked( mActivatedPosition, false );
    }
    else
    {
      getListView().setItemChecked( position, true );
    }
    mActivatedPosition = position;
  }
}
