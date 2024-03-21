package project.taskmaster.choremaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<String> userList;
    private List<String> userRoles;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private boolean actionMode; // Variable to keep track of the current mode

    // Modified constructor to include userRoles and actionMode
    UserAdapter(Context context, List<String> data, List<String> userRoles, boolean actionMode) {
        this.mInflater = LayoutInflater.from(context);
        this.userList = data;
        this.userRoles = userRoles;
        this.actionMode = actionMode;
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
        holder.textViewUserName.setText(userList.get(position));

        String role = userRoles.get(position);

        if (actionMode) {
            holder.buttonDelete.setImageResource(R.drawable.baseline_delete_24);
        } else {
            if ("admin".equals(role)) {
                holder.buttonDelete.setImageResource(R.drawable.baseline_remove_24);
            } else {
                holder.buttonDelete.setImageResource(R.drawable.baseline_add_24);
            }
        }
    }

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

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void addUser(String user, String role) {
        this.userList.add(user);
        this.userRoles.add(role);
        notifyItemInserted(this.userList.size() - 1);
    }

    public void removeUser(int position) {
        if (position >= 0 && position < userList.size()) {
            this.userList.remove(position);
            notifyItemRemoved(position);
        }
    }
    public void setActionMode(boolean actionMode) {
        this.actionMode = actionMode;
        notifyDataSetChanged();
    }
    public boolean getActionMode(){
        return actionMode;
    }

    public void setUserRoles(List<String> userRoles) {
        this.userRoles = userRoles;
        notifyDataSetChanged();
    }
}