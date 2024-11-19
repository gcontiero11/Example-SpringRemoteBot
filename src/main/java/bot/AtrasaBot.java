package bot;


import bot.interfaces.Analise;
import bot.interfaces.Choosing;
import bot.services.analise.AnaliseWhileLosing;
import bot.services.analise.DefaultAnalise;
import bot.services.choose_card.AgressiveChoosing;
import bot.services.choose_card.PassiveChoosing;
import bot.services.utils.PowerCalculatorService;
import model.CardToPlay;
import model.GameIntel;
import model.TrucoCard;
import service.BotServiceProvider;

import java.util.List;

import static bot.interfaces.Analise.HandStatus.*;


public class AtrasaBot implements BotServiceProvider {
    private Analise.HandStatus status;

    @Override
    public boolean getMaoDeOnzeResponse(GameIntel intel) {
        Analise analise = createAnaliseInstance(intel);
        status = analise.myHand(intel);
        if (status == GOD) return true;
        if (status == GOOD) {
            int scoreDistance = intel.getScore() - intel.getOpponentScore();
            if (scoreDistance >= 4) return true;
            return PowerCalculatorService.powerOfCard(intel, 0) >= 9;
        }
        return false;
    }

    @Override
    public boolean decideIfRaises(GameIntel intel) {
        int scoreDistance = intel.getScore() - intel.getOpponentScore();
        List<TrucoCard> myCards = intel.getCards();
        Analise analise = createAnaliseInstance(intel);
        status = analise.myHand(intel);
        if (scoreDistance <= -9){
            if ((status == MEDIUM || status == BAD) && myCards.size() == 3) return true;
        }
        if (status == GOD && myCards.size() <= 2) return true;
        if(status == GOOD && myCards.size() == 2 && PowerCalculatorService.powerOfCard(intel, 1) >= 8) return true;
        return intel.getOpponentCard().isPresent() && myCards.size() == 1;
    }

    @Override
    public CardToPlay chooseCard(GameIntel intel) {
        List<TrucoCard> myCards = intel.getCards();
        Analise analise = createAnaliseInstance(intel);
        status = analise.myHand(intel);
        Choosing chooser = createChoosingInstance(intel);
        if (myCards.size() == 3) return chooser.firstRoundChoose();
        if (myCards.size() == 2) return chooser.secondRoundChoose();
        else return chooser.thirdRoundChoose();
    }

    @Override
    public int getRaiseResponse(GameIntel intel) {
        int myScore = intel.getScore();
        int oppScore = intel.getOpponentScore();
        int scoreDistance = myScore - oppScore;
        List<TrucoCard> myCards = intel.getCards();

        if (!myCards.isEmpty()){
            Analise analise = createAnaliseInstance(intel);
            status = analise.myHand(intel);
        }

        if (status == GOD) return 1;

        if (myCards.size() == 3){
            if (status == BAD) return -1;
            else return 0;
        }

        if (myCards.size() == 2){
            if (PowerCalculatorService.wonFirstRound(intel)) return 0;

            if (PowerCalculatorService.lostFirstRound(intel)){
                if (status == GOOD) return 0;
                return -1;
            }
            if (status == GOOD && scoreDistance <= -6) {
                return 1;
            }
            if (status == GOOD) return 0;
            return -1;
        }

        if (status == GOOD && scoreDistance <= -6) return 1;
        if (status == BAD || (status == MEDIUM && scoreDistance <= -4)) return -1;
        return 0;
    }

    private Analise createAnaliseInstance(GameIntel intel) {
        int myScore = intel.getScore();
        int oppScore = intel.getOpponentScore();
        int scoreDistance = myScore - oppScore;

        if (oppScore > myScore && scoreDistance < -6) {
            return new AnaliseWhileLosing(intel);
        } else {
            return new DefaultAnalise(intel);
        }

    }
    private Choosing createChoosingInstance(GameIntel intel) {

        int myScore = intel.getScore();
        int oppScore = intel.getOpponentScore();
        int scoreDistance = myScore - oppScore;

        if (oppScore > myScore && scoreDistance < -6) {
            return new PassiveChoosing(intel,status);
        } else {
            return new AgressiveChoosing(intel,status);
        }

    }
}
