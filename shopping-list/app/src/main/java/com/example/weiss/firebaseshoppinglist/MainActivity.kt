package com.example.weiss.firebaseshoppinglist

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_main.*

lateinit var mDatabase: DatabaseReference
var shopList: MutableList<ShopItem>? = null
lateinit var adapter: ShopListAdapter
private var listViewItems: ListView? = null

class MainActivity : AppCompatActivity(), ItemRowListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //reference for FAB
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        listViewItems = findViewById<View>(R.id.items_list) as ListView

        //Adding click listener for FAB
        fab.setOnClickListener { view ->
            //Show Dialog here to add new Item
            addNewItemDialog()
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.orderByKey().addValueEventListener(itemListener)
        shopList = mutableListOf<ShopItem>()
        adapter = ShopListAdapter(this, shopList!!)
        listViewItems!!.setAdapter(adapter)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("done").setValue(isDone);
    }

    //delete an item
    override fun onItemDelete(itemObjectId: String) {
        //get child reference in database via the ObjectID
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        //deletion can be done via removeValue() method
        itemReference.removeValue()
    }

    /**
     * This method will show a dialog bix where user can enter new item
     * to be added
     */
    private fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)
        alert.setMessage("Add New Item")
        alert.setTitle("Enter To Do Item Text")
        alert.setView(itemEditText)
        alert.setPositiveButton("Submit") { dialog, positiveButton ->
            val shopItem = ShopItem.create()
            shopItem.itemText = itemEditText.text.toString()
            shopItem.done = false
            //We first make a push so that a new item is made with a unique ID
            val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()
            shopItem.objectId = newItem.key
            //then, we used the reference to set the value on that ID
            newItem.setValue(shopItem)
            dialog.dismiss()
            Toast.makeText(this, "Item saved with ID " + shopItem.objectId, Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }

    var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        val items = dataSnapshot.children.iterator()
        shopList!!.clear();
        //Check if current database contains any collection
        if (items.hasNext()) {
            val shopListindex = items.next()
            val itemsIterator = shopListindex.children.iterator()

            //check if the collection has any to do items or not
            while (itemsIterator.hasNext()) {
                //get current item
                val currentItem = itemsIterator.next()
                val shopItem = ShopItem.create()
                //get current data in a map
                val map = currentItem.getValue() as HashMap<String, Any>
                //key will return Firebase ID
                shopItem.objectId = currentItem.key
                shopItem.done = map.get("done") as Boolean?
                shopItem.itemText = map.get("itemText") as String?
                shopList!!.add(shopItem);
            }
        }
        //alert adapter that has changed
        adapter.notifyDataSetChanged()
    }
}
