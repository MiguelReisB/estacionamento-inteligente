package com.mycompany.mavenproject1;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;
import service.OperacaoInvalidaException;
import util.ErroBanco;

/**
 * Roda o acesso ao banco/API fora da thread da tela, pra janela nao travar
 * enquanto espera o servidor responder.
 *
 * @author henri
 */
public final class Tarefa {

    public interface Acao<T> {
        T executar() throws Exception;
    }

    private Tarefa() {
    }

    public static <T> void rodar(Acao<T> acao, Consumer<T> sucesso, Consumer<Exception> erro) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return acao.executar();
            }

            @Override
            protected void done() {
                T resultado;
                try {
                    resultado = get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    erro.accept(causa instanceof Exception ex ? ex : new Exception(causa));
                    return;
                }
                sucesso.accept(resultado);
            }
        }.execute();
    }

    // mensagem que da pra mostrar pro usuario
    public static String mensagem(Exception e) {
        if (e instanceof OperacaoInvalidaException) {
            return e.getMessage();
        }
        if (e instanceof SQLException sql) {
            return ErroBanco.mensagemAmigavel(sql);
        }
        if (e instanceof IOException) {
            String texto = e.getMessage();
            if (texto == null || texto.isBlank() || e instanceof java.net.ConnectException
                    || e instanceof java.net.http.HttpTimeoutException) {
                return "Não foi possível falar com a API. Verifique se o sistema foi iniciado corretamente.";
            }
            return texto;
        }
        return "Erro inesperado: " + (e.getMessage() == null ? e.toString() : e.getMessage());
    }
}
