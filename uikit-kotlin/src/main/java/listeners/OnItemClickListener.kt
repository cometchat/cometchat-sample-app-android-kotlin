package listeners

/*

* Purpose - <code>OnItemClickListener&lt;T&gt;</code> is an Generic class used to provide methods
 like <code>OnItemClick(T var,int position)</code> & <code>OnItemLongClick(T var,int position)</code>

* Created on - 20th December 2019

* Modified on  - 16th January 2020

*/
abstract class OnItemClickListener<T> {
    abstract fun OnItemClick(t: Any, position: Int)
    fun OnItemLongClick(t: Any, position: Int) {}
}