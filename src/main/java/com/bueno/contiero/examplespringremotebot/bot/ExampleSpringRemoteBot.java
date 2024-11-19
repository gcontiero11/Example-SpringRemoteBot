package com.bueno.contiero.examplespringremotebot.bot;


import com.bueno.contiero.examplespringremotebot.bot.interfaces.Analise;
import com.bueno.contiero.examplespringremotebot.bot.interfaces.Choosing;
import com.bueno.contiero.examplespringremotebot.bot.services.analise.AnaliseWhileLosing;
import com.bueno.contiero.examplespringremotebot.bot.services.analise.DefaultAnalise;
import com.bueno.contiero.examplespringremotebot.bot.services.choose_card.AgressiveChoosing;
import com.bueno.contiero.examplespringremotebot.bot.services.choose_card.PassiveChoosing;
import com.bueno.contiero.examplespringremotebot.bot.services.utils.PowerCalculatorService;
import com.bueno.contiero.examplespringremotebot.model.CardToPlay;
import com.bueno.contiero.examplespringremotebot.model.GameIntel;
import com.bueno.contiero.examplespringremotebot.model.TrucoCard;
import org.springframework.stereotype.Service;
import com.bueno.contiero.examplespringremotebot.service.BotServiceProvider;

import java.util.List;

import static com.bueno.contiero.examplespringremotebot.bot.interfaces.Analise.HandStatus.*;

@Service
public class ExampleSpringRemoteBot implements BotServiceProvider {
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
