public class TarefaMisturadora extends Thread
{
    private byte[] pedacoEsquerdo;
    private byte[] pedacoDireito;
    private String rotulo;

    public TarefaMisturadora (byte[] pedacoEsquerdo, byte[] pedacoDireito, String rotulo) throws Exception
    {
        if (pedacoEsquerdo==null)
            throw new Exception ("Pedaco esquerdo ausente");

        if (pedacoDireito==null)
            throw new Exception ("Pedaco direito ausente");

        if (rotulo==null || rotulo.equals(""))
            throw new Exception ("Rotulo ausente");

        this.pedacoEsquerdo = pedacoEsquerdo;
        this.pedacoDireito  = pedacoDireito;
        this.rotulo         = rotulo;
    }

    private byte[]  misturado;
    private boolean pronto = false;

    @Override
    public void run ()
    {
        System.out.println ("[log] "+this.rotulo+": iniciando mistura ("+this.pedacoEsquerdo.length+"+"+this.pedacoDireito.length+" elementos)...");

        byte[] ret = new byte [this.pedacoEsquerdo.length+this.pedacoDireito.length];

        int i=0, j=0, k=0;

        while (i<this.pedacoEsquerdo.length && j<this.pedacoDireito.length)
            if (this.pedacoEsquerdo[i]<=this.pedacoDireito[j])
                ret[k++] = this.pedacoEsquerdo[i++];
            else
                ret[k++] = this.pedacoDireito[j++];

        while (i<this.pedacoEsquerdo.length)
            ret[k++] = this.pedacoEsquerdo[i++];

        while (j<this.pedacoDireito.length)
            ret[k++] = this.pedacoDireito[j++];

        this.misturado = ret;
        this.pronto    = true;

        System.out.println ("[log] "+this.rotulo+": mistura concluida ("+this.misturado.length+" elementos).");
    }

    // so pode ser chamado depois de dar join nesta tarefa
    public byte[] getMisturado () throws Exception
    {
        if (!this.pronto)
            throw new Exception ("Mistura ainda nao terminou (de join na tarefa antes)");

        return this.misturado;
    }
}
