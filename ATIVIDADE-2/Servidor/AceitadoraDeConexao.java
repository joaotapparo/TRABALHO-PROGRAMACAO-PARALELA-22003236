import java.net.*;
import java.util.*;

public class AceitadoraDeConexao extends Thread
{
    private ServerSocket        pedido;
    private ArrayList<Parceiro> usuarios;

    public AceitadoraDeConexao
    (String porta, ArrayList<Parceiro> usuarios)
    throws Exception
    {
        if (porta==null)
            throw new Exception ("Porta ausente");

        try
        {
            this.pedido =
            new ServerSocket (Integer.parseInt(porta));
        }
        catch (Exception  erro)
        {
            throw new Exception ("Porta invalida");
        }

        if (usuarios==null)
            throw new Exception ("Usuarios ausentes");

        this.usuarios = usuarios;
    }

    public void run ()
    {
        for(;;)
        {
            Socket conexao=null;
            try
            {
                conexao = this.pedido.accept();
            }
            catch (Exception erro)
            {
                continue;
            }

            System.out.println ("\n[log] cliente conectado ("+conexao.getInetAddress().getHostAddress()+").");

            SupervisoraDeConexao supervisoraDeConexao=null;
            try
            {
                supervisoraDeConexao =
                new SupervisoraDeConexao (conexao, usuarios);
            }
            catch (Exception erro)
            {} // sei que passei parametros corretos para o construtor
            supervisoraDeConexao.start();

            // o servidor atende a UM UNICO cliente por vez; so
            // voltamos a aceitar conexoes quando a supervisora
            // terminar, ou seja, quando o cliente atual mandar
            // PedidoParaSair (ou a conexao com ele cair); outros
            // clientes que tentarem se conectar nesse meio tempo
            // ficam aguardando na fila do ServerSocket
            try
            {
                supervisoraDeConexao.join();
            }
            catch (InterruptedException erro)
            {}

            System.out.println ("[log] cliente desconectado; aguardando o proximo cliente.\n");
        }
    }
}
