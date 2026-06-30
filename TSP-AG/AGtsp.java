import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Implementação do Algoritmo Genético para o Problema do Caixeiro Viajante (TSP).
 *
 * Representação do cromossomo: cada cromossomo é uma ArrayList<Cidade> representando
 * uma permutação das cidades (a ordem de visita).
 *
 * Operadores genéticos utilizados:
 *   - Seleção: Roleta Viciada (seleção proporcional ao fitness)
 *   - Cruzamento: PMX – Partially Mapped Crossover (garante permutações válidas)
 *   - Mutação: Swap – troca de dois genes (cidades) aleatórios no cromossomo
 *   - Fitness: 1 / distânciaTotal  (quanto menor a rota, maior o fitness)
 */
public class AGtsp {

    // Lista de cidades carregadas do arquivo CSV
    ArrayList<Cidade> cidades = new ArrayList<>();

    private int tamPopulacao;           // Número de cromossomos na população
    private int tamCromossomo = 0;      // Número de cidades (definido após carga)
    private int probMutacao;            // Probabilidade de mutação (0-100)
    private int qtdeCruzamentos;        // Quantidade de cruzamentos por geração
    private int numeroGeracoes;         // Número de gerações a executar

    // Representação da população: lista de cromossomos, cada cromossomo é uma lista de cidades
    private ArrayList<ArrayList<Cidade>> populacao = new ArrayList<>();

    // Roleta virtual para seleção proporcional ao fitness
    private ArrayList<Integer> roletaVirtual = new ArrayList<>();

    private Random random = new Random();

    // =========================================================================
    // CONSTRUTOR
    // =========================================================================

    /**
     * @param tamPopulacao    Tamanho da população (ex: 100)
     * @param probMutacao     Probabilidade de mutação em % (ex: 5 para 5%)
     * @param qtdeCruzamentos Quantidade de cruzamentos por geração (ex: 50)
     * @param numeroGeracoes  Número total de gerações (ex: 500)
     */
    public AGtsp(int tamPopulacao, int probMutacao, int qtdeCruzamentos, int numeroGeracoes) {
        this.tamPopulacao = tamPopulacao;
        this.probMutacao = probMutacao;
        this.qtdeCruzamentos = qtdeCruzamentos;
        this.numeroGeracoes = numeroGeracoes;
    }

    // =========================================================================
    // EXECUÇÃO PRINCIPAL
    // =========================================================================

    /**
     * Executa o algoritmo genético completo.
     */
    public void executar() {
        criarPopulacao();

        System.out.println("=== Algoritmo Genético – Problema do Caixeiro Viajante ===\n");
        System.out.printf("Parâmetros: população=%d | mutação=%d%% | cruzamentos=%d | gerações=%d%n%n",
                tamPopulacao, probMutacao, qtdeCruzamentos, numeroGeracoes);

        for (int geracao = 0; geracao < this.numeroGeracoes; geracao++) {

            // 1. Construção da roleta proporcional ao fitness
            criarRoletaVirtual();

            // 2. Geração de novos indivíduos por cruzamento e mutação
            ArrayList<ArrayList<Cidade>> novaPopulacao = new ArrayList<>(populacao); // elitismo simples

            for (int c = 0; c < this.qtdeCruzamentos; c++) {
                // Seleção de dois pais pela roleta
                int idxPai1 = selecionarPelaRoleta();
                int idxPai2 = selecionarPelaRoleta();

                ArrayList<Cidade> pai1 = populacao.get(idxPai1);
                ArrayList<Cidade> pai2 = populacao.get(idxPai2);

                // Cruzamento PMX gerando dois filhos
                ArrayList<ArrayList<Cidade>> filhos = cruzamentoPMX(pai1, pai2);

                // Mutação (swap) com probabilidade definida
                for (ArrayList<Cidade> filho : filhos) {
                    if (random.nextInt(100) < probMutacao) {
                        mutacaoSwap(filho);
                    }
                    novaPopulacao.add(filho);
                }
            }

            // 3. Seleção dos melhores para manter o tamanho da população
            populacao = selecionarMelhores(novaPopulacao, tamPopulacao);

            // 4. Exibição da evolução a cada 50 gerações
            if ((geracao + 1) % 50 == 0 || geracao == 0) {
                int melhorIdx = obterMelhor();
                double dist = calcularDistanciaTotal(populacao.get(melhorIdx));
                System.out.printf("Geração %4d | Melhor distância: %.2f%n", geracao + 1, dist);
            }
        }

        // Resultado final
        int melhorIdx = obterMelhor();
        System.out.println("\n==========================================");
        System.out.println("Melhor solução encontrada:");
        mostrarRota(populacao.get(melhorIdx));
    }

    // =========================================================================
    // CRIAÇÃO DA POPULAÇÃO INICIAL
    // =========================================================================

    /**
     * Gera a população inicial com permutações aleatórias das cidades.
     */
    private void criarPopulacao() {
        this.tamCromossomo = cidades.size();
        populacao.clear();

        for (int i = 0; i < tamPopulacao; i++) {
            ArrayList<Cidade> cromossomo = new ArrayList<>(cidades);
            Collections.shuffle(cromossomo, random);
            populacao.add(cromossomo);
        }
    }

    // =========================================================================
    // FITNESS
    // =========================================================================

    /**
     * Calcula a distância total de uma rota (cromossomo).
     * A rota é circular: retorna à cidade inicial no final.
     *
     * @param cromossomo Permutação de cidades representando a rota
     * @return Distância total do percurso
     */
    private double calcularDistanciaTotal(ArrayList<Cidade> cromossomo) {
        double distancia = 0.0;
        int n = cromossomo.size();

        for (int i = 0; i < n - 1; i++) {
            distancia += cromossomo.get(i).distancia(cromossomo.get(i + 1));
        }
        // Retorno à cidade inicial
        distancia += cromossomo.get(n - 1).distancia(cromossomo.get(0));

        return distancia;
    }

    /**
     * Calcula o fitness de um cromossomo.
     * Fitness = 1 / distânciaTotal (quanto menor a distância, maior o fitness).
     *
     * @param cromossomo Permutação de cidades
     * @return Valor de fitness (inversamente proporcional à distância)
     */
    private double calcularFitness(ArrayList<Cidade> cromossomo) {
        return 1.0 / calcularDistanciaTotal(cromossomo);
    }

    // =========================================================================
    // SELEÇÃO – ROLETA VICIADA
    // =========================================================================

    /**
     * Constrói a roleta virtual proporcional ao fitness de cada indivíduo.
     * Indivíduos com maior fitness ocupam mais "fatias" da roleta.
     */
    private void criarRoletaVirtual() {
        roletaVirtual.clear();

        // Normalização: usamos rankings em vez de fitness direto para evitar problemas
        // com valores muito pequenos (1/distância). Usa-se fitness * 10000 arredondado.
        double totalFitness = 0.0;
        double[] fitnesses = new double[populacao.size()];

        for (int i = 0; i < populacao.size(); i++) {
            fitnesses[i] = calcularFitness(populacao.get(i));
            totalFitness += fitnesses[i];
        }

        for (int i = 0; i < populacao.size(); i++) {
            // Fatias proporcionais ao fitness (mínimo 1 fatia por indivíduo)
            int fatias = (int) Math.max(1, Math.round((fitnesses[i] / totalFitness) * 1000));
            for (int j = 0; j < fatias; j++) {
                roletaVirtual.add(i);
            }
        }
    }

    /**
     * Sorteia um índice da roleta virtual.
     *
     * @return Índice do indivíduo selecionado
     */
    private int selecionarPelaRoleta() {
        int idx = random.nextInt(roletaVirtual.size());
        return roletaVirtual.get(idx);
    }

    // =========================================================================
    // CRUZAMENTO PMX (Partially Mapped Crossover)
    // =========================================================================

    /**
     * Realiza o cruzamento PMX entre dois pais, gerando dois filhos.
     *
     * Etapas do PMX:
     *   1. Escolhe dois pontos de corte aleatórios [ponto1, ponto2].
     *   2. Copia o segmento pai1[ponto1..ponto2] diretamente para filho1
     *      e o segmento pai2[ponto1..ponto2] para filho2.
     *   3. Preenche os genes restantes usando o mapeamento entre os segmentos
     *      para garantir que não haja duplicações.
     *
     * @param pai1 Primeiro pai (permutação de cidades)
     * @param pai2 Segundo pai (permutação de cidades)
     * @return Lista com dois filhos válidos (permutações completas sem repetição)
     */
    private ArrayList<ArrayList<Cidade>> cruzamentoPMX(ArrayList<Cidade> pai1, ArrayList<Cidade> pai2) {
        int n = pai1.size();

        // Sorteia dois pontos de corte distintos
        int ponto1 = random.nextInt(n);
        int ponto2 = random.nextInt(n);
        if (ponto1 > ponto2) {
            int temp = ponto1;
            ponto1 = ponto2;
            ponto2 = temp;
        }
        // Garante que os pontos sejam diferentes
        if (ponto1 == ponto2) {
            ponto2 = Math.min(ponto2 + 1, n - 1);
        }

        // Inicializa filhos com nulls
        ArrayList<Cidade> filho1 = new ArrayList<>(Collections.nCopies(n, null));
        ArrayList<Cidade> filho2 = new ArrayList<>(Collections.nCopies(n, null));

        // Passo 1: copia o segmento entre os pontos de corte
        for (int i = ponto1; i <= ponto2; i++) {
            filho1.set(i, pai1.get(i));
            filho2.set(i, pai2.get(i));
        }

        // Passo 2: preenche os demais genes para filho1 usando pai2
        preencherFilhoPMX(filho1, pai2, pai1, ponto1, ponto2);

        // Passo 3: preenche os demais genes para filho2 usando pai1
        preencherFilhoPMX(filho2, pai1, pai2, ponto1, ponto2);

        ArrayList<ArrayList<Cidade>> filhos = new ArrayList<>();
        filhos.add(filho1);
        filhos.add(filho2);
        return filhos;
    }

    /**
     * Preenche as posições nulas de um filho usando o outro pai,
     * respeitando o mapeamento PMX para evitar duplicações.
     *
     * @param filho       Filho a ser preenchido (segmento central já copiado)
     * @param paiOrigem   Pai cujos genes serão usados para preencher posições nulas
     * @param paiMapeado  Pai cujo segmento central foi copiado para o filho
     * @param ponto1      Início do segmento copiado (inclusive)
     * @param ponto2      Fim do segmento copiado (inclusive)
     */
    private void preencherFilhoPMX(ArrayList<Cidade> filho, ArrayList<Cidade> paiOrigem,
                                    ArrayList<Cidade> paiMapeado, int ponto1, int ponto2) {
        int n = filho.size();

        for (int i = 0; i < n; i++) {
            // Posições já preenchidas pelo segmento copiado são ignoradas
            if (i >= ponto1 && i <= ponto2) continue;

            Cidade candidato = paiOrigem.get(i);

            // Resolve conflitos via mapeamento PMX
            while (filho.contains(candidato)) {
                // Encontra a posição de 'candidato' no paiOrigem (segmento do filho)
                int posNoFilho = paiMapeado.indexOf(candidato);
                candidato = paiOrigem.get(posNoFilho);
            }

            filho.set(i, candidato);
        }
    }

    // =========================================================================
    // MUTAÇÃO – SWAP (Troca de posições)
    // =========================================================================

    /**
     * Mutação por troca (swap): seleciona dois índices aleatórios e troca as
     * cidades de posição, preservando a validade da permutação.
     *
     * @param cromossomo Cromossomo a ser mutado (alterado in-place)
     */
    private void mutacaoSwap(ArrayList<Cidade> cromossomo) {
        int i = random.nextInt(cromossomo.size());
        int j = random.nextInt(cromossomo.size());

        // Garante que as posições sejam diferentes
        while (i == j) {
            j = random.nextInt(cromossomo.size());
        }

        Cidade temp = cromossomo.get(i);
        cromossomo.set(i, cromossomo.get(j));
        cromossomo.set(j, temp);
    }

    // =========================================================================
    // SELEÇÃO DOS MELHORES (Truncamento)
    // =========================================================================

    /**
     * Seleciona os melhores indivíduos de uma população expandida,
     * mantendo apenas os 'quantidade' com menor distância total.
     *
     * @param populacaoExpandida População após geração de filhos
     * @param quantidade         Número de indivíduos a manter
     * @return Nova população com os melhores indivíduos
     */
    private ArrayList<ArrayList<Cidade>> selecionarMelhores(
            ArrayList<ArrayList<Cidade>> populacaoExpandida, int quantidade) {

        // Ordena pela menor distância total (maior fitness)
        populacaoExpandida.sort((a, b) ->
                Double.compare(calcularDistanciaTotal(a), calcularDistanciaTotal(b)));

        // Retorna os primeiros 'quantidade' (melhores)
        return new ArrayList<>(populacaoExpandida.subList(0, Math.min(quantidade, populacaoExpandida.size())));
    }

    // =========================================================================
    // OBTER MELHOR INDIVÍDUO
    // =========================================================================

    /**
     * Retorna o índice do melhor indivíduo da população atual
     * (aquele com menor distância total).
     *
     * @return Índice do melhor cromossomo
     */
    public int obterMelhor() {
        int melhorIdx = 0;
        double menorDistancia = calcularDistanciaTotal(populacao.get(0));

        for (int i = 1; i < populacao.size(); i++) {
            double dist = calcularDistanciaTotal(populacao.get(i));
            if (dist < menorDistancia) {
                menorDistancia = dist;
                melhorIdx = i;
            }
        }
        return melhorIdx;
    }

    // =========================================================================
    // EXIBIÇÃO DA ROTA
    // =========================================================================

    /**
     * Exibe a rota do cromossomo informado, incluindo o retorno à cidade inicial
     * e a distância total do percurso.
     *
     * @param cromossomo Cromossomo (permutação de cidades) a ser exibido
     */
    public void mostrarRota(ArrayList<Cidade> cromossomo) {
        System.out.print("Rota:\n");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cromossomo.size(); i++) {
            sb.append(cromossomo.get(i).getNome());
            if (i < cromossomo.size() - 1) {
                sb.append(" -> ");
            }
        }
        // Retorno à cidade inicial
        sb.append(" -> ").append(cromossomo.get(0).getNome());

        System.out.println(sb.toString());

        double distancia = calcularDistanciaTotal(cromossomo);
        System.out.printf("%nDistância total: %.2f%n", distancia);
    }

    // =========================================================================
    // CARGA DE CIDADES A PARTIR DE ARQUIVO CSV
    // =========================================================================

    /**
     * Carrega as cidades a partir de um arquivo CSV no formato:
     *   nome,x,y
     *
     * @param arquivo Caminho para o arquivo CSV
     */
    public void carregarCidades(String arquivo) {
        String linha;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), "UTF-8"))) {

            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] dados = linha.split(",");
                if (dados.length < 3) continue;

                String nome = dados[0].trim();
                double x = Double.parseDouble(dados[1].trim());
                double y = Double.parseDouble(dados[2].trim());

                Cidade cidade = new Cidade(nome, x, y);
                cidades.add(cidade);
            }

            System.out.println("Cidades carregadas: " + cidades.size());

        } catch (IOException e) {
            System.err.println("Erro ao carregar arquivo de cidades: " + e.getMessage());
        }
    }

    // =========================================================================
    // MÉTODO MAIN
    // =========================================================================

    /**
     * Ponto de entrada do programa.
     * Configura os parâmetros do algoritmo genético e executa a busca.
     */
    public static void main(String[] args) {
        // Parâmetros do Algoritmo Genético
        int tamPopulacao    = 150;   // Tamanho da população
        int probMutacao     = 8;     // Probabilidade de mutação em %
        int qtdeCruzamentos = 100;   // Cruzamentos por geração
        int numeroGeracoes  = 500;   // Número de gerações

        AGtsp ag = new AGtsp(tamPopulacao, probMutacao, qtdeCruzamentos, numeroGeracoes);

        // Carrega as cidades do arquivo CSV
        ag.carregarCidades("cidades.csv");

        if (ag.cidades.isEmpty()) {
            System.err.println("Nenhuma cidade foi carregada. Verifique o arquivo cidades.csv.");
            return;
        }

        // Executa o algoritmo genético
        ag.executar();
    }
}
