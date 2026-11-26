import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente
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

    private static Parceiro conecte (String host, int porta) throws Exception
    {
        Socket conexao=null;
        try
        {
            conexao = new Socket (host, porta);
        }
        catch (Exception erro)
        {
            throw new Exception ("Nao foi possivel conectar a "+host+":"+porta);
        }

        ObjectOutputStream transmissor=null;
        try
        {
            transmissor =
            new ObjectOutputStream(
            conexao.getOutputStream());
        }
        catch (Exception erro)
        {
            throw new Exception ("Nao foi possivel conectar a "+host+":"+porta);
        }

        // se o servidor estiver atendendo outro cliente, ficamos
        // parados aqui ate que ele termine (o servidor so cria o
        // seu ObjectOutputStream quando comeca a nos atender)
        ObjectInputStream receptor=null;
        try
        {
            receptor =
            new ObjectInputStream(
            conexao.getInputStream());
        }
        catch (Exception erro)
        {
            throw new Exception ("Nao foi possivel conectar a "+host+":"+porta);
        }

        return new Parceiro (conexao, receptor, transmissor);
    }

    private static void encerreConexoes (Parceiro[] servidores)
    {
        for (int i=0; i<servidores.length; i++)
        {
            if (servidores[i]==null)
                continue;

            try
            {
                servidores[i].receba (new PedidoParaSair ());
                servidores[i].adeus  ();
            }
            catch (Exception erro)
            {} // ja estamos encerrando mesmo
        }
    }

    private static void desista (Parceiro[] servidores, String mensagem)
    {
        System.err.println ("\n"+mensagem);
        Cliente.encerreConexoes (servidores);
        System.exit (1);
    }

    public static void main (String[] args)
    {
        if (args.length<1)
        {
            System.err.println ("Uso esperado: java Cliente HOST:PORTA [HOST:PORTA ...]");
            System.err.println ("Exemplo:      java Cliente 192.168.0.10:3000 192.168.0.11:3000\n");
            return;
        }

        int      quantosServidores = args.length;
        String[] hosts             = new String [quantosServidores];
        int[]    portas            = new int    [quantosServidores];

        try
        {
            for (int i=0; i<quantosServidores; i++)
            {
                String[] hostEPorta = args[i].split(":");
                if (hostEPorta.length!=2)
                    throw new Exception ();

                hosts [i] = hostEPorta[0];
                portas[i] = Integer.parseInt(hostEPorta[1]);

                // o servidor atende um cliente por vez; se o mesmo
                // servidor fosse indicado 2 vezes, esperariamos por
                // nos mesmos para sempre
                for (int j=0; j<i; j++)
                    if (hosts[j].equals(hosts[i]) && portas[j]==portas[i])
                        throw new Exception ();
            }
        }
        catch (Exception erro)
        {
            System.err.println ("Indique os servidores como HOST:PORTA, sem repeticoes!\n");
            return;
        }

        int quantidade = Cliente.leiaInt ("Quantos elementos tera o vetor?");

        while (quantidade<1)
            quantidade = Cliente.leiaInt ("Quantidade invalida! Quantos elementos tera o vetor?");

        byte[] vetor = new byte [quantidade];

        boolean digitar = Cliente.leiaBoolean ("Deseja digitar os elementos (true) ou deseja que sejam gerados aleatoriamente (false)?");

        if (digitar)
            for (int i=0; i<quantidade; i++)
                vetor[i] = Cliente.leiaByte ("Elemento "+(i+1)+" de "+quantidade+" (byte: -128 a 127):");
        else
        {
            Random sorteio = new Random ();

            for (int i=0; i<quantidade; i++)
                vetor[i] = (byte)(sorteio.nextInt(256)-128);
        }

        System.out.println ("[log] vetor com "+quantidade+" elementos (tipo byte) gerado ("+(digitar?"digitado":"aleatorio")+").");

        // conectando a cada servidor e perguntando o seu poder computacional
        Parceiro[] servidores    = new Parceiro [quantosServidores];
        int[]      processadores = new int      [quantosServidores];
        int        totalDeProcessadores = 0;

        for (int i=0; i<quantosServidores; i++)
        {
            System.out.println ("[log] conectando a "+args[i]+" (se ele estiver atendendo outro cliente, aguardaremos)...");

            try
            {
                servidores[i] = Cliente.conecte (hosts[i], portas[i]);

                servidores[i].receba (new PedidoDeCapacidade ());

                Comunicado comunicado = servidores[i].envie ();
                if (!(comunicado instanceof Capacidade))
                    throw new Exception ("O servidor "+args[i]+" nao informou sua capacidade");

                processadores[i]      = ((Capacidade)comunicado).getProcessadores();
                totalDeProcessadores += processadores[i];
            }
            catch (Exception erro)
            {
                Cliente.desista (servidores, erro.getMessage());
            }

            System.out.println ("[log] "+args[i]+" conectado; "+processadores[i]+" processador(es).");
        }

        // tamanhos dos pedacos PROPORCIONAIS ao poder computacional
        // (quantidade de processadores) de cada servidor; o que sobrar
        // da divisao inteira e distribuido, 1 a 1, entre os primeiros
        int[] tamanhos = new int [quantosServidores];
        int   distribuidos = 0;

        for (int i=0; i<quantosServidores; i++)
        {
            tamanhos[i]   = (int)((long)quantidade*processadores[i]/totalDeProcessadores);
            distribuidos += tamanhos[i];
        }

        for (int i=0; distribuidos<quantidade; i=(i+1)%quantosServidores)
        {
            tamanhos[i]++;
            distribuidos++;
        }

        long inicio = System.currentTimeMillis ();

        // pedindo a ordenacao de cada pedaco, todas ao mesmo tempo
        ArrayList<ConversaDeOrdenacao> ordenacoes  = new ArrayList<ConversaDeOrdenacao> ();
        ArrayList<Parceiro>            trabalhando = new ArrayList<Parceiro> (); // servidores que receberam pedaco

        try
        {
            int posicao=0;
            for (int i=0; i<quantosServidores; i++)
            {
                if (tamanhos[i]==0) // vetor pequeno demais para usar este servidor
                {
                    System.out.println ("[log] "+args[i]+" nao recebera elementos (vetor pequeno demais).");
                    continue;
                }

                byte[] pedaco = new byte [tamanhos[i]];
                for (int j=0; j<tamanhos[i]; j++)
                    pedaco[j] = vetor[posicao+j];

                System.out.println ("[log] "+args[i]+" ("+processadores[i]+"/"+totalDeProcessadores+" dos processadores) recebera "+
                                    tamanhos[i]+" elementos (posicoes "+posicao+" a "+(posicao+tamanhos[i]-1)+" do vetor original).");

                ordenacoes .add (new ConversaDeOrdenacao (servidores[i], "servidor "+args[i], pedaco));
                trabalhando.add (servidores[i]);

                posicao = posicao+tamanhos[i];
            }
        }
        catch (Exception erro)
        {
            Cliente.desista (servidores, erro.getMessage());
        }

        for (ConversaDeOrdenacao ordenacao:ordenacoes)
            ordenacao.start ();

        byte[][] atual = new byte [ordenacoes.size()][];

        for (int i=0; i<ordenacoes.size(); i++)
        {
            try
            {
                ordenacoes.get(i).join ();
                atual[i] = ordenacoes.get(i).getResultado ();
            }
            catch (Exception erro)
            {
                Cliente.desista (servidores, "Um servidor falhou; a ordenacao ficaria incompleta!");
            }
        }

        System.out.println ("[log] todos os "+ordenacoes.size()+" pedacos voltaram ordenados (join realizado em cada conversa).");

        // mergeando em rodadas, como na atividade 1, mas cada dupla
        // e mergeada por um servidor diferente; como ha no maximo
        // tantos pedacos quantos servidores trabalhando, cada rodada
        // tem no maximo metade disso em duplas, e nenhum servidor
        // recebe dois pedidos ao mesmo tempo
        int rodada=1;
        while (atual.length>1)
        {
            int duplas = atual.length/2;
            int sobra  = atual.length%2;

            System.out.println ("[log] rodada "+rodada+": pedindo "+duplas+" mergeamento(s) para "+atual.length+" pedaco(s).");

            ConversaDeMergeamento[] mergeamentos = new ConversaDeMergeamento [duplas];

            try
            {
                for (int i=0; i<duplas; i++)
                {
                    mergeamentos[i] = new ConversaDeMergeamento (trabalhando.get(i), "servidor "+(i+1)+" da rodada "+rodada,
                                                                 atual[2*i], atual[2*i+1]);
                    mergeamentos[i].start ();
                }
            }
            catch (Exception erro)
            {
                Cliente.desista (servidores, erro.getMessage());
            }

            // so comeca a proxima rodada depois de esperar
            // (join) todas as conversas desta rodada
            byte[][] novo = new byte [duplas+sobra][];

            for (int i=0; i<duplas; i++)
            {
                try
                {
                    mergeamentos[i].join ();
                    novo[i] = mergeamentos[i].getResultado ();
                }
                catch (Exception erro)
                {
                    Cliente.desista (servidores, "Um servidor falhou; o mergeamento ficaria incompleto!");
                }
            }

            if (sobra==1)
                novo[duplas] = atual[atual.length-1];

            atual = novo;

            System.out.println ("[log] rodada "+rodada+" concluida (join realizado em cada conversa); restam "+atual.length+" pedaco(s).");
            rodada++;
        }

        vetor = atual[0]; // vetor passa a ser o vetor inteiro, ja ordenado

        long fim = System.currentTimeMillis ();
        long tempoOrdenacao = fim-inicio;

        System.out.println ("[log] ordenacao distribuida concluida em "+tempoOrdenacao+"ms.");

        // informando a cada servidor que estamos encerrando o seu uso,
        // para que ele possa atender, em sequencia, outro cliente
        Cliente.encerreConexoes (servidores);

        System.out.println ("[log] PedidoParaSair enviado a todos os servidores.");

        int opcao;
        do
        {
            System.out.println ();
            System.out.println ("1 - Mostrar o vetor inteiro");
            System.out.println ("2 - Mostrar um pedaco do vetor");
            System.out.println ("3 - Mostrar o tempo da ordenacao distribuida");
            System.out.println ("4 - Sair");

            opcao = Cliente.leiaInt ("Escolha uma opcao:");

            if (opcao==1)
                Cliente.mostreVetor (vetor);
            else if (opcao==2)
            {
                int de  = Cliente.leiaInt ("A partir de que posicao (0 a "+(vetor.length-1)+")?");
                int ate = Cliente.leiaInt ("Ate que posicao (0 a "+(vetor.length-1)+")?");

                if (de<0 || ate>vetor.length-1 || de>ate)
                    System.err.println ("Intervalo invalido!");
                else
                {
                    byte[] pedaco = new byte [ate-de+1];
                    for (int i=0; i<pedaco.length; i++)
                        pedaco[i] = vetor[de+i];

                    Cliente.mostreVetor (pedaco);
                }
            }
            else if (opcao==3)
                System.out.println ("Tempo da ordenacao distribuida: "+tempoOrdenacao+"ms");
            else if (opcao!=4)
                System.err.println ("Opcao invalida!");
        }
        while (opcao!=4);

        System.out.println ("Obrigado por usar este programa!");
    }
}
