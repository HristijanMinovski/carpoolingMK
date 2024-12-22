package com.example.workshop;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private List<ScheduledRide> rides; // Листата на возења
    private Context mContext;
    private DatabaseHelper db; // За базата
    private String clientUsername; // Клиент кој прави резервација

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView startLocation;
        public TextView endLocation;
        public TextView price;
        public TextView driverRating;
        public Button accept;
        public TextView username;

        public ViewHolder(View itemView) {
            super(itemView);
            startLocation = itemView.findViewById(R.id.startlocation);
            endLocation = itemView.findViewById(R.id.endlocation);
            price = itemView.findViewById(R.id.price);
            driverRating = itemView.findViewById(R.id.rating);
            accept = itemView.findViewById(R.id.accept);
            username = itemView.findViewById(R.id.user);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drivers_available, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScheduledRide ride = rides.get(position);

        holder.startLocation.setText("Почетна локација: " + ride.getStartLocation());
        holder.endLocation.setText("Крајна локација: " + ride.getEndLocation());
        holder.price.setText("Цена по лице: " + String.format("%.2f", ride.getPrice()) + " ден.");
        holder.driverRating.setText("Рејтинг на возач: " + String.format("%.1f", ride.getDriverRating()));
        holder.username.setText("Корисничко име: " + ride.getUsername());


        holder.accept.setOnClickListener(v -> {
            int rideId = ride.getRideId();
            int clientId = db.getUserIdByUsername(clientUsername);
            String driver_username=ride.getUsername();
            int driver_id=db.getUserIdByUsername(driver_username);
            db.addRideHistory(clientId,driver_id,0,0,"driver");
            db.addRideHistory(driver_id,clientId,0,0,"client");
            boolean success = db.addClientToRide(rideId, clientId);
            if (success) {
                Intent intent = new Intent(mContext, ReservationConfirmationActivity.class);
                intent.putExtra("start_location", ride.getStartLocation());
                intent.putExtra("driver_username", ride.getUsername());
                intent.putExtra("start_time", ride.getStartTime());
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, "Грешка при додавање на клиентот во рутата!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return rides == null ? 0 : rides.size();
    }

    public myAdapter(List<ScheduledRide> myList, Context context, String clientUsername) {
        this.rides = myList;
        this.mContext = context;
        this.db = new DatabaseHelper(context);
        this.clientUsername = clientUsername;
    }
}
