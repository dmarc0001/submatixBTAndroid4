package de.dmarcini.submatix.android4.utils;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.gui.FragmentCommonActivity;

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

  public ColorizedPreferenceCategory( Context context )
  {
    super( context );
    getTheme( context );
  }

  public ColorizedPreferenceCategory( Context context, AttributeSet attrs )
  {
    super( context, attrs );
    getTheme( context );
  }

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
   * @return Der Kategirietitel
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
