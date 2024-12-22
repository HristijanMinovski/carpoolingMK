package com.example.workshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;  // Define the version
    private static final String DATABASE_NAME = "carpoolmk.db";  // Define the database name
    private Context context;

    // Constructor
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_USERS =
                "CREATE TABLE Users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "password TEXT NOT NULL, " +
                        "username TEXT NOT NULL UNIQUE," +
                        "user_type TEXT," +
                        "email TEXT NOT NULL UNIQUE," +
                        "rating_client FLOAT DEFAULT 0.0," +
                        "rating_driver FLOAT DEFAULT 0.0," +
                        "number_of_drives INTEGER DEFAULT 0," +
                        "number_of_trips INTEGER DEFAULT 0)";
        String CREATE_VEHICLES_TABLE =
                "CREATE TABLE Vehicles (" +
                        "vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "driver_id INTEGER NOT NULL UNIQUE, " +
                        "vehicle_name TEXT NOT NULL, " +
                        "is_active INTEGER DEFAULT 0, " + // 0 = inactive, 1 = active
                        "year INTEGER, " +
                        "FOREIGN KEY(driver_id) REFERENCES Users(id) ON DELETE CASCADE)";

        String CREATE_RIDES_TABLE =
                "CREATE TABLE ScheduledRides (" +
                        "ride_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "vehicle_id INTEGER NOT NULL, " +
                        "driver_id INTEGER NOT NULL UNIQUE, " +
                        "start_time TEXT NOT NULL, " +
                        "start_location TEXT NOT NULL, " +
                        "end_location TEXT NOT NULL, " +
                        "start_lat REAL, " +
                        "start_lng REAL, " +
                        "end_lat REAL, " +
                        "end_lng REAL, " +
                        "client_list TEXT, " +  // Could store as JSON or comma-separated IDs
                        "available_space INTEGER NOT NULL DEFAULT 4, " +
                        "ride_per_person INTEGER NOT NULL, " +
                        "FOREIGN KEY(vehicle_id) REFERENCES Vehicles(vehicle_id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(driver_id) REFERENCES Users(id) ON DELETE CASCADE)";
        String CREATE_RIDES_HISTORY_TABLE =
                "CREATE TABLE RidesHistory (" +
                        "ride_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "ocenuvac INTEGER NOT NULL, " +
                        "ocenet INTEGER NOT NULL, " +
                        "rating INTEGER NOT NULL, " +
                        "dali_oceneto INTEGER NOT NULL," +
                        "tip_user TEXT NOT NULL, "+
                        "FOREIGN KEY(ocenuvac) REFERENCES Users(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(ocenet) REFERENCES Users(id) ON DELETE CASCADE)";
        db.execSQL(CREATE_RIDES_HISTORY_TABLE);
        db.execSQL(CREATE_RIDES_TABLE);
        db.execSQL(CREATE_VEHICLES_TABLE);
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Vehicles");
        db.execSQL("DROP TABLE IF EXISTS ScheduledRides");
        db.execSQL("DROP TABLE IF EXISTS RidesHistory");
        onCreate(db);
    }

    public int checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        int isValid = 0;
        if (cursor != null && cursor.moveToFirst()) {
            isValid = 1;
        }

        if (cursor != null) {
            cursor.close();
        }

        db.close();
        return isValid;
    }

    public boolean checkUserUnique(String username, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM Users WHERE username = ? AND email = ?";

        Cursor cursor = db.rawQuery(query, new String[]{username, email});

        boolean isUnique = !cursor.moveToFirst(); // Returns true if no rows found
        cursor.close();
        return isUnique;
    }

    public boolean updateRating(String username, float newRating, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            // Прво проверуваме дали постои корисникот во базата
            String query = "SELECT * FROM Users WHERE username=?";
            cursor = db.rawQuery(query, new String[]{username});

            if (cursor != null && cursor.moveToFirst()) {
                // Земање на тековниот рејтинг и бројот на патувања/возења
                float currentRating;
                int count;
                ContentValues values = new ContentValues();

                if (type.equals("client")) { // Ако е клиент
                    currentRating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating_client"));
                    count = cursor.getInt(cursor.getColumnIndexOrThrow("number_of_trips"));

                    // Пресметка на нов рејтинг
                    float updatedRating = (currentRating * count + newRating) / (count + 1);
                    values.put("rating_client", updatedRating);
                    values.put("number_of_trips", count + 1);

                } else if (type.equals("driver")) { // Ако е возач
                    currentRating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating_driver"));
                    count = cursor.getInt(cursor.getColumnIndexOrThrow("number_of_drives"));

                    // Пресметка на нов рејтинг
                    float updatedRating = (currentRating * count + newRating) / (count + 1);
                    values.put("rating_driver", updatedRating);
                    values.put("number_of_drives", count + 1);

                } else {
                    // Ако типот не е валиден
                    return false;
                }

                // Ажурирање на податоците во базата
                int rowsUpdated = db.update("Users", values, "username=?", new String[]{username});
                return rowsUpdated > 0; // Враќаме true ако успешно ажуриравме барем една редица
            } else {
                // Корисникот не е пронајден
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public boolean insertUser(String username, String password, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", username);
        values.put("password", password);
        values.put("email", email);

        long result = db.insert("Users", null, values);
        db.close();
        return result != -1; // Returns true if insertion is successful
    }

    // VEHICLE

    public boolean insertVehicle(int driverId, String vehicleName, int isActive, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("driver_id", driverId);
        values.put("vehicle_name", vehicleName);
        values.put("is_active", isActive);
        values.put("year", year);
        long result = db.insert("Vehicles", null, values);
        if (result == -1) {
            Log.e("DatabaseError", "Failed to insert vehicle for driverId: " + driverId);
        } else {
            Log.d("DatabaseDebug", "Vehicle inserted successfully with ID: " + result);
        }
        return result != -1;
    }



    public boolean updateVehicle(int vehicleId, int driverId, String vehicleName, int year, int isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("vehicle_name", vehicleName);
        values.put("year", year);
        values.put("driver_id", driverId);
        values.put("is_active", isActive);

        int rowsUpdated = db.update("Vehicles", values, "vehicle_id = ?", new String[]{String.valueOf(vehicleId)});
        db.close();
        return rowsUpdated > 0; // Враќа true ако е ажуриран ред, инаку false
    }

    public String getVehicleName(int driverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT vehicle_name FROM Vehicles WHERE driver_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(driverId)});

        String vehicleName = null; // default null ако нема податоци
        if (cursor.moveToFirst()) {
            vehicleName = cursor.getString(cursor.getColumnIndexOrThrow("vehicle_name"));
        } else {
            Log.e("DatabaseError", "No vehicle found for driverId " + driverId);
        }
        cursor.close();
        db.close();
        return vehicleName != null ? vehicleName : "Немате внесено возолио";
    }

    public int getVehicleYear(int driverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT year FROM Vehicles WHERE driver_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(driverId)});

        int vehicleYear = 0;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("year");
            if (columnIndex == -1) {
                Log.e("DatabaseError", "Column 'year' not found.");
            } else {
                vehicleYear = cursor.getInt(columnIndex);
            }
        } else {
            Log.e("DatabaseError", "Немате внесено возолио " + driverId);
        }
        cursor.close();
        db.close();
        return vehicleYear;
    }

    public float getDriverRating(int driverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT rating_driver FROM Users WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(driverId)});

        float driverRating = 0.0f;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("rating_driver");
            if (columnIndex == -1) {
                Log.e("DatabaseError", "Column 'rating_driver' not found.");
            } else {
                driverRating = cursor.getFloat(columnIndex);
            }
        } else {
            Log.e("DatabaseError", "No user found for driverId " + driverId);
        }
        cursor.close();
        db.close();
        return driverRating;
    }

    public boolean vehicleUnique(int driverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM Vehicles WHERE driver_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(driverId)});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    public String getUserName(int driverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT username FROM Users WHERE id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(driverId)});

        String username = null;
        if (cursor.moveToFirst()) {

            int columnIndex = cursor.getColumnIndex("username");
            if (columnIndex == -1) {
                Log.e("DatabaseError", "Column 'username' not found in query result.");
            } else {
                username = cursor.getString(columnIndex);
            }
        } else {
            Log.e("DatabaseError", "No user found with ID " + driverId);
        }
        cursor.close();
        db.close();
        return username;
    }

    public int getUserIdByUsername(String username) {
        if (username == null || username.isEmpty()) {
            Log.e("DatabaseError", "Username is null or empty");
            return -1;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id FROM Users WHERE username = ?";
        Cursor cursor = null;
        int userId = -1;

        try {
            cursor = db.rawQuery(query, new String[]{username});

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("id");
                if (columnIndex == -1) {
                    Log.e("DatabaseError", "Column 'id' not found in query result.");
                } else {
                    userId = cursor.getInt(columnIndex);
                }
            } else {
                Log.e("DatabaseError", "No user found with username: " + username);
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error querying user by username: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return userId;
    }

    public int getVehicleIdbyDriverId(int driverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int vehicleId = -1;
        Cursor cursor = db.rawQuery("SELECT vehicle_id FROM Vehicles WHERE driver_id = ?", new String[]{String.valueOf(driverId)});
        if (cursor.moveToFirst()) {
            vehicleId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return vehicleId;
    }


    //RIDES

    // Insert Ride
    public boolean insertRide(int vehicleId, int driverId, String startLocation, String endLocation,
                              String startTime, String client_list, int ridePerPerson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        double[] startCoordinates = getCoordinatesFromLocation(startLocation);
        double[] endCoordinates = getCoordinatesFromLocation(endLocation);
        values.put("vehicle_id", vehicleId);
        values.put("driver_id", driverId);
        values.put("start_location", startLocation);
        values.put("end_location", endLocation);
        values.put("start_time", startTime);
        values.put("ride_per_person", ridePerPerson);
        values.put("client_list",client_list);
        values.put("start_lat", startCoordinates[0]);
        values.put("start_lng", startCoordinates[1]);
        values.put("end_lat", endCoordinates[0]);
        values.put("end_lng", endCoordinates[1]);

        long result = db.insert("ScheduledRides", null, values);
        return result != -1;
    }

    // Delete Ride
    public boolean deleteRide(int rideId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("ScheduledRides", "ride_id" + " = ?", new String[]{String.valueOf(rideId)});
        return rowsDeleted > 0;
    }

    public boolean updateVehicleActiveStatus(int driverId, int isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("is_active", isActive);
        int rowsUpdated = db.update("Vehicles", values, "driver_id = ?", new String[]{String.valueOf(driverId)});
        return rowsUpdated > 0;
    }

    public boolean addClientToRide(int rideId, int clientId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Пребарај ја тековната клиентска листа и достапни места
        String query = "SELECT client_list, available_space FROM ScheduledRides WHERE ride_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(rideId)});

        if (cursor.moveToFirst()) {
            // Пронајдете индекси за колоните
            int clientListIndex = cursor.getColumnIndex("client_list");
            int availableSpaceIndex = cursor.getColumnIndex("available_space");

            // Проверете дали колоните се валидни
            if (clientListIndex != -1 && availableSpaceIndex != -1) {
                String clientList = cursor.getString(clientListIndex);
                int availableSpace = cursor.getInt(availableSpaceIndex);

                // Проверете дали има достапни места
                if (availableSpace > 0) {
                    // Додадете го клиентот во листата
                    if (clientList == null || clientList.isEmpty()) {
                        clientList = String.valueOf(clientId);
                    } else {
                        clientList = clientList + "," + clientId;
                    }
                    availableSpace--;
                    ContentValues values = new ContentValues();
                    values.put("client_list", clientList);
                    values.put("available_space", availableSpace);

                    int rowsUpdated = db.update("ScheduledRides", values, "ride_id = ?",
                            new String[]{String.valueOf(rideId)});
                    cursor.close();


                    if (availableSpace == 0) {
                        deactivateVehicleByRideId(rideId);
                        deleteRide(rideId);
                    }
                    return rowsUpdated > 0;
                } else {
                    Log.e("AddClientToRide", "Нема достапни места за возење со rideId: " + rideId);
                }
            } else {
                Log.e("DatabaseError", "Колони client_list или available_space не се пронајдени.");
            }
        } else {
            Log.e("DatabaseError", "Не постои ред со ride_id " + rideId);
        }

        cursor.close();
        return false;
    }

    private void deactivateVehicleByRideId(int rideId) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT vehicle_id FROM ScheduledRides WHERE ride_id = ?",
                new String[]{String.valueOf(rideId)});
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("vehicle_id");
            if (columnIndex != -1) { // Проверете дали колоната постои
                int vehicleId = cursor.getInt(columnIndex);

                // Ажурирај го is_active во табелата Vehicles
                ContentValues values = new ContentValues();
                values.put("is_active", 0);
                db.update("Vehicles", values, "vehicle_id = ?", new String[]{String.valueOf(vehicleId)});
            } else {
                Log.e("DatabaseError", "Колоната vehicle_id не е пронајдена во табелата ScheduledRides.");
            }
        } else {
            Log.e("DatabaseError", "Не постои ред со ride_id " + rideId);
        }
        cursor.close();
    }

    public int getRideIdbyDriverId(int driverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int rideId = -1;
        Cursor cursor = null;

        try {
            // Query to find the ride ID based on the driver ID
            String query = "SELECT ride_id FROM ScheduledRides WHERE driver_id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(driverId)});

            // Check if the cursor is not empty and the column exists
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("ride_id");
                if (columnIndex != -1) { // Check if the column exists
                    rideId = cursor.getInt(columnIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return rideId;
    }

//HistoryDrives

    public void addRideHistory(int ocenuvac, int ocenet, float rating, int oceneto, String tip_user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Пополнување на податоците
        values.put("ocenuvac", ocenuvac);
        values.put("ocenet", ocenet);
        values.put("rating", rating);
        values.put("dali_oceneto",oceneto);
        values.put("tip_user",tip_user);

        // Внеси го записот во табелата
        db.insert("RidesHistory", null, values);
        //String username = getUserName(ocenet);
        // Ажурирање на рејтингот на возачот
        //updateRating(username, rating, "driver");
        db.close();
    }

    public List<ScheduledRide> getFilteredRides(double clientStartLat, double clientStartLng, double clientEndLat, double clientEndLng, double radius) {
        List<ScheduledRide> rides = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL query to filter rides based on distance from start and end locations
        String query = "SELECT r.ride_id, r.start_location, r.end_location, r.ride_per_person, u.rating_driver, u.username, r.start_time " +
                "FROM ScheduledRides r " +
                "JOIN Vehicles v ON r.vehicle_id = v.vehicle_id " +
                "JOIN Users u ON r.driver_id = u.id " +
                "WHERE v.is_active = 1 " +
                "AND ABS(r.start_lat - ?) < ? AND ABS(r.start_lng - ?) < ? " +
                "AND ABS(r.end_lat - ?) < ? AND ABS(r.end_lng - ?) < ? " +
                "ORDER BY u.rating_driver DESC";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(clientStartLat),
                String.valueOf(radius),
                String.valueOf(clientStartLng),
                String.valueOf(radius),
                String.valueOf(clientEndLat),
                String.valueOf(radius),
                String.valueOf(clientEndLng),
                String.valueOf(radius)
        });

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int rideId = cursor.getInt(0); // ride_id
                    String startLocation = cursor.getString(1); // start_location
                    String endLocation = cursor.getString(2); // end_location
                    double price = cursor.getDouble(3); // price
                    double driverRating = cursor.getDouble(4); // driver_rating
                    String username = cursor.getString(5); // username
                    String startTime = cursor.getString(6); // start_time

                    // Создавање на ScheduledRide објект
                    ScheduledRide ride = new ScheduledRide(
                            rideId,
                            startLocation,
                            endLocation,
                            price,
                            driverRating,
                            username,
                            startTime
                    );

                    rides.add(ride);
                }
            } finally {
                cursor.close();
            }
        }

        db.close();
        return rides;
    }

    // Претворање на String (адреса/локација) во координати
    public double[] getCoordinatesFromLocation(String locationName) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        double[] coordinates = new double[2]; // [0] = latitude, [1] = longitude

        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                coordinates[0] = address.getLatitude();
                coordinates[1] = address.getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coordinates; // Врати го како [latitude, longitude]
    }

    // Претворање на координати во име на локација

    public Ride getUnratedRideForClient(int clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Ride ride = null;
        try {
            // SQL запрос за добивање на првиот неоценет возач за дадениот клиент
            String query = "SELECT RidesHistory.ride_id, RidesHistory.ocenet, Users.username " +
                    "FROM RidesHistory " +
                    "JOIN Users ON RidesHistory.ocenet = Users.id " +
                    "WHERE RidesHistory.ocenuvac = ? AND RidesHistory.dali_oceneto = 0 " +
                    "ORDER BY RidesHistory.ride_id DESC LIMIT 1";

            // Извршување на SQL запросот
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(clientId)});
            // Ако се најде резултат
            if (cursor != null && cursor.moveToFirst()) {
                int rideId = cursor.getInt(cursor.getColumnIndexOrThrow("ride_id"));
                int driverId = cursor.getInt(cursor.getColumnIndexOrThrow("ocenet"));
                String driverUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));

                // Создавање на објект за возило
                ride = new Ride(rideId, driverId, driverUsername);
            }
            // Затворање на курсор
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return ride;
    }

    public int getPricePerPerson(int driverId) {
        int pricePerPerson = 0;
        SQLiteDatabase db = this.getReadableDatabase(); // Отворање на базата на податоци

        // SQL запрос за да се добие ride_per_person
        String query = "SELECT ride_per_person FROM ScheduledRides WHERE driver_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(driverId)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("ride_per_person");
                if (columnIndex != -1) {
                    pricePerPerson = cursor.getInt(columnIndex);
                } else {
                    Log.e("Database", "Колоната 'ride_per_person' не постои во резултатот.");
                }
            }
            cursor.close();
        }
        db.close();
        return pricePerPerson;
    }
    public int getFirstUnratedOcenet(int ocenuvacId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int ocenetId = -1; // Default value ако нема резултат

        try {
            // SQL query
            String query = "SELECT ocenet FROM RidesHistory WHERE ocenuvac = ? AND dali_oceneto = 0 LIMIT 1";
            cursor = db.rawQuery(query, new String[]{String.valueOf(ocenuvacId)});

            if (cursor != null && cursor.moveToFirst()) {
                // Земаме го `ocenet` од првиот (и единствен) резултат
                ocenetId = cursor.getInt(cursor.getColumnIndexOrThrow("ocenet"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return ocenetId; // Враќа го ID-то на првиот "ocenet" кој не е оценет
    }
    public boolean updateDaliOceneto(int ocenuvacId, int ocenetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dali_oceneto", 1);
        int rowsUpdated = db.update(
                "RidesHistory",
                values,
                "ocenuvac = ? AND ocenet = ?",
                new String[]{String.valueOf(ocenuvacId), String.valueOf(ocenetId)}
        );
        db.close();
        return rowsUpdated > 0; // Враќа true ако успешно ажурира барем еден запис
    }

}

