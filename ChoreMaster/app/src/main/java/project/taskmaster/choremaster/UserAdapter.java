package project.taskmaster.choremaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<String> userList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // Data is passed into the constructor
    UserAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.userList = data;
    }

    // Inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    // Binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String user = userList.get(position);
        holder.textViewUserName.setText(user);
    }

    // Total number of rows
    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewUserName;
        ImageButton buttonDelete;

        ViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textViewUsername);
            buttonDelete = itemView.findViewById(R.id.btnRemoveUser);
            buttonDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Convenience method for getting data at click position
    String getItem(int id) {
        return userList.get(id);
    }

    // Allows click events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void addUser(String user) {
        this.userList.add(user);
        notifyItemInserted(this.userList.size() - 1);
    }

    // Method to remove a user
    public void removeUser(int position) {
        if (position >= 0 && position < userList.size()) {
            this.userList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
