package de.dmarcini.submatix.android4.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class configSPX42Fragment extends Fragment
{
  public static final String   TAG            = configSPX42Fragment.class.getSimpleName();
  private final DisplayMetrics displayMetrics = new DisplayMetrics();
  private View                 rootView       = null;

  /**
   * 
   * Der Konstruktor
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.11.2012
   */
  public configSPX42Fragment()
  {}

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    Log.v( TAG, "onCreate()..." );
  }

  /**
   * Wenn das View erzeugt wurde, noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    Log.v( TAG, "onCreateView()..." );
    // Höhe des Views feststellen
    getActivity().getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
    // Verbindungsseite ausgewählt
    Log.v( TAG, "item for connect device selected!" );
    rootView = makeConfigView( inflater, container );
    return rootView;
  }

  /**
   * Wenn das View wieder nach vorn kommt / reaktiviert wurde
   */
  @Override
  public synchronized void onResume()
  {
    super.onResume();
    Log.v( TAG, "onResume()..." );
  }

  /**
   * 
   * Die Anzeige der Verbunden/trennen Seite
   * 
   * Project: SubmatixBluethoothLoggerAndroid4Tablet Package: de.dmarcini.submatix.android4.gui
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 04.11.2012
   * @param inflater
   * @param container
   * @return
   */
  private View makeConfigView( LayoutInflater inflater, ViewGroup container )
  {
    View rootView;
    int height, width;
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_spx42config, container, false );
    // Dimensionen checken
    height = displayMetrics.heightPixels;
    width = displayMetrics.widthPixels;
    Log.v( TAG, String.format( "screen has height: %d pixels and width: %d pixels", height, width ) );
    // Objekte lokalisieren
    return( rootView );
  }
}
