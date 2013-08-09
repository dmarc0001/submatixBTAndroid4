package de.dmarcini.submatix.android4.utils;

public class ReadLogItemObj
{
  public boolean isSaved;
  public String  itemName;
  public String  itemNameOnSPX;
  public String  itemDetail;
  public int     dbId;
  public int     numberOnSPX;
  public boolean isMarked = false;

  /**
   * 
   * Konstruktor für einen Loglist Eintrag
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
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
    this.dbId = -1;
    this.numberOnSPX = -1;
  }

  /**
   * 
   * Konstruktor für einen Loglist Eintrag mit Datenbankid
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
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
    this.numberOnSPX = -1;
  }

  /**
   * 
   * Konstruktor für einen Loglist Eintrag mit Datenbankid und Numer auf SPX
   * 
   * Project: SubmatixBTLoggerAndroid_4 Package: de.dmarcini.submatix.android4.utils
   * 
   * @author Dirk Marciniak (dirk_marciniak@arcor.de)
   * 
   *         Stand: 06.08.2013
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

  @SuppressWarnings( "unused" )
  private ReadLogItemObj()
  {}
}
