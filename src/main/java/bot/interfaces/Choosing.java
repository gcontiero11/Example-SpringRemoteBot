package bot.interfaces;


import model.CardToPlay;

public interface Choosing {
    CardToPlay firstRoundChoose();
    CardToPlay secondRoundChoose();
    CardToPlay thirdRoundChoose();
}
