package helpers;

public enum StateEnum {

    /*
     * Enumeração dos estados possívis para uma letra
     * em comparação com uma letra da palavra gabarito
     * na partida.
     * 
     * Descrição dos estados:
     * - UNDISCOVERED: letra desconhecida, não informada
     * ainda pelo jogador. Estado inicial de todas as letras.
     * 
     * - DISCOVERED_AND_WRONG: a letra está presente na palavra
     * gabarito, mas em uma outra posição.
     * 
     * - DISCOVERED_AND_RIGHT: a letra está presente na palavra
     * gabarito e na posição correta.
     * 
     * - WRONG: a letra não está presente na palavra gabarito.
     * 
     */

    UNDISCOVERED,
    DISCOVERED_AND_WRONG,
    DISCOVERED_AND_RIGHT,
    WRONG
}
