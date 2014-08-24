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
package de.dmarcini.submatix.android4.full.utils;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dmarcini.submatix.android4.full.R;
import de.dmarcini.submatix.android4.full.gui.FragmentCommonActivity;

/**
 * 
 * Eigene, farbig markierte Preferenz Kategorie, erbt von PreferenceCategory
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public class ColorizedPreferenceCategory extends PreferenceCategory
{
  private int                 currStyle = R.style.AppDarkTheme;
  private static final String TAG       = ColorizedPreferenceCategory.class.getSimpleName();

  /**
   * Konstruktor mit Kontext
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 11.11.2013
   * 
   * @param context
   */
  public ColorizedPreferenceCategory( Context context )
  {
    super( context );
    getTheme( context );
  }

  /**
   * Konstruktor mit Context und Attibuten
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 11.11.2013
   * 
   * @param context
   * @param attrs
   */
  public ColorizedPreferenceCategory( Context context, AttributeSet attrs )
  {
    super( context, attrs );
    getTheme( context );
  }

  /**
   * Konstruktor mit Context und Attibuten und Style
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
   * 
   * Stand: 11.11.2013
   * 
   * @param context
   * @param attrs
   * @param defStyle
   */
  public ColorizedPreferenceCategory( Context context, AttributeSet attrs, int defStyle )
  {
    super( context, attrs, defStyle );
    getTheme( context );
  }

  /**
   * 
   * Das aktuelle Theme lesen
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 28.01.2013
   * 
   * @param context
   * @return
   */
  private int getTheme( Context context )
  {
    Log.v( TAG, "check for theme in app..." );
    currStyle = FragmentCommonActivity.getAppStyle();
    return( currStyle );
  }

  /**
   * We catch the view after its creation, and before the activity will use it, in order to make our changes
   * 
   * @param parent
   * @return Der Kategorietitel
   */
  @Override
  protected View onCreateView( ViewGroup parent )
  {
    // And it's just a TextView!
    TextView categoryTitle = ( TextView )super.onCreateView( parent );
    switch ( currStyle )
    {
      case R.style.AppDarkTheme:
        categoryTitle.setTextAppearance( this.getContext(), R.style.preferenceCategoryDark );
        categoryTitle.setBackgroundColor( getContext().getResources().getColor( R.color.preferenceCategoryDark_backgroundColor ) );
        break;
      case R.style.AppLightTheme:
        categoryTitle.setTextAppearance( this.getContext(), R.style.preferenceCategoryLight );
        categoryTitle.setBackgroundColor( getContext().getResources().getColor( R.color.preferenceCategoryLight_backgroundColor ) );
        break;
      default:
        Log.e( TAG, "Theme is UNKNOWN: " + currStyle );
    }
    categoryTitle.setMinHeight( 65 );
    categoryTitle.setPadding( 10, 32, 10, 4 );
    return categoryTitle;
  }
}
