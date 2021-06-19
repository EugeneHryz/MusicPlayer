package com.example.musicplayer.playlistdialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.musicplayer.AppContainer;
import com.example.musicplayer.MusicPlayerApp;
import com.example.musicplayer.R;

import java.util.Objects;


public class EnterPlaylistNameDialog extends DialogFragment {

    public static final String TAG = "EnterPlaylistNameDialog";

    private EnterPlaylistNameListener listener;

    private long playlistId;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        View view = Objects.requireNonNull(getActivity()).getLayoutInflater()
                .inflate(R.layout.enter_playlist_name_dialog, null);

        EditText editText = view.findViewById(R.id.enter_playlist_name_edit_text);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialogBuilder.setTitle("Enter playlist name")
                .setView(view)
                .setNegativeButton("Cancel", (dialog, which) -> { })
                .setPositiveButton("Add", (dialog, which) -> {
                    AppContainer container = ((MusicPlayerApp)
                            Objects.requireNonNull(getActivity().getApplicationContext())).appContainer;

                    String uri = (container.playlistDataProvider
                            .createPlaylist(editText.getText().toString())).toString();
                    playlistId = Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));

                    Toast.makeText(getContext(), "Playlist created", Toast.LENGTH_SHORT).show();
                });

        return dialogBuilder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onNameEntered(playlistId);
        }

        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity())
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void setListener(EnterPlaylistNameListener listener) {
        this.listener = listener;
    }

    public interface EnterPlaylistNameListener {

        void onNameEntered(long playlistId);
    }
}
