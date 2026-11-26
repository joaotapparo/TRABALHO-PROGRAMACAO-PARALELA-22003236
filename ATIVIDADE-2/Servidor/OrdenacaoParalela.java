// O que sobrou do programa da atividade 1: a ordenacao PARALELA
// (TarefasOrdenadoras + rodadas de TarefasMisturadoras), usando os
// processadores disponiveis na maquina onde o servidor executa.
public class OrdenacaoParalela
{
    public static byte[] ordene (byte[] vetor) throws Exception
    {
        if (vetor==null)
            throw new Exception ("Vetor ausente");

        if (vetor.length==0)
            throw new Exception ("Vetor vazio");

        int quantidade = vetor.length;

        int nPedacos = Runtime.getRuntime().availableProcessors()-1;

        if (nPedacos<1)
            nPedacos = 1;

        if (nPedacos>quantidade)
            nPedacos = quantidade;

        System.out.println ("[log] processadores disponiveis: "+Runtime.getRuntime().availableProcessors()+"; serao usadas "+nPedacos+" ordenadoras.");

        int tamanhoBase    = quantidade/nPedacos;
        int pedacosMaiores = quantidade%nPedacos;

        TarefaOrdenadora[] ordenadoras = new TarefaOrdenadora [nPedacos];

        int posicao=0;
        for (int i=0; i<nPedacos; i++)
        {
            int tamanho = tamanhoBase + (i<pedacosMaiores ? 1 : 0);

            byte[] pedaco = new byte [tamanho];
            for (int j=0; j<tamanho; j++)
                pedaco[j] = vetor[posicao+j];

            System.out.println ("[log] Ordenadora "+(i+1)+" recebera "+tamanho+" elementos (posicoes "+posicao+" a "+(posicao+tamanho-1)+" do vetor recebido).");

            ordenadoras[i] = new TarefaOrdenadora (pedaco, "Ordenadora "+(i+1));
            ordenadoras[i].start ();

            posicao = posicao+tamanho;
        }

        byte[][] atual = new byte [nPedacos][];

        for (int i=0; i<nPedacos; i++)
        {
            ordenadoras[i].join ();
            atual[i] = ordenadoras[i].getOrdenado ();
        }

        System.out.println ("[log] todas as "+nPedacos+" ordenadoras finalizadas (join realizado em cada uma).");

        int rodada=1;
        while (atual.length>1)
        {
            int duplas = atual.length/2;
            int sobra  = atual.length%2;

            System.out.println ("[log] rodada "+rodada+": iniciando "+duplas+" misturadora(s) para "+atual.length+" pedaco(s).");

            TarefaMisturadora[] misturadoras = new TarefaMisturadora [duplas];

            for (int i=0; i<duplas; i++)
            {
                misturadoras[i] = new TarefaMisturadora (atual[2*i], atual[2*i+1], "Misturadora R"+rodada+"-"+(i+1));
                misturadoras[i].start ();
            }

            // so comeca a proxima rodada depois de esperar
            // (join) todas as misturadoras desta rodada
            byte[][] novo = new byte [duplas+sobra][];

            for (int i=0; i<duplas; i++)
            {
                misturadoras[i].join ();
                novo[i] = misturadoras[i].getMisturado ();
            }

            if (sobra==1)
                novo[duplas] = atual[atual.length-1];

            atual = novo;

            System.out.println ("[log] rodada "+rodada+" concluida (join realizado em cada misturadora); restam "+atual.length+" pedaco(s).");
            rodada++;
        }

        return atual[0]; // o vetor inteiro, ja ordenado
    }
}
