/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bancocomagregacao;

import bancocomagregacao.exceptions.NonexistentEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author Wilson Wolf Costa <wilson.w.costa@gmail.com>
 */
public class TelefoneJpaController implements Serializable {

    public TelefoneJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Telefone telefone) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente clienteId = telefone.getClienteId();
            if (clienteId != null) {
                clienteId = em.getReference(clienteId.getClass(), clienteId.getId());
                telefone.setClienteId(clienteId);
            }
            em.persist(telefone);
            if (clienteId != null) {
                clienteId.getTelefoneCollection().add(telefone);
                clienteId = em.merge(clienteId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Telefone telefone) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Telefone persistentTelefone = em.find(Telefone.class, telefone.getId());
            Cliente clienteIdOld = persistentTelefone.getClienteId();
            Cliente clienteIdNew = telefone.getClienteId();
            if (clienteIdNew != null) {
                clienteIdNew = em.getReference(clienteIdNew.getClass(), clienteIdNew.getId());
                telefone.setClienteId(clienteIdNew);
            }
            telefone = em.merge(telefone);
            if (clienteIdOld != null && !clienteIdOld.equals(clienteIdNew)) {
                clienteIdOld.getTelefoneCollection().remove(telefone);
                clienteIdOld = em.merge(clienteIdOld);
            }
            if (clienteIdNew != null && !clienteIdNew.equals(clienteIdOld)) {
                clienteIdNew.getTelefoneCollection().add(telefone);
                clienteIdNew = em.merge(clienteIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = telefone.getId();
                if (findTelefone(id) == null) {
                    throw new NonexistentEntityException("The telefone with id " + id + " no longer exists.");
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
            Telefone telefone;
            try {
                telefone = em.getReference(Telefone.class, id);
                telefone.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The telefone with id " + id + " no longer exists.", enfe);
            }
            Cliente clienteId = telefone.getClienteId();
            if (clienteId != null) {
                clienteId.getTelefoneCollection().remove(telefone);
                clienteId = em.merge(clienteId);
            }
            em.remove(telefone);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Telefone> findTelefoneEntities() {
        return findTelefoneEntities(true, -1, -1);
    }

    public List<Telefone> findTelefoneEntities(int maxResults, int firstResult) {
        return findTelefoneEntities(false, maxResults, firstResult);
    }

    private List<Telefone> findTelefoneEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Telefone.class));
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

    public Telefone findTelefone(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Telefone.class, id);
        } finally {
            em.close();
        }
    }

    public int getTelefoneCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Telefone> rt = cq.from(Telefone.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
