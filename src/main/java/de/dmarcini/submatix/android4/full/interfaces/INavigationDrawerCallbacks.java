package de.dmarcini.submatix.android4.full.interfaces;

import de.dmarcini.submatix.android4.full.content.ContentSwitcher;

/**
 * Callbacks interface that all activities using this fragment must implement.
 */
public interface INavigationDrawerCallbacks
{
  /**
   * Called when an item in the navigation drawer is selected.
   *
   * @param pItem Der gewählte Programmeintrag mit allen Parametern
   */
  void onNavigationDrawerItemSelected(ContentSwitcher.ProgItem pItem);
}
