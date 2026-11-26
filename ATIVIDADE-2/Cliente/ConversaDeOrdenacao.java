// Uma thread por servidor: todas rodam ao mesmo tempo, cada uma
// pedindo a um servidor diferente que ordene o seu pedaco do vetor.
public class ConversaDeOrdenacao extends Thread
{
    private Parceiro servidor;
    private String   nome;
    private byte[]   pedaco;

    private byte[]  resultado = null;
    private boolean deuCerto  = false;

    public ConversaDeOrdenacao (Parceiro servidor, String nome, byte[] pedaco) throws Exception
    {
        if (servidor==null)
            throw new Exception ("Servidor ausente");

        if (nome==null || nome.equals(""))
            throw new Exception ("Nome ausente");

        if (pedaco==null || pedaco.length==0)
            throw new Exception ("Pedaco ausente");

        this.servidor = servidor;
        this.nome     = nome;
        this.pedaco   = pedaco;
    }

    public void run ()
    {
        try
        {
            System.out.println ("[log] enviando para "+this.nome+" -> ordenacao de "+this.pedaco.length+" elementos.");

            this.servidor.receba (new PedidoDeOrdenacao (this.pedaco));

            Comunicado comunicado = this.servidor.envie ();
            if (comunicado instanceof ComunicadoDeDesligamento)
                throw new Exception ("O servidor foi desligado");
            if (!(comunicado instanceof OrdenacaoPronta))
                throw new Exception ("Resposta inesperada");

            System.out.println ("[log] "+this.nome+" avisou: ordenacao pronta; pedindo o resultado.");

            this.servidor.receba (new PedidoDeResultado ());

            comunicado = this.servidor.envie ();
            if (comunicado instanceof ComunicadoDeDesligamento)
                throw new Exception ("O servidor foi desligado");
            if (!(comunicado instanceof Resultado))
                throw new Exception ("Resposta inesperada");

            this.resultado = ((Resultado)comunicado).getVetorResultante();
            this.deuCerto  = true;

            System.out.println ("[log] "+this.nome+" devolveu "+this.resultado.length+" elementos ordenados.");
        }
        catch (Exception erro)
        {
            System.err.println ("Falha com o "+this.nome+": "+erro.getMessage());
        }
    }

    public boolean deuCerto ()
    {
        return this.deuCerto;
    }

    // so pode ser chamado depois de dar join nesta conversa
    public byte[] getResultado () throws Exception
    {
        if (!this.deuCerto)
            throw new Exception ("Ordenacao nao foi obtida (de join na conversa antes)");

        return this.resultado;
    }
}
