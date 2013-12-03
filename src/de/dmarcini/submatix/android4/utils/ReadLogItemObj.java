package de.dmarcini.submatix.android4.utils;

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
public class ReadLogItemObj
{
  @SuppressWarnings( "javadoc" )
  public boolean isSaved;
  @SuppressWarnings( "javadoc" )
  public String  itemName;
  @SuppressWarnings( "javadoc" )
  public String  itemNameOnSPX;
  @SuppressWarnings( "javadoc" )
  public String  itemDetail;
  @SuppressWarnings( "javadoc" )
  public int     dbId;
  @SuppressWarnings( "javadoc" )
  public int     numberOnSPX;
  @SuppressWarnings( "javadoc" )
  public long    startTimeMilis;
  @SuppressWarnings( "javadoc" )
  public boolean isMarked = false;

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
    this.dbId = -1;
    this.numberOnSPX = -1;
    this.startTimeMilis = 0L;
  }

  /**
   * 
   * Konstruktor für einen Loglist Eintrag mit Datenbankid
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
   */
  public ReadLogItemObj( final boolean _isSaved, final String _itemName, final String _itemNameOnSpx, final String _itemDetail, final int _dbid )
  {
    this.isSaved = _isSaved;
    this.itemName = _itemName;
    this.itemNameOnSPX = _itemNameOnSpx;
    this.itemDetail = _itemDetail;
    this.dbId = _dbid;
    this.numberOnSPX = -1;
    this.startTimeMilis = 0L;
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
    this.startTimeMilis = 0L;
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

  @SuppressWarnings( "unused" )
  private ReadLogItemObj()
  {}
}
