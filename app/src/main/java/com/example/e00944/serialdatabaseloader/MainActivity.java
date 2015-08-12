package com.example.e00944.serialdatabaseloader;

import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements SerialDatabaseHelper.OnDatabaseOperationListener {

    DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHandler(this);
        db.setListener(this);

        //add some contacts
        for(int i=0; i<=5; i++){
            db.addContact("Contact"+i,""+i);
        }
        //query added contacts
        db.getContact();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(db!=null)db.close();
    }

    @Override
    public void onDatabaseUpdated() {
        Toast.makeText(this, "onDatabaseUpdated", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onQueryCompleted(Cursor cursor) {
        Toast.makeText(this, "onQueryCompleted", Toast.LENGTH_LONG).show();
        cursor.moveToFirst();
        do{
            //this is just for demonstration, actual implementation will store read values
            // in array or something.
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_NAME));
        }while(cursor.moveToNext());

        cursor.close();
    }
}
