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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.dmarcini.submatix.android4.full.BuildConfig;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.utils.ProjectConst;

/**
 * Eine Anzeige für Online View
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.gui
 *
 * @author Dirk Marciniak (dirk_marciniak@online.de)
 *         <p/>
 *         Stand: 15.12.2014
 */
public class OnlineHelpFragment extends Fragment
{
  @SuppressWarnings( "javadoc" )
  public static final String       TAG               = OnlineHelpFragment.class.getSimpleName();
  private             MainActivity runningActivity   = null;
  private             WebView      OnlineHelpWebView = null;
  private             String       fragmentTitle     = "unknown";
  private             String       onlineUrl         = "file:///android_asset/www/index_de.html";

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    runningActivity = ( MainActivity ) getActivity();
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onActivityCreated: ACTIVITY ATTACH");
    }
    try
    {
      OnlineHelpWebView = ( WebView ) runningActivity.findViewById(R.id.onlineHelpWebView);
      // Enable Javascript
      WebSettings webSettings = OnlineHelpWebView.getSettings();
      webSettings.setJavaScriptEnabled(true);
      OnlineHelpWebView.setWebViewClient(new WebViewClient());
      //TODO: WebViewClient filtern, nur submatix-Domains....
      OnlineHelpWebView.loadUrl(onlineUrl);
      // OnlineHelpWebView.loadUrl("http://beta.html5test.com/");
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "onActivityCreated: gui objects not allocated!");
    }
    //
    // den Titel in der Actionbar setzten
    // Aufruf via create
    //
    Bundle arguments = getArguments();
    if( arguments != null && arguments.containsKey(ProjectConst.ARG_ITEM_CONTENT) )
    {
      fragmentTitle = arguments.getString(ProjectConst.ARG_ITEM_CONTENT);
      runningActivity.onSectionAttached(fragmentTitle);
    }
    else
    {
      Log.w(TAG, "onActivityCreated: TITLE NOT SET!");
    }
    //
    // im Falle eines restaurierten Frames
    //
    if( savedInstanceState != null && savedInstanceState.containsKey(ProjectConst.ARG_ITEM_CONTENT) )
    {
      fragmentTitle = savedInstanceState.getString(ProjectConst.ARG_ITEM_CONTENT);
      runningActivity.onSectionAttached(fragmentTitle);
    }
  }

  public void setUrl(String urlStr)
  {
    onlineUrl = urlStr;
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    runningActivity = ( MainActivity ) activity;
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onAttach: ATTACH");
    }
  }

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreate...");
    }
  }

  /**
   * Wenn das View erzeugt wird (nach onCreate), noch ein paar Sachen erledigen
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView;
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreateView...");
    }
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e(TAG, "onCreateView: container is NULL ...");
      return (null);
    }
    //
    // View aus Resource laden
    //
    rootView = inflater.inflate(R.layout.fragment_online_help, container, false);
    return rootView;
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
    super.onSaveInstanceState(savedInstanceState);
    fragmentTitle = savedInstanceState.getString(ProjectConst.ARG_ITEM_CONTENT);
    savedInstanceState.putString(ProjectConst.ARG_ITEM_CONTENT, fragmentTitle);
  }

  //TODO: Backspace handhaben
//  #    @Override
//       public void onBackPressed() {
//  if(mWebView.canGoBack()) {
//    mWebView.goBack();
//  } else {
//    super.onBackPressed();
//  }
//}

}
