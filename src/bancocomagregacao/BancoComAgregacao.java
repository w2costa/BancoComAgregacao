/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bancocomagregacao;

import java.util.ArrayList;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Wilson Wolf Costa <wilson.w.costa@gmail.com>
 */
public class BancoComAgregacao {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");
        ClienteJpaController clienteDao = new ClienteJpaController(emf);
        TelefoneJpaController telefoneDao = new TelefoneJpaController(emf);

        // lado n da relacao
        Telefone fone = new Telefone();
        fone.setNumero("22222226");
        telefoneDao.create(fone);

        // Cria lado 1 da realcao
        Cliente c = new Cliente();
        c.setNome("Zé Mané");

        // conecta as duas classes
        c.setTelefoneCollection(new ArrayList());
        c.getTelefoneCollection().add(fone);

        // persiste o cliente
        clienteDao.create(c);

    }

}
