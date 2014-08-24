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

/**
 * 
 * Klasse die einen einzelnen Logeintrag vom SPX darstellt/kapselt
 * 
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 * 
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
@SuppressWarnings( "javadoc" )
public class ReadLogItemObj
{
  public boolean isSaved        = false;
  public String  itemName       = null;
  public String  itemNameOnSPX  = null;
  public String  itemDetail     = null;
  public int     dbId           = -1;
  public int     numberOnSPX    = -1;
  public long    startTimeMilis = -1;
  public boolean isMarked       = false;
  public int     tagId          = -1;
  public String  fileOnMobile   = null;
  public float   firstTemp      = 0.0F;
  public float   lowTemp        = 0.0F;
  public int     maxDepth       = -1;
  public int     countSamples   = 0;
  public int     diveLen        = 0;
  public String  units          = "m";
  public String  notes          = null;
  public String  geoLon         = null;
  public Object  geoLat         = null;

  /**
   * 
   * Konstruktor für einen Loglist Eintrag
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 06.08.2013
   * 
   * @param _isSaved
   * @param _itemName
   * @param _itemNameOnSpx
   * @param _itemDetail
   */
  public ReadLogItemObj( final boolean _isSaved, final String _itemName, final String _itemNameOnSpx, final String _itemDetail )
  {
    this.isSaved = _isSaved;
    this.itemName = _itemName;
    this.itemNameOnSPX = _itemNameOnSpx;
    this.itemDetail = _itemDetail;
  }

  /**
   * 
   * Konstruktor für einen Loglist Eintrag mit Datenbankid
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 06.01.2014
   * 
   * @param _isSaved
   * @param _itemName
   * @param _itemNameOnSpx
   * @param _itemDetail
   * @param _dbid
   */
  public ReadLogItemObj( final boolean _isSaved, final String _itemName, final String _itemNameOnSpx, final String _itemDetail, final int _dbid )
  {
    this.isSaved = _isSaved;
    this.itemName = _itemName;
    this.itemNameOnSPX = _itemNameOnSpx;
    this.itemDetail = _itemDetail;
    this.dbId = _dbid;
  }

  /**
   * 
   * Konstruktor für einen Loglist Eintrag mit Datenbankid und Numer auf SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 06.08.2013
   * 
   * @param _isSaved
   * @param _itemName
   * @param _itemNameOnSpx
   * @param _itemDetail
   * @param _dbid
   * @param _numOnSpx
   */
  public ReadLogItemObj( final boolean _isSaved, final String _itemName, final String _itemNameOnSpx, final String _itemDetail, final int _dbid, final int _numOnSpx )
  {
    this.isSaved = _isSaved;
    this.itemName = _itemName;
    this.itemNameOnSPX = _itemNameOnSpx;
    this.itemDetail = _itemDetail;
    this.dbId = _dbid;
    this.numberOnSPX = _numOnSpx;
  }

  /**
   * 
   * Konstruktor mit noch mehr Parametern
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * 
   * Stand: 19.08.2013
   * 
   * @param _isSaved
   * @param _itemName
   * @param _itemNameOnSpx
   * @param _itemDetail
   * @param _dbid
   * @param _numOnSpx
   * @param _milis
   */
  public ReadLogItemObj( final boolean _isSaved, final String _itemName, final String _itemNameOnSpx, final String _itemDetail, final int _dbid, final int _numOnSpx, long _milis )
  {
    this.isSaved = _isSaved;
    this.itemName = _itemName;
    this.itemNameOnSPX = _itemNameOnSpx;
    this.itemDetail = _itemDetail;
    this.dbId = _dbid;
    this.numberOnSPX = _numOnSpx;
    this.startTimeMilis = _milis;
  }

  /**
   * 
   * Der Nackte Konstruktor
   * 
   * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.full.utils
   * 
   * Stand: 08.01.2014
   */
  public ReadLogItemObj()
  {}
}
