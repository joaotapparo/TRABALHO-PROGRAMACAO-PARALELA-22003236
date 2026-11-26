public class PedidoDeMergeamento extends Comunicado
{
    private byte[] vetorEsquerdo;
    private byte[] vetorDireito;

    // os dois vetores ja devem vir ordenados
    public PedidoDeMergeamento (byte[] vetorEsquerdo, byte[] vetorDireito) throws Exception
    {
        if (vetorEsquerdo==null)
            throw new Exception ("Vetor esquerdo ausente");

        if (vetorDireito==null)
            throw new Exception ("Vetor direito ausente");

        this.vetorEsquerdo = vetorEsquerdo;
        this.vetorDireito  = vetorDireito;
    }

    public byte[] getVetorEsquerdo ()
    {
        return this.vetorEsquerdo;
    }

    public byte[] getVetorDireito ()
    {
        return this.vetorDireito;
    }

    public String toString ()
    {
        return ("mergeamento de "+this.vetorEsquerdo.length+"+"+this.vetorDireito.length+" elementos");
    }
}
