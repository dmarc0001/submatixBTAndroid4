package de.dmarcini.submatix.android4.utils;

public class ReadLogItemObj
{
  public boolean isSaved;
  public String  itemName;
  public String  itemDetail;

  public ReadLogItemObj( final boolean _isSaved, final String _itemName, final String _itemDetail )
  {
    this.isSaved = _isSaved;
    this.itemName = _itemName;
    this.itemDetail = _itemDetail;
  }

  private ReadLogItemObj()
  {}
}
