// Uma thread por dupla de pedacos ja ordenados: todas rodam ao mesmo
// tempo, cada uma pedindo a um servidor diferente que mergeie a sua dupla.
public class ConversaDeMergeamento extends Thread
{
    private Parceiro servidor;
    private String   nome;
    private byte[]   pedacoEsquerdo;
    private byte[]   pedacoDireito;

    private byte[]  resultado = null;
    private boolean deuCerto  = false;

    public ConversaDeMergeamento (Parceiro servidor, String nome, byte[] pedacoEsquerdo, byte[] pedacoDireito) throws Exception
    {
        if (servidor==null)
            throw new Exception ("Servidor ausente");

        if (nome==null || nome.equals(""))
            throw new Exception ("Nome ausente");

        if (pedacoEsquerdo==null)
            throw new Exception ("Pedaco esquerdo ausente");

        if (pedacoDireito==null)
            throw new Exception ("Pedaco direito ausente");

        this.servidor       = servidor;
        this.nome           = nome;
        this.pedacoEsquerdo = pedacoEsquerdo;
        this.pedacoDireito  = pedacoDireito;
    }

    public void run ()
    {
        try
        {
            System.out.println ("[log] enviando para "+this.nome+" -> mergeamento de "+this.pedacoEsquerdo.length+"+"+this.pedacoDireito.length+" elementos.");

            this.servidor.receba (new PedidoDeMergeamento (this.pedacoEsquerdo, this.pedacoDireito));

            Comunicado comunicado = this.servidor.envie ();
            if (comunicado instanceof ComunicadoDeDesligamento)
                throw new Exception ("O servidor foi desligado");
            if (!(comunicado instanceof MergeamentoPronto))
                throw new Exception ("Resposta inesperada");

            System.out.println ("[log] "+this.nome+" avisou: mergeamento pronto; pedindo o resultado.");

            this.servidor.receba (new PedidoDeResultado ());

            comunicado = this.servidor.envie ();
            if (comunicado instanceof ComunicadoDeDesligamento)
                throw new Exception ("O servidor foi desligado");
            if (!(comunicado instanceof Resultado))
                throw new Exception ("Resposta inesperada");

            this.resultado = ((Resultado)comunicado).getVetorResultante();
            this.deuCerto  = true;

            System.out.println ("[log] "+this.nome+" devolveu "+this.resultado.length+" elementos mergeados.");
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
            throw new Exception ("Mergeamento nao foi obtido (de join na conversa antes)");

        return this.resultado;
    }
}
