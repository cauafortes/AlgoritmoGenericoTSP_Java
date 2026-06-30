/**
 * Classe que representa uma Cidade no Problema do Caixeiro Viajante.
 * Contém nome e coordenadas (x, y) para cálculo de distância euclidiana.
 */
public class Cidade {

    private String nome;
    private double x;
    private double y;

    /**
     * Construtor da classe Cidade.
     *
     * @param nome Nome ou identificador da cidade
     * @param x    Coordenada X da cidade
     * @param y    Coordenada Y da cidade
     */
    public Cidade(String nome, double x, double y) {
        this.nome = nome;
        this.x = x;
        this.y = y;
    }

    public String getNome() {
        return nome;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * Calcula a distância euclidiana entre esta cidade e outra cidade.
     *
     * @param outra A cidade de destino
     * @return Distância euclidiana entre as duas cidades
     */
    public double distancia(Cidade outra) {
        double dx = this.x - outra.x;
        double dy = this.y - outra.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return nome;
    }
}
