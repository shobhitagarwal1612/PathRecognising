package pathrecgnisingapp.silive.in.myapplication;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;

public class DB {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "long";

    private static final String DATABASE_NAME = "Map";
    private static final String DATABASE_TABLE = "Co_ordinates";
    private static final int DATABASE_VERSION = 1;

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub//
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROWID
                    + " INTEGER PRIMARY KEY, " + KEY_LATITUDE
                    + " DOUBLE NOT NULL, " + KEY_LONGITUDE
                    + " DOUBLE NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }

    }

    public DB(Context c) {
        ourContext = c;
    }
public void upgradeDatabase(){
    ourDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
    ourDatabase.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROWID
            + " INTEGER PRIMARY KEY, " + KEY_LATITUDE
            + " DOUBLE NOT NULL, " + KEY_LONGITUDE
            + " DOUBLE NOT NULL);");


}
    public DB open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
    }

    public long createEntry(double x2, double y2) {
        // TODO Auto-generated method stub
        ContentValues cv = new ContentValues();
        cv.put(KEY_LATITUDE, x2);
        cv.put(KEY_LONGITUDE, y2);
        return ourDatabase.insert(DATABASE_TABLE, null, cv);
    }

    public String getData() {
        // TODO Auto-generated method stub
        String[] columns = new String[] { KEY_ROWID, KEY_LATITUDE,
                KEY_LONGITUDE };
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null,
                null, null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROWID);
        int iLat = c.getColumnIndex(KEY_LATITUDE);
        int iLong = c.getColumnIndex(KEY_LONGITUDE);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            result = result + c.getDouble(iRow) + "\n" + c.getDouble(iLat)
                    + "			 " + c.getDouble(iLong) + " \n";
        }
        return result;
    }


}
