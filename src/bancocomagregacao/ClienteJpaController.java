/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bancocomagregacao;

import bancocomagregacao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Wilson Wolf Costa <wilson.w.costa@gmail.com>
 */
public class ClienteJpaController implements Serializable {

    public ClienteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cliente cliente) {
        if (cliente.getTelefoneCollection() == null) {
            cliente.setTelefoneCollection(new ArrayList<Telefone>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Telefone> attachedTelefoneCollection = new ArrayList<Telefone>();
            for (Telefone telefoneCollectionTelefoneToAttach : cliente.getTelefoneCollection()) {
                telefoneCollectionTelefoneToAttach = em.getReference(telefoneCollectionTelefoneToAttach.getClass(), telefoneCollectionTelefoneToAttach.getId());
                attachedTelefoneCollection.add(telefoneCollectionTelefoneToAttach);
            }
            cliente.setTelefoneCollection(attachedTelefoneCollection);
            em.persist(cliente);
            for (Telefone telefoneCollectionTelefone : cliente.getTelefoneCollection()) {
                Cliente oldClienteIdOfTelefoneCollectionTelefone = telefoneCollectionTelefone.getClienteId();
                telefoneCollectionTelefone.setClienteId(cliente);
                telefoneCollectionTelefone = em.merge(telefoneCollectionTelefone);
                if (oldClienteIdOfTelefoneCollectionTelefone != null) {
                    oldClienteIdOfTelefoneCollectionTelefone.getTelefoneCollection().remove(telefoneCollectionTelefone);
                    oldClienteIdOfTelefoneCollectionTelefone = em.merge(oldClienteIdOfTelefoneCollectionTelefone);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cliente cliente) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente persistentCliente = em.find(Cliente.class, cliente.getId());
            Collection<Telefone> telefoneCollectionOld = persistentCliente.getTelefoneCollection();
            Collection<Telefone> telefoneCollectionNew = cliente.getTelefoneCollection();
            Collection<Telefone> attachedTelefoneCollectionNew = new ArrayList<Telefone>();
            for (Telefone telefoneCollectionNewTelefoneToAttach : telefoneCollectionNew) {
                telefoneCollectionNewTelefoneToAttach = em.getReference(telefoneCollectionNewTelefoneToAttach.getClass(), telefoneCollectionNewTelefoneToAttach.getId());
                attachedTelefoneCollectionNew.add(telefoneCollectionNewTelefoneToAttach);
            }
            telefoneCollectionNew = attachedTelefoneCollectionNew;
            cliente.setTelefoneCollection(telefoneCollectionNew);
            cliente = em.merge(cliente);
            for (Telefone telefoneCollectionOldTelefone : telefoneCollectionOld) {
                if (!telefoneCollectionNew.contains(telefoneCollectionOldTelefone)) {
                    telefoneCollectionOldTelefone.setClienteId(null);
                    telefoneCollectionOldTelefone = em.merge(telefoneCollectionOldTelefone);
                }
            }
            for (Telefone telefoneCollectionNewTelefone : telefoneCollectionNew) {
                if (!telefoneCollectionOld.contains(telefoneCollectionNewTelefone)) {
                    Cliente oldClienteIdOfTelefoneCollectionNewTelefone = telefoneCollectionNewTelefone.getClienteId();
                    telefoneCollectionNewTelefone.setClienteId(cliente);
                    telefoneCollectionNewTelefone = em.merge(telefoneCollectionNewTelefone);
                    if (oldClienteIdOfTelefoneCollectionNewTelefone != null && !oldClienteIdOfTelefoneCollectionNewTelefone.equals(cliente)) {
                        oldClienteIdOfTelefoneCollectionNewTelefone.getTelefoneCollection().remove(telefoneCollectionNewTelefone);
                        oldClienteIdOfTelefoneCollectionNewTelefone = em.merge(oldClienteIdOfTelefoneCollectionNewTelefone);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = cliente.getId();
                if (findCliente(id) == null) {
                    throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente;
            try {
                cliente = em.getReference(Cliente.class, id);
                cliente.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.", enfe);
            }
            Collection<Telefone> telefoneCollection = cliente.getTelefoneCollection();
            for (Telefone telefoneCollectionTelefone : telefoneCollection) {
                telefoneCollectionTelefone.setClienteId(null);
                telefoneCollectionTelefone = em.merge(telefoneCollectionTelefone);
            }
            em.remove(cliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Cliente> findClienteEntities() {
        return findClienteEntities(true, -1, -1);
    }

    public List<Cliente> findClienteEntities(int maxResults, int firstResult) {
        return findClienteEntities(false, maxResults, firstResult);
    }

    private List<Cliente> findClienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cliente.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Cliente findCliente(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public int getClienteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cliente> rt = cq.from(Cliente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
