package com.github.dawidd6.andttt.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.dawidd6.andttt.ClientService;
import com.github.dawidd6.andttt.MainActivity;
import com.github.dawidd6.andttt.R;
import com.github.dawidd6.andttt.events.DisconnectEvent;
import com.github.dawidd6.andttt.events.SendEvent;
import com.github.dawidd6.andttt.game.Player;
import com.github.dawidd6.andttt.proto.*;
import com.github.dawidd6.andttt.proto.Error;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class OnlineFragment extends BaseGameFragment {
    public static final String TAG = "OnlineFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isOnline = true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAllTilesClickable(false);

        player1.setName(getArguments().getString("name"));
        player2.setName("");

        showConclusion(getString(R.string.waiting), Color.BLUE);
        restartButton.setClickable(false);
        restartButton.setAlpha(0.5f);

        Request request = Request.newBuilder()
                .setStarterPack(StarterPackRequest.newBuilder())
                .build();
        EventBus.getDefault().post(new SendEvent(request));

        ConnectivityManager connManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        assert connManager != null;
        connManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onLost(Network network) {
                super.onLost(network);

                onDisconnect(null);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Request request = Request.newBuilder()
                .setLeaveRoom(LeaveRoomRequest.newBuilder())
                .build();
        EventBus.getDefault().post(new SendEvent(request));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStarterPack(StarterPackResponse response) {
        player1.setSymbol(response.getMySymbol());
        player1.setTurn(response.getMyTurn());

        player2.setName(response.getEnemyName());
        player2.setSymbol(response.getEnemySymbol());
        player2.setTurn(response.getEnemyTurn());

        restartGame();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMove(MoveResponse response) {
        makeMove(player1, player2, response.getPosition());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnemyMoved(EnemyMovedResponse response) {
        makeMove(player2, player1, response.getPosition());
        if(game.isPlaying())
            hideConclusion();
    }

    public void C_O_W_A_R_D_S() {
        showYesNo(R.string.enemy_left, ((dialog, which) -> {
            dialog.dismiss();

            getFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }), ((dialog, which) -> {
            dialog.dismiss();

            getActivity().onBackPressed();
        }));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnemyLeft(EnemyLeftResponse response) {
        C_O_W_A_R_D_S();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnemyDisconnected(EnemyDisconnectedResponse response) {
        C_O_W_A_R_D_S();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRestart(RestartResponse response) {
        switch (response.getRestart()) {
            case REQUESTED:
                showYesNo(R.string.question_restart, ((dialog, which) -> {
                    dialog.dismiss();

                    Request request = Request.newBuilder()
                            .setRestart(RestartRequest.newBuilder()
                                    .setRestart(Restart.APPROVED))
                            .build();
                    EventBus.getDefault().post(new SendEvent(request));


                    Request request1 = Request.newBuilder()
                            .setStarterPack(StarterPackRequest.newBuilder())
                            .build();
                    EventBus.getDefault().post(new SendEvent(request1));
                }), ((dialog, which) -> {
                    dialog.dismiss();

                    Request request = Request.newBuilder()
                            .setRestart(RestartRequest.newBuilder()
                                    .setRestart(Restart.DENIED))
                            .build();
                    EventBus.getDefault().post(new SendEvent(request));
                }));
                break;
            case APPROVED:
                dismissLoading();
                Request request = Request.newBuilder()
                        .setStarterPack(StarterPackRequest.newBuilder())
                        .build();
                EventBus.getDefault().post(new SendEvent(request));
                break;
            case DENIED:
                dismissLoading();
                showError(R.string.denied_restart, ((dialog, which) -> {
                    dialog.dismiss();
                }));
                break;
        }
    }

    @Override
    public void randomize() {}

    @Override
    public void restartGame() {
        super.restartGame();
        
        setAllTilesClickable(player1.isTurn());
        restartButton.setClickable(false);
        restartButton.setAlpha(0.5f);

        if(player1.isTurn())
            hideConclusion();
        else
            showConclusion(getString(R.string.waiting), Color.BLUE);
    }

    @Override
    protected void makeMove(Player playerWithTurn, Player playerWithoutTurn, int i) {
        super.makeMove(playerWithTurn, playerWithoutTurn, i);

        if(game.isPlaying()) {
            setAllTilesClickable(player1.isTurn());

            if (player2.isTurn())
                showConclusion(getString(R.string.waiting), Color.BLUE);
        } else {
            restartButton.setClickable(true);
            restartButton.setAlpha(1.0f);
        }
    }

    @Override
    public void onClickTile(View view) {
        setAllTilesClickable(false);

        int i = Character.getNumericValue(getResources().getResourceEntryName(view.getId()).charAt(1));
        Request request = Request.newBuilder()
                .setMove(MoveRequest.newBuilder()
                        .setPosition(i))
                .build();
        EventBus.getDefault().post(new SendEvent(request));
    }

    @Override
    public void onClickRestart(View view) {
        showLoading(R.string.waiting_for_opponent);

        Request request = Request.newBuilder()
                .setRestart(RestartRequest.newBuilder()
                        .setRestart(Restart.REQUESTED))
                .build();
        EventBus.getDefault().post(new SendEvent(request));
    }
}