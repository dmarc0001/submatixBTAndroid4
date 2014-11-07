package de.dmarcini.submatix.android4.full.interfaces;

/**
 * Callbacks interface that all activities using this fragment must implement.
 */
public interface INavigationDrawerCallbacks
{
  /**
   * Called when an item in the navigation drawer is selected.
   * 
   * @param position
   */
  void onNavigationDrawerItemSelected( int position );
}
