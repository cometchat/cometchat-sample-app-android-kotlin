package listeners;

import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;

/*

* Purpose - <code>OnItemClickListener&lt;T&gt;</code> is an Generic class used to provide methods
 like <code>OnItemClick(T var,int position)</code> & <code>OnItemLongClick(T var,int position)</code>

* Created on - 20th December 2019

* Modified on  - 16th January 2020

*/

public abstract class OnItemClickListener<T> {

    public abstract void OnItemClick(T var, int position);

    public void OnItemLongClick(T var,int position) {

    }
}
