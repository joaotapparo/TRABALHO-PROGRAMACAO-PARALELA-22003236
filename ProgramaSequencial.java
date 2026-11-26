import java.util.Random;

public class ProgramaSequencial
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

    private static void ordene (byte[] vetor, int inicio, int fim)
    {
        if (inicio>=fim)
            return;

        int meio = (inicio+fim)/2;

        ProgramaSequencial.ordene (vetor, inicio, meio);
        ProgramaSequencial.ordene (vetor, meio+1, fim);
        ProgramaSequencial.intercale (vetor, inicio, meio, fim);
    }

    private static void intercale (byte[] vetor, int inicio, int meio, int fim)
    {
        byte[] esquerda = new byte [meio-inicio+1];
        byte[] direita  = new byte [fim-meio];

        for (int i=0; i<esquerda.length; i++)
            esquerda[i] = vetor[inicio+i];

        for (int i=0; i<direita.length; i++)
            direita[i] = vetor[meio+1+i];

        int i=0, j=0, k=inicio;

        while (i<esquerda.length && j<direita.length)
            if (esquerda[i]<=direita[j])
                vetor[k++] = esquerda[i++];
            else
                vetor[k++] = direita[j++];

        while (i<esquerda.length)
            vetor[k++] = esquerda[i++];

        while (j<direita.length)
            vetor[k++] = direita[j++];
    }

    public static void main (String[] args)
    {
        try
        {
            int quantidade = ProgramaSequencial.leiaInt ("Quantos elementos tera o vetor?");

            while (quantidade<1)
                quantidade = ProgramaSequencial.leiaInt ("Quantidade invalida! Quantos elementos tera o vetor?");

            byte[] vetor = new byte [quantidade];

            boolean digitar = ProgramaSequencial.leiaBoolean ("Deseja digitar os elementos (true) ou deseja que sejam gerados aleatoriamente (false)?");

            if (digitar)
                for (int i=0; i<quantidade; i++)
                    vetor[i] = ProgramaSequencial.leiaByte ("Elemento "+(i+1)+" de "+quantidade+" (byte: -128 a 127):");
            else
            {
                Random sorteio = new Random ();

                for (int i=0; i<quantidade; i++)
                    vetor[i] = (byte)(sorteio.nextInt(256)-128);
            }

            System.out.println ("[log] vetor com "+quantidade+" elementos (tipo byte) gerado ("+(digitar?"digitado":"aleatorio")+").");
            System.out.println ("[log] iniciando ordenacao sequencial (sem nenhuma thread, so a main)...");

            long inicio = System.currentTimeMillis ();

            ProgramaSequencial.ordene (vetor, 0, vetor.length-1);

            long fim = System.currentTimeMillis ();
            long tempoOrdenacao = fim-inicio;

            System.out.println ("[log] ordenacao sequencial concluida em "+tempoOrdenacao+"ms.");

            int opcao;
            do
            {
                System.out.println ();
                System.out.println ("1 - Mostrar o vetor inteiro");
                System.out.println ("2 - Mostrar um pedaco do vetor");
                System.out.println ("3 - Mostrar o tempo da ordenacao sequencial");
                System.out.println ("4 - Sair");

                opcao = ProgramaSequencial.leiaInt ("Escolha uma opcao:");

                if (opcao==1)
                    ProgramaSequencial.mostreVetor (vetor);
                else if (opcao==2)
                {
                    int de  = ProgramaSequencial.leiaInt ("A partir de que posicao (0 a "+(vetor.length-1)+")?");
                    int ate = ProgramaSequencial.leiaInt ("Ate que posicao (0 a "+(vetor.length-1)+")?");

                    if (de<0 || ate>vetor.length-1 || de>ate)
                        System.err.println ("Intervalo invalido!");
                    else
                    {
                        byte[] pedaco = new byte [ate-de+1];
                        for (int i=0; i<pedaco.length; i++)
                            pedaco[i] = vetor[de+i];

                        ProgramaSequencial.mostreVetor (pedaco);
                    }
                }
                else if (opcao==3)
                    System.out.println ("Tempo da ordenacao sequencial: "+tempoOrdenacao+"ms");
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
