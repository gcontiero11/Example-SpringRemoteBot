package bot.interfaces;


import model.GameIntel;
import model.TrucoCard;

import java.util.List;

public abstract class Analise{

    public enum HandStatus {BAD, MEDIUM, GOOD, GOD}

    public HandStatus myHand(GameIntel intel) {
        List<TrucoCard> myCards = intel.getCards();
        if (myCards.size() == 3){
            return threeCardsHandler(myCards);
        }
        if (myCards.size() == 2){
            return twoCardsHandler(myCards);
        }
        if(myCards.size() == 1){
            return oneCardHandler();
        }
        return HandStatus.GOD;
    }

    public abstract HandStatus threeCardsHandler(List<TrucoCard> myCards);
    public abstract HandStatus twoCardsHandler(List<TrucoCard> myCards);
    public abstract HandStatus oneCardHandler();
}
