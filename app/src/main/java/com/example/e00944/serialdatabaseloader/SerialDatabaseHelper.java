package com.example.e00944.serialdatabaseloader;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Helper class for database operations. This class performs all database operations on a worker thread
 * which in turn maintains a queue for tasks. This prevents simultaneous access to database object from more
 * than one thread.
 */
public class SerialDatabaseHelper extends HandlerThread {

    Handler mainThreadHandler, workerThreadHandler;
    public static final int OPERATION_INSERT=1, OPERATION_QUERY=2, OPERATION_DELETE=3, OPERATION_UPDATE=4;
    public static final int INSERT_COMPLETED=5, QUERY_COMPLETED=6, DELETE_COMPLETED=7, UPDATE_COMPLETED=8;
    private SQLiteDatabase dataBase;
    private OnDatabaseOperationListener dbUpdateListener;


    /**
     * Sets the listener for database update notifications
     * @param dbUpdateListener
     */
    public void setDbUpdateListener(OnDatabaseOperationListener dbUpdateListener){
        this.dbUpdateListener = dbUpdateListener;
    }

    /**
     * Sets the database to use
     * @param dataBase
     */
    public void setDataBase(SQLiteDatabase dataBase){
        this.dataBase = dataBase;
    }


    public SerialDatabaseHelper(String name) {
        super(name);
        this.start();
        mainThreadHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case INSERT_COMPLETED:
                        if(SerialDatabaseHelper.this.dbUpdateListener != null)
                        SerialDatabaseHelper.this.dbUpdateListener.onDatabaseUpdated();
                        break;
                    case UPDATE_COMPLETED:
                        if(SerialDatabaseHelper.this.dbUpdateListener != null)
                        SerialDatabaseHelper.this.dbUpdateListener.onDatabaseUpdated();
                        break;
                    case QUERY_COMPLETED:
                        if(SerialDatabaseHelper.this.dbUpdateListener != null)
                        SerialDatabaseHelper.this.dbUpdateListener.onQueryCompleted((Cursor)msg.obj);
                        break;
                    case DELETE_COMPLETED:
                        if(SerialDatabaseHelper.this.dbUpdateListener != null)
                        SerialDatabaseHelper.this.dbUpdateListener.onDatabaseUpdated();
                        break;
                }
            }
        };
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        workerThreadHandler = new Handler(getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case OPERATION_INSERT:
                        insertInDatabase(msg);
                        break;
                    case OPERATION_UPDATE:
                        break;
                    case OPERATION_QUERY:
                        queryFromDatabase(msg);
                        break;
                    case OPERATION_DELETE:
                        break;
                }
            }
        };
    }

    /**
     * Convinient method to insert values in table. Operation will be performed on worker thread
     * so that UI thread remains interactive. When the operation is successful, client gets notified
     * via overridden onDatabaseUpdated()method that is invoked on UI thread so that UI can be updated.
     * @param table
     * @param values
     */
    public void insert(String table, ContentValues values){
        Message msg = Message.obtain();
        InsertDataHolder holder = new InsertDataHolder(table, values);
        msg.obj = holder;
        msg.what = OPERATION_INSERT;
        workerThreadHandler.sendMessage(msg);
    }

    /**
     * Actually insert the data in table.
     * @param msg
     */
    private void insertInDatabase(Message msg){
        if(dataBase != null && msg != null){
            try {
                InsertDataHolder holder = (InsertDataHolder) msg.obj;
                dataBase.insert(holder.table, null, holder.values);
                mainThreadHandler.sendEmptyMessage(INSERT_COMPLETED);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    /**
     * Convinient method to fetch data from the table. Operation will be performed on worker thread
     * so that UI thread remains interactive. When the operation is successful, client gets notified
     * via overridden onQueryCompleted(Cursor cursor)method that is invoked on UI thread so that UI can be updated.
     * @param sqlQuery
     * @param selectionArgs
     */
    public void query(String sqlQuery, String[] selectionArgs){
        Message msg = Message.obtain();
        QueryDataHolder holder = new QueryDataHolder(sqlQuery, selectionArgs);
        msg.obj = holder;
        msg.what = OPERATION_QUERY;
        workerThreadHandler.sendMessage(msg);
    }

    /**
     * Actually query the database.
     * @param msg
     */
    private void queryFromDatabase(Message msg){
        if(dataBase != null && msg != null){
            try {
                QueryDataHolder holder =  (QueryDataHolder)msg.obj;
                Cursor cursor = dataBase.rawQuery(holder.sqlQuery, holder.selectionArgs);
                Message m = Message.obtain();
                m.what = QUERY_COMPLETED;
                m.obj = cursor;
                mainThreadHandler.sendMessage(m);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the database in use.
     */
    public void close(){
        if(dataBase != null){
            dataBase.close();
        }
    }


    /**
     * interface that client code implements to get notified of database update
     */
    public interface OnDatabaseOperationListener{
        /**
         * Called when the database has been modified, either via insert, update or delete
         * operation.
         */
        public void onDatabaseUpdated();

        /**
         * Called when the sql query is successfully executed. The Cursor corresponding to the
         * sql is supplied as parameter
         * @param cursor
         */
        public void onQueryCompleted(Cursor cursor);
    }

    /**
     * Helper class to encapsulate the data required for the INSERT operation on
     * SQLiteDatabase
     */
    public class InsertDataHolder{
        public String table;
        public ContentValues values;
        public InsertDataHolder(String table, ContentValues values){
            this.table = table;
            this.values = values;
        }

    }

    /**
     * Helper class to encapsulate the data required for the SQL QUERY on
     * SQLiteDatabase
     */
    public class QueryDataHolder{
        String sqlQuery;
        String[] selectionArgs;
        public QueryDataHolder(String sqlQuery, String[] selectionArgs){
            this.sqlQuery = sqlQuery;
            this.selectionArgs = selectionArgs;
        }

    }
}
