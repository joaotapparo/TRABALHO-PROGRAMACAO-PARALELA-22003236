import java.util.Random;

public class Programa
{
    private static int leiaInt (String pergunta)
    {
        int ret=0;
        boolean valido=false;

        while (!valido)
        {
            System.out.println (pergunta);
            try
            {
                ret = Teclado.getUmInt();
                valido = true;
            }
            catch (Exception erro)
            {
                System.err.println (erro.getMessage());
            }
        }

        return ret;
    }

    private static boolean leiaBoolean (String pergunta)
    {
        boolean ret=false;
        boolean valido=false;

        while (!valido)
        {
            System.out.println (pergunta);
            try
            {
                ret = Teclado.getUmBoolean();
                valido = true;
            }
            catch (Exception erro)
            {
                System.err.println (erro.getMessage());
            }
        }

        return ret;
    }

    private static byte leiaByte (String pergunta)
    {
        byte ret=0;
        boolean valido=false;

        while (!valido)
        {
            System.out.println (pergunta);
            try
            {
                ret = Teclado.getUmByte();
                valido = true;
            }
            catch (Exception erro)
            {
                System.err.println (erro.getMessage());
            }
        }

        return ret;
    }

    private static void mostreVetor (byte[] vetor)
    {
        System.out.print ("[");

        for (int i=0; i<vetor.length-1; i++)
            System.out.print (vetor[i]+", ");

        if (vetor.length>0)
            System.out.print (vetor[vetor.length-1]);

        System.out.println ("]");
    }

    public static void main (String[] args)
    {
        try
        {
            int quantidade = Programa.leiaInt ("Quantos elementos tera o vetor?");

            while (quantidade<1)
                quantidade = Programa.leiaInt ("Quantidade invalida! Quantos elementos tera o vetor?");

            byte[] vetor = new byte [quantidade];

            boolean digitar = Programa.leiaBoolean ("Deseja digitar os elementos (true) ou deseja que sejam gerados aleatoriamente (false)?");

            if (digitar)
                for (int i=0; i<quantidade; i++)
                    vetor[i] = Programa.leiaByte ("Elemento "+(i+1)+" de "+quantidade+" (byte: -128 a 127):");
            else
            {
                Random sorteio = new Random ();

                for (int i=0; i<quantidade; i++)
                    vetor[i] = (byte)(sorteio.nextInt(256)-128);
            }

            System.out.println ("[log] vetor com "+quantidade+" elementos (tipo byte) gerado ("+(digitar?"digitado":"aleatorio")+").");

            int nPedacos = Runtime.getRuntime().availableProcessors()-1;

            if (nPedacos<1)
                nPedacos = 1;

            if (nPedacos>quantidade)
                nPedacos = quantidade;

            System.out.println ("[log] processadores disponiveis: "+Runtime.getRuntime().availableProcessors()+"; serao usadas "+nPedacos+" ordenadoras.");

            long inicio = System.currentTimeMillis ();

  
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

                System.out.println ("[log] Ordenadora "+(i+1)+" recebera "+tamanho+" elementos (posicoes "+posicao+" a "+(posicao+tamanho-1)+" do vetor original).");

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

                // a main so comeca a proxima rodada depois de esperar
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

            vetor = atual[0]; // vetor passa a ser o vetor inteiro, ja ordenado

            long fim = System.currentTimeMillis ();
            long tempoOrdenacao = fim-inicio;

            System.out.println ("[log] ordenacao paralela concluida em "+tempoOrdenacao+"ms.");

            int opcao;
            do
            {
                System.out.println ();
                System.out.println ("1 - Mostrar o vetor inteiro");
                System.out.println ("2 - Mostrar um pedaco do vetor");
                System.out.println ("3 - Mostrar o tempo da ordenacao paralela");
                System.out.println ("4 - Sair");

                opcao = Programa.leiaInt ("Escolha uma opcao:");

                if (opcao==1)
                    Programa.mostreVetor (vetor);
                else if (opcao==2)
                {
                    int de  = Programa.leiaInt ("A partir de que posicao (0 a "+(vetor.length-1)+")?");
                    int ate = Programa.leiaInt ("Ate que posicao (0 a "+(vetor.length-1)+")?");

                    if (de<0 || ate>vetor.length-1 || de>ate)
                        System.err.println ("Intervalo invalido!");
                    else
                    {
                        byte[] pedaco = new byte [ate-de+1];
                        for (int i=0; i<pedaco.length; i++)
                            pedaco[i] = vetor[de+i];

                        Programa.mostreVetor (pedaco);
                    }
                }
                else if (opcao==3)
                    System.out.println ("Tempo da ordenacao paralela: "+tempoOrdenacao+"ms");
                else if (opcao!=4)
                    System.err.println ("Opcao invalida!");
            }
            while (opcao!=4);
        }
        catch (Exception erro)
        {
            System.err.println (erro.getMessage());
        }
    }
}
