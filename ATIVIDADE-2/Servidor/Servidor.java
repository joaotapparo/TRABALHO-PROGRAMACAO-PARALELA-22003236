import java.util.*;

public class Servidor
{
    public static String PORTA_PADRAO = "3000";

    public static void main (String[] args)
    {
        if (args.length>1)
        {
            System.err.println ("Uso esperado: java Servidor [PORTA]\n");
            return;
        }

        String porta=Servidor.PORTA_PADRAO;

        if (args.length==1)
            porta = args[0];

        // embora o servidor atenda um unico cliente por vez,
        // guardamos o usuario corrente aqui para poder avisa-lo
        // caso o servidor seja desativado no meio do atendimento
        ArrayList<Parceiro> usuarios =
        new ArrayList<Parceiro> ();

        AceitadoraDeConexao aceitadoraDeConexao=null;
        try
        {
            aceitadoraDeConexao =
            new AceitadoraDeConexao (porta, usuarios);
            aceitadoraDeConexao.start();
        }
        catch (Exception erro)
        {
            System.err.println ("Escolha uma porta apropriada e liberada para uso!\n");
            return;
        }

        System.out.println ("[log] servidor na porta "+porta+", em maquina com "+
                            Runtime.getRuntime().availableProcessors()+" processador(es).");

        for(;;)
        {
            System.out.println ("O servidor esta ativo! Para desativa-lo,");
            System.out.println ("use o comando \"DESATIVAR\"\n");
            System.out.print   ("> ");

            String comando=null;
            try
            {
                comando = Teclado.getUmString();
            }
            catch (Exception erro)
            {}

            if (comando==null) // teclado fechado; o servidor segue atendendo
            {
                System.out.println ("[log] teclado indisponivel; encerre o servidor com Ctrl+C");
                return; // a thread AceitadoraDeConexao continua viva
            }

            if (comando.toLowerCase().equals("desativar"))
            {
                synchronized (usuarios)
                {
                    ComunicadoDeDesligamento comunicadoDeDesligamento =
                    new ComunicadoDeDesligamento ();

                    for (Parceiro usuario:usuarios)
                    {
                        try
                        {
                            usuario.receba (comunicadoDeDesligamento);
                            usuario.adeus  ();
                        }
                        catch (Exception erro)
                        {}
                    }
                }

                System.out.println ("O servidor foi desativado!\n");
                System.exit(0);
            }
            else
                System.err.println ("Comando invalido!\n");
        }
    }
}
