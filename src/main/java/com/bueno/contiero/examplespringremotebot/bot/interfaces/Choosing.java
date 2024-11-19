package com.bueno.contiero.examplespringremotebot.bot.interfaces;


import com.bueno.contiero.examplespringremotebot.model.CardToPlay;

public interface Choosing {
    CardToPlay firstRoundChoose();
    CardToPlay secondRoundChoose();
    CardToPlay thirdRoundChoose();
}
