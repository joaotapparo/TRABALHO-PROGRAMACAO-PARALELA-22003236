import java.io.*;
import java.net.*;
import java.util.*;

public class SupervisoraDeConexao extends Thread
{
    private byte[]              vetorResultante=null; // da ultima ordenacao/mergeamento
    private Parceiro            usuario;
    private Socket              conexao;
    private ArrayList<Parceiro> usuarios;

    public SupervisoraDeConexao
    (Socket conexao, ArrayList<Parceiro> usuarios)
    throws Exception
    {
        if (conexao==null)
            throw new Exception ("Conexao ausente");

        if (usuarios==null)
            throw new Exception ("Usuarios ausentes");

        this.conexao  = conexao;
        this.usuarios = usuarios;
    }

    public void run ()
    {
        ObjectOutputStream transmissor;
        try
        {
            transmissor =
            new ObjectOutputStream(
            this.conexao.getOutputStream());
        }
        catch (Exception erro)
        {
            return;
        }

        ObjectInputStream receptor=null;
        try
        {
            receptor=
            new ObjectInputStream(
            this.conexao.getInputStream());
        }
        catch (Exception err0)
        {
            try
            {
                transmissor.close();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread

            return;
        }

        try
        {
            this.usuario =
            new Parceiro (this.conexao,
                          receptor,
                          transmissor);
        }
        catch (Exception erro)
        {} // sei que passei os parametros corretos

        try
        {
            synchronized (this.usuarios)
            {
                this.usuarios.add (this.usuario);
            }

            for(;;)
            {
                Comunicado comunicado = this.usuario.envie ();

                if (comunicado==null)
                    return;
                else if (comunicado instanceof PedidoDeCapacidade)
                {
                    int processadores = Runtime.getRuntime().availableProcessors();

                    System.out.println ("[log] pedido de capacidade; respondendo: "+processadores+" processador(es).");

                    this.usuario.receba (new Capacidade (processadores));
                }
                else if (comunicado instanceof PedidoDeOrdenacao)
                {
                    PedidoDeOrdenacao pedido = (PedidoDeOrdenacao)comunicado;

                    System.out.println ("[log] recebi um pedido de "+pedido+".");

                    long inicio = System.currentTimeMillis ();

                    // ordena PARALELAMENTE, como na atividade 1
                    this.vetorResultante = OrdenacaoParalela.ordene (pedido.getVetor());

                    long fim = System.currentTimeMillis ();

                    System.out.println ("[log] ordenacao paralela concluida em "+(fim-inicio)+"ms; avisando o cliente.");

                    this.usuario.receba (new OrdenacaoPronta ());
                }
                else if (comunicado instanceof PedidoDeMergeamento)
                {
                    PedidoDeMergeamento pedido = (PedidoDeMergeamento)comunicado;

                    System.out.println ("[log] recebi um pedido de "+pedido+".");

                    TarefaMisturadora misturadora =
                    new TarefaMisturadora (pedido.getVetorEsquerdo(),
                                           pedido.getVetorDireito (),
                                           "Misturadora");
                    misturadora.start ();
                    misturadora.join  ();

                    this.vetorResultante = misturadora.getMisturado ();

                    System.out.println ("[log] mergeamento concluido; avisando o cliente.");

                    this.usuario.receba (new MergeamentoPronto ());
                }
                else if (comunicado instanceof PedidoDeResultado)
                {
                    if (this.vetorResultante==null)
                        throw new Exception ("Resultado pedido sem ordenacao/mergeamento previo");

                    System.out.println ("[log] enviando o resultado ("+this.vetorResultante.length+" elementos).");

                    this.usuario.receba (new Resultado (this.vetorResultante));
                    this.vetorResultante = null; // ja foi entregue
                }
                else if (comunicado instanceof PedidoParaSair)
                {
                    synchronized (this.usuarios)
                    {
                        this.usuarios.remove (this.usuario);
                    }
                    this.usuario.adeus();
                    return; // encerra a supervisora; a aceitadora (que deu join) volta a aceitar
                }
            }
        }
        catch (Exception erro)
        {
            System.err.println ("[log] atendimento interrompido: "+erro.getMessage());

            synchronized (this.usuarios)
            {
                this.usuarios.remove (this.usuario);
            }

            try
            {
                transmissor.close ();
                receptor   .close ();
            }
            catch (Exception falha)
            {} // so tentando fechar antes de acabar a thread

            return;
        }
    }
}
