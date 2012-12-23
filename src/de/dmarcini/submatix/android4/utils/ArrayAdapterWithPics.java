/**
 * Eigener Arrayadapter, der Icons beinhaltet
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 23.12.2012
 */
package de.dmarcini.submatix.android4.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import de.dmarcini.submatix.android4.R;
import de.dmarcini.submatix.android4.content.ContentSwitcher;

/**
 * Das Objekt leitet sich vom ArrayAdapter ab
 * 
 * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 23.12.2012
 */
public class ArrayAdapterWithPics extends ArrayAdapter<ContentSwitcher.ProgItem>
{
  private static final String TAG = ArrayAdapterWithPics.class.getSimpleName();

  /**
   * 
   * Mein Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 23.12.2012
   * @param context
   * @param textViewResourceId
   * @param objects
   */
  public ArrayAdapterWithPics( Context context, int textViewResourceId, List<ContentSwitcher.ProgItem> objects )
  {
    super( context, textViewResourceId, objects );
  }

  /* private view holder class */
  private class ViewHolder
  {
    ImageView imageView;
    TextView  txtTitle;
  }

  @Override
  public View getView( int position, View convertView, ViewGroup parent )
  {
    ViewHolder holder = null;
    ContentSwitcher.ProgItem progItem = getItem( position );
    LayoutInflater mInflater = ( LayoutInflater )getContext().getSystemService( Activity.LAYOUT_INFLATER_SERVICE );
    if( convertView == null )
    {
      convertView = mInflater.inflate( R.layout.array_with_pic_adapter_view, null );
      holder = new ViewHolder();
      holder.txtTitle = ( TextView )convertView.findViewById( R.id.arrayListTextView );
      holder.imageView = ( ImageView )convertView.findViewById( R.id.arrayAdapterIconView );
      convertView.setTag( holder );
    }
    else
    {
      holder = ( ViewHolder )convertView.getTag();
    }
    holder.txtTitle.setText( progItem.content );
    holder.imageView.setImageResource( progItem.resId );
    if( parent instanceof ListView )
    {
      ListView pView = ( ListView )parent;
      if( pView.getCheckedItemPosition() == position )
      {
        int bg = pView.getContext().getApplicationContext().getResources().getColor( android.R.color.holo_blue_bright );
        convertView.setBackgroundColor( bg );
        Log.i( TAG, "this is SELECTED!" );
      }
      else
      {
        int bg = pView.getContext().getApplicationContext().getResources().getColor( android.R.color.background_dark );
        convertView.setBackgroundColor( bg );
      }
    }
    return convertView;
  }
}
