package com.github.dawidd6.andttt.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.github.dawidd6.andttt.MainActivity;
import com.github.dawidd6.andttt.R;
import com.github.dawidd6.andttt.proto.Symbol;

import java.util.Random;
import java.util.Vector;

public class SingleFragment extends BaseGameFragment {
    public static final String TAG = "SingleFragment";
    private int delay;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        player1.setName(getString(R.string.player));
        player2.setName(getString(R.string.ai));

        delay = getResources().getInteger(R.integer.ai_move_delay);
        delay = MainActivity.isAnimationEnabled ? delay : 0;
    }

    @Override
    public void restartGame() {
        super.restartGame();

        AIMove();
    }

    @Override
    public void onClickTile(View view) {
        super.onClickTile(view);

        AIMove();
    }

    private void AIMove() {
        setAllTilesClickable(false);

        new Handler().postDelayed(() -> {
            if (player2.isTurn())
                makeMove(player2, player1, computeMove());
            if(player1.isTurn())
                setAllTilesClickable(true);
        }, delay);
    }

    public int computeMove() {
        Vector<Integer> nonePositions = new Vector<>();
        Random rand = new Random();
        int counter = 0;

        for(Symbol s : new Symbol[] {player2.getSymbol(), player1.getSymbol()}) {
            nonePositions.removeAllElements();
            for(int p[] : game.getPatterns()) {
                for(int j = 0; j < 3; j++) {
                    if(game.getTile(p[j]) == s)
                        counter++;
                    else if(game.getTile(p[j]) == Symbol.NO) {
                        counter = counter + 8;
                        nonePositions.add(p[j]);
                    }
                }

                if(counter == 10)
                    return nonePositions.lastElement();
                else
                    counter = 0;
            }
        }

        return nonePositions.elementAt(rand.nextInt(nonePositions.size()));
    }
}