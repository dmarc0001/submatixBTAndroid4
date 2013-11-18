package de.dmarcini.submatix.android4.gui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dmarcini.submatix.android4.ApplicationDEBUG;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.utils.BuildVersion;

/**
 * 
 * Eine "Über das Programm" Seite
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class ProgramAboutFragment extends Fragment
{
  public static final String TAG                     = ProgramAboutFragment.class.getSimpleName();
  private Activity           runningActivity         = null;
  private TextView           aboutVersionTextView    = null;
  private TextView           aboutProgrammerTextView = null;
  private TextView           aboutBuildTextView      = null;
  private TextView           aboutBuildDateTextView  = null;

  @Override
  public void onActivityCreated( Bundle bundle )
  {
    super.onActivityCreated( bundle );
    runningActivity = getActivity();
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onActivityCreated: ACTIVITY ATTACH" );
    try
    {
      aboutVersionTextView = ( TextView )runningActivity.findViewById( R.id.aboutVersionTextView );
      aboutProgrammerTextView = ( TextView )runningActivity.findViewById( R.id.aboutProgrammerTextView );
      aboutBuildTextView = ( TextView )runningActivity.findViewById( R.id.aboutBuildTextView );
      aboutBuildDateTextView = ( TextView )runningActivity.findViewById( R.id.aboutBuildDateTextView );
      aboutVersionTextView.setText( String.format( "%s: %s", runningActivity.getResources().getString( R.string.app_version_prefix ), BuildVersion.getVersion() ) );
      aboutProgrammerTextView.setText( runningActivity.getResources().getString( R.string.app_programmer_name ) );
      aboutBuildTextView.setText( String.format( "%s: %s", runningActivity.getResources().getString( R.string.app_build_prefix ), BuildVersion.getBuildAsString() ) );
      aboutBuildDateTextView.setText( String.format( "%s: %s", runningActivity.getResources().getString( R.string.app_build_date_prefix ), BuildVersion.getdefaukltDateString() ) );
    }
    catch( NullPointerException ex )
    {
      Log.e( TAG, "onActivityCreated: gui objects not allocated!" );
    }
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = activity;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onAttach: ATTACH" );
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreate..." );
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
  {
    View rootView;
    if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView..." );
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e( TAG, "onCreateView: container is NULL ..." );
      return( null );
    }
    //
    // wenn die laufende Activity eine AreaDetailActivity ist, dann gibts das View schon
    //
    if( runningActivity instanceof AreaDetailActivity )
    {
      if( ApplicationDEBUG.DEBUG ) Log.d( TAG, "onCreateView: running from AreaDetailActivity ..." );
      return( null );
    }
    //
    // Verbindungsseite via twoPane ausgewählt
    //
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_about, container, false );
    return rootView;
  }
}
