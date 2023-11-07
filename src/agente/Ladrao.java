package agente;

import java.util.*;

import algoritmo.ProgramaLadrao;

public class Ladrao extends ProgramaLadrao {

    int memoria[][] = new int[30][30];
    Grafo grafo = new Grafo();

    int esperaDefault = 500;
    int espera = esperaDefault;

    //static int lider[] = new int[4];
    static int ladroesExperiencia[] = new int[4];
    static int id = 1;

    /*Map<Integer, Integer> ladroesIdsToIndex = Map.ofEntries( 
            entry(200, 0),
            entry(210, 1),
            entry(220, 2),
            entry(230, 3)
    );*/

    Map<Integer, Integer> ladroesIdsToIndex = new HashMap();

    ArrayList<Integer> ladroesIds = new ArrayList<>(Arrays.asList(200, 210, 220, 230));
    ArrayList<Integer> ladroesVisaoIdsArray = new ArrayList<>();

    Map<Integer, int[]> ladroesVistosPosicaoVisao = new HashMap<>();
    Map<Integer, int[]> ladroesVistosPosicaoMemoria = new HashMap<>();

    Queue<int[]> ultimasPosicoes = new LinkedList<>();
    int ultimasPosicoesCont = 1;
    int ultimasPosicoesIDArray = 0;

    boolean maisDeDoisEmGrupo = false;

    int idArray = 0;
    int emObjetivo = 0;

    int xBanco;
    int yBanco;
    int xMoeda;
    int yMoeda;

    public int acao() {
        ladroesIdsToIndex.put(200, 0);
        ladroesIdsToIndex.put(210, 1);
        ladroesIdsToIndex.put(220, 2);
        ladroesIdsToIndex.put(230, 3);

        ladroesVisaoIdsArray.clear();
        ladroesVistosPosicaoVisao.clear();
        maisDeDoisEmGrupo = false;

        if (ultimasPosicoes.size() > 10) {
            List<int[]> auxList = new ArrayList<>();

            Arrays.asList(ultimasPosicoes.toArray()).forEach(i->auxList.add((int[]) i));

            ultimasPosicoes = new LinkedList<>(auxList.subList(0,9));
        }

        idArray = id - 1;

        //System.out.println(idArray);
        id = id % 4 + 1;
        ultimasPosicoesCont = ultimasPosicoesCont % 10 + 1;
        ultimasPosicoesIDArray = ultimasPosicoesCont - 1;
        //System.out.println(ultimasPosicoesCont + " ultimasPosicoesCont");


        boolean temPoupador = false;
        int posicaoPoupador = 25;

        memoriaOndepassou();
        memoriaOqueTem();

        ladraoExperiencia();

        int ladroesCont = 0;

        // VISÃO
        for (int i = 0; i < sensor.getVisaoIdentificacao().length; i++) {

            // SAIR DO OBJETIVO
            if (sensor.getVisaoIdentificacao()[i] == 100 || sensor.getVisaoIdentificacao()[i] == 110) {

                ladroesExperiencia[idArray] += 10;
                emObjetivo = 0;
                temPoupador = true;
                posicaoPoupador = i;

                if (i != 7 && i != 11 && i != 12 && i != 16) {
                    espera--;
                }

                if (i == 7 || i == 11 || i == 12 || i == 16) {
                    espera = esperaDefault;
                }
            }

            if (ladroesIds.contains(sensor.getVisaoIdentificacao()[i])) {
                ladroesVistosPosicaoVisao.put(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]), visaoIndexToCoodinates(i));

                ladroesCont++;
            }
        }

        if (ladroesCont > 1) {
            maisDeDoisEmGrupo = true;
            boolean continuar = false;
            int poupador[] = visaoIndexToCoodinates(posicaoPoupador);

            List<Integer> movimentosLadroes = new ArrayList<>();
            ladroesVistosPosicaoVisao.forEach((i, j) -> movimentosLadroes.add(checkMovimentosSize(grafo.buscaLargura(this.grafo.matrizVisao, j[0], j[1], poupador[0], poupador[1]))));
            int distanciaPoupador = checkMovimentosSize(grafo.buscaLargura(this.grafo.matrizVisao, 2, 2, poupador[0], poupador[1]));

            for (int i : movimentosLadroes) {
                continuar = distanciaPoupador < i;
            }

            if (!continuar) {
                //System.out.println(idArray + "saindo de perto");

                //return (int) (Math.random() * 5);
                emObjetivo = 0;
                return priorizarNovosLugares();
            }
        }

        // FICAR NO OBJETIVO
        if (emObjetivo != 0) {

            switch (emObjetivo) {

                case 1:
                    return caçar();

                case 2:
                    return vaiParaObjetivo(xBanco, yBanco);

                /*case 3:
                    return vaiParaObjetivo(xMoeda, yMoeda);*/
            }
        }

        // ROLETA RUSSA
        if ((int) (Math.random() * espera) == 0) {

            espera = esperaDefault;

            switch ((int) (Math.random() * 2)) {
                case 0:
                    //Muda o objetivo -> Caçar.
                    emObjetivo = 1;
                    return caçar();
                case 1:
                    boolean temBanco = false;

                    for (int i = 0; i < 30; i++) {
                        for (int j = 0; j < 30; j++) {

                            if (memoria[i][j] == 2147483646) {
                                temBanco = true;
                                xBanco = i;
                                yBanco = j;
                            }
                        }
                    }

                    if (temBanco) {
                        //Muda o objetivo -> Camperar.
                        emObjetivo = 2;
                        //System.out.println(idArray + "banco");
                        return vaiParaObjetivo(xBanco, yBanco);
                    }

                    emObjetivo = 1;
                    return caçar();

                /*case 2:
                    int qtdMoedasJuntas = 0;

                    for (int i = 0; i < 30; i++) {
                        for (int j = 0; j < 30; j++) {

                            if (memoria[i][j] == 2147483645) {
                                qtdMoedasJuntas++;
                                xMoeda = i;
                                yMoeda = j;
                            }

                        }
                    }

                    if (qtdMoedasJuntas > 8) {
                        //Muda o objetivo -> Camperar em volta de muitas moedas
                        emObjetivo = 3;
                        System.out.println(idArray + "moedas");
                        return vaiParaObjetivo(xMoeda, yMoeda);
                    }
                    emObjetivo = 1;
                    return caçar();*/
            }
        }

        // VAI ATRAS DO POUPADOR
        if (temPoupador && emObjetivo == 0) {

            // Movimenta de acordo com a posição do poupador no campo de visão.
            int visaoIndexes[] = visaoIndexToCoodinates(posicaoPoupador);
            //System.out.println(idArray + "perseguir");
            return movimentaVisao(visaoIndexes[0], visaoIndexes[1]);
        }

        // CHEIRO
        int menor = Integer.MAX_VALUE;
        int aux = 0;

        for (int i = 0; i < 8; i++) {
            if (sensor.getAmbienteOlfatoPoupador()[i] >= 1) {

                if (menor > sensor.getAmbienteOlfatoPoupador()[i]) {
                    menor = sensor.getAmbienteOlfatoPoupador()[i];
                    aux = i;
                }
            }
        }

        if (menor >= 1 && menor <= 5) {
            // Movimenta de acordo com a posição do poupador no campo de olfato.
            //System.out.println(idArray + "olfato");
            return movimentaOlfato(aux);
        }

        //Caso não veja nenhum poupador na visão ou no olfato, se movimenta de acordo com a memoria.
        return priorizarNovosLugares();

        // Move aleatoriamente Até encontrar algum poupador.
        //return (int) (Math.random() * 5);
        //return 0;
    }

    public void ladraoExperiencia() {
        int cont = 0;

        for (int i = 0; i < memoria.length; i++) {
            for (int j = 0; j < memoria[i].length; j++) {
                switch (memoria[i][j]) {
                    case 2147483647:
                        cont += 1;
                        break;
                    case 2147483646:
                        cont += 3;
                        break;
                    case 2147483645:
                        cont += 4;
                        break;
                    case 2147483644:
                        cont += 5;
                        break;
                }
            }
        }

        if (sensor.getNumeroDeMoedas() > 0) {
            cont *= sensor.getNumeroDeMoedas();
        }

        ladroesExperiencia[idArray] = cont;
    }

    // Mudanças de objetivo:

    private int caçar() {
        boolean cacar = true;

        //System.out.println(idArray + "caçar");
        ArrayList<Integer> toRemove = new ArrayList();

        ladroesVistosPosicaoMemoria.forEach((i, j) -> {
            if (j[0] == sensor.getPosicao().y && j[1] == sensor.getPosicao().x) {
                emObjetivo = 0;
                toRemove.add(i);
            }
        });

        toRemove.forEach(i -> ladroesVistosPosicaoMemoria.remove(i));

        if(toRemove.size() > 0){
            return priorizarNovosLugares();
        }

        int max = 0;

        if (ladroesVisaoIdsArray.size() > 0) {
            for (int i = 0; i < ladroesVisaoIdsArray.size(); i++) {
                if (ladroesExperiencia[ladroesVisaoIdsArray.get(i)] > max) {
                    max = ladroesExperiencia[ladroesVisaoIdsArray.get(i)];
                }
            }

            if (max < ladroesExperiencia[idArray]) {
                cacar = false;
            }
        }

        if (cacar) {
            //System.out.println(idArray + "caçar");
            List<List<Integer>> movimentosLadroes = new ArrayList<>();

            ladroesVistosPosicaoMemoria.forEach((i, j) -> {
                List<Integer> movimentos = grafo.buscaLargura(this.grafo.matrizMemoria, sensor.getPosicao().y, sensor.getPosicao().x, j[0], j[1]);

                if (checkMovimentosSize(movimentos) < Integer.MAX_VALUE) {
                    movimentosLadroes.add(movimentos);
                }
            });

            int[] menorCaminho = null;

            if (movimentosLadroes.size() > 0) {
                if (movimentosLadroes.get(0) != null) {
                    menorCaminho = new int[]{0, movimentosLadroes.get(0).size()};

                    for (List<Integer> i : movimentosLadroes) {
                        if (i.size() < menorCaminho[1]) {
                            menorCaminho = new int[]{movimentosLadroes.indexOf(i), i.size()};
                        }
                    }
                }
            }

            if (menorCaminho != null) {
                if (menorCaminho[1] > 0) {
                    return movimentosLadroes.get(menorCaminho[0]).get(1);
                } else {
                    return priorizarNovosLugares();
                }
            } else {
                return priorizarNovosLugares();
            }
        } else {
            return priorizarNovosLugares();
        }
        //return 0;
    }

    public int vaiParaObjetivo(int xDestino, int yDestino) {
        //System.out.println(idArray + "irObjetivo");
        List<Integer> movimentos = grafo.buscaLargura(this.grafo.matrizMemoria, sensor.getPosicao().y, sensor.getPosicao().x, xDestino, yDestino);

        if (movimentos != null) {
            if (movimentos.size() > 0) {
                return movimentos.get(1);
            } else {
                return priorizarNovosLugares();
            }
        } else {
            return priorizarNovosLugares();
        }
    }

    //Metodos da memoria:

    private int priorizarNovosLugares() {
        //System.out.println(idArray + "priorizar");
        // Prioriza visitar lugares menos visitados.
        int x = sensor.getPosicao().y;
        int y = sensor.getPosicao().x;

        ArrayList<Integer> menores = new ArrayList<Integer>();

        int m = Integer.MAX_VALUE;

        /*Map<Integer, Integer> indexes = Map.ofEntries(
                entry(1, memoria[forMinRangeX(x)][y]),
                entry(2, memoria[forMaxRangeX(x)][y]),
                entry(3, memoria[x][forMaxRangeY(y)]),
                entry(4, memoria[x][forMinRangeY(y)])
        );*/

        /*Map<Integer, int[]> indexesCoordenada = Map.ofEntries(
                entry(1, new int[]{forMinRangeX(x), y}),
                entry(2, new int[]{forMaxRangeX(x), y}),
                entry(3, new int[]{x, forMaxRangeY(y)}),
                entry(4, new int[]{x, forMinRangeY(y)})
        );*/

        Map<Integer, Integer> indexes = new HashMap<>();
        indexes.put(1, memoria[forMinRangeX(x)][y]);
        indexes.put(2, memoria[forMaxRangeX(x)][y]);
        indexes.put(3, memoria[x][forMaxRangeY(y)]);
        indexes.put(4, memoria[x][forMinRangeY(y)]);

        Map<Integer, int[]> indexesCoordenada = new HashMap<>();
        indexesCoordenada.put(1, new int[]{forMinRangeX(x), y});
        indexesCoordenada.put(2, new int[]{forMaxRangeX(x), y});
        indexesCoordenada.put(3, new int[]{x, forMaxRangeY(y)});
        indexesCoordenada.put(4, new int[]{x, forMinRangeY(y)});

        for (int i = 1; i <= indexes.size(); i++) {
            if (indexes.get(i) < m && indexes.get(i) != -1) {
                m = indexes.get(i);

                menores.clear();

                if (!ultimasPosicoes.contains(indexesCoordenada.get(i))) {
                    menores.add(i);
                }
            }
        }

        for (int i = 1; i <= indexes.size(); i++) {
            if (indexes.get(i) == m && !menores.contains(i) && indexes.get(i) != -1) {
                if (!ultimasPosicoes.contains(indexesCoordenada.get(i))) {
                    menores.add(i);
                }
            }
        }

        int randomIndex = (int) (Math.random() * menores.size());

        ultimasPosicoes.add(indexesCoordenada.get(menores.get(randomIndex)));

        return menores.get(randomIndex);
    }

    public void memoriaOndepassou() {
        memoria[sensor.getPosicao().y][sensor.getPosicao().x]++;
    }

    public void memoriaOqueTem() {
        //Atualiza na memoria com o que ele viu.
        for (int i = 0; i < sensor.getVisaoIdentificacao().length; i++) {
            int coordinatesMemoria[] = filtraPosicaoMemoria(i);

            int valor;

            switch (sensor.getVisaoIdentificacao()[i]) {
                case 1:
                    valor = 2147483647;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    break;
                case 3:
                    valor = 2147483646;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    break;
                case 4:
                    valor = 2147483645;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    break;
                case 5:
                    valor = 2147483644;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    break;
                case 200:
                    valor = 2147483640;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    ladroesVistosPosicaoMemoria.put(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]), coordinatesMemoria);
                    ladroesVisaoIdsArray.add(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]));
                    break;
                case 210:
                    valor = 2147483641;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    ladroesVistosPosicaoMemoria.put(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]), coordinatesMemoria);
                    ladroesVisaoIdsArray.add(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]));
                    break;
                case 220:
                    valor = 2147483642;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    ladroesVistosPosicaoMemoria.put(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]), coordinatesMemoria);
                    ladroesVisaoIdsArray.add(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]));
                    break;
                case 230:
                    valor = 2147483643;
                    memoria[coordinatesMemoria[0]][coordinatesMemoria[1]] = valor;
                    ladroesVistosPosicaoMemoria.put(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]), coordinatesMemoria);
                    ladroesVisaoIdsArray.add(ladroesIdsToIndex.get(sensor.getVisaoIdentificacao()[i]));
                    break;
            }
        }
    }

    public int[] filtraPosicaoMemoria(int i) {
        //Armazena o valor no determinado lugar da memoria.
        int x = sensor.getPosicao().y;
        int y = sensor.getPosicao().x;
        int coordenadas[] = new int[]{0, 0};

        switch (i) {
            case 0:
                coordenadas = new int[]{x - 2, y - 2};
                break;
            case 1:
                coordenadas = new int[]{x - 2, y - 1};
                break;
            case 2:
                coordenadas = new int[]{x - 2, y};
                break;
            case 3:
                coordenadas = new int[]{x - 2, y + 1};
                break;
            case 4:
                coordenadas = new int[]{x - 2, y + 2};
                break;
            case 5:
                coordenadas = new int[]{x - 1, y - 2};
                break;
            case 6:
                coordenadas = new int[]{x - 1, y - 1};
                break;
            case 7:
                coordenadas = new int[]{x - 1, y};
                break;
            case 8:
                coordenadas = new int[]{x - 1, y + 1};
                break;
            case 9:
                coordenadas = new int[]{x - 1, y + 2};
                break;
            case 10:
                coordenadas = new int[]{x, y - 2};
                break;
            case 11:
                coordenadas = new int[]{x, y - 1};
                break;
            case 12:
                coordenadas = new int[]{x, y + 1};
                break;
            case 13:
                coordenadas = new int[]{x, +2};
                break;
            case 14:
                coordenadas = new int[]{x + 1, y - 2};
                break;
            case 15:
                coordenadas = new int[]{x + 1, y - 1};
                break;
            case 16:
                coordenadas = new int[]{x + 1, y};
                break;
            case 17:
                coordenadas = new int[]{x + 1, y + 1};
                break;
            case 18:
                coordenadas = new int[]{x + 1, y + 2};
                break;
            case 19:
                coordenadas = new int[]{x + 2, y - 2};
                break;
            case 20:
                coordenadas = new int[]{x + 2, y - 1};
                break;
            case 21:
                coordenadas = new int[]{x + 2, y};
                break;
            case 22:
                coordenadas = new int[]{x + 2, y + 1};
                break;
            case 23:
                coordenadas = new int[]{x + 2, y + 2};
                break;
        }

        return coordenadas;
    }

    public int[] visaoIndexToCoodinates(int index) {
        //Armazena o valor no determinado lugar da memoria.

        for (int i = 0; i < grafo.matrizVisaoAux.length; i++) {
            for (int j = 0; j < grafo.matrizVisaoAux[i].length; j++) {
                if (grafo.matrizVisaoAux[i][j] == index) {
                    return new int[]{i, j};
                }
            }
        }

        return new int[]{0, 0};
    }

    public int checkMovimentosSize(List<Integer> movimentos) {
        if (movimentos != null) {
            return movimentos.size();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    //Metodos de movimentação:
    public int movimentaVisao(int xDestino, int yDestino) {
        List<Integer> movimentos = grafo.buscaLargura(this.grafo.matrizVisao, 2, 2, xDestino, yDestino);

        if (movimentos != null) {
            if (movimentos.size() > 0) {
                return movimentos.get(1);
            } else {
                return priorizarNovosLugares();
            }
        } else {
            return priorizarNovosLugares();
        }
    }

    public int movimentaOlfato(int i) {

        if (i == 0 && sensor.getVisaoIdentificacao()[7] == 0 && sensor.getVisaoIdentificacao()[11] == 0) {
            return moveEsquerdaOuCima();
        }

        if (i == 0 && sensor.getVisaoIdentificacao()[7] == 0 && sensor.getVisaoIdentificacao()[11] != 0) {
            return moveCima();
        }

        if (i == 0 && sensor.getVisaoIdentificacao()[7] != 0 && sensor.getVisaoIdentificacao()[11] == 0) {
            return moveEsquerda();
        }

        if (i == 1) {
            return moveCima();
        }

        if (i == 2 && sensor.getVisaoIdentificacao()[7] == 0 && sensor.getVisaoIdentificacao()[12] == 0) {
            return moveDireitaOuCima();
        }

        if (i == 2 && sensor.getVisaoIdentificacao()[7] == 0 && sensor.getVisaoIdentificacao()[12] != 0) {
            return moveCima();
        }

        if (i == 2 && sensor.getVisaoIdentificacao()[7] != 0 && sensor.getVisaoIdentificacao()[12] == 0) {
            return moveDireita();
        }

        if (i == 3) {
            return moveEsquerda();
        }

        if (i == 4) {
            return moveDireita();
        }

        if (i == 5 && sensor.getVisaoIdentificacao()[16] == 0 && sensor.getVisaoIdentificacao()[11] == 0) {
            return moveEsquerdaOuBaixo();
        }

        if (i == 5 && sensor.getVisaoIdentificacao()[16] == 0 && sensor.getVisaoIdentificacao()[11] != 0) {
            return moveBaixo();
        }

        if (i == 5 && sensor.getVisaoIdentificacao()[16] != 0 && sensor.getVisaoIdentificacao()[11] == 0) {
            return moveEsquerda();
        }

        if (i == 6) {
            return moveBaixo();
        }

        if (i == 7 && sensor.getVisaoIdentificacao()[16] == 0 && sensor.getVisaoIdentificacao()[12] == 0) {
            return moveDireitaOuBaixo();
        }

        if (i == 7 && sensor.getVisaoIdentificacao()[16] == 0 && sensor.getVisaoIdentificacao()[12] != 0) {
            return moveBaixo();
        }

        if (i == 7 && sensor.getVisaoIdentificacao()[16] != 0 && sensor.getVisaoIdentificacao()[12] == 0) {
            return moveDireita();
        }

        return 0;
    }


    public int moveEsquerdaOuCima() {

        int aleatorio = (int) (Math.random() * 2);

        if (aleatorio == 0) return 1;
        else return 4;

    }

    public int moveDireitaOuCima() {

        int aleatorio = (int) (Math.random() * 2);

        if (aleatorio == 0) return 1;
        else return 3;

    }

    public int moveEsquerdaOuBaixo() {

        int aleatorio = (int) (Math.random() * 2);

        if (aleatorio == 0) return 2;
        else return 4;

    }

    public int moveDireitaOuBaixo() {

        int aleatorio = (int) (Math.random() * 2);

        if (aleatorio == 0) return 2;
        else return 3;

    }

    public int moveCima() {
        return 1;
    }

    public int moveBaixo() {
        return 2;
    }

    public int moveEsquerda() {
        return 4;
    }

    public int moveDireita() {
        return 3;
    }

    public int forMinRangeX(int x) {
        if (x - 1 < 0) {
            return x;
        }

        return x - 1;
    }

    public int forMinRangeY(int y) {
        if (y - 1 < 0) {
            return y;
        }

        return y - 1;
    }

    public int forMaxRangeX(int x) {
        if (x + 1 > 29) {
            return x;
        }

        return x + 1;
    }

    public int forMaxRangeY(int y) {
        if (y + 1 > 29) {
            return y;
        }

        return y + 1;
    }

    public class Node {
        int x = -1;
        int y = -1;
        int k = -1;
        Node pai = null;

        public Node(int x, int y, int k) {
            this.x = x;
            this.y = y;
            this.k = k;
        }
    }

    public class Grafo {
        int matrizMemoria[][][][] = new int[30][30][4][3];
        int matrizVisao[][][][] = new int[5][5][4][3];
        int matrizVisaoAux[][] = new int[5][5];

        public Grafo() {
            for (int i = 0; i < matrizMemoria.length; i++) {
                for (int j = 0; j < matrizMemoria.length; j++) {
                    if (!(j - 1 < 0)) {
                        matrizMemoria[i][j][0][0] = i;
                        matrizMemoria[i][j][0][1] = j - 1;
                        matrizMemoria[i][j][0][2] = 4;
                    } else {
                        matrizMemoria[i][j][0][0] = -1;
                        matrizMemoria[i][j][0][1] = -1;
                        matrizMemoria[i][j][0][2] = -1;
                    }
                    if (!(j + 1 > matrizMemoria.length - 1)) {
                        matrizMemoria[i][j][1][0] = i;
                        matrizMemoria[i][j][1][1] = j + 1;
                        matrizMemoria[i][j][1][2] = 3;
                    } else {
                        matrizMemoria[i][j][1][0] = -1;
                        matrizMemoria[i][j][1][1] = -1;
                        matrizMemoria[i][j][1][2] = -1;
                    }
                    if (!(i - 1 < 0)) {
                        matrizMemoria[i][j][2][0] = i - 1;
                        matrizMemoria[i][j][2][1] = j;
                        matrizMemoria[i][j][2][2] = 1;
                    } else {
                        matrizMemoria[i][j][2][0] = -1;
                        matrizMemoria[i][j][2][1] = -1;
                        matrizMemoria[i][j][2][2] = -1;
                    }
                    if (!(i + 1 > matrizMemoria.length - 1)) {
                        matrizMemoria[i][j][3][0] = i + 1;
                        matrizMemoria[i][j][3][1] = j;
                        matrizMemoria[i][j][3][2] = 2;
                    } else {
                        matrizMemoria[i][j][3][0] = -1;
                        matrizMemoria[i][j][3][1] = -1;
                        matrizMemoria[i][j][3][2] = -1;
                    }

                }
            }

            int aux = 0;

            for (int i = 0; i < matrizVisaoAux.length; i++) {
                for (int j = 0; j < matrizVisaoAux[i].length; j++) {
                    if (aux <= 11) {
                        matrizVisaoAux[i][j] = aux;
                    }

                    if (aux == 12) {
                        matrizVisaoAux[i][j] = -1;
                    }

                    if (aux > 12) {
                        matrizVisaoAux[i][j] = aux - 1;
                    }

                    aux++;

                }
            }

            for (int i = 0; i < matrizVisao.length; i++) {
                for (int j = 0; j < matrizVisao.length; j++) {
                    if (!(j - 1 < 0)) {
                        matrizVisao[i][j][0][0] = i;
                        matrizVisao[i][j][0][1] = j - 1;
                        matrizVisao[i][j][0][2] = 4;
                    } else {
                        matrizVisao[i][j][0][0] = -1;
                        matrizVisao[i][j][0][1] = -1;
                        matrizVisao[i][j][0][2] = -1;
                    }
                    if (!(j + 1 > matrizVisao.length - 1)) {
                        matrizVisao[i][j][1][0] = i;
                        matrizVisao[i][j][1][1] = j + 1;
                        matrizVisao[i][j][1][2] = 3;
                    } else {
                        matrizVisao[i][j][1][0] = -1;
                        matrizVisao[i][j][1][1] = -1;
                        matrizVisao[i][j][1][2] = -1;
                    }
                    if (!(i - 1 < 0)) {
                        matrizVisao[i][j][2][0] = i - 1;
                        matrizVisao[i][j][2][1] = j;
                        matrizVisao[i][j][2][2] = 1;
                    } else {
                        matrizVisao[i][j][2][0] = -1;
                        matrizVisao[i][j][2][1] = -1;
                        matrizVisao[i][j][2][2] = -1;
                    }
                    if (!(i + 1 > matrizVisao.length - 1)) {
                        matrizVisao[i][j][3][0] = i + 1;
                        matrizVisao[i][j][3][1] = j;
                        matrizVisao[i][j][3][2] = 2;
                    } else {
                        matrizVisao[i][j][3][0] = -1;
                        matrizVisao[i][j][3][1] = -1;
                        matrizVisao[i][j][3][2] = -1;
                    }
                }
            }
        }

        public List<Integer> buscaLargura(int[][][][] grafoOriginal, int xS, int yS, int xD, int yD) {
            int[][][][] grafo = Arrays.copyOf(grafoOriginal, grafoOriginal.length);
            Queue<Node> q = new LinkedList<>();
            ArrayList<Node> visitados = new ArrayList<>();

            Node node = new Node(xS, yS, -1);
            node.pai = null;

            visitados.add(node);
            q.add(node);

            while (!q.isEmpty()) {
                node = q.remove();

                for (int[] i : grafo[node.x][node.y]) {
                    if (i[0] != -1) {
                        int xG = i[0];
                        int yG = i[1];
                        int zG = i[2];

                        boolean visitado = false;

                        for (Node it : visitados) {
                            if (it.x == xG && it.y == yG) {
                                visitado = true;
                                break;
                            }
                        }

                        if (!visitado && memoria[xG][yG] < 100000) {
                            Node newNode = new Node(xG, yG, zG);
                            newNode.pai = node;

                            visitados.add(newNode);
                            q.add(newNode);

                        } else if (xG == xD && yG == yD) {
                            Node newNode = new Node(xG, yG, zG);
                            newNode.pai = node;

                            List<Integer> movimentos = new ArrayList<>();

                            while (newNode != null) {
                                movimentos.add(newNode.k);
                                newNode = newNode.pai;
                            }

                            Collections.reverse(movimentos);

                            return movimentos;
                        }
                    }
                }
            }

            return null;
        }
    }
}