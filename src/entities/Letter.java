package entities;

import java.util.ArrayList;
import java.util.List;

import helpers.StateEnum;

public class Letter {
    /*
     * Definição da classe "Letter", elemento base
     * para a lógica do jogo desenvolvido. Possui os
     * atributos 'letter', o qual recebe uma letra (char)
     * e state, que recebe o estado daquela letra em relação
     * à palavra gabarito daquela partida.
     */
    private char letter;
    private StateEnum state;

    public Letter(char letter) {
        state = StateEnum.UNDISCOVERED;
        this.letter = letter;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public StateEnum getStatesEnum() {
        return state;
    }

    public void setState(StateEnum state) {
        this.state = state;
    }

}