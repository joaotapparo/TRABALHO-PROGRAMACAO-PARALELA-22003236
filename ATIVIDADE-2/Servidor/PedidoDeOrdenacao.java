public class PedidoDeOrdenacao extends Comunicado
{
    private byte[] vetor;

    public PedidoDeOrdenacao (byte[] vetor) throws Exception
    {
        if (vetor==null)
            throw new Exception ("Vetor ausente");

        if (vetor.length==0)
            throw new Exception ("Vetor vazio");

        this.vetor = vetor;
    }

    public byte[] getVetor ()
    {
        return this.vetor;
    }

    public String toString ()
    {
        return ("ordenacao de "+this.vetor.length+" elementos");
    }
}
