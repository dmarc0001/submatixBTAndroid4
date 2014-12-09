//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.submatix.android4.full.gui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dmarcini.submatix.android4.full.ApplicationDEBUG;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.utils.BuildVersion;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

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
  @SuppressWarnings( "javadoc" )
  public static final String TAG                     = ProgramAboutFragment.class.getSimpleName();
  private MainActivity       runningActivity         = null;
  private TextView           aboutVersionTextView    = null;
  private TextView           aboutProgrammerTextView = null;
  private TextView           aboutBuildTextView      = null;
  private TextView           aboutBuildDateTextView  = null;
  private String             fragmentTitle           = "unknown";

  @Override
  public void onActivityCreated( Bundle savedInstanceState )
  {
    super.onActivityCreated( savedInstanceState );
    runningActivity = ( MainActivity )getActivity();
    //
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
    //
    // den Titel in der Actionbar setzten
    // Aufruf via create
    //
    Bundle arguments = getArguments();
    if( arguments != null && arguments.containsKey( ProjectConst.ARG_ITEM_CONTENT ) )
    {
      fragmentTitle = arguments.getString( ProjectConst.ARG_ITEM_CONTENT );
      runningActivity.onSectionAttached( fragmentTitle );
    }
    else
    {
      Log.w( TAG, "onActivityCreated: TITLE NOT SET!" );
    }
    //
    // im Falle eines restaurierten Frames
    //
    if( savedInstanceState != null && savedInstanceState.containsKey( ProjectConst.ARG_ITEM_CONTENT ) )
    {
      fragmentTitle = savedInstanceState.getString( ProjectConst.ARG_ITEM_CONTENT );
      runningActivity.onSectionAttached( fragmentTitle );
    }
  }

  @Override
  public void onAttach( Activity activity )
  {
    super.onAttach( activity );
    runningActivity = ( MainActivity )activity;
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
    // View aus Resource laden
    //
    rootView = inflater.inflate( R.layout.fragment_about, container, false );
    return rootView;
  }

  @Override
  public void onSaveInstanceState( Bundle savedInstanceState )
  {
    super.onSaveInstanceState( savedInstanceState );
    fragmentTitle = savedInstanceState.getString( ProjectConst.ARG_ITEM_CONTENT );
    savedInstanceState.putString( ProjectConst.ARG_ITEM_CONTENT, fragmentTitle );
  }
}
